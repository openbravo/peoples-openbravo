/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.InventoryCountProcess;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.CostingRuleInit;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DbUtility;

public class CostingRuleProcess implements Process {
  private ProcessLogger logger;
  private static Logger log4j = Logger.getLogger(CostingBackground.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));
    try {
      OBContext.setAdminMode(false);
      final String recordID = (String) bundle.getParams().get("M_Costing_Rule_ID");
      final CostingRule rule = OBDal.getInstance().get(CostingRule.class, recordID);

      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(rule.getClient().getId());
      // FIXME: Review checks to include organization filter!!
      Set<String> childOrgs = osp.getChildTree(rule.getOrganization().getId(), true);
      Set<String> naturalOrgs = osp.getNaturalTree(rule.getOrganization().getId());

      // Checks
      boolean hasProductWithCost = checkProductWithCost(rule, naturalOrgs);
      boolean hasTrxWithNoPreviousCostingRule = checkTrxWithNoPreviousCostingRule(rule, naturalOrgs);
      // 1. Not mixed products. Cannot mix products with calculated costs and products with
      // transactions that do not have a previous costing rule.
      if (hasProductWithCost && hasTrxWithNoPreviousCostingRule) {
        throw new OBException("@MixedProductsWithCost@");
      }
      if (hasProductWithCost) {
        // Product with legacy cost. Trx must be set to calculated and legacy process executed.
        updateLegacyCostTrx();
        // Product with costing rule. All trx must be calculated.
        checkAllTrxCalculated(naturalOrgs, childOrgs);
      } else if (hasTrxWithNoPreviousCostingRule) {
        // Product configured to have cost not calculated cannot have records in M_Costing nor
        // transactions with cost calculated.
        checkNoTrxWithCostCalculated(naturalOrgs, childOrgs);
      }
      // Inventories are only needed if the costing rule is updating a previous rule or legacy cost
      // engine was used.
      if (hasProductWithCost) {
        // Create inventories.
        List<Warehouse> whl = getWarehouselist(childOrgs);
        for (Warehouse wh : whl) {
          for (String orgId : childOrgs) {
            createPhysicalInventories(orgId, wh, rule);
          }
        }

        // Process closing physical inventories.
        for (CostingRuleInit cri : rule.getCostingRuleInitList()) {
          new InventoryCountProcess().processInventory(cri.getCloseInventory());
        }

        // Set valid from date
        Date startingDate = new Date();
        rule.setStartingDate(startingDate);

        // Update cost of inventories and process starting physical inventories.
        for (CostingRuleInit cri : rule.getCostingRuleInitList()) {
          for (InventoryCountLine icl : cri.getInitInventory()
              .getMaterialMgmtInventoryCountLineList()) {
            BigDecimal cost = CostingUtils.getTransactionCost(icl
                .getMaterialMgmtMaterialTransactionList().get(0), startingDate, true);
            InventoryCountLine initICL = getInitIcl(cri.getInitInventory(), icl.getLineNo());
            initICL.setCost(cost);
            OBDal.getInstance().save(initICL);
          }
          new InventoryCountProcess().processInventory(cri.getInitInventory());
        }

      } else {
        rule.setStartingDate(getFirstDate());
      }

      rule.setValidated(true);
      OBDal.getInstance().save(rule);
    } catch (final OBException e) {
      OBDal.getInstance().rollbackAndClose();
      logger.log(e.getMessage());
      log4j.error(e.getMessage(), e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(e.getMessage());
      bundle.setResult(msg);

    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      logger.log(message);
      log4j.error(message, e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(message);
      bundle.setResult(msg);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void updateLegacyCostTrx() {
    // TODO Auto-generated method stub

  }

  private boolean checkProductWithCost(CostingRule rule, Set<String> naturalOrgs) {
    Date currentDate = new Date();
    StringBuffer where = new StringBuffer();
    where.append(" as p");
    where.append(" where p." + Product.PROPERTY_CALCULATECOST + " = true");
    where.append("   and p." + Product.PROPERTY_ORGANIZATION + " in :porgs");
    where.append("   and (p." + Product.PROPERTY_COSTTYPE + " is not null");
    // TODO: Change query if current algorithm property is added to product.
    where.append("     or exists (select 1 from " + CostingRule.ENTITY_NAME + " as cr");
    where.append("        where cr." + CostingRule.PROPERTY_ORGANIZATION + " = :ruleorg");
    where.append("          and cr != :rule");
    where.append("          and cr." + CostingRule.PROPERTY_VALIDATED + " = true");
    where.append("          and cr." + CostingRule.PROPERTY_STARTINGDATE + " < " + currentDate);
    where.append("          and COALESCE(cr." + CostingRule.PROPERTY_ENDINGDATE + ", "
        + currentDate + ") >= " + currentDate);
    where.append("         )");
    where.append("     )");

    OBQuery<Product> pQry = OBDal.getInstance().createQuery(Product.class, where.toString());
    pQry.setFilterOnReadableOrganization(false);
    pQry.setNamedParameter("porgs", naturalOrgs);
    pQry.setNamedParameter("ruleOrg", rule.getOrganization());
    pQry.setNamedParameter("rule", rule);
    return pQry.count() > 0;
  }

  private boolean checkTrxWithNoPreviousCostingRule(CostingRule rule, Set<String> naturalOrgs) {
    Date currentDate = new Date();
    StringBuffer where = new StringBuffer();
    where.append(" as p");
    where.append(" where p." + Product.PROPERTY_CALCULATECOST + " = true");
    where.append("   and p." + Product.PROPERTY_ORGANIZATION + " in :porgs");
    // No previous costing rule for product
    where.append("   and p." + Product.PROPERTY_COSTTYPE + " is null");
    // TODO: Change query if current algorithm property is added to product.
    where.append("   and not exists (select 1 from " + CostingRule.ENTITY_NAME + " as cr");
    where.append("      where cr." + CostingRule.PROPERTY_ORGANIZATION + " = :ruleorg");
    where.append("        and cr != :rule");
    where.append("        and cr." + CostingRule.PROPERTY_VALIDATED + " = true");
    where.append("        and cr." + CostingRule.PROPERTY_STARTINGDATE + " < " + currentDate);
    where.append("        and COALESCE(cr." + CostingRule.PROPERTY_ENDINGDATE + ", " + currentDate
        + ") >= " + currentDate);
    where.append("     )");
    // Has trx.
    where.append(" and exists (select 1 from " + MaterialTransaction.ENTITY_NAME);
    where.append("   where " + MaterialTransaction.PROPERTY_PRODUCT + " = p");

    OBQuery<Product> pQry = OBDal.getInstance().createQuery(Product.class, where.toString());
    pQry.setFilterOnReadableOrganization(false);
    pQry.setNamedParameter("porgs", naturalOrgs);
    pQry.setNamedParameter("ruleOrg", rule.getOrganization());
    pQry.setNamedParameter("rule", rule);
    return pQry.count() > 0;
  }

  private void checkAllTrxCalculated(Set<String> naturalOrgs, Set<String> childOrgs) {
    StringBuffer where = new StringBuffer();
    where.append(" as p");
    where.append(" where p." + Product.PROPERTY_CALCULATECOST + " = true");
    where.append("   and p." + Product.PROPERTY_ORGANIZATION + " in :porgs");
    where.append("   and exists (select 1 from " + MaterialTransaction.ENTITY_NAME + " as trx ");
    where.append("       where trx." + MaterialTransaction.PROPERTY_PRODUCT + " = p");
    where.append("         and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    where.append("         and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + "in :childOrgs");
    where.append("     )");
    OBQuery<Product> pQry = OBDal.getInstance().createQuery(Product.class, where.toString());
    pQry.setFilterOnReadableOrganization(false);
    pQry.setNamedParameter("porgs", naturalOrgs);
    pQry.setNamedParameter("childOrgs", childOrgs);
    if (pQry.count() > 0) {
      throw new OBException("@TrxWithCostNoCalculated@");
    }
  }

  private void checkNoTrxWithCostCalculated(Set<String> naturalOrgs, Set<String> childOrgs) {
    StringBuffer where = new StringBuffer();
    where.append(" as p");
    where.append(" where p." + Product.PROPERTY_CALCULATECOST + " = true");
    where.append("   and p." + Product.PROPERTY_ORGANIZATION + " in :porgs");
    where.append("   and exists (select 1 from " + MaterialTransaction.ENTITY_NAME + " as trx ");
    where.append("      where trx." + MaterialTransaction.PROPERTY_PRODUCT + " = p");
    where.append("        and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    where.append("        and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + " in :childOrgs");
    where.append("     )");
    OBQuery<Product> pQry = OBDal.getInstance().createQuery(Product.class, where.toString());
    pQry.setFilterOnReadableOrganization(false);
    pQry.setNamedParameter("porgs", naturalOrgs);
    pQry.setNamedParameter("childOrgs", childOrgs);
    if (pQry.count() > 0) {
      throw new OBException("@ProductsWithTrxCalculated@");
    }

    where = new StringBuffer();
    where.append(" as p");
    where.append(" where p." + Product.PROPERTY_CALCULATECOST + " = true");
    where.append("   and p." + Product.PROPERTY_ORGANIZATION + " in :porgs");
    where.append("   and exists (select 1 from " + Costing.ENTITY_NAME + " as costing ");
    where.append("       where costing." + Costing.PROPERTY_PRODUCT + " = p");
    where.append("         and costing." + Costing.PROPERTY_COSTTYPE + " = 'AV'");
    where.append("     )");
    pQry = OBDal.getInstance().createQuery(Product.class, where.toString());
    pQry.setFilterOnReadableOrganization(false);
    pQry.setNamedParameter("porgs", naturalOrgs);
    if (pQry.count() > 0) {
      throw new OBException("@ProductsWithCosting@");
    }
  }

  private List<Warehouse> getWarehouselist(Set<String> orgs) {
    OBQuery<Warehouse> whQry = OBDal.getInstance().createQuery(Warehouse.class,
        Warehouse.PROPERTY_ORGANIZATION + ".id in :orgs");
    whQry.setFilterOnReadableOrganization(false);
    whQry.setNamedParameter("orgs", orgs);
    return whQry.list();
  }

  private Date getFirstDate() {
    SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
    try {
      return outputFormat.parse("01-01-1900");
    } catch (ParseException e) {
      // Error parsing the date.
      log4j.error("Error parsing the date.", e);
      return null;
    }
  }

  private void createPhysicalInventories(String orgId, Warehouse wh, CostingRule rule) {
    List<StorageDetail> stock = getStock(orgId, wh);
    if (!stock.isEmpty()) {
      Organization org = OBDal.getInstance().get(Organization.class, orgId);
      CostingRuleInit cri = OBProvider.getInstance().get(CostingRuleInit.class);
      cri.setOrganization(org);
      cri.setWarehouse(wh);
      cri.setCostingRule(rule);
      List<CostingRuleInit> criList = rule.getCostingRuleInitList();
      criList.add(cri);
      rule.setCostingRuleInitList(criList);
      InventoryCount closeInv = OBProvider.getInstance().get(InventoryCount.class);
      closeInv.setOrganization(org);
      closeInv.setWarehouse(wh);
      cri.setCloseInventory(closeInv);
      InventoryCount initInv = OBProvider.getInstance().get(InventoryCount.class);
      closeInv.setOrganization(org);
      closeInv.setWarehouse(wh);
      cri.setInitInventory(initInv);
      long line = 10;
      for (StorageDetail sd : stock) {
        InventoryCountLine closeLine = OBProvider.getInstance().get(InventoryCountLine.class);
        closeLine.setOrganization(org);
        closeLine.setLineNo(line);
        closeLine.setPhysInventory(closeInv);
        closeLine.setBookQuantity(sd.getQuantityOnHand());
        closeLine.setQuantityCount(BigDecimal.ZERO);
        InventoryCountLine initLine = OBProvider.getInstance().get(InventoryCountLine.class);
        initLine.setOrganization(org);
        initLine.setLineNo(line);
        initLine.setPhysInventory(initInv);
        initLine.setBookQuantity(BigDecimal.ZERO);
        initLine.setQuantityCount(sd.getQuantityOnHand());
        OBDal.getInstance().save(closeLine);
        OBDal.getInstance().save(initLine);

        line += 10;
      }
      OBDal.getInstance().save(closeInv);
      OBDal.getInstance().save(initInv);
      OBDal.getInstance().save(cri);
      OBDal.getInstance().save(rule);
    }
  }

  private List<StorageDetail> getStock(String orgId, Warehouse wh) {
    StringBuffer where = new StringBuffer();
    where.append(" as sd ");
    where.append("   join sd." + StorageDetail.PROPERTY_STORAGEBIN + " as locator");
    where.append("   join sd." + StorageDetail.PROPERTY_PRODUCT + " as p");
    where.append(" where sd." + StorageDetail.PROPERTY_ORGANIZATION + ".id = :orgId");
    where.append("   and locator." + Locator.PROPERTY_WAREHOUSE + " = :wh");
    where.append("   and sd." + StorageDetail.PROPERTY_QUANTITYONHAND + " <> 0");
    where.append("   and p." + Product.PROPERTY_CALCULATECOST + " = true");

    OBQuery<StorageDetail> sdQry = OBDal.getInstance().createQuery(StorageDetail.class,
        where.toString());
    sdQry.setFilterOnReadableOrganization(false);
    sdQry.setNamedParameter("orgId", orgId);
    sdQry.setNamedParameter("wh", wh);

    return sdQry.list();
  }

  private InventoryCountLine getInitIcl(InventoryCount initInventory, Long lineNo) {
    OBQuery<InventoryCountLine> iclQry = OBDal.getInstance().createQuery(
        InventoryCountLine.class,
        InventoryCountLine.PROPERTY_PHYSINVENTORY + " = :inventory and "
            + InventoryCountLine.PROPERTY_LINENO + " = :line");
    iclQry.setNamedParameter("inventory", initInventory);
    iclQry.setNamedParameter("line", lineNo);
    return iclQry.uniqueResult();
  }
}
