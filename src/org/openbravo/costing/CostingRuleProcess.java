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
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.DateType;
import org.hibernate.type.StringType;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.materialmgmt.InventoryCountProcess;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
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
  private static final Logger log4j = Logger.getLogger(CostingRuleProcess.class);

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
      final Set<String> childOrgs = osp.getChildTree(rule.getOrganization().getId(), true);
      final Set<String> naturalOrgs = osp.getNaturalTree(rule.getOrganization().getId());

      // Checks
      migrationCheck();
      boolean existsPreviousRule = existsPreviousRule(rule);
      boolean existsTransactions = existsTransactions(naturalOrgs, childOrgs);
      if (existsPreviousRule) {
        // Product with costing rule. All trx must be calculated.
        checkAllTrxCalculated(naturalOrgs, childOrgs);
      } else if (existsTransactions) {
        // Product configured to have cost not calculated cannot have transactions with cost
        // calculated.
        checkNoTrxWithCostCalculated(naturalOrgs, childOrgs);
      }
      // Inventories are only needed if the costing rule is updating a previous rule or legacy cost
      // engine was used.
      if (existsPreviousRule) {
        createCostingRuleInits(rule, childOrgs);
        // Set valid from date
        Date startingDate = DateUtils.truncate(new Date(), Calendar.SECOND);
        rule.setStartingDate(startingDate);
        log4j.debug("setting starting date " + startingDate);
        OBDal.getInstance().flush();

        // Update cost of inventories and process starting physical inventories.
        for (CostingRuleInit cri : rule.getCostingRuleInitList()) {
          for (InventoryCountLine icl : cri.getCloseInventory()
              .getMaterialMgmtInventoryCountLineList()) {
            MaterialTransaction trx = getInventoryLineTransaction(icl);
            // Remove 1 second from transaction date to ensure that cost is calculated with previous
            // costing rule.
            trx.setTransactionProcessDate(DateUtils.addSeconds(startingDate, -1));
            Currency cur = FinancialUtils.getLegalEntityCurrency(trx.getOrganization());
            BigDecimal trxCost = CostingUtils.getTransactionCost(trx, startingDate, true, cur);
            BigDecimal cost = trxCost.divide(trx.getMovementQuantity().abs(), cur
                .getCostingPrecision().intValue(), RoundingMode.HALF_UP);

            trx.setCostCalculated(true);
            trx.setCostingStatus("CC");
            trx.setTransactionCost(trxCost);
            OBDal.getInstance().save(trx);
            InventoryCountLine initICL = getInitIcl(cri.getInitInventory(), icl);
            initICL.setCost(cost);
            OBDal.getInstance().save(initICL);
          }
          OBDal.getInstance().flush();
          new InventoryCountProcess().processInventory(cri.getInitInventory());
        }
      }

      rule.setValidated(true);
      CostingStatus.getInstance().setMigrated();
      OBDal.getInstance().save(rule);
    } catch (final OBException e) {
      OBDal.getInstance().rollbackAndClose();
      String resultMsg = OBMessageUtils.parseTranslation(e.getMessage());
      logger.log(resultMsg);
      log4j.error(e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(resultMsg);
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
    bundle.setResult(msg);
  }

  private void migrationCheck() {
    if (isCostingMigrationNeeded() && !CostingStatus.getInstance().isMigrated()) {
      throw new OBException("@CostMigrationNotDone@");
    }
  }

  private boolean isCostingMigrationNeeded() {
    OBQuery<org.openbravo.model.materialmgmt.cost.Costing> costingQry = OBDal.getInstance()
        .createQuery(org.openbravo.model.materialmgmt.cost.Costing.class, "");
    costingQry.setFilterOnReadableClients(false);
    costingQry.setFilterOnReadableOrganization(false);

    return costingQry.count() > 0;
  }

  private boolean existsPreviousRule(CostingRule rule) {
    StringBuffer where = new StringBuffer();
    where.append(" as cr");
    where.append(" where cr." + CostingRule.PROPERTY_ORGANIZATION + " = :ruleOrg");
    where.append("   and cr." + CostingRule.PROPERTY_VALIDATED + " = true");

    OBQuery<CostingRule> crQry = OBDal.getInstance().createQuery(CostingRule.class,
        where.toString());
    crQry.setFilterOnReadableOrganization(false);
    crQry.setNamedParameter("ruleOrg", rule.getOrganization());
    return crQry.count() > 0;
  }

  private boolean existsTransactions(Set<String> naturalOrgs, Set<String> childOrgs) {
    StringBuffer where = new StringBuffer();
    where.append(" as p");
    where.append(" where p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    where.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("   and p." + Product.PROPERTY_ORGANIZATION + ".id in (:porgs)");
    where.append("   and exists (select 1 from " + MaterialTransaction.ENTITY_NAME);
    where.append("     where " + MaterialTransaction.PROPERTY_PRODUCT + " = p)");
    where.append("      and " + MaterialTransaction.PROPERTY_ORGANIZATION + " .id in (:childOrgs)");

    OBQuery<Product> pQry = OBDal.getInstance().createQuery(Product.class, where.toString());
    pQry.setFilterOnReadableOrganization(false);
    pQry.setNamedParameter("porgs", naturalOrgs);
    pQry.setNamedParameter("childOrgs", childOrgs);
    return pQry.count() > 0;
  }

  private void checkAllTrxCalculated(Set<String> naturalOrgs, Set<String> childOrgs) {
    StringBuffer where = new StringBuffer();
    where.append(" as p");
    where.append(" where p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    where.append("  and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("  and p." + Product.PROPERTY_ORGANIZATION + ".id in :porgs");
    where.append("  and exists (select 1 from " + MaterialTransaction.ENTITY_NAME + " as trx ");
    where.append("   where trx." + MaterialTransaction.PROPERTY_PRODUCT + " = p");
    where.append("     and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in :childOrgs");
    where.append("     and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    where.append("   )");
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
    where.append(" where p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    where.append("  and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("  and p." + Product.PROPERTY_ORGANIZATION + ".id in :porgs");
    where.append("  and exists (select 1 from " + MaterialTransaction.ENTITY_NAME + " as trx ");
    where.append("   where trx." + MaterialTransaction.PROPERTY_PRODUCT + " = p");
    where.append("     and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    where.append("     and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in :childOrgs");
    where.append("   )");
    OBQuery<Product> pQry = OBDal.getInstance().createQuery(Product.class, where.toString());
    pQry.setFilterOnReadableOrganization(false);
    pQry.setNamedParameter("porgs", naturalOrgs);
    pQry.setNamedParameter("childOrgs", childOrgs);
    if (pQry.count() > 0) {
      throw new OBException("@ProductsWithTrxCalculated@");
    }
  }

  protected void createCostingRuleInits(CostingRule rule, Set<String> childOrgs) {
    // Create inventories.
    List<Object[]> whorgl = getWarehouseAndOrgsWithStock(childOrgs);
    for (Object[] record : whorgl) {
      // Warehouse wh = OBDal.getInstance().get(Warehouse.class, record[0]);
      createPhysicalInventories((String) record[1], (Warehouse) record[0], rule);
    }

    // Process closing physical inventories.
    for (CostingRuleInit cri : rule.getCostingRuleInitList()) {
      new InventoryCountProcess().processInventory(cri.getCloseInventory());
    }
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> getWarehouseAndOrgsWithStock(Set<String> orgs) {
    StringBuffer select = new StringBuffer();
    select.append(" select distinct");
    select.append(" locator." + Locator.PROPERTY_WAREHOUSE);
    select.append(", sd." + StorageDetail.PROPERTY_ORGANIZATION + ".id");
    select.append("\n from " + StorageDetail.ENTITY_NAME + " as sd");
    select.append("   join sd." + StorageDetail.PROPERTY_STORAGEBIN + " as locator");
    select.append("   join sd." + StorageDetail.PROPERTY_PRODUCT + " as p");
    select.append("\n where sd." + StorageDetail.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    select.append("   and (sd." + StorageDetail.PROPERTY_QUANTITYONHAND + " <> 0");
    select.append("        or sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY + " <> 0)");
    select.append("   and p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    select.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    Query querySelect = OBDal.getInstance().getSession().createQuery(select.toString());
    querySelect.setParameterList("orgs", orgs);
    return querySelect.list();
  }

  private void createPhysicalInventories(String orgId, Warehouse wh, CostingRule rule) {
    Organization org = OBDal.getInstance().get(Organization.class, orgId);
    CostingRuleInit cri = OBProvider.getInstance().get(CostingRuleInit.class);
    cri.setClient(org.getClient());
    cri.setOrganization(org);
    cri.setWarehouse(wh);
    cri.setCostingRule(rule);
    List<CostingRuleInit> criList = rule.getCostingRuleInitList();
    criList.add(cri);
    rule.setCostingRuleInitList(criList);

    InventoryCount closeInv = OBProvider.getInstance().get(InventoryCount.class);
    closeInv.setClient(org.getClient());
    closeInv.setOrganization(org);
    closeInv.setName(OBMessageUtils.messageBD("CostCloseInventory"));
    closeInv.setWarehouse(wh);
    closeInv.setMovementDate(new Date());
    cri.setCloseInventory(closeInv);

    InventoryCount initInv = OBProvider.getInstance().get(InventoryCount.class);
    initInv.setClient(org.getClient());
    initInv.setOrganization(org);
    initInv.setName(OBMessageUtils.messageBD("CostInitInventory"));
    initInv.setWarehouse(wh);
    initInv.setMovementDate(new Date());
    cri.setInitInventory(initInv);
    OBDal.getInstance().save(rule);
    OBDal.getInstance().save(closeInv);
    OBDal.getInstance().save(initInv);
    OBDal.getInstance().flush();
    insertLines(closeInv, true, orgId, wh.getId());
    insertLines(initInv, false, orgId, wh.getId());
    OBDal.getInstance().refresh(closeInv);
    OBDal.getInstance().refresh(initInv);

    OBDal.getInstance().flush();
  }

  private void insertLines(InventoryCount closeInv, boolean isClosing, String orgId, String whId) {
    // In case get_uuid is not already registered, it's registered now.
    OBDal.getInstance().registerSQLFunction("get_uuid",
        new StandardSQLFunction("get_uuid", new StringType()));
    OBDal.getInstance().registerSQLFunction("now", new StandardSQLFunction("now", new DateType()));

    // FIXME: Insert should be done with a loop based on scroll.
    StringBuffer insert = new StringBuffer();
    insert.append("insert into " + InventoryCountLine.ENTITY_NAME + "(");
    insert.append(" id ");
    insert.append(", " + InventoryCountLine.PROPERTY_ACTIVE);
    insert.append(", " + InventoryCountLine.PROPERTY_CLIENT);
    insert.append(", " + InventoryCountLine.PROPERTY_ORGANIZATION);
    insert.append(", " + InventoryCountLine.PROPERTY_CREATIONDATE);
    insert.append(", " + InventoryCountLine.PROPERTY_CREATEDBY);
    insert.append(", " + InventoryCountLine.PROPERTY_UPDATED);
    insert.append(", " + InventoryCountLine.PROPERTY_UPDATEDBY);
    insert.append(", " + InventoryCountLine.PROPERTY_PHYSINVENTORY);
    insert.append(", " + InventoryCountLine.PROPERTY_LINENO);
    insert.append(", " + InventoryCountLine.PROPERTY_STORAGEBIN);
    insert.append(", " + InventoryCountLine.PROPERTY_PRODUCT);
    insert.append(", " + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE);
    insert.append(", " + InventoryCountLine.PROPERTY_QUANTITYCOUNT);
    insert.append(", " + InventoryCountLine.PROPERTY_BOOKQUANTITY);
    insert.append(", " + InventoryCountLine.PROPERTY_ORDERQUANTITY);
    insert.append(", " + InventoryCountLine.PROPERTY_QUANTITYORDERBOOK);
    insert.append(", " + InventoryCountLine.PROPERTY_UOM);
    insert.append(", " + InventoryCountLine.PROPERTY_ORDERUOM);
    insert.append(" )\n select get_uuid()");
    insert.append(", sd." + StorageDetail.PROPERTY_ACTIVE);
    insert.append(", sd." + StorageDetail.PROPERTY_CLIENT);
    insert.append(", sd." + StorageDetail.PROPERTY_ORGANIZATION);
    insert.append(", now()");
    insert.append(", u");
    insert.append(", now()");
    insert.append(", u");
    insert.append(", inv");
    insert.append(", 10L");
    insert.append(", locator");
    insert.append(", p");
    insert.append(", sd." + StorageDetail.PROPERTY_ATTRIBUTESETVALUE);
    if (isClosing) {
      // Closing inventories set qty count to zero to empty the stock
      // O is multiplied to force a cast to BigDecimal
      insert.append(", 0 * sd." + StorageDetail.PROPERTY_QUANTITYONHAND);
      insert.append(", sd." + StorageDetail.PROPERTY_QUANTITYONHAND);
      insert.append(", 0 * sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY);
      insert.append(", sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY);
    } else {
      // Init inventories set qty book to zero to restore the stock
      // O is multiplied to force a cast to BigDecimal
      insert.append(", sd." + StorageDetail.PROPERTY_QUANTITYONHAND);
      insert.append(", 0 * sd." + StorageDetail.PROPERTY_QUANTITYONHAND);
      insert.append(", sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY);
      insert.append(", 0 * sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY);
    }
    insert.append(", sd." + StorageDetail.PROPERTY_UOM);
    insert.append(", sd." + StorageDetail.PROPERTY_ORDERUOM);
    insert.append("\n from " + StorageDetail.ENTITY_NAME + " as sd");
    insert.append("   join sd." + StorageDetail.PROPERTY_STORAGEBIN + " as locator");
    insert.append("   join sd." + StorageDetail.PROPERTY_PRODUCT + " as p");
    insert.append(", " + User.ENTITY_NAME + " as u");
    insert.append(", " + InventoryCount.ENTITY_NAME + " as inv");
    insert.append("\n where sd." + StorageDetail.PROPERTY_ORGANIZATION + ".id = :orgId");
    insert.append("   and locator." + Locator.PROPERTY_WAREHOUSE + ".id = :wh");
    insert.append("   and (sd." + StorageDetail.PROPERTY_QUANTITYONHAND + " <> 0");
    insert.append("    or sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY + " <> 0)");
    insert.append("   and p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    insert.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    insert.append("   and inv.id = :inv");
    insert.append("   and u.id = :user");
    // insert.append("\n order by p." + Product.PROPERTY_NAME);
    Query queryInsert = OBDal.getInstance().getSession().createQuery(insert.toString());
    queryInsert.setString("orgId", orgId);
    queryInsert.setString("wh", whId);
    queryInsert.setString("inv", closeInv.getId());
    queryInsert.setString("user", (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    queryInsert.executeUpdate();
  }

  protected MaterialTransaction getInventoryLineTransaction(InventoryCountLine icl) {
    OBQuery<MaterialTransaction> trxQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class,
        MaterialTransaction.PROPERTY_PHYSICALINVENTORYLINE + ".id = :invline");
    trxQry.setFilterOnReadableClients(false);
    trxQry.setFilterOnReadableOrganization(false);
    trxQry.setNamedParameter("invline", icl.getId());
    MaterialTransaction trx = trxQry.uniqueResult();
    return trx;
  }

  protected InventoryCountLine getInitIcl(InventoryCount initInventory, InventoryCountLine icl) {
    StringBuffer where = new StringBuffer();
    where.append(InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inventory");
    where.append(" and " + InventoryCountLine.PROPERTY_PRODUCT + ".id = :product");
    where.append(" and " + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE + ".id = :asi");
    where.append(" and " + InventoryCountLine.PROPERTY_STORAGEBIN + ".id = :locator");
    if (icl.getOrderUOM() == null) {
      where.append(" and " + InventoryCountLine.PROPERTY_ORDERUOM + " is null");
    } else {
      where.append(" and " + InventoryCountLine.PROPERTY_ORDERUOM + ".id = :orderuom");
    }
    OBQuery<InventoryCountLine> iclQry = OBDal.getInstance().createQuery(InventoryCountLine.class,
        where.toString());
    iclQry.setFilterOnReadableClients(false);
    iclQry.setFilterOnReadableOrganization(false);
    iclQry.setNamedParameter("inventory", initInventory.getId());
    iclQry.setNamedParameter("product", icl.getProduct().getId());
    iclQry.setNamedParameter("asi", icl.getAttributeSetValue().getId());
    iclQry.setNamedParameter("locator", icl.getStorageBin().getId());
    if (icl.getOrderUOM() != null) {
      iclQry.setNamedParameter("orderuom", icl.getOrderUOM().getId());
    }
    return iclQry.uniqueResult();
  }
}
