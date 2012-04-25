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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationType;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.CostingAlgorithm;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.CostingRuleInit;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DbUtility;

public class CostingMigrationProcess implements Process {
  private ProcessLogger logger;
  private static Logger log4j = Logger.getLogger(CostingRuleProcess.class);
  private static CostingAlgorithm averageAlgorithm = null;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));
    try {
      OBContext.setAdminMode(false);

      // FIXME: Add proper messages
      if (isMigrated()) {
        throw new OBException("Migration already done");
      } else {
        if (isCostingMigrationNotNeeded()) {
          throw new OBException("Migration not needed");
        }
      }

      boolean migrationStarted = rulesCreated();
      if (!migrationStarted) {
        doChecks();
        prepareInstance();
        createRules();
      } else {
        checkAllInventoriesAreProcessed();
        for (CostingRule rule : getRules()) {
          rule.setValidated(true);
          OBDal.getInstance().save(rule);
        }
        createCostMigratedPreference();
      }
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
    bundle.setResult(msg);
  }

  private void prepareInstance() {
    // TODO update products with costtype = 'AV' to calculate_cost = true
    StringBuffer update = new StringBuffer();
    update.append("update " + Product.ENTITY_NAME);
    update.append(" set " + Product.PROPERTY_CALCULATECOST + " = true");
    update.append(" where " + Product.PROPERTY_COSTTYPE + " is not null");
    update.append("   and " + Product.PROPERTY_COSTTYPE + " <> 'ST'");
    Query updateProduct = OBDal.getInstance().getSession().createQuery(update.toString());
    updateProduct.executeUpdate();

    // TODO insert STANDARD cost for products with costtype = 'ST'.

  }

  private boolean isCostingMigrationNotNeeded() {
    OBQuery<Costing> costingQry = OBDal.getInstance().createQuery(Costing.class, "");
    costingQry.setFilterOnReadableClients(false);
    costingQry.setFilterOnReadableOrganization(false);

    return costingQry.count() == 0;
  }

  private boolean isMigrated() {
    OBQuery<Preference> prefQry = OBDal.getInstance().createQuery(Preference.class,
        Preference.PROPERTY_ATTRIBUTE + " = 'CostingMigrationDone'");
    prefQry.setFilterOnReadableClients(false);
    prefQry.setFilterOnReadableOrganization(false);

    return prefQry.count() > 0;
  }

  private boolean rulesCreated() {
    OBQuery<CostingRule> crQry = OBDal.getInstance().createQuery(CostingRule.class, "");
    crQry.setFilterOnReadableClients(false);
    crQry.setFilterOnReadableOrganization(false);

    return crQry.count() > 0;
  }

  private void doChecks() {
    // TODO Check unposted documents in the future.

  }

  private void createRules() throws Exception {
    List<Client> clients = getClients();
    for (Client client : clients) {
      List<Organization> legalEntities = getLegalEntitiesOfClient(client);
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client.getId());
      for (Organization org : legalEntities) {
        CostingRule rule = createCostingRule(org);
        processRule(rule, osp);
      }
    }
  }

  private static List<Client> getClients() {
    OBCriteria<Client> obcClient = OBDal.getInstance().createCriteria(Client.class);
    obcClient.setFilterOnReadableClients(false);
    obcClient.add(Restrictions.ne(Client.PROPERTY_ID, "0"));
    return obcClient.list();
  }

  private List<Organization> getLegalEntitiesOfClient(Client client) {
    StringBuffer where = new StringBuffer();
    where.append(" as org");
    where.append(" join org." + Organization.PROPERTY_ORGANIZATIONTYPE + " as orgType");
    where.append(" where org." + Organization.PROPERTY_CLIENT + ".id = :client");
    where.append("   and orgType." + OrganizationType.PROPERTY_LEGALENTITY + " = true");
    OBQuery<Organization> orgQry = OBDal.getInstance().createQuery(Organization.class,
        where.toString());
    orgQry.setFilterOnReadableClients(false);
    orgQry.setFilterOnReadableOrganization(false);
    orgQry.setNamedParameter("client", client.getId());
    return orgQry.list();
  }

  private CostingRule createCostingRule(Organization org) {
    CostingRule rule = OBProvider.getInstance().get(CostingRule.class);
    rule.setClient(org.getClient());
    rule.setOrganization(org);
    rule.setCostingAlgorithm(getAverageAlgorithm());
    rule.setValidated(false);
    rule.setEndingDate(null);
    rule.setStartingDate(null);
    OBDal.getInstance().save(rule);
    return rule;
  }

  private void processRule(CostingRule rule, OrganizationStructureProvider osp) {
    final Set<String> childOrgs = osp.getChildTree(rule.getOrganization().getId(), true);
    CostingRuleProcess crp = new CostingRuleProcess();
    crp.createCostingRuleInits(rule, childOrgs);

    // Set valid from date
    Date startingDate = new Date();
    rule.setStartingDate(startingDate);
    log4j.debug("setting starting date " + startingDate);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(rule);

    // Update cost of inventories and process starting physical inventories.
    for (CostingRuleInit cri : rule.getCostingRuleInitList()) {
      for (InventoryCountLine icl : cri.getCloseInventory().getMaterialMgmtInventoryCountLineList()) {
        MaterialTransaction trx = crp.getInventoryLineTransaction(icl);

        trx.setTransactionProcessDate(DateUtils.addSeconds(trx.getTransactionProcessDate(), -1));
        BigDecimal cost = getLegacyProductCost(trx.getProduct()).multiply(
            trx.getMovementQuantity().abs());
        trx.setCostCalculated(true);
        trx.setTransactionCost(cost);
        OBDal.getInstance().save(trx);
        InventoryCountLine initICL = crp.getInitIcl(cri.getInitInventory(), icl);
        initICL.setCost(cost);
        OBDal.getInstance().save(initICL);
      }
    }
  }

  private BigDecimal getLegacyProductCost(Product product) {
    Date date = new Date();
    StringBuffer where = new StringBuffer();
    where.append(Costing.PROPERTY_PRODUCT + ".id = :product");
    where.append("  and " + Costing.PROPERTY_STARTINGDATE + " <= :startingDate");
    where.append("  and " + Costing.PROPERTY_ENDINGDATE + " > :endingDate");
    where.append("  and " + Costing.PROPERTY_COSTTYPE + " = 'AV'");
    OBQuery<Costing> costQry = OBDal.getInstance().createQuery(Costing.class, where.toString());
    costQry.setFilterOnReadableOrganization(false);
    costQry.setFilterOnReadableClients(false);
    costQry.setNamedParameter("product", product.getId());
    costQry.setNamedParameter("startingDate", date);
    costQry.setNamedParameter("endingDate", date);

    if (costQry.count() > 0) {
      if (costQry.count() > 1) {
        log4j.warn("More than one cost found for same date: " + OBDateUtils.formatDate(date)
            + " for product: " + product.getName() + " (" + product.getId() + ")");
      }
      return costQry.list().get(0).getCost();
    }
    // If no average cost is found try with standard cost.
    where = new StringBuffer();
    where.append(Costing.PROPERTY_PRODUCT + ".id = :product");
    where.append("  and " + Costing.PROPERTY_STARTINGDATE + " <= :startingDate");
    where.append("  and " + Costing.PROPERTY_ENDINGDATE + " > :endingDate");
    where.append("  and " + Costing.PROPERTY_COSTTYPE + " = 'ST'");
    costQry = OBDal.getInstance().createQuery(Costing.class, where.toString());
    costQry.setFilterOnReadableOrganization(false);
    costQry.setFilterOnReadableClients(false);
    costQry.setNamedParameter("product", product.getId());
    costQry.setNamedParameter("startingDate", date);
    costQry.setNamedParameter("endingDate", date);

    if (costQry.count() > 0) {
      if (costQry.count() > 1) {
        log4j.warn("More than one cost found for same date: " + OBDateUtils.formatDate(date)
            + " for product: " + product.getName() + " (" + product.getId() + ")");
      }
      return costQry.list().get(0).getCost();
    }
    return BigDecimal.ZERO;
  }

  private void checkAllInventoriesAreProcessed() {
    StringBuffer where = new StringBuffer();
    where.append(" as cri ");
    where.append("   join cri." + CostingRuleInit.PROPERTY_INITINVENTORY + " as ipi");
    where.append(" where ipi." + InventoryCount.PROPERTY_PROCESSED + " = false");

    OBQuery<CostingRuleInit> criQry = OBDal.getInstance().createQuery(CostingRuleInit.class,
        where.toString());
    criQry.setFilterOnReadableClients(false);
    criQry.setFilterOnReadableOrganization(false);
    List<CostingRuleInit> criList = criQry.list();
    if (criList.isEmpty()) {
      return;
    }
    StringBuffer inventoryList = new StringBuffer();
    for (CostingRuleInit cri : criList) {
      if (inventoryList.length() > 0) {
        inventoryList.append(", ");
      }
      inventoryList.append(cri.getInitInventory().getIdentifier());
    }
    // FIXME: Add message to AD
    throw new OBException("@unprocessedInventories@ " + inventoryList.toString());
  }

  private List<CostingRule> getRules() {
    OBCriteria<CostingRule> crCrit = OBDal.getInstance().createCriteria(CostingRule.class);
    crCrit.setFilterOnReadableClients(false);
    crCrit.setFilterOnReadableOrganization(false);

    return crCrit.list();
  }

  /**
   * Create a preference to be able to determine that the instance is ready to use APRM.
   */
  private void createCostMigratedPreference() {
    Organization org0 = OBDal.getInstance().get(Organization.class, "0");
    Client client0 = OBDal.getInstance().get(Client.class, "0");

    Preference newPref = OBProvider.getInstance().get(Preference.class);
    newPref.setClient(client0);
    newPref.setOrganization(org0);
    newPref.setPropertyList(false);
    newPref.setAttribute("CostingMigrationDone");

    OBDal.getInstance().save(newPref);
  }

  private static CostingAlgorithm getAverageAlgorithm() {
    if (averageAlgorithm != null) {
      return averageAlgorithm;
    }
    OBCriteria<CostingAlgorithm> costalgCrit = OBDal.getInstance().createCriteria(
        CostingAlgorithm.class);
    costalgCrit.add(Restrictions.eq(CostingAlgorithm.PROPERTY_JAVACLASSNAME,
        "org.openbravo.costing.AverageAlgorithm"));
    costalgCrit.add(Restrictions.eq(CostingAlgorithm.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, "0")));
    costalgCrit.setFilterOnReadableClients(false);
    costalgCrit.setFilterOnReadableOrganization(false);
    averageAlgorithm = (CostingAlgorithm) costalgCrit.uniqueResult();
    return averageAlgorithm;
  }
}
