/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.mobile.core.process.PropertyByType;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonToDataConverter;

@DataSynchronization(entity = "OBPOS_App_Cashup")
public class ProcessCashClose extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  private static final Logger log = Logger.getLogger(ProcessCashClose.class);
  JSONObject jsonResponse = new JSONObject();

  protected String getImportQualifier() {
    return "OBPOS_App_Cashup";
  }

  public JSONObject saveRecord(JSONObject jsonCashup) throws Exception {
    String cashUpId = jsonCashup.getString("id");
    JSONObject jsonData = new JSONObject();
    Date cashUpDate = new Date();
    Date currentDate = new Date();
    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
        jsonCashup.getString("posterminal"));

    try {
      if (jsonCashup.has("cashUpDate") && jsonCashup.get("cashUpDate") != null
          && StringUtils.isNotEmpty(jsonCashup.getString("cashUpDate"))) {
        String strCashUpDate = (String) jsonCashup.getString("cashUpDate");
        cashUpDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(PropertyByType.DATETIME,
            ((String) strCashUpDate).subSequence(0, ((String) strCashUpDate).lastIndexOf(".")));
      } else {
        log.debug("Error processing cash close: error retrieving cashUp date. Using current date");
      }
      if (jsonCashup.has("currentDate") && jsonCashup.get("currentDate") != null
          && StringUtils.isNotEmpty(jsonCashup.getString("currentDate"))) {
        String strCurrentDate = (String) jsonCashup.getString("currentDate");
        String dateFormatStr = posTerminal.getOrganization().getObposDateFormat();
        if (dateFormatStr == null) {
          dateFormatStr = OBPropertiesProvider.getInstance().getOpenbravoProperties()
              .getProperty("dateFormat.java");
        }

        DateFormat isodatefmt = new SimpleDateFormat(dateFormatStr);
        currentDate = isodatefmt.parse(strCurrentDate);
      } else {
        log.debug("Error processing cash close: error retrieving current date. Using server current date");
      }
    } catch (Exception e) {
      log.debug("Error processing cash close: error retrieving cashUp date. Using current date");
    }

    OBContext.setOBContext(jsonCashup.getString("userId"), OBContext.getOBContext().getRole()
        .getId(), OBContext.getOBContext().getCurrentClient().getId(), posTerminal
        .getOrganization().getId());
    OBPOSAppCashup cashUp = getCashUp(cashUpId, jsonCashup, cashUpDate);

    if (cashUp.isProcessed() && !cashUp.isProcessedbo()) {
      cashUp.setJsoncashup(jsonCashup.toString());
      if (posTerminal.getMasterterminal() != null) {
        // On slave only mark as processed BO
        cashUp.setProcessedbo(Boolean.TRUE);
        jsonData.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      } else if (posTerminal.isMaster()) {
        // Reconciliation and invoices of slaves
        String query = OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + ".id = :parantCashupId";
        OBQuery<OBPOSAppCashup> appCashupQuery = OBDal.getInstance().createQuery(
            OBPOSAppCashup.class, query);
        appCashupQuery.setNamedParameter("parantCashupId", cashUpId);
        List<OBPOSAppCashup> slaveCashupList = appCashupQuery.list();
        List<String> slaveCashupIds = new ArrayList<String>();
        List<OBPOSApplications> posTerminalList = new ArrayList<OBPOSApplications>();
        for (int i = 0; i < slaveCashupList.size(); i++) {
          OBPOSApplications posTerm = slaveCashupList.get(i).getPOSTerminal();
          posTerm.getOBPOSAppPaymentList();
          posTerminalList.add(posTerm);
        }
        for (int i = 0; i < slaveCashupList.size(); i++) {
          OBPOSAppCashup slaveCashup = slaveCashupList.get(i);
          String dbJsoncashup = slaveCashup.getJsoncashup();
          if (StringUtils.isEmpty(dbJsoncashup)) {
            throw new OBException(
                "Cash up can not proceed, JSON data for shared Cash up not found: "
                    + slaveCashup.getIdentifier());
          }
          JSONObject slaveJsonCashup = new JSONObject(dbJsoncashup);
          doReconciliationAndInvoices(posTerminalList.get(i), slaveCashup.getId(),
              slaveCashup.getCashUpDate(), slaveJsonCashup, jsonData, false, null);
          slaveCashupIds.add(slaveCashup.getId());
        }
        // Reconciliation and invoices of master
        doReconciliationAndInvoices(posTerminal, cashUpId, cashUpDate, jsonCashup, jsonData, true,
            slaveCashupIds);
        // Accumulate slave Payment Method Cashup on Master
        doAccumulatePaymentMethodCashup(cashUpId);
      } else {
        doReconciliationAndInvoices(posTerminal, cashUpId, cashUpDate, jsonCashup, jsonData, true,
            null);
      }
    } else {
      // This cashup is a cash order. Nothing needs to be done
      jsonData.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      // Associate master/slave cashup
      if ((posTerminal.isMaster() && posTerminal.getOBPOSApplicationsMasterterminalIDList().size() != cashUp
          .getOBPOSAppCashupObposParentCashupIDList().size())
          || (posTerminal.getMasterterminal() != null && cashUp.getObposParentCashup() == null)) {
        associateMasterSlave(cashUp, posTerminal);
      }
    }
    return jsonData;
  }

  private void doAccumulatePaymentMethodCashup(String cashUpId) {

    // Get slave OBPOSAppCashup
    OBCriteria<OBPOSAppCashup> obCriteria = OBDal.getInstance()
        .createCriteria(OBPOSAppCashup.class);
    obCriteria.add(Restrictions.eq(OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + ".id", cashUpId));
    List<OBPOSAppCashup> cashupList = obCriteria.list();
    List<String> cashUpIds = new ArrayList<String>();
    for (OBPOSAppCashup appCashup : cashupList) {
      cashUpIds.add(appCashup.getId());
    }

    // Sum shared PaymentMethodCashup
    String query = "select searchkey, sum(startingcash), sum(totalDeposits), sum(totalDrops), sum(totalreturns), sum(totalsales) "
        + "from OBPOS_Paymentmethodcashup "
        + "where cashUp.id in :cashUpIds and paymentType.paymentMethod.isshared = 'Y'"
        + "group by 1";
    final Session session = OBDal.getInstance().getSession();
    final Query paymentQuery = session.createQuery(query);
    paymentQuery.setParameterList("cashUpIds", cashUpIds);
    List<?> paymentList = paymentQuery.list();
    for (int i = 0; i < paymentList.size(); i++) {
      Object[] item = (Object[]) paymentList.get(i);
      // Get master payment method cashup
      OBPOSPaymentMethodCashup masterCashup = getPaymentMethodCashup(cashUpId, (String) item[0]);
      if (masterCashup != null) {
        masterCashup.setStartingcash(masterCashup.getStartingcash().add((BigDecimal) item[1]));
        masterCashup.setTotalDeposits(masterCashup.getTotalDeposits().add((BigDecimal) item[2]));
        masterCashup.setTotalDrops(masterCashup.getTotalDrops().add((BigDecimal) item[3]));
        masterCashup.setTotalreturns(masterCashup.getTotalreturns().add((BigDecimal) item[4]));
        masterCashup.setTotalsales(masterCashup.getTotalsales().add((BigDecimal) item[5]));
        OBDal.getInstance().save(masterCashup);
      } else {
        throw new OBException(
            "Cash up can not proceed, not found all slave terminal shared payment methods on master terminal: "
                + (String) item[0]);
      }
    }

    // Remove shared PaymentMethodCashup
    List<String> paymentMethodCashupIds = new ArrayList<String>();
    for (OBPOSAppCashup appCashup : cashupList) {
      List<OBPOSPaymentMethodCashup> paymentMethodCashupList = appCashup
          .getOBPOSPaymentmethodcashupList();
      for (OBPOSPaymentMethodCashup paymentMethodCashup : paymentMethodCashupList) {
        if (paymentMethodCashup.getPaymentType().getPaymentMethod().isShared()) {
          paymentMethodCashupIds.add(paymentMethodCashup.getId());
        }
      }
    }
    if (!paymentMethodCashupIds.isEmpty()) {
      String delete = "delete from OBPOS_Paymentmethodcashup where id in :paymentMethodCashupIds";
      final Query paymentDelete = session.createQuery(delete);
      paymentDelete.setParameterList("paymentMethodCashupIds", paymentMethodCashupIds);
      paymentDelete.executeUpdate();
    }
  }

  private OBPOSPaymentMethodCashup getPaymentMethodCashup(String cashUpId, String searchKey) {
    final OBQuery<OBPOSPaymentMethodCashup> obQuery = OBDal.getInstance().createQuery(
        OBPOSPaymentMethodCashup.class, "cashUp.id = :cashUpId and searchKey = :searchKey");
    obQuery.setNamedParameter("cashUpId", cashUpId);
    obQuery.setNamedParameter("searchKey", searchKey);
    final List<OBPOSPaymentMethodCashup> paymentMethodCashupList = obQuery.list();
    return paymentMethodCashupList.size() > 0 ? paymentMethodCashupList.get(0) : null;
  }

  private void doReconciliationAndInvoices(OBPOSApplications posTerminal, String cashUpId,
      Date currentDate, JSONObject jsonCashup, JSONObject jsonData, boolean skipSlave,
      List<String> slaveCashupIds) throws Exception {
    // check if there is a reconciliation in draft status
    for (OBPOSAppPayment payment : posTerminal.getOBPOSAppPaymentList()) {
      if (payment.getFinancialAccount() == null) {
        continue;
      }
      if (skipSlave && posTerminal.getMasterterminal() != null
          && payment.getPaymentMethod().isShared()) {
        // Skip share payment method on slave terminals
        continue;
      }
      final OBCriteria<FIN_Reconciliation> recconciliations = OBDal.getInstance().createCriteria(
          FIN_Reconciliation.class);
      recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_DOCUMENTSTATUS, "DR"));
      recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT,
          payment.getFinancialAccount()));
      for (final FIN_Reconciliation r : recconciliations.list()) {
        // will end up in the pos error window
        throw new OBException(
            "Cash up can not proceed, there is a reconcilliation in draft status, payment method: "
                + payment.getIdentifier() + ", reconcilliation: " + r.getDocumentNo() + " ("
                + r.getAccount().getName() + ")");
      }
    }

    OBCriteria<OBPOSErrors> errorsQuery = OBDal.getInstance().createCriteria(OBPOSErrors.class);
    errorsQuery.add(Restrictions.ne(OBPOSErrors.PROPERTY_TYPEOFDATA, "OBPOS_App_Cashup"));
    errorsQuery.add(Restrictions.eq(OBPOSErrors.PROPERTY_ORDERSTATUS, "N"));
    errorsQuery.add(Restrictions.eq(OBPOSErrors.PROPERTY_OBPOSAPPLICATIONS, posTerminal));
    if (errorsQuery.count() > 0) {
      throw new OBException(
          "There are errors related to non-created customers, orders, or cash management movements pending to be processed. Process them before processing the cash ups");
    }

    // This cashup is a closed box
    TriggerHandler.getInstance().disable();
    try {
      getOrderGroupingProcessor().groupOrders(posTerminal, cashUpId, currentDate);

      CashCloseProcessor processor = getCashCloseProcessor();
      JSONArray cashMgmtIds = jsonCashup.getJSONArray("cashMgmtIds");
      JSONObject result = processor.processCashClose(
          OBDal.getInstance().get(OBPOSApplications.class, jsonCashup.getString("posterminal")),
          jsonCashup, cashMgmtIds, currentDate, slaveCashupIds);
      // add the messages returned by processCashClose...
      jsonData.put("messages", result.opt("messages"));
      jsonData.put("next", result.opt("next"));
      jsonData.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } finally {
      OBDal.getInstance().flush();
      TriggerHandler.getInstance().enable();
    }
  }

  protected CashCloseProcessor getCashCloseProcessor() {
    return WeldUtils.getInstanceFromStaticBeanManager(CashCloseProcessor.class);
  }

  protected OrderGroupingProcessor getOrderGroupingProcessor() {
    return WeldUtils.getInstanceFromStaticBeanManager(OrderGroupingProcessor.class);
  }

  private synchronized void associateMasterSlave(OBPOSAppCashup cashUp,
      OBPOSApplications posTerminal) {
    if (posTerminal.isMaster()) {
      // Find slaves cashup
      String query = OBPOSAppCashup.PROPERTY_POSTERMINAL + "."
          + OBPOSApplications.PROPERTY_MASTERTERMINAL + ".id = :terminalId and "
          + OBPOSAppCashup.PROPERTY_ISPROCESSEDBO + " = 'N' and "
          + OBPOSAppCashup.PROPERTY_ISPROCESSED + " = 'N' and "
          + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + " is null";
      OBQuery<OBPOSAppCashup> appCashupQuery = OBDal.getInstance().createQuery(
          OBPOSAppCashup.class, query);
      appCashupQuery.setNamedParameter("terminalId", posTerminal.getId());
      List<OBPOSAppCashup> appCashupList = appCashupQuery.list();
      for (OBPOSAppCashup appCashup : appCashupList) {
        // Determine if exist close slave cashup for slave terminal and this master cashup
        query = "select count(*) from " + OBPOSAppCashup.ENTITY_NAME + " where "
            + OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id = ? and "
            + OBPOSAppCashup.PROPERTY_ISPROCESSEDBO + " = 'Y' and "
            + OBPOSAppCashup.PROPERTY_ISPROCESSED + " = 'Y' and "
            + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + " is not null and "
            + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + ".id = ?";
        if (countAppCashup(query, appCashup.getPOSTerminal().getId(), cashUp.getId()) == 0) {
          appCashup.setObposParentCashup(cashUp);
        }
      }
    } else if (posTerminal.getMasterterminal() != null && cashUp.getObposParentCashup() == null) {
      // Determine if exist open master cashup
      String query = "select count(*) from " + OBPOSAppCashup.ENTITY_NAME + " where "
          + OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id = ? and "
          + OBPOSAppCashup.PROPERTY_ISPROCESSED + " = 'Y' and "
          + OBPOSAppCashup.PROPERTY_ISPROCESSEDBO + " = 'Y' and "
          + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + " is not null and "
          + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + "." + OBPOSAppCashup.PROPERTY_ISPROCESSED
          + " = 'N' and " + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + "."
          + OBPOSAppCashup.PROPERTY_ISPROCESSEDBO + " = 'N'";
      if (countAppCashup(query, cashUp.getPOSTerminal().getId(), null) == 0) {
        // Find master cashup
        query = OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id = :terminalId and "
            + OBPOSAppCashup.PROPERTY_ISPROCESSEDBO + " = 'N' and "
            + OBPOSAppCashup.PROPERTY_ISPROCESSED + " = 'N' ";
        OBQuery<OBPOSAppCashup> appCashupQuery = OBDal.getInstance().createQuery(
            OBPOSAppCashup.class, query);
        appCashupQuery.setNamedParameter("terminalId", posTerminal.getMasterterminal().getId());
        List<OBPOSAppCashup> appCashupList = appCashupQuery.list();
        if (appCashupList.size() > 0 && cashUp.getObposParentCashup() == null) {
          cashUp.setObposParentCashup(appCashupList.get(0));
        }
      }
    }
  }

  private Long countAppCashup(String query, String posterminal, String parentCashUp) {
    final Session session = OBDal.getInstance().getSession();
    final Query count = session.createQuery(query);
    count.setParameter(0, posterminal);
    if (parentCashUp != null) {
      count.setParameter(1, parentCashUp);
    }
    Long value = (Long) count.uniqueResult();
    return value != null ? value.longValue() : 0;
  }

  /**
   * Get a cashup. If cashup not exist it's created, otherwise update the cashup data into database.
   * 
   * @param cashUpId
   *          The cashUp identifier
   * @param jsonCashup
   *          The input object with cashup data
   * @param cashUpDate2
   * @return Cashup object
   * @throws JSONException
   */
  private OBPOSAppCashup getCashUp(String cashUpId, JSONObject jsonCashup, Date cashUpDate)
      throws JSONException, SQLException {
    // CashUp record will be read from the database with a "for update" clause to force the process
    // to get the lock on the record. The reason for this is to prevent the same cash up from being
    // processed twice in case of very quick duplicated requests.
    // These shouldn't happen in general but may happen specifically in case of unreliable networks
    OBPOSAppCashup cashUp = null;
    Query cashUpQuery = OBDal.getInstance().getSession()
        .createQuery("from OBPOS_App_Cashup where id=?");
    cashUpQuery.setString(0, cashUpId);
    // The record will be locked to this process until it ends. Other requests to process this cash
    // up will be locked until this one finishes
    cashUpQuery.setLockOptions(LockOptions.UPGRADE);
    cashUp = (OBPOSAppCashup) cashUpQuery.uniqueResult();

    if (cashUp == null) {
      // create the cashup if no exists
      try {
        OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
            jsonCashup.getString("posterminal"));
        cashUp = OBProvider.getInstance().get(OBPOSAppCashup.class);
        cashUp.setId(cashUpId);
        cashUp.setOrganization(posTerminal.getOrganization());
        cashUp.setCashUpDate(cashUpDate);
        cashUp.setPOSTerminal(posTerminal);
        cashUp.setUserContact(OBContext.getOBContext().getUser());
        cashUp.setBeingprocessed(jsonCashup.getString("isbeingprocessed").equalsIgnoreCase("Y"));
        cashUp.setNewOBObject(true);
        OBDal.getInstance().save(cashUp);
        if (jsonCashup.has("creationDate")) {
          String cashUpCreationDate = jsonCashup.getString("creationDate");
          cashUp.set("creationDate", (Date) JsonToDataConverter.convertJsonToPropertyValue(
              PropertyByType.DATETIME,
              (cashUpCreationDate).subSequence(0, (cashUpCreationDate).lastIndexOf("."))));
        }
      } catch (JSONException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    updateOrCreateCashupInfo(cashUpId, jsonCashup, cashUpDate);
    OBDal.getInstance().flush();
    return cashUp;
  }

  private void updateOrCreateCashupInfo(String cashUpId, JSONObject jsonCashup, Date cashUpDate)
      throws JSONException, OBException {
    OBPOSAppCashup cashup = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);
    // Update cashup info
    updateCashUpInfo(cashup, jsonCashup, cashUpDate);

    // Update taxes
    if (jsonCashup.has("cashTaxInfo")) {
      JSONArray taxCashupInfo = jsonCashup.getJSONArray("cashTaxInfo");
      for (int i = 0; i < taxCashupInfo.length(); ++i) {
        JSONObject tax = taxCashupInfo.getJSONObject(i);
        createTaxCashUp(cashup, tax);
      }
    }

    // Update paymentmethodcashup
    if (jsonCashup.has("cashPaymentMethodInfo")) {
      JSONArray paymentCashupInfo = jsonCashup.getJSONArray("cashPaymentMethodInfo");
      for (int i = 0; i < paymentCashupInfo.length(); ++i) {
        JSONObject payment = paymentCashupInfo.getJSONObject(i);
        // Set Amount To Keep
        if (jsonCashup.has("cashCloseInfo")) {
          // Get the paymentMethod id
          JSONArray cashCloseInfo = jsonCashup.getJSONArray("cashCloseInfo");
          for (int j = 0; j < cashCloseInfo.length(); ++j) {
            JSONObject paymentMethod = cashCloseInfo.getJSONObject(j);
            if (paymentMethod.getString("paymentTypeId").equals(
                payment.getString("paymentMethodId"))) {
              payment.put("amountToKeep",
                  paymentMethod.getJSONObject("paymentMethod").getString("amountToKeep"));
            }
          }
        }
        createPaymentMethodCashUp(cashup, payment);
      }
    }
  }

  /**
   * Update the cashup info
   * 
   * @param cashup
   * @param jsonCashup
   * @throws JSONException
   */
  private void updateCashUpInfo(OBPOSAppCashup cashup, JSONObject jsonCashup, Date cashUpDate)
      throws JSONException, OBException {

    if (cashup.isProcessed() && jsonCashup.getString("isprocessed").equalsIgnoreCase("N")) {
      throw new OBException("The cashup is processed, and it can not be set as unprocessed");
    }
    cashup.setNetsales(new BigDecimal(jsonCashup.getString("netSales")));
    cashup.setGrosssales(new BigDecimal(jsonCashup.getString("grossSales")));
    cashup.setNetreturns(new BigDecimal(jsonCashup.getString("netReturns")));
    cashup.setGrossreturns(new BigDecimal(jsonCashup.getString("grossReturns")));
    cashup.setTotalretailtransactions(new BigDecimal(jsonCashup
        .getString("totalRetailTransactions")));
    cashup.setProcessed(jsonCashup.getString("isprocessed").equalsIgnoreCase("Y"));
    cashup.setCashUpDate(cashUpDate);
    OBDal.getInstance().save(cashup);
  }

  /**
   * Create the OBPOSPaymentMethodCashup object from json
   * 
   * @param jsonCashup
   * @return
   * @throws JSONException
   */
  private void createPaymentMethodCashUp(OBPOSAppCashup cashup, JSONObject jsonCashup)
      throws JSONException {
    OBPOSPaymentMethodCashup newPaymentMethodCashUp = OBDal.getInstance().get(
        OBPOSPaymentMethodCashup.class, jsonCashup.get("id"));

    if (newPaymentMethodCashUp == null) {
      newPaymentMethodCashUp = OBProvider.getInstance().get(OBPOSPaymentMethodCashup.class);
      newPaymentMethodCashUp.setNewOBObject(true);
      newPaymentMethodCashUp.setId(jsonCashup.get("id"));
    }
    JSONPropertyToEntity.fillBobFromJSON(newPaymentMethodCashUp.getEntity(),
        newPaymentMethodCashUp, jsonCashup);
    newPaymentMethodCashUp.setCashUp(cashup);

    newPaymentMethodCashUp.setOrganization(cashup.getOrganization());

    newPaymentMethodCashUp.setClient(cashup.getClient());

    newPaymentMethodCashUp.setSearchkey((String) jsonCashup.get("searchKey"));
    newPaymentMethodCashUp.setStartingcash(new BigDecimal(jsonCashup.getString("startingCash")));

    newPaymentMethodCashUp.setTotalsales(new BigDecimal(jsonCashup.getString("totalSales")));
    newPaymentMethodCashUp.setTotalreturns(new BigDecimal(jsonCashup.getString("totalReturns")));
    newPaymentMethodCashUp.setTotalDeposits(new BigDecimal(jsonCashup.getString("totalDeposits")));
    newPaymentMethodCashUp.setTotalDrops(new BigDecimal(jsonCashup.getString("totalDrops")));

    if (jsonCashup.has("amountToKeep")) {
      newPaymentMethodCashUp.setAmountToKeep(new BigDecimal(jsonCashup.getString("amountToKeep")));
    }
    newPaymentMethodCashUp.setRate(new BigDecimal(jsonCashup.getString("rate")));
    newPaymentMethodCashUp.setIsocode((String) jsonCashup.get("isocode"));

    OBPOSAppPayment appPayment = OBDal.getInstance().get(OBPOSAppPayment.class,
        jsonCashup.getString("paymentMethodId"));
    newPaymentMethodCashUp.setPaymentType(appPayment);

    String name = appPayment.getPaymentMethod().getName();
    newPaymentMethodCashUp.setName(name);
    OBDal.getInstance().save(newPaymentMethodCashUp);
  }

  /**
   * Create the OBPOSTaxCashup object from json
   * 
   * @param jsonCashup
   * @return
   * @throws JSONException
   */
  private void createTaxCashUp(OBPOSAppCashup cashup, JSONObject jsonCashup) throws JSONException {
    OBPOSTaxCashup newTax = OBDal.getInstance().get(OBPOSTaxCashup.class,
        jsonCashup.getString("id"));
    if (newTax == null) {
      newTax = OBProvider.getInstance().get(OBPOSTaxCashup.class);
      newTax.setNewOBObject(true);
      newTax.setId(jsonCashup.get("id"));
    }
    JSONPropertyToEntity.fillBobFromJSON(newTax.getEntity(), newTax, jsonCashup);

    newTax.setCashup(cashup);
    newTax.setName((String) jsonCashup.get("name"));
    newTax.setAmount(new BigDecimal(jsonCashup.getString("amount")));
    newTax.setOrdertype((String) jsonCashup.get("orderType"));
    newTax.setOrganization(cashup.getOrganization());
    newTax.setClient(cashup.getClient());
    OBDal.getInstance().save(newTax);
  }

  // We do not have to check if the role has access because now, we update cashup with every order.
  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  // The check of duplicates ids is done in this class
  @Override
  protected boolean additionalCheckForDuplicates(JSONObject record) {
    return false;
  }

  @Override
  protected void additionalProcessForRecordsSavedInErrorsWindow(JSONObject record) {
    try {
      String cashUpId = record.getString("id");
      OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);
      if (cashUp != null
          && (record.has("isprocessed") && record.getString("isprocessed").equals("Y"))) {
        cashUp.setProcessed(Boolean.TRUE);
        OBDal.getInstance().save(cashUp);
      }
    } catch (Exception e) {
    }
  }
}