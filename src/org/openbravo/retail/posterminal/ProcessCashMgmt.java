/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.retail.config.CashManagementEvents;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "FIN_Finacc_Transaction")
public class ProcessCashMgmt extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  private static final Logger log = Logger.getLogger(ProcessCashMgmt.class);

  @Inject
  @Any
  private Instance<ProcessCashMgmtHook> cashMgmtProcesses;

  protected String getImportQualifier() {
    return "FIN_Finacc_Transaction";
  }

  public JSONObject saveRecord(JSONObject jsonsent) throws Exception {

    // Update CashUp Report
    JSONObject jsoncashup = jsonsent.getJSONObject("cashUpReportInformation");
    Date cashUpDate = new Date();

    UpdateCashup.getAndUpdateCashUp(jsoncashup.getString("id"), jsoncashup, cashUpDate);

    OBPOSAppPayment paymentMethod = OBDal.getInstance().get(OBPOSAppPayment.class,
        jsonsent.getString("paymentMethodId"));
    OBContext.setOBContext(jsonsent.getString("userId"),
        OBContext.getOBContext().getRole().getId(), OBContext.getOBContext().getCurrentClient()
            .getId(), paymentMethod.getObposApplications().getOrganization().getId());
    String description = jsonsent.getString("description");
    if (description.length() > 60) {
      description = description.substring(0, 60);
    }
    BigDecimal amount = BigDecimal.valueOf(jsonsent.getDouble("amount"));
    BigDecimal origAmount = BigDecimal.valueOf(jsonsent.getDouble("origAmount"));
    String type = jsonsent.getString("type");
    String cashManagementReasonId = jsonsent.getString("reasonId");
    String cashupId = jsonsent.getString("cashup_id");
    OBPOSAppCashup cashup = OBDal.getInstance().get(OBPOSAppCashup.class, cashupId);
    if (cashup == null) {
      throw new OBException(
          "The cashup with ID '"
              + jsonsent.getString("cashup_id")
              + "' does not exists in the system. Please synchronize it first and then process this entry.");
    }
    TerminalTypePaymentMethod terminalPaymentMethod = paymentMethod.getPaymentMethod();
    // Save cash up events for payment method status
    OBPOSPaymentcashupEvents paymentcashupEvent = null;

    OBCriteria<OBPOSPaymentMethodCashup> paymentMethodCashupQuery = OBDal.getInstance()
        .createCriteria(OBPOSPaymentMethodCashup.class);
    paymentMethodCashupQuery.add(Restrictions.eq(OBPOSPaymentMethodCashup.PROPERTY_PAYMENTTYPE
        + ".id", paymentMethod.getId()));
    paymentMethodCashupQuery.add(Restrictions.eq(OBPOSPaymentMethodCashup.PROPERTY_CASHUP + ".id",
        cashup.getId()));
    OBPOSPaymentMethodCashup paymentmethodcashup = (OBPOSPaymentMethodCashup) paymentMethodCashupQuery
        .uniqueResult();
    paymentcashupEvent = OBProvider.getInstance().get(OBPOSPaymentcashupEvents.class);
    paymentcashupEvent.setObposPaymentmethodcashup(paymentmethodcashup);
    paymentcashupEvent.setName(description);
    paymentcashupEvent.setAmount(amount);
    paymentcashupEvent.setType(type);
    paymentcashupEvent.setCurrency(jsonsent.getString("isocode"));
    paymentcashupEvent.setRate(origAmount.divide(amount, 2, RoundingMode.HALF_UP));
    OBDal.getInstance().save(paymentcashupEvent);

    if (!jsonsent.has("defaultProcess") || jsonsent.getString("defaultProcess").equals("null")
        || "Y".equals(jsonsent.getString("defaultProcess"))) {
      GLItem glItemMain;
      GLItem glItemSecondary;
      // The GL/item in both transactions must be the same, and it must be the original type of
      // transaction
      if (type.equals("drop")) {
        glItemMain = terminalPaymentMethod.getGLItemForDrops();
        glItemSecondary = terminalPaymentMethod.getGLItemForDrops();
      } else {
        glItemMain = terminalPaymentMethod.getGLItemForDeposits();
        glItemSecondary = terminalPaymentMethod.getGLItemForDeposits();
      }
      if (jsonsent.has("glItem")) {
        glItemMain = OBDal.getInstance().get(GLItem.class, jsonsent.getString("glItem"));
      }

      // get and prepare the cashMgmtTrxDate
      Date cashMgmtTrxDate = new Date();
      if (jsonsent.has("creationDate") && jsonsent.get("creationDate") != null
          && StringUtils.isNotEmpty(jsonsent.getString("creationDate"))
          && !"null".equals(jsonsent.get("creationDate"))) {
        final String strCashMgmtTrxDate = jsonsent.getString("creationDate");
        if (!strCashMgmtTrxDate.substring(strCashMgmtTrxDate.length() - 1).equals("Z")) {
          log.error(String
              .format(
                  "The cashup date must be provided in ISO 8601 format and be an UTC date (value: '%s')",
                  strCashMgmtTrxDate));
        }
        // get the timezoneOffset
        final long timezoneOffset;
        if (jsonsent.has("timezoneOffset") && jsonsent.get("timezoneOffset") != null
            && StringUtils.isNotEmpty(jsonsent.getString("timezoneOffset"))
            && !"null".equals(jsonsent.get("timezoneOffset"))) {
          timezoneOffset = (long) Double.parseDouble(jsonsent.getString("timezoneOffset"));
        } else {
          timezoneOffset = -((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar
              .getInstance().get(Calendar.DST_OFFSET)) / (60 * 1000));
          log.error("Error processing cash close (1): error retrieving the timezoneOffset. Using the current timezoneOffset");
        }
        cashMgmtTrxDate = OBMOBCUtils.calculateClientDatetime(strCashMgmtTrxDate, timezoneOffset);
      } else {
        log.debug("Error processing cash close (2): error retrieving cashUp date. Using current server date");
      }
      cashMgmtTrxDate = OBMOBCUtils.stripTime(cashMgmtTrxDate);

      FIN_FinancialAccount account = paymentMethod.getFinancialAccount();

      FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
      transaction.setNewOBObject(true);
      transaction.setId(jsonsent.getString("id"));
      transaction.setObposAppCashup(cashup);
      transaction.setCurrency(account.getCurrency());
      transaction.setAccount(account);
      transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
      transaction.setGLItem(glItemMain);
      if (type.equals("drop")) {
        transaction.setPaymentAmount(amount);
        account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        transaction.setTransactionType("BPW");
        transaction.setStatus("PWNC");
      } else {
        transaction.setDepositAmount(amount);
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        transaction.setTransactionType("BPD");
        transaction.setStatus("RDNC");
      }
      transaction.setProcessed(true);
      transaction.setDescription(description);
      transaction.setDateAcct(cashMgmtTrxDate);
      transaction.setTransactionDate(cashMgmtTrxDate);

      OBDal.getInstance().save(transaction);
      paymentcashupEvent.setFINFinaccTransaction(transaction);
      OBDal.getInstance().save(paymentcashupEvent);

      CashManagementEvents event = OBDal.getInstance().get(CashManagementEvents.class,
          cashManagementReasonId);

      if (event != null) {
        FIN_FinancialAccount secondAccount = event.getFinancialAccount();

        FIN_FinaccTransaction secondTransaction = OBProvider.getInstance().get(
            FIN_FinaccTransaction.class);
        secondTransaction.setCurrency(secondAccount.getCurrency());
        secondTransaction.setObposAppCashup(cashup);
        secondTransaction.setAccount(secondAccount);
        secondTransaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(event
            .getFinancialAccount()) + 10);
        secondTransaction.setGLItem(glItemSecondary);
        // The second transaction describes the opposite movement of the first transaction.
        // If the first is a deposit, the second is a drop
        if (type.equals("deposit")) {
          secondTransaction.setPaymentAmount(origAmount);
          secondAccount.setCurrentBalance(secondAccount.getCurrentBalance().subtract(origAmount));
          secondTransaction.setTransactionType("BPW");
          secondTransaction.setStatus("PWNC");
        } else {
          secondTransaction.setDepositAmount(origAmount);
          secondAccount.setCurrentBalance(secondAccount.getCurrentBalance().add(origAmount));
          secondTransaction.setTransactionType("BPD");
          secondTransaction.setStatus("RDNC");
        }
        secondTransaction.setProcessed(true);
        secondTransaction.setDescription(description);
        secondTransaction.setDateAcct(cashMgmtTrxDate);
        secondTransaction.setTransactionDate(cashMgmtTrxDate);
        OBDal.getInstance().save(secondTransaction);
        paymentcashupEvent.setRelatedTransaction(secondTransaction);
        OBDal.getInstance().save(paymentcashupEvent);
      }
    }
    // Call all OrderProcess injected.
    String extendedType = jsonsent.has("extendedType") ? jsonsent.getString("extendedType") : "";
    executeHooks(cashMgmtProcesses, jsonsent, extendedType, paymentMethod, cashup,
        paymentcashupEvent, amount, origAmount);

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;
  }

  @Override
  protected String getProperty() {
    return "OBPOS_retail.cashmanagement";
  }

  protected void executeHooks(Instance<? extends Object> hooks, JSONObject jsonsent, String type,
      OBPOSAppPayment paymentMethod, OBPOSAppCashup cashup,
      OBPOSPaymentcashupEvents paymentcashupEvent, BigDecimal amount, BigDecimal origAmount)
      throws Exception {

    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      ((ProcessCashMgmtHook) proc).exec(jsonsent, type, paymentMethod, cashup, paymentcashupEvent,
          amount, origAmount);
    }
  }
}
