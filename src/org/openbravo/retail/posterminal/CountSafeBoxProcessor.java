/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.retail.config.CashManagementEvents;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class CountSafeBoxProcessor {

  private static final Logger logger = LogManager.getLogger();
  private static final String PAYMENT_CLEARED = "RPPC";
  private static final String WITHDRAWN_NOT_CLEARED = "PWNC";
  private static final String DEPOSITED_NOT_CLEARED = "RDNC";

  @Inject
  @Any
  private Instance<CountSafeboxHook> countSafeboxHooks;

  public JSONObject processCountSafeBox(OBPOSSafeBox safeBox, JSONObject jsonCountSafeBox,
      Date countSafeBoxDate) throws Exception {

    long t0 = System.currentTimeMillis();

    JSONArray countSafeBoxInfo = jsonCountSafeBox.getJSONArray("countSafeBoxInfo");

    boolean isInitialCount = jsonCountSafeBox.optBoolean("isInitialCount", false);
    OBPOSApplications touchpoint = OBDal.getInstance()
        .get(OBPOSApplications.class, jsonCountSafeBox.getString("touchpointId"));

    OBPOS_SafeboxCount safeboxCount = null;
    if (isInitialCount) {
      initializeSafeboxTerminalHistoryRecord(safeBox, countSafeBoxDate, touchpoint);
    } else {
      safeboxCount = createSafeboxCountHistoryRecord(safeBox, countSafeBoxDate, touchpoint,
          jsonCountSafeBox);
    }

    ArrayList<FIN_Reconciliation> arrayReconciliations = new ArrayList<FIN_Reconciliation>();

    for (int i = 0; i < countSafeBoxInfo.length(); i++) {

      JSONObject countSafeBoxObj = countSafeBoxInfo.getJSONObject(i);

      BigDecimal difference = new BigDecimal(countSafeBoxObj.getString("difference"));
      BigDecimal differenceToApply = difference;
      BigDecimal foreignDifference = new BigDecimal(0);
      BigDecimal foreignExpected = BigDecimal.valueOf(countSafeBoxObj.getDouble("foreignExpected"));
      BigDecimal amountToKeep = null;
      if (!countSafeBoxObj.getJSONObject("paymentMethod").isNull("amountToKeep")) {
        amountToKeep = BigDecimal
            .valueOf(countSafeBoxObj.getJSONObject("paymentMethod").getDouble("amountToKeep"));
      } else {
        amountToKeep = BigDecimal.ZERO;
      }

      if (countSafeBoxObj.has("foreignDifference")) {
        foreignDifference = new BigDecimal(countSafeBoxObj.getString("foreignDifference"));
        differenceToApply = foreignDifference;
      }
      String paymentTypeId = countSafeBoxObj.getString("paymentTypeId");

      OBPOSSafeBoxPaymentMethod paymentType = OBDal.getInstance()
          .get(OBPOSSafeBoxPaymentMethod.class, paymentTypeId);
      if (paymentType.getFINFinancialaccount() == null) {
        continue;
      }
      FIN_FinaccTransaction diffTransaction = null;
      if (!differenceToApply.equals(BigDecimal.ZERO)) {
        diffTransaction = createDifferenceTransaction(safeBox, paymentType, differenceToApply,
            countSafeBoxDate, isInitialCount);
        OBDal.getInstance().save(diffTransaction);
      }

      if (safeboxCount != null) {
        BigDecimal counted = foreignExpected.subtract(foreignDifference);
        addSafeboxCountPaymentMethod(safeboxCount, foreignExpected, counted, amountToKeep,
            countSafeBoxObj);
      }

      if (!paymentType.isAutomateMovementToOtherAccount() || isInitialCount) {
        continue;
      }

      FIN_Reconciliation reconciliation = createReconciliation(countSafeBoxObj, safeBox,
          paymentType.getFINFinancialaccount(), paymentType, countSafeBoxDate);

      if (diffTransaction != null) {
        diffTransaction.setReconciliation(reconciliation);
      }

      arrayReconciliations.add(reconciliation);
      OBDal.getInstance().save(reconciliation);

      BigDecimal reconciliationTotal = foreignExpected.add(foreignDifference);
      if (reconciliationTotal.compareTo(new BigDecimal(0)) != 0) {

        if (amountToKeep.compareTo(new BigDecimal(0)) != 0) {
          reconciliationTotal = reconciliationTotal.subtract(amountToKeep);
        }
        if (reconciliationTotal.compareTo(BigDecimal.ZERO) != 0) {
          FIN_FinaccTransaction paymentTransaction = createTotalTransferTransactionPayment(safeBox,
              reconciliation, paymentType, reconciliationTotal, countSafeBoxDate);
          OBDal.getInstance().save(paymentTransaction);

          FIN_FinaccTransaction depositTransaction = createTotalTransferTransactionDeposit(safeBox,
              reconciliation, paymentType, reconciliationTotal, countSafeBoxDate);
          OBDal.getInstance().save(depositTransaction);
        }
      }

      associateTransactions(paymentType, reconciliation);
    }

    for (FIN_Reconciliation reconciliation : arrayReconciliations) {
      reconciliation.setDocumentNo(getReconciliationDocumentNo(reconciliation.getDocumentType()));
      OBDal.getInstance().save(reconciliation);
    }

    long t1 = System.currentTimeMillis();

    OBDal.getInstance().flush();

    long t2 = System.currentTimeMillis();

    logger.debug("Count Safe Box Processor. Total time: " + (t2 - t0) + ". Processing: " + (t1 - t0)
        + ". Flush: " + (t2 - t1));

    if (!isInitialCount) {
      executeHooks(safeboxCount, jsonCountSafeBox);
      flagSafeboxCashupsAsCounted(safeBox);
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;
  }

  private void executeHooks(OBPOS_SafeboxCount safeboxCount, JSONObject jsonCountSafeBox)
      throws Exception {
    for (CountSafeboxHook hook : countSafeboxHooks) {
      hook.exec(safeboxCount, jsonCountSafeBox);
    }
  }

  private void addSafeboxCountPaymentMethod(OBPOS_SafeboxCount safeboxCount,
      BigDecimal foreignExpected, BigDecimal counted, BigDecimal amountToKeep,
      JSONObject countSafeBoxObj) throws JSONException {
    OBPOS_SafeboxCountPaymentMethod safeboxCountPaymentMethod = OBProvider.getInstance()
        .get(OBPOS_SafeboxCountPaymentMethod.class);
    safeboxCountPaymentMethod.setOrganization(safeboxCount.getOrganization());
    safeboxCountPaymentMethod.setObposSafeboxCount(safeboxCount);
    safeboxCountPaymentMethod.setExpectedAmount(foreignExpected);
    safeboxCountPaymentMethod.setCount(counted);
    safeboxCountPaymentMethod.setAmountToKeep(amountToKeep);
    String paymentTypeId = countSafeBoxObj.getString("paymentTypeId");
    safeboxCountPaymentMethod.setObposSafeboxPaymentmethod(
        OBDal.getInstance().getProxy(OBPOSSafeBoxPaymentMethod.class, paymentTypeId));
    safeboxCount.getOBPOSSafeboxCountPMList().add(safeboxCountPaymentMethod);
  }

  private OBPOS_SafeboxCount createSafeboxCountHistoryRecord(OBPOSSafeBox safeBox,
      Date countSafeBoxDate, OBPOSApplications touchpoint, JSONObject jsonCountSafeBox)
      throws JSONException {

    OBPOS_SafeboxCount safeboxCount = OBProvider.getInstance().get(OBPOS_SafeboxCount.class);
    String userId = jsonCountSafeBox.getString("userId");
    safeboxCount.setUser(OBDal.getInstance().getProxy(User.class, userId));
    safeboxCount.setCountdate(countSafeBoxDate);
    safeboxCount.setSafeBox(safeBox);
    safeboxCount.setOrganization(safeBox.getOrganization());
    safeboxCount.setTouchpoint(touchpoint);

    OBDal.getInstance().save(safeboxCount);
    return safeboxCount;
  }

  private void flagSafeboxCashupsAsCounted(OBPOSSafeBox safeBox) {
    //@formatter:off
    String hql =
            " update OBPOS_Safebox_Touchpoint " +
            " set iscounted = true " +
            " where obposSafebox.id = :safeboxId " +
            " and iscounted = false ";
    //@formatter:on
    OBDal.getInstance()
        .getSession()
        .createQuery(hql)
        .setParameter("safeboxId", safeBox.getId())
        .executeUpdate();
  }

  private void initializeSafeboxTerminalHistoryRecord(OBPOSSafeBox safeBox, Date countSafeBoxDate,
      OBPOSApplications touchpoint) {
    OBPOSSafeboxTouchpoint historyRecord = OBProvider.getInstance()
        .get(OBPOSSafeboxTouchpoint.class);

    historyRecord.setDateIn(countSafeBoxDate);
    historyRecord.setObposSafebox(safeBox);
    historyRecord.setTouchpoint(touchpoint);
    OBDal.getInstance().save(historyRecord);
  }

  private void associateTransactions(OBPOSSafeBoxPaymentMethod paymentType,
      FIN_Reconciliation reconciliation) {
    ScrollableResults transactions = getCashupTransactionsQuery(paymentType)
        .scroll(ScrollMode.FORWARD_ONLY);
    try {
      while (transactions.next()) {
        FIN_FinaccTransaction transaction = (FIN_FinaccTransaction) transactions.get(0);
        transaction.setStatus(PAYMENT_CLEARED);
        transaction.setReconciliation(reconciliation);

        // not all transactions have payment (i.e. deposits don't have), if there is payment, set it
        // as cleared
        if (transaction.getFinPayment() != null) {
          transaction.getFinPayment().setStatus(PAYMENT_CLEARED);
        }
      }
    } finally {
      transactions.close();
    }
  }

  private OBCriteria<FIN_FinaccTransaction> getCashupTransactionsQuery(
      final OBPOSSafeBoxPaymentMethod paymentType) {
    final OBCriteria<FIN_FinaccTransaction> query = OBDal.getInstance()
        .createCriteria(FIN_FinaccTransaction.class);
    query.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_ACCOUNT + ".id",
        paymentType.getFINFinancialaccount().getId()));

    return query;
  }

  private FIN_Reconciliation createReconciliation(JSONObject countSafeBoxObj, OBPOSSafeBox safeBox,
      FIN_FinancialAccount account, OBPOSSafeBoxPaymentMethod paymentType, Date countSafeBoxDate)
      throws JSONException {

    BigDecimal startingBalance;
    OBCriteria<FIN_Reconciliation> reconciliationsForAccount = OBDal.getInstance()
        .createCriteria(FIN_Reconciliation.class);

    Organization reconciliationOrganization = safeBox.getOrganization();

    reconciliationsForAccount.add(Restrictions.eq("account", account));
    reconciliationsForAccount.addOrderBy("creationDate", false);
    reconciliationsForAccount.setMaxResults(1);
    List<FIN_Reconciliation> reconciliations = reconciliationsForAccount.list();
    if (reconciliations.isEmpty()) {
      startingBalance = account.getInitialBalance();
    } else {
      startingBalance = reconciliations.get(0).getEndingBalance();
    }

    FIN_Reconciliation reconciliation = OBProvider.getInstance().get(FIN_Reconciliation.class);
    if (countSafeBoxObj.has("id")) {
      reconciliation.setId(countSafeBoxObj.getString("id"));
      reconciliation.setNewOBObject(true);
    }
    reconciliation.setAccount(account);
    reconciliation.setOrganization(reconciliationOrganization);
    reconciliation.setDocumentType(reconciliationOrganization.getObposCDoctyperecon());
    reconciliation.setDocumentNo("99999999temp");
    reconciliation.setEndingDate(countSafeBoxDate);
    reconciliation.setTransactionDate(countSafeBoxDate);
    if (countSafeBoxObj.has("paymentMethod")
        && !countSafeBoxObj.getJSONObject("paymentMethod").isNull("amountToKeep")) {
      reconciliation.setEndingBalance(BigDecimal
          .valueOf(countSafeBoxObj.getJSONObject("paymentMethod").getDouble("amountToKeep")));
    } else {
      reconciliation.setEndingBalance(new BigDecimal(0));
    }
    reconciliation.setStartingbalance(startingBalance);
    reconciliation.setDocumentStatus("CO");
    reconciliation.setProcessNow(false);
    reconciliation.setProcessed(true);

    return reconciliation;

  }

  private String getReconciliationDocumentNo(DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "",
        "FIN_Reconciliation", "", doctype == null ? "" : doctype.getId(), false, true);
  }

  private FIN_FinaccTransaction createDifferenceTransaction(OBPOSSafeBox safeBox,
      OBPOSSafeBoxPaymentMethod payment, BigDecimal difference, Date countSafeBoxDate,
      boolean isInitialCount) {
    FIN_FinancialAccount account = payment.getFINFinancialaccount();
    GLItem glItem = null;

    glItem = payment.getCashDifferences();

    FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    transaction.setCurrency(account.getCurrency());
    transaction.setAccount(account);
    transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
    transaction.setGLItem(glItem);
    if (difference.compareTo(BigDecimal.ZERO) < 0) {
      transaction.setPaymentAmount(difference.abs());
      account.setCurrentBalance(account.getCurrentBalance().subtract(difference.abs()));
      transaction.setTransactionType("BPW");
      transaction.setStatus(isInitialCount ? WITHDRAWN_NOT_CLEARED : PAYMENT_CLEARED);
    } else {
      transaction.setDepositAmount(difference);
      account.setCurrentBalance(account.getCurrentBalance().add(difference));
      transaction.setTransactionType("BPD");
      transaction.setStatus(isInitialCount ? DEPOSITED_NOT_CLEARED : PAYMENT_CLEARED);
    }
    transaction.setProcessed(true);
    transaction.setDescription("GL Item: " + glItem.getName());
    transaction.setDateAcct(OBMOBCUtils.stripTime(countSafeBoxDate));
    transaction.setTransactionDate(OBMOBCUtils.stripTime(countSafeBoxDate));

    return transaction;
  }

  private FIN_FinaccTransaction createTotalTransferTransactionPayment(OBPOSSafeBox safeBox,
      FIN_Reconciliation reconciliation, OBPOSSafeBoxPaymentMethod paymentType,
      BigDecimal reconciliationTotal, Date countSafeBoxDate) {

    FIN_FinancialAccount account = paymentType.getFINFinancialaccount();
    GLItem glItem = null;

    glItem = paymentType.getGLItemForCashDropDeposit();

    FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    transaction.setId(OBMOBCUtils.getUUIDbyString(reconciliation.getId() + "Payment"));
    transaction.setNewOBObject(true);
    transaction.setCurrency(account.getCurrency());
    transaction.setAccount(account);
    transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
    transaction.setGLItem(glItem);
    if (reconciliationTotal.compareTo(BigDecimal.ZERO) < 0) {
      transaction.setDepositAmount(reconciliationTotal.abs());
      transaction.setTransactionType("BPD");
    } else {
      transaction.setPaymentAmount(reconciliationTotal);
      transaction.setTransactionType("BPW");
    }
    transaction.setProcessed(true);
    transaction.setStatus(PAYMENT_CLEARED);
    transaction.setDescription(
        String.format("GL Item: %s, Safe Box: %s", glItem.getName(), safeBox.getSearchKey()));
    transaction.setDateAcct(OBMOBCUtils.stripTime(countSafeBoxDate));
    transaction.setTransactionDate(OBMOBCUtils.stripTime(countSafeBoxDate));
    transaction.setReconciliation(reconciliation);

    account.setCurrentBalance(account.getCurrentBalance().subtract(reconciliationTotal));

    return transaction;

  }

  private FIN_FinaccTransaction createTotalTransferTransactionDeposit(OBPOSSafeBox safeBox,
      FIN_Reconciliation reconciliation, OBPOSSafeBoxPaymentMethod paymentType,
      BigDecimal reconciliationTotal, Date countSafeBoxDate) {

    GLItem glItem = null;

    glItem = paymentType.getGLItemForCashDropDeposit();

    List<CashManagementEvents> orgCashMngmtEvents = safeBox.getOrganization()
        .getOBRETCOCashManagementEventsList();
    CashManagementEvents cashMgmtClosureEvent = null;
    for (CashManagementEvents cashMgmtEvent : orgCashMngmtEvents) {
      if (cashMgmtEvent.getPaymentMethod().getId() == paymentType.getPaymentMethod().getId()
          && cashMgmtEvent.getEventtype().equals("CL")
          && cashMgmtEvent.getFinancialAccount() != null) {
        cashMgmtClosureEvent = cashMgmtEvent;
        break;
      }
    }
    if (cashMgmtClosureEvent == null) {
      throw new OBException(
          "There is no close event defined for the payment method with a financial account in organization window");
    }
    FIN_FinancialAccount accountFrom = paymentType.getFINFinancialaccount();
    FIN_FinancialAccount accountTo = cashMgmtClosureEvent.getFinancialAccount();

    String accountFromCurrency = accountFrom.getCurrency().getId();
    String accountToCurrency = accountTo.getCurrency().getId();
    BigDecimal conversionRate = new BigDecimal(1);
    if (!accountFromCurrency.equals(accountToCurrency)) {
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(accountFromCurrency);
      parameters.add(accountToCurrency);
      parameters.add(null);
      parameters.add(null);
      parameters.add(safeBox.getClient().getId());
      parameters.add(safeBox.getOrganization().getId());

      String procedureName = "obpos_currency_rate";
      conversionRate = (BigDecimal) CallStoredProcedure.getInstance()
          .call(procedureName, parameters, null);
    }

    FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    transaction.setId(OBMOBCUtils.getUUIDbyString(reconciliation.getId() + "Deposit"));
    transaction.setNewOBObject(true);
    transaction.setCurrency(accountTo.getCurrency());
    transaction.setAccount(accountTo);
    transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(accountTo) + 10);
    transaction.setGLItem(glItem);
    if (reconciliationTotal.compareTo(BigDecimal.ZERO) < 0) {
      transaction.setPaymentAmount(reconciliationTotal.multiply(conversionRate)
          .abs()
          .setScale(accountTo.getCurrency().getStandardPrecision().intValue(),
              RoundingMode.HALF_EVEN));
      transaction.setTransactionType("BPW");
    } else {
      transaction.setDepositAmount(reconciliationTotal.multiply(conversionRate)
          .setScale(accountTo.getCurrency().getStandardPrecision().intValue(),
              RoundingMode.HALF_EVEN));
      transaction.setTransactionType("BPD");
    }
    transaction.setProcessed(true);
    transaction.setStatus("RDNC");
    transaction.setDescription(
        String.format("GL Item: %s, Safe Box: %s", glItem.getName(), safeBox.getSearchKey()));
    transaction.setDateAcct(OBMOBCUtils.stripTime(countSafeBoxDate));
    transaction.setTransactionDate(OBMOBCUtils.stripTime(countSafeBoxDate));

    accountTo.setCurrentBalance(accountTo.getCurrentBalance().add(reconciliationTotal));

    return transaction;

  }
}
