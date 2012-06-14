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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.DateType;
import org.hibernate.type.StringType;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.alert.Alert;
import org.openbravo.model.ad.alert.AlertRecipient;
import org.openbravo.model.ad.alert.AlertRule;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationType;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.CostingAlgorithm;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.CostingRuleInit;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
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
  private final String alertRuleName = "Products with transactions without available cost on date.";

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));
    try {
      OBContext.setAdminMode(false);

      if (CostingStatus.getInstance().isMigrated()) {
        throw new OBException("@CostMigratedInstance@");
      } else {
        if (isCostingMigrationNotNeeded()) {
          throw new OBException("@CostMigrationNoNeeded@");
        }
      }

      // FIXME: Load functions in core
      OBDal.getInstance().registerSQLFunction("get_uuid",
          new StandardSQLFunction("get_uuid", new StringType()));
      OBDal.getInstance()
          .registerSQLFunction("now", new StandardSQLFunction("now", new DateType()));

      if (!isMigrationFirstPhaseCompleted()) {
        doChecks();

        updateLegacyCosts();
        createRules();
        createMigrationFirstPhaseCompletedPreference();
      } else {
        checkAllInventoriesAreProcessed();
        for (CostingRule rule : getRules()) {
          rule.setValidated(true);
          OBDal.getInstance().save(rule);
        }
        CostingStatus.getInstance().setMigrated();
        deleteMigrationFirstPhaseCompletedPreference();
      }
    } catch (final OBException e) {
      OBDal.getInstance().rollbackAndClose();
      String resultMsg = OBMessageUtils.parseTranslation(e.getMessage());
      logger.log(resultMsg);
      log4j.error(e.getMessage(), e);
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

  private void doChecks() {
    // Check all transactions have a legacy cost available.
    AlertRule legacyCostAvailableAlert = getLegacyCostAvailableAlert();
    if (legacyCostAvailableAlert == null) {
      Organization org0 = OBDal.getInstance().get(Organization.class, "0");
      Client client0 = OBDal.getInstance().get(Client.class, "0");

      legacyCostAvailableAlert = OBProvider.getInstance().get(AlertRule.class);
      legacyCostAvailableAlert.setClient(client0);
      legacyCostAvailableAlert.setOrganization(org0);
      legacyCostAvailableAlert.setName(alertRuleName);
      // Header tab of Product window
      legacyCostAvailableAlert.setTab(OBDal.getInstance().get(org.openbravo.model.ad.ui.Tab.class,
          "180"));
      StringBuffer sql = new StringBuffer();
      sql.append("select t.m_product_id as referencekey_id, '0' as ad_role_id, null as ad_user_id,");
      sql.append("\n    'Product ' || p.name || ' has transactions on dates without available");
      sql.append(" costs. Min date ' || min(t.movementdate) || '. Max date ' || max(t.movementdate)");
      sql.append(" as description,");
      sql.append("\n    'Y' as isactive, p.ad_org_id, p.ad_client_id,");
      sql.append("\n    now() as created, '0' as createdby, now() as updated, '0' as updatedby,");
      sql.append("\n    p.name as record_id");
      sql.append("\nfrom m_transaction t join m_product p on t.m_product_id = p.m_product_id");
      sql.append("\nwhere not exists (select 1 from m_costing c ");
      sql.append("\n                  where t.isactive = 'Y'");
      sql.append("\n                    and t.m_product_id = c.m_product_id");
      sql.append("\n                    and t.movementdate >= c.datefrom");
      sql.append("\n                    and t.movementdate < c.dateto)");
      sql.append("\ngroup by t.m_product_id, p.ad_org_id, p.ad_client_id, p.name");
      legacyCostAvailableAlert.setSql(sql.toString());

      OBDal.getInstance().save(legacyCostAvailableAlert);
      OBDal.getInstance().flush();

      insertAlertRecipients(legacyCostAvailableAlert);
    }

    // Delete previous alerts
    StringBuffer delete = new StringBuffer();
    delete.append("delete from " + Alert.ENTITY_NAME);
    delete.append(" where " + Alert.PROPERTY_ALERTRULE + " = :alertRule ");
    Query queryDelete = OBDal.getInstance().getSession().createQuery(delete.toString());
    queryDelete.setEntity("alertRule", legacyCostAvailableAlert);
    queryDelete.executeUpdate();

    if (legacyCostAvailableAlert.isActive()) {

      SQLQuery alertQry = OBDal.getInstance().getSession()
          .createSQLQuery(legacyCostAvailableAlert.getSql());
      alertQry.addScalar("REFERENCEKEY_ID", StringType.INSTANCE);
      alertQry.addScalar("AD_ROLE_ID", StringType.INSTANCE);
      alertQry.addScalar("AD_USER_ID", StringType.INSTANCE);
      alertQry.addScalar("DESCRIPTION", StringType.INSTANCE);
      alertQry.addScalar("ISACTIVE", StringType.INSTANCE);
      alertQry.addScalar("AD_ORG_ID", StringType.INSTANCE);
      alertQry.addScalar("AD_CLIENT_ID", StringType.INSTANCE);
      alertQry.addScalar("CREATED", DateType.INSTANCE);
      alertQry.addScalar("CREATEDBY", StringType.INSTANCE);
      alertQry.addScalar("UPDATED", DateType.INSTANCE);
      alertQry.addScalar("UPDATEDBY", StringType.INSTANCE);
      alertQry.addScalar("RECORD_ID", StringType.INSTANCE);
      List<?> rows = alertQry.list();
      for (final Object row : rows) {
        final Object[] values = (Object[]) row;
        Alert alert = OBProvider.getInstance().get(Alert.class);
        alert.setCreatedBy(OBDal.getInstance().get(User.class, "0"));
        alert.setUpdatedBy(OBDal.getInstance().get(User.class, "0"));
        alert.setClient(OBDal.getInstance().get(Client.class, values[6]));
        alert.setOrganization(OBDal.getInstance().get(Organization.class, values[5]));
        alert.setAlertRule(legacyCostAvailableAlert);
        alert.setRecordID((String) values[11]);
        alert.setReferenceSearchKey((String) values[0]);
        alert.setDescription((String) values[3]);
        alert.setUserContact(null);
        alert.setRole(OBDal.getInstance().get(org.openbravo.model.ad.access.Role.class, "0"));
        OBDal.getInstance().save(alert);
      }
      if (SessionHandler.isSessionHandlerPresent()) {
        SessionHandler.getInstance().commitAndStart();
      }
      if (rows.size() > 0) {
        throw new OBException("@TrxWithNoCost@");
      }
    }
  }

  private void updateLegacyCosts() {
    log4j.debug("UpdateLegacyCosts");
    // Reset costs in m_transaction and m_transaction_cost.
    Query queryDelete = OBDal.getInstance().getSession()
        .createQuery("delete from " + TransactionCost.ENTITY_NAME);
    queryDelete.executeUpdate();

    // FIXME: Update should be done with a loop based on scroll.
    StringBuffer update = new StringBuffer();
    update.append("update " + MaterialTransaction.ENTITY_NAME);
    update.append(" set " + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false,");
    update.append("     " + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " = null");
    update.append(" where " + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " <> 0");
    update.append("   or " + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true");
    Query updateQry = OBDal.getInstance().getSession().createQuery(update.toString());
    updateQry.executeUpdate();
    OBDal.getInstance().flush();

    for (Client client : getClients()) {
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client.getId());
      Currency clientCur = client.getCurrency();
      log4j.debug("** Processing client: " + client.getIdentifier() + " with currency: "
          + clientCur.getIdentifier());
      for (Organization legalEntity : getLegalEntitiesOfClient(client)) {
        Currency orgCur = legalEntity.getCurrency() != null ? legalEntity.getCurrency() : clientCur;
        log4j.debug("** Processing organization: " + legalEntity.getIdentifier()
            + " with currency: " + orgCur.getIdentifier());
        boolean conversionNeeded = !clientCur.getId().equals(orgCur.getId());
        Date minDate = null;
        Date maxDate = null;
        Set<String> naturalTree = osp.getNaturalTree(legalEntity.getId());
        List<Costing> legacyCosts = getLegacyCostBatch(legalEntity, naturalTree);
        while (!legacyCosts.isEmpty()) {
          log4j.debug("**** Processing legacy costs batch: " + legacyCosts.size());
          for (Costing cost : legacyCosts) {
            if (minDate == null || minDate.after(cost.getStartingDate())) {
              minDate = cost.getStartingDate();
            }
            if (maxDate == null || maxDate.before(cost.getEndingDate())) {
              maxDate = cost.getEndingDate();
            }
            updateTrxLegacyCosts(cost, !conversionNeeded, clientCur.getStandardPrecision(),
                naturalTree);
          }
          if (SessionHandler.isSessionHandlerPresent()) {
            SessionHandler.getInstance().commitAndStart();
          }
          legacyCosts = getLegacyCostBatch(legalEntity, naturalTree);
        }

        // If minDate is null there isn't any cost to update.
        if (!orgCur.getId().equals(clientCur.getId()) && minDate != null) {
          Organization convOrg = legalEntity;
          boolean hasTrxToConvert = hasTrxToConvert(naturalTree, minDate, maxDate);
          while (hasTrxToConvert) {
            log4j.debug("**** HasTrxToConvert ");
            for (ConversionRate convRate : getConversionRates(convOrg, clientCur, orgCur, minDate,
                maxDate)) {
              convertTrxLegacyCosts(convRate, orgCur.getStandardPrecision(), naturalTree);
            }

            if (SessionHandler.isSessionHandlerPresent()) {
              SessionHandler.getInstance().commitAndStart();
            }
            hasTrxToConvert = hasTrxToConvert(naturalTree, minDate, maxDate);
            if (hasTrxToConvert) {
              if (convOrg.getId().equals("0")) {
                throw new OBException("@NoCurrencyConversion@");
              }
              convOrg = osp.getParentOrg(convOrg);
            }

          }
        }
      }
    }

    updateWithCeroCostRemainingTrx();

    insertTrxCosts();
    insertStandardCosts();
  }

  private void createRules() throws Exception {
    // Delete manually created rules.
    Query queryDelete = OBDal.getInstance().getSession()
        .createQuery("delete from " + CostingRule.ENTITY_NAME);
    queryDelete.executeUpdate();

    List<Client> clients = getClients();
    for (Client client : clients) {
      List<Organization> legalEntities = getLegalEntitiesOfClient(client);
      Currency clientCurrency = client.getCurrency();
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client.getId());
      for (Organization org : legalEntities) {
        CostingRule rule = createCostingRule(org);
        processRule(rule, osp, clientCurrency);
      }
    }
  }

  private void processRule(CostingRule rule, OrganizationStructureProvider osp,
      Currency clientCurrency) {
    final Set<String> childOrgs = osp.getChildTree(rule.getOrganization().getId(), true);
    Currency orgCurrency = rule.getOrganization().getCurrency() != null ? rule.getOrganization()
        .getCurrency() : clientCurrency;
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
        BigDecimal cost = getLegacyProductCost(trx.getProduct());
        trx.setCostCalculated(true);
        OBDal.getInstance().save(trx);
        InventoryCountLine initICL = crp.getInitIcl(cri.getInitInventory(), icl);
        if (!clientCurrency.getId().equals(orgCurrency.getId())) {
          cost = FinancialUtils.getConvertedAmount(cost, clientCurrency, orgCurrency, startingDate,
              rule.getOrganization(), FinancialUtils.PRECISION_COSTING);
        }
        initICL.setCost(cost);
        OBDal.getInstance().save(initICL);
      }
    }
  }

  private boolean isCostingMigrationNotNeeded() {
    OBQuery<Costing> costingQry = OBDal.getInstance().createQuery(Costing.class, "");
    costingQry.setFilterOnReadableClients(false);
    costingQry.setFilterOnReadableOrganization(false);

    return costingQry.count() == 0;
  }

  private boolean isMigrationFirstPhaseCompleted() {
    OBQuery<Preference> prefQry = OBDal.getInstance().createQuery(Preference.class,
        Preference.PROPERTY_ATTRIBUTE + " = 'CostingMigrationFirstPhaseCompleted'");
    prefQry.setFilterOnReadableClients(false);
    prefQry.setFilterOnReadableOrganization(false);

    return prefQry.count() > 0;
  }

  private AlertRule getLegacyCostAvailableAlert() {
    String where = AlertRule.PROPERTY_NAME + " = '" + alertRuleName + "'";
    OBQuery<AlertRule> alertQry = OBDal.getInstance().createQuery(AlertRule.class, where);
    alertQry.setFilterOnActive(false);

    return alertQry.uniqueResult();
  }

  private List<Costing> getLegacyCostBatch(Organization legalEntity, Set<String> naturalTree) {
    StringBuffer where = new StringBuffer();
    where.append(" as c");
    where.append(" where c." + Costing.PROPERTY_CLIENT + " = :client");
    where.append("   and exists (select 1 from " + MaterialTransaction.ENTITY_NAME + " as trx");
    where.append("     where trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    where.append("       and trx." + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " is null");
    where.append("       and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= c."
        + Costing.PROPERTY_STARTINGDATE);
    where.append("       and trx." + MaterialTransaction.PROPERTY_PRODUCT + " = c."
        + Costing.PROPERTY_PRODUCT);
    where.append("       and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < c."
        + Costing.PROPERTY_ENDINGDATE);
    where.append("     )");
    where.append(" order by " + Costing.PROPERTY_PRODUCT + ", " + Costing.PROPERTY_STARTINGDATE
        + ", " + Costing.PROPERTY_ENDINGDATE + " desc");

    OBQuery<Costing> costingQry = OBDal.getInstance().createQuery(Costing.class, where.toString());
    costingQry.setFilterOnReadableClients(false);
    costingQry.setFilterOnReadableOrganization(false);
    costingQry.setNamedParameter("client", legalEntity.getClient());
    costingQry.setNamedParameter("orgs", naturalTree);
    costingQry.setMaxResult(1000);
    return costingQry.list();
  }

  private void updateTrxLegacyCosts(Costing cost, boolean calculated, Long standardPrecision,
      Set<String> naturalTree) {
    log4j.debug("****** UpdateTrxLegacyCosts");

    // FIXME: Update should be done with a loop based on scroll.
    StringBuffer update = new StringBuffer();
    update.append("update " + MaterialTransaction.ENTITY_NAME);
    update.append(" set " + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = :isCalculated,");
    update.append("     " + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " = round(:cost * abs("
        + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + "), :precision)");
    update.append(" where " + MaterialTransaction.PROPERTY_PRODUCT + " = :product");
    update.append("   and " + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    update.append("   and " + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= :dateFrom");
    update.append("   and " + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :dateTo");

    Query trxUpdate = OBDal.getInstance().getSession().createQuery(update.toString());
    trxUpdate.setBoolean("isCalculated", calculated);
    trxUpdate.setBigDecimal("cost", cost.getCost());
    trxUpdate.setLong("precision", standardPrecision);
    trxUpdate.setEntity("product", cost.getProduct());
    trxUpdate.setParameterList("orgs", naturalTree);
    trxUpdate.setDate("dateFrom", cost.getStartingDate());
    trxUpdate.setDate("dateTo", cost.getEndingDate());
    int updated = trxUpdate.executeUpdate();
    log4j.debug("****** UpdateTrxLegacyCosts updated:" + updated);
  }

  private boolean hasTrxToConvert(Set<String> naturalTree, Date minDate, Date maxDate) {
    StringBuffer where = new StringBuffer();
    where.append(" as trx");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    where.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= :minDate");
    where.append("   and trx." + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :maxDate");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    where.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " is not null");
    OBQuery<MaterialTransaction> whereQry = OBDal.getInstance().createQuery(
        MaterialTransaction.class, where.toString());
    whereQry.setFilterOnReadableClients(false);
    whereQry.setFilterOnReadableOrganization(false);
    whereQry.setNamedParameter("orgs", naturalTree);
    whereQry.setNamedParameter("minDate", minDate);
    whereQry.setNamedParameter("maxDate", maxDate);

    return whereQry.count() > 0;
  }

  private List<ConversionRate> getConversionRates(Organization organization, Currency fromCur,
      Currency toCur, Date minDate, Date maxDate) {
    StringBuffer where = new StringBuffer();
    where.append(" as cr");
    where.append(" where cr." + ConversionRate.PROPERTY_ORGANIZATION + ".id = :organizationId");
    where.append("   and cr." + ConversionRate.PROPERTY_CURRENCY + ".id = :fromCur");
    where.append("   and cr." + ConversionRate.PROPERTY_TOCURRENCY + ".id = :toCur");
    where.append("   and cr." + ConversionRate.PROPERTY_VALIDFROMDATE + " < :maxDate");
    where.append("   and cr." + ConversionRate.PROPERTY_VALIDTODATE + " > :minDate");
    where.append("   and cr." + ConversionRate.PROPERTY_ACTIVE + " = true");
    where.append(" order by cr." + ConversionRate.PROPERTY_VALIDFROMDATE);

    OBQuery<ConversionRate> convRateQry = OBDal.getInstance().createQuery(ConversionRate.class,
        where.toString());
    convRateQry.setFilterOnReadableClients(false);
    convRateQry.setFilterOnReadableOrganization(false);
    convRateQry.setNamedParameter("organizationId", organization.getId());
    convRateQry.setNamedParameter("fromCur", fromCur.getId());
    convRateQry.setNamedParameter("toCur", toCur.getId());
    convRateQry.setNamedParameter("maxDate", maxDate);
    convRateQry.setNamedParameter("minDate", minDate);
    return convRateQry.list();
  }

  private void convertTrxLegacyCosts(ConversionRate convRate, Long standardPrecision,
      Set<String> naturalTree) {
    log4j.debug("****** ConvertTrxLegacyCosts");
    // FIXME: Update should be done with a loop based on scroll.
    StringBuffer convUpd = new StringBuffer();
    convUpd.append(" update " + MaterialTransaction.ENTITY_NAME);
    convUpd.append(" set " + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true,");
    convUpd.append("     " + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " = round("
        + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " * :convRate, :precision)");
    convUpd.append(" where " + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    convUpd.append("   and " + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    convUpd.append("   and " + MaterialTransaction.PROPERTY_MOVEMENTDATE + " >= :dateFrom");
    convUpd.append("   and " + MaterialTransaction.PROPERTY_MOVEMENTDATE + " < :dateTo");

    Query trxUpdate = OBDal.getInstance().getSession().createQuery(convUpd.toString());
    trxUpdate.setBigDecimal("convRate", convRate.getMultipleRateBy());
    trxUpdate.setLong("precision", standardPrecision);
    trxUpdate.setParameterList("orgs", naturalTree);
    trxUpdate.setDate("dateFrom", convRate.getValidFromDate());
    trxUpdate.setDate("dateTo", convRate.getValidToDate());
    int updated = trxUpdate.executeUpdate();
    log4j.debug("****** ConvertTrxLegacyCosts updated: " + updated);
  }

  private void updateWithCeroCostRemainingTrx() {
    log4j.debug("****** updateWithCeroRemainingTrx");

    // FIXME: Update should be done with a loop based on scroll.
    StringBuffer update = new StringBuffer();
    update.append("update " + MaterialTransaction.ENTITY_NAME);
    update.append(" set " + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = true,");
    update.append("     " + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " = 0");
    update.append(" where " + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " is null");

    Query trxUpdate = OBDal.getInstance().getSession().createQuery(update.toString());
    // trxUpdate.setBigDecimal("cost", BigDecimal.ZERO);
    int updated = trxUpdate.executeUpdate();
    log4j.debug("****** updateWithCeroRemainingTrx updated:" + updated);
  }

  private void insertAlertRecipients(AlertRule alertRule) {
    // FIXME: Insert should be done with a loop based on scroll.
    StringBuffer insert = new StringBuffer();
    insert.append("insert into " + AlertRecipient.ENTITY_NAME);
    insert.append(" (id ");
    insert.append(", " + AlertRecipient.PROPERTY_ACTIVE);
    insert.append(", " + AlertRecipient.PROPERTY_CLIENT);
    insert.append(", " + AlertRecipient.PROPERTY_ORGANIZATION);
    insert.append(", " + AlertRecipient.PROPERTY_CREATIONDATE);
    insert.append(", " + AlertRecipient.PROPERTY_CREATEDBY);
    insert.append(", " + AlertRecipient.PROPERTY_UPDATED);
    insert.append(", " + AlertRecipient.PROPERTY_UPDATEDBY);
    insert.append(", " + AlertRecipient.PROPERTY_ROLE);
    insert.append(", " + AlertRecipient.PROPERTY_ALERTRULE);
    insert.append(" )\n select get_uuid()");
    insert.append(", r." + Role.PROPERTY_ACTIVE);
    insert.append(", r." + Role.PROPERTY_CLIENT);
    insert.append(", r." + Role.PROPERTY_ORGANIZATION);
    insert.append(", now()");
    insert.append(", u");
    insert.append(", now()");
    insert.append(", u");
    insert.append(", r");
    insert.append(", ar");
    insert.append(" from " + Role.ENTITY_NAME + " as r");
    insert.append(", " + User.ENTITY_NAME + " as u");
    insert.append(", " + AlertRule.ENTITY_NAME + " as ar");
    insert.append("  where r." + Role.PROPERTY_MANUAL + " = false");
    insert.append("    and r." + Role.PROPERTY_CLIENT + ".id <> '0'");
    insert.append("    and u.id = '0'");
    insert.append("    and ar.id = :ar");

    Query queryInsert = OBDal.getInstance().getSession().createQuery(insert.toString());
    queryInsert.setString("ar", alertRule.getId());
    int inserted = queryInsert.executeUpdate();
    log4j.debug("** inserted alert recipients: " + inserted);
  }

  private void insertTrxCosts() {
    // FIXME: Insert should be done with a loop based on scroll.
    StringBuffer insert = new StringBuffer();
    insert.append("insert into " + TransactionCost.ENTITY_NAME);
    insert.append(" (id ");
    insert.append(", " + TransactionCost.PROPERTY_ACTIVE);
    insert.append(", " + TransactionCost.PROPERTY_CLIENT);
    insert.append(", " + TransactionCost.PROPERTY_ORGANIZATION);
    insert.append(", " + TransactionCost.PROPERTY_CREATIONDATE);
    insert.append(", " + TransactionCost.PROPERTY_CREATEDBY);
    insert.append(", " + TransactionCost.PROPERTY_UPDATED);
    insert.append(", " + TransactionCost.PROPERTY_UPDATEDBY);
    insert.append(", " + TransactionCost.PROPERTY_INVENTORYTRANSACTION);
    insert.append(", " + TransactionCost.PROPERTY_COST);
    insert.append(", " + TransactionCost.PROPERTY_COSTDATE);
    insert.append(" )\n select get_uuid()");
    insert.append(", t." + MaterialTransaction.PROPERTY_ACTIVE);
    insert.append(", t." + MaterialTransaction.PROPERTY_CLIENT);
    insert.append(", t." + MaterialTransaction.PROPERTY_ORGANIZATION);
    insert.append(", now()");
    insert.append(", u");
    insert.append(", now()");
    insert.append(", u");
    insert.append(", t");
    insert.append(", t." + MaterialTransaction.PROPERTY_TRANSACTIONCOST);
    insert.append(", t." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);
    insert.append(" from " + MaterialTransaction.ENTITY_NAME + " as t");
    insert.append(", " + User.ENTITY_NAME + " as u");
    insert.append("  where t." + MaterialTransaction.PROPERTY_TRANSACTIONCOST + " is not null");
    insert.append("    and not exists (select 1 from " + TransactionCost.ENTITY_NAME + " as tc");
    insert.append("        where t = tc)");
    insert.append("    and u.id = :user");

    Query queryInsert = OBDal.getInstance().getSession().createQuery(insert.toString());
    queryInsert.setString("user", (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    queryInsert.executeUpdate();
  }

  private void insertStandardCosts() {
    // Insert STANDARD cost for products with costtype = 'ST'.
    // FIXME: Insert should be done with a loop based on scroll.
    StringBuffer insert = new StringBuffer();
    insert.append("insert into " + Costing.ENTITY_NAME);
    insert.append(" (id ");
    insert.append(", " + Costing.PROPERTY_ACTIVE);
    insert.append(", " + Costing.PROPERTY_CLIENT);
    insert.append(", " + Costing.PROPERTY_ORGANIZATION);
    insert.append(", " + Costing.PROPERTY_CREATIONDATE);
    insert.append(", " + Costing.PROPERTY_CREATEDBY);
    insert.append(", " + Costing.PROPERTY_UPDATED);
    insert.append(", " + Costing.PROPERTY_UPDATEDBY);
    insert.append(", " + Costing.PROPERTY_PRODUCT);
    insert.append(", " + Costing.PROPERTY_COSTTYPE);
    insert.append(", " + Costing.PROPERTY_COST);
    insert.append(", " + Costing.PROPERTY_STARTINGDATE);
    insert.append(", " + Costing.PROPERTY_ENDINGDATE);
    insert.append(", " + Costing.PROPERTY_MANUAL);
    insert.append(", " + Costing.PROPERTY_PERMANENT);
    insert.append(" )\n select get_uuid()");
    insert.append(", c." + Costing.PROPERTY_ACTIVE);
    insert.append(", c." + Costing.PROPERTY_CLIENT);
    insert.append(", org");
    insert.append(", now()");
    insert.append(", u");
    insert.append(", now()");
    insert.append(", u");
    insert.append(", c." + Costing.PROPERTY_PRODUCT);
    insert.append(", 'STA'");
    insert.append(", c." + Costing.PROPERTY_COST);
    insert.append(", to_date(to_char(:startingDate), to_char('DD-MM-YYYY HH24:MI:SS'))");

    insert.append(", c." + Costing.PROPERTY_ENDINGDATE);
    insert.append(", c." + Costing.PROPERTY_MANUAL);
    insert.append(", c." + Costing.PROPERTY_PERMANENT);
    insert.append("\n from " + Costing.ENTITY_NAME + " as c");
    insert.append("   join c." + Costing.PROPERTY_PRODUCT + " as p");
    insert.append(", " + User.ENTITY_NAME + " as u");
    insert.append(", " + Organization.ENTITY_NAME + " as org");
    insert.append("   join org." + Organization.PROPERTY_ORGANIZATIONTYPE + " as ot");
    insert.append("\n where c." + Costing.PROPERTY_COSTTYPE + " = 'ST'");
    insert.append("   and c." + Costing.PROPERTY_STARTINGDATE
        + " <= to_date(to_char(:limitDate), to_char('DD-MM-YYYY HH24:MI:SS'))");
    insert.append("   and c." + Costing.PROPERTY_ENDINGDATE
        + " > to_date(to_char(:limitDate2), to_char('DD-MM-YYYY HH24:MI:SS'))");
    insert.append("   and u.id = :user");
    insert.append("   and ot." + OrganizationType.PROPERTY_LEGALENTITY + " = true");
    insert.append("   and org." + Organization.PROPERTY_CLIENT + " = c." + Costing.PROPERTY_CLIENT);
    insert.append("   and (ad_isorgincluded(c." + Costing.PROPERTY_ORGANIZATION + ".id, org."
        + Organization.PROPERTY_ID + ", c." + Costing.PROPERTY_CLIENT + ".id) <> -1");
    insert.append("   or ad_isorgincluded(org." + Organization.PROPERTY_ID + ".id, c."
        + Costing.PROPERTY_ORGANIZATION + ", c." + Costing.PROPERTY_CLIENT + ".id) <> -1)");
    insert.append("   and (ad_isorgincluded(p." + Product.PROPERTY_ORGANIZATION + ".id, org."
        + Organization.PROPERTY_ID + ", p." + Product.PROPERTY_CLIENT + ".id) <> -1");
    insert.append("   or ad_isorgincluded(org." + Organization.PROPERTY_ID + ".id, p."
        + Product.PROPERTY_ORGANIZATION + ", p." + Product.PROPERTY_CLIENT + ".id) <> -1)");

    Query queryInsert = OBDal.getInstance().getSession().createQuery(insert.toString());
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    String startingDate = dateFormatter.format(new Date());
    queryInsert.setString("startingDate", startingDate);
    queryInsert.setString("limitDate", startingDate);
    queryInsert.setString("limitDate2", startingDate);
    queryInsert.setString("user", (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    queryInsert.executeUpdate();

  }

  private CostingRule createCostingRule(Organization org) {
    CostingRule rule = OBProvider.getInstance().get(CostingRule.class);
    rule.setClient(org.getClient());
    rule.setOrganization(org);
    rule.setCostingAlgorithm(getAverageAlgorithm());
    rule.setValidated(false);
    rule.setStartingDate(null);
    OBDal.getInstance().save(rule);
    return rule;
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
    throw new OBException("@unprocessedInventories@: " + inventoryList.toString());
  }

  private List<CostingRule> getRules() {
    OBCriteria<CostingRule> crCrit = OBDal.getInstance().createCriteria(CostingRule.class);
    crCrit.setFilterOnReadableClients(false);
    crCrit.setFilterOnReadableOrganization(false);

    return crCrit.list();
  }

  /**
   * Create a preference to be able to determine that the migration first phase is completed.
   */
  private void createMigrationFirstPhaseCompletedPreference() {
    createPreference("CostingMigrationFirstPhaseCompleted", null);
  }

  private void createPreference(String attribute, String value) {
    Organization org0 = OBDal.getInstance().get(Organization.class, "0");
    Client client0 = OBDal.getInstance().get(Client.class, "0");

    Preference newPref = OBProvider.getInstance().get(Preference.class);
    newPref.setClient(client0);
    newPref.setOrganization(org0);
    newPref.setPropertyList(false);
    newPref.setAttribute(attribute);
    newPref.setSearchKey(value);

    OBDal.getInstance().save(newPref);
  }

  private void deleteMigrationFirstPhaseCompletedPreference() {
    OBQuery<Preference> prefQry = OBDal.getInstance().createQuery(Preference.class,
        Preference.PROPERTY_ATTRIBUTE + " = 'CostingMigrationFirstPhaseCompleted'");
    prefQry.setFilterOnReadableClients(false);
    prefQry.setFilterOnReadableOrganization(false);

    if (!prefQry.list().isEmpty()) {
      OBDal.getInstance().remove(prefQry.list().get(0));
    }
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

  private static List<Client> getClients() {
    OBCriteria<Client> obcClient = OBDal.getInstance().createCriteria(Client.class);
    obcClient.setFilterOnReadableClients(false);
    obcClient.add(Restrictions.ne(Client.PROPERTY_ID, "0"));
    return obcClient.list();
  }
}
