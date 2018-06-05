/*
 ************************************************************************************
 * Copyright (C) 2016-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.mobile.core.process.PropertyByType;
import org.openbravo.service.importprocess.ImportEntryManager;
import org.openbravo.service.json.JsonToDataConverter;

public class UpdateCashup {

  private static final Logger log = Logger.getLogger(ImportEntryManager.class);

  /**
   * Get and update a cashup. If cashup not exist it's created, otherwise update the cashup data
   * into database.
   * 
   * @param cashUpId
   *          The cashUp identifier
   * @param jsonCashup
   *          The input object with cashup data
   * @return Cashup object
   * @throws JSONException
   */
  public static OBPOSAppCashup getAndUpdateCashUp(String cashUpId, JSONObject jsonCashup,
      Date cashUpDate) throws JSONException, SQLException {
    // CashUp record will be read from the database with a "for update" clause to force the process
    // to get the lock on the record. The reason for this is to prevent the same cash up from being
    // processed twice in case of very quick duplicated requests.
    // These shouldn't happen in general but may happen specifically in case of unreliable networks
    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
        jsonCashup.getString("posterminal"));
    Date cashUpReportDate = null;
    Date lastCashUpReportDate = null;
    OBPOSAppCashup cashUp = null;
    Query cashUpQuery = OBDal.getInstance().getSession()
        .createQuery("from OBPOS_App_Cashup where id=:cashUpId");
    cashUpQuery.setParameter("cashUpId", cashUpId);
    // The record will be locked to this process until it ends. Other requests to process this cash
    // up will be locked until this one finishes
    cashUpQuery.setLockOptions(LockOptions.UPGRADE);
    cashUp = (OBPOSAppCashup) cashUpQuery.uniqueResult();

    if (jsonCashup.has("creationDate")) {
      String cashUpReportString = jsonCashup.getString("creationDate");
      if (cashUpReportString.lastIndexOf(".") != -1) {
        cashUpReportDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(
            PropertyByType.DATETIME,
            (cashUpReportString).subSequence(0, (cashUpReportString).lastIndexOf(".")));
      } else if (cashUpReportString.lastIndexOf("Z") != -1) {
        cashUpReportDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(
            PropertyByType.DATETIME,
            (cashUpReportString).subSequence(0, (cashUpReportString).lastIndexOf("Z")) + ":00");
      } else {
        cashUpReportDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(
            PropertyByType.DATETIME, cashUpReportString);
      }
    }

    if (cashUp == null) {
      // create the cashup if no exists
      try {
        cashUp = OBProvider.getInstance().get(OBPOSAppCashup.class);
        cashUp.setId(cashUpId);
        cashUp.setOrganization(posTerminal.getOrganization());
        cashUp.setCashUpDate(cashUpDate);
        cashUp.setCreationDate(cashUpReportDate);
        cashUp.setPOSTerminal(posTerminal);
        cashUp.setUserContact(OBContext.getOBContext().getUser());
        cashUp.setBeingprocessed(jsonCashup.getString("isbeingprocessed").equalsIgnoreCase("Y"));
        cashUp.setNewOBObject(true);
        OBDal.getInstance().save(cashUp);

        // If synchronize mode is active, there is no way to process two cashups with the same id at
        // the same time.
        // If synchronize mode is not active, we have to persist the header of the cashup. Doing
        // this, we avoid possible conflicts trying to save two cashups with the same id at the same
        // time.
        if (!POSUtils.isSynchronizedModeEnabled()) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getConnection(false).commit();
        }

      } catch (JSONException e) {
        throw new OBException("Cashup JSON seems to be corrupted: ", e);
      } catch (Exception e) {
        Query maybeCashupWasCreatedInParallel = OBDal.getInstance().getSession()
            .createQuery("from OBPOS_App_Cashup where id=:cashUpId");
        maybeCashupWasCreatedInParallel.setParameter("cashUpId", cashUpId);
        cashUp = (OBPOSAppCashup) maybeCashupWasCreatedInParallel.uniqueResult();
        // If cashup exists, then other process (such as OpenTill) created it in parallel, and
        // everything is fine. Otherwise, the process should fail.
        if (cashUp == null) {
          throw new OBException(e);
        }
      }
    }

    if (jsonCashup.has("lastcashupeportdate")) {
      String lastCashUpReportString = jsonCashup.getString("lastcashupeportdate");
      lastCashUpReportDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(
          PropertyByType.DATETIME,
          (lastCashUpReportString).subSequence(0, (lastCashUpReportString).lastIndexOf(".")));
    }

    // If the cashup is new or the incoming cashup report is newer, update the cashUp info
    if (cashUp.getLastcashupreportdate() == null
        || (lastCashUpReportDate != null && lastCashUpReportDate.getTime() >= cashUp
            .getLastcashupreportdate().getTime())) {
      if (jsonCashup.has("objToSend")) {
        JSONObject jsonInfoCashUp = new JSONObject(jsonCashup.getString("objToSend"));
        // JSONObject jsonInfoCashUp = (JSONObject) jsonCashup.get("objToSend");
        updateOrCreateCashupInfo(cashUpId, jsonInfoCashUp, cashUpDate, cashUpReportDate,
            lastCashUpReportDate);
      } else {
        updateOrCreateCashupInfo(cashUpId, jsonCashup, cashUpDate, cashUpReportDate,
            lastCashUpReportDate);
      }
    } else {
      log.debug("Don't need to update cashUp");
    }

    // Associate master/slave cashup
    if ((posTerminal.isMaster() && posTerminal.getOBPOSApplicationsMasterterminalIDList().size() != cashUp
        .getOBPOSAppCashupObposParentCashupIDList().size())
        || (posTerminal.getMasterterminal() != null && cashUp.getObposParentCashup() == null)) {
      associateMasterSlave(cashUp, posTerminal);
    }

    OBDal.getInstance().flush();
    return cashUp;
  }

  /**
   * Update the cashup info
   * 
   * @param cashup
   * @param jsonCashup
   * @throws JSONException
   */
  private static void updateCashUpInfo(OBPOSAppCashup cashup, JSONObject jsonCashup,
      Date cashUpDate, Date cashUpReportDate, Date lastCashUpReportDate) throws JSONException,
      OBException {

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
    cashup.setLastcashupreportdate(lastCashUpReportDate);
    OBDal.getInstance().save(cashup);
    if (cashup.isProcessed()) {
      // In case of Slave/Master Check if slave is processed or not
      for (OBPOSAppCashup slaveCashup : cashup.getOBPOSAppCashupObposParentCashupIDList()) {
        if (!slaveCashup.isProcessed()) {
          log.debug("Master/Slave association ("
              + new Date()
              + "): Unlink slave terminal: "
              + slaveCashup.getPOSTerminal().getName()
              + " and Cashup id: "
              + slaveCashup.getId()
              + " linked with master terminal: "
              + cashup.getPOSTerminal().getName()
              + " and Cashup id: "
              + cashup.getId()
              + " because slave terminal has no transactions and it will be associated with the next master terminal cashup");
          slaveCashup.setObposParentCashup(null);
          OBDal.getInstance().save(slaveCashup);
        }
      }
    }

  }

  private static void updateOrCreateCashupInfo(String cashUpId, JSONObject jsonCashup,
      Date cashUpDate, Date cashUpReportDate, Date lastCashUpReportDate) throws JSONException,
      OBException {
    OBPOSAppCashup cashup = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);
    // Update cashup info
    updateCashUpInfo(cashup, jsonCashup, cashUpDate, cashUpReportDate, lastCashUpReportDate);

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
        if ((payment.has("newPaymentMethod") && payment.getString("newPaymentMethod")
            .equals("true"))
            || (payment.has("usedInCurrentTrx") && payment.getString("usedInCurrentTrx").equals(
                "true"))) {
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
                BigDecimal expected = BigDecimal.ZERO;
                BigDecimal difference = BigDecimal.ZERO;
                BigDecimal rate = new BigDecimal(payment.getString("rate"));
                if (rate.compareTo(BigDecimal.ONE) == 0) {
                  expected = new BigDecimal(paymentMethod.getString("expected"));
                  difference = new BigDecimal(paymentMethod.getString("difference"));
                } else if (paymentMethod.has("foreignExpected")) {
                  expected = new BigDecimal(paymentMethod.getString("foreignExpected"));
                  difference = new BigDecimal(
                      paymentMethod.has("foreignDifference") ? paymentMethod
                          .getString("foreignDifference") : paymentMethod.getString("difference"));
                }
                payment.put("totalCounted",
                    expected.add(difference).setScale(2, RoundingMode.HALF_UP).toString());
              }
            }
          }
          createPaymentMethodCashUp(cashup, payment);
        }
      }
    }
  }

  /**
   * Create the OBPOSPaymentMethodCashup object from json
   * 
   * @param jsonCashup
   * @throws JSONException
   */
  private static void createPaymentMethodCashUp(OBPOSAppCashup cashup, JSONObject jsonCashup)
      throws JSONException {
    OBPOSPaymentMethodCashup newPaymentMethodCashUp = OBDal.getInstance().get(
        OBPOSPaymentMethodCashup.class, jsonCashup.get("id"));

    if (newPaymentMethodCashUp == null) {
      newPaymentMethodCashUp = OBProvider.getInstance().get(OBPOSPaymentMethodCashup.class);
      newPaymentMethodCashUp.setNewOBObject(true);
      newPaymentMethodCashUp.setId(jsonCashup.get("id"));
      cashup.getOBPOSPaymentmethodcashupList().add(newPaymentMethodCashUp);
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
    newPaymentMethodCashUp.setTotalCounted(jsonCashup.has("totalCounted") ? new BigDecimal(
        jsonCashup.getString("totalCounted")) : BigDecimal.ZERO);
    newPaymentMethodCashUp.setAmountToKeep(jsonCashup.has("amountToKeep") ? new BigDecimal(
        jsonCashup.getString("amountToKeep")) : BigDecimal.ZERO);
    newPaymentMethodCashUp.setRate(new BigDecimal(jsonCashup.getString("rate")));
    newPaymentMethodCashUp.setIsocode((String) jsonCashup.get("isocode"));

    OBPOSAppPayment appPayment = OBDal.getInstance().get(OBPOSAppPayment.class,
        jsonCashup.getString("paymentMethodId"));
    newPaymentMethodCashUp.setPaymentType(appPayment);

    String name = appPayment.getCommercialName();
    newPaymentMethodCashUp.setName(name);
    OBDal.getInstance().save(newPaymentMethodCashUp);
  }

  /**
   * Create the OBPOSTaxCashup object from json
   * 
   * @param jsonCashup
   * @throws JSONException
   */
  private static void createTaxCashUp(OBPOSAppCashup cashup, JSONObject jsonCashup)
      throws JSONException {
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

  public static void associateMasterSlave(OBPOSAppCashup cashUp, OBPOSApplications posTerminal) {
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
            + OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id = :terminalId and "
            + OBPOSAppCashup.PROPERTY_ISPROCESSEDBO + " = 'Y' and "
            + OBPOSAppCashup.PROPERTY_ISPROCESSED + " = 'Y' and "
            + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + " is not null and "
            + OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + ".id = :parentCashup";
        if (countAppCashup(query, appCashup.getPOSTerminal().getId(), cashUp.getId()) == 0) {
          log.debug("Master/Slave association (" + new Date() + "): Associating slave terminal: "
              + appCashup.getPOSTerminal().getName() + " and Cashup id: " + appCashup.getId()
              + " with master terminal: " + cashUp.getPOSTerminal().getName() + " and Cashup id: "
              + cashUp.getId());
          appCashup.setObposParentCashup(cashUp);
        }
      }
    } else if (posTerminal.getMasterterminal() != null && cashUp.getObposParentCashup() == null) {
      // Determine if exist open master cashup
      String query = "select count(*) from " + OBPOSAppCashup.ENTITY_NAME + " where "
          + OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id = :terminalId and "
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
          log.debug("Master/Slave association (" + new Date() + "): Associating slave terminal: "
              + cashUp.getPOSTerminal().getName() + " and Cashup id: " + cashUp.getId()
              + " with master terminal: " + appCashupList.get(0).getPOSTerminal().getName()
              + " and Cashup id: " + appCashupList.get(0).getId());
          cashUp.setObposParentCashup(appCashupList.get(0));
        }
      }
    }
  }

  private static Long countAppCashup(String query, String posterminal, String parentCashUp) {
    final Session session = OBDal.getInstance().getSession();
    final Query count = session.createQuery(query);
    count.setParameter("terminalId", posterminal);
    if (parentCashUp != null) {
      count.setParameter("parentCashup", parentCashUp);
    }
    Long value = (Long) count.uniqueResult();
    return value != null ? value.longValue() : 0;
  }
}
