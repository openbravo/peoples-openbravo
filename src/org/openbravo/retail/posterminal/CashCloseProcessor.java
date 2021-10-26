/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
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
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class CashCloseProcessor {

  private static final Logger logger = LogManager.getLogger();

  @Inject
  @Any
  private Instance<CashupHook> cashupHooks;

  public JSONObject processCashClose(OBPOSApplications posTerminal, JSONObject jsonCashup,
      JSONArray cashMgmtIds, Date cashUpDate) throws Exception {
    return processCashClose(posTerminal, jsonCashup, cashMgmtIds, cashUpDate,
        new ArrayList<String>());
  }

  public JSONObject processCashClose(OBPOSApplications posTerminal, JSONObject jsonCashup,
      JSONArray cashMgmtIds, Date currentDate, List<String> slaveCashupIds) throws Exception {

    long t0 = System.currentTimeMillis();

    String cashUpId = jsonCashup.getString("id");
    slaveCashupIds.add(cashUpId);
    String userId = jsonCashup.getString("userId");
    JSONArray cashCloseInfo = jsonCashup.getJSONArray("cashCloseInfo");
    OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);
    ArrayList<FIN_Reconciliation> arrayReconciliations = new ArrayList<FIN_Reconciliation>();

    for (int i = 0; i < cashCloseInfo.length(); i++) {

      JSONObject cashCloseObj = cashCloseInfo.getJSONObject(i);

      BigDecimal difference = new BigDecimal(cashCloseObj.getString("difference"));
      BigDecimal differenceToApply = difference;
      BigDecimal foreignDifference = new BigDecimal(0);

      if (cashCloseObj.has("foreignDifference")) {
        foreignDifference = new BigDecimal(cashCloseObj.getString("foreignDifference"));
        differenceToApply = foreignDifference;
      }
      String paymentTypeId = cashCloseObj.getString("paymentTypeId");
      OBPOSAppPayment paymentType = OBDal.getInstance().get(OBPOSAppPayment.class, paymentTypeId);
      if (paymentType.getFinancialAccount() == null) {
        continue;
      }
      FIN_FinaccTransaction diffTransaction = null;
      if (!differenceToApply.equals(BigDecimal.ZERO)) {
        diffTransaction = createDifferenceTransaction(posTerminal, paymentType, differenceToApply,
            currentDate, cashUp);
        OBDal.getInstance().save(diffTransaction);
      }

      boolean isAutomaticDeposit = true;
      for (FinAccPaymentMethod finAccPaymentMethod : paymentType.getFinancialAccount()
          .getFinancialMgmtFinAccPaymentMethodList()) {
        if (finAccPaymentMethod.getPaymentMethod()
            .getId()
            .equals(paymentType.getPaymentMethod().getPaymentMethod().getId())) {
          isAutomaticDeposit = finAccPaymentMethod.isAutomaticDeposit();
          break;
        }
      }
      if (!isAutomaticDeposit) {
        continue;
      }

      FIN_Reconciliation reconciliation = createReconciliation(cashCloseObj, posTerminal,
          paymentType.getFinancialAccount(), currentDate, paymentType, slaveCashupIds);
      if (diffTransaction != null) {
        diffTransaction.setReconciliation(reconciliation);
      }

      arrayReconciliations.add(reconciliation);
      OBDal.getInstance().save(reconciliation);

      OBPOSAppCashReconcil recon = createCashUpReconciliation(posTerminal, paymentType,
          reconciliation, cashUp);
      OBDal.getInstance().save(recon);

      BigDecimal reconciliationTotal = BigDecimal.valueOf(cashCloseObj.getDouble("foreignExpected"))
          .add(foreignDifference);
      if (reconciliationTotal.compareTo(new BigDecimal(0)) != 0) {

        if (!cashCloseObj.getJSONObject("paymentMethod").isNull("amountToKeep") && BigDecimal
            .valueOf(cashCloseObj.getJSONObject("paymentMethod").getDouble("amountToKeep"))
            .compareTo(new BigDecimal(0)) != 0) {

          BigDecimal amountToKeep = BigDecimal
              .valueOf(cashCloseObj.getJSONObject("paymentMethod").getDouble("amountToKeep"));
          reconciliationTotal = reconciliationTotal.subtract(amountToKeep);
        }
        if (reconciliationTotal.compareTo(BigDecimal.ZERO) != 0) {
          FIN_FinaccTransaction paymentTransaction = createTotalTransferTransactionPayment(
              posTerminal, reconciliation, paymentType, reconciliationTotal, currentDate, cashUp);
          OBDal.getInstance().save(paymentTransaction);

          FIN_FinaccTransaction depositTransaction = createTotalTransferTransactionDeposit(
              posTerminal, reconciliation, paymentType, reconciliationTotal, currentDate, cashUp);
          OBDal.getInstance().save(depositTransaction);
        }
      }

      associateTransactions(paymentType, reconciliation, slaveCashupIds);
    }

    // Reconciliate payment methods of type "Defined in SabeBox"
    JSONArray cashPaymentMethodInfo = jsonCashup.getJSONArray("cashPaymentMethodInfo");
    for (int i = 0; i < cashPaymentMethodInfo.length(); i++) {
      // Check if current payment method is from Safe Box
      JSONObject cashPaymentMethodInfoObj = cashPaymentMethodInfo.getJSONObject(i);
      OBPOSAppPayment paymentMethod = OBDal.getInstance()
          .get(OBPOSAppPayment.class, cashPaymentMethodInfoObj.getString("paymentMethodId"));

      if (!paymentMethod.getPaymentMethod().isSafebox()) {
        continue;
      }

      FIN_Reconciliation reconciliation = createReconciliation(new JSONObject(), posTerminal,
          paymentMethod.getFinancialAccount(), currentDate, paymentMethod, slaveCashupIds);

      arrayReconciliations.add(reconciliation);
      OBDal.getInstance().save(reconciliation);

      OBPOSAppCashReconcil recon = createCashUpReconciliation(posTerminal, paymentMethod,
          reconciliation, cashUp);
      OBDal.getInstance().save(recon);

      associateTransactions(paymentMethod, reconciliation, slaveCashupIds);
    }

    for (FIN_Reconciliation reconciliation : arrayReconciliations) {
      reconciliation.setDocumentNo(getReconciliationDocumentNo(reconciliation.getDocumentType()));
      OBDal.getInstance().save(reconciliation);
    }

    User user = OBDal.getInstance().get(User.class, userId);
    cashUp.setUserContact(user);

    long t1 = System.currentTimeMillis();

    // Hook for procesing cashups..
    JSONArray messages = new JSONArray(); // all messages returned by hooks
    String next = executeHooks(messages, posTerminal, cashUp, jsonCashup);

    long t2 = System.currentTimeMillis();

    // done and done
    cashUp.setProcessedbo(true);

    OBDal.getInstance().flush();

    long t3 = System.currentTimeMillis();

    logger.debug("Cash Up Processor. Total time: " + (t3 - t0) + ". Processing: " + (t1 - t0)
        + ". Hooks: " + (t2 - t1) + ". Flush: " + (t3 - t2));

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    result.put("messages", messages);
    result.put("next", next);
    return result;
  }

  private String executeHooks(JSONArray messages, OBPOSApplications posTerminal,
      OBPOSAppCashup cashUp, JSONObject jsonCashup) throws Exception {
    String next = null; // the first next action of all hooks wins
    for (CashupHook hook : cashupHooks) {
      CashupHookResult result = hook.exec(posTerminal, cashUp, jsonCashup);
      if (result != null) {
        if (result.getMessage() != null && !result.getMessage().equals("")) {
          messages.put(result.getMessage());
        }
        if (next == null && result.getNextAction() != null && !result.getNextAction().equals("")) {
          next = result.getNextAction();
        }
      }
    }
    return next;
  }

  private void associateTransactions(OBPOSAppPayment paymentType, FIN_Reconciliation reconciliation,
      List<String> slaveCashupIds) {
    ScrollableResults transactions = getCashupTransactionsQuery(paymentType, slaveCashupIds)
        .scroll(ScrollMode.FORWARD_ONLY);
    try {
      while (transactions.next()) {
        FIN_FinaccTransaction transaction = (FIN_FinaccTransaction) transactions.get(0);
        transaction.setStatus("RPPC");
        transaction.setReconciliation(reconciliation);

        // not all transactions have payment (i.e. deposits don't have), if there is payment, set it
        // as cleared
        if (transaction.getFinPayment() != null) {
          transaction.getFinPayment().setStatus("RPPC");
        }
      }
    } finally {
      transactions.close();
    }
  }

  private OBCriteria<FIN_FinaccTransaction> getCashupTransactionsQuery(
      final OBPOSAppPayment paymentType, final List<String> slaveCashupIds) {
    final OBCriteria<FIN_FinaccTransaction> query = OBDal.getInstance()
        .createCriteria(FIN_FinaccTransaction.class);
    query.add(
        Restrictions.in(FIN_FinaccTransaction.PROPERTY_OBPOSAPPCASHUP + ".id", slaveCashupIds));
    query.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_ACCOUNT + ".id",
        paymentType.getFinancialAccount().getId()));

    return query;
  }

  private FIN_Reconciliation createReconciliation(JSONObject cashCloseObj,
      OBPOSApplications posTerminal, FIN_FinancialAccount account, Date currentDate,
      OBPOSAppPayment paymentType, List<String> slaveCashupIds) throws JSONException {

    BigDecimal startingBalance;
    OBCriteria<FIN_Reconciliation> reconciliationsForAccount = OBDal.getInstance()
        .createCriteria(FIN_Reconciliation.class);
    final Organization reconciliationOrganization = getReconciliationOrganization(posTerminal,
        paymentType, slaveCashupIds);
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
    if (cashCloseObj.has("id")) {
      reconciliation.setId(cashCloseObj.getString("id"));
      reconciliation.setNewOBObject(true);
    }
    reconciliation.setAccount(account);
    reconciliation.setOrganization(reconciliationOrganization);
    reconciliation.setDocumentType(reconciliationOrganization.getObposCDoctyperecon());
    reconciliation.setDocumentNo("99999999temp");
    reconciliation.setEndingDate(currentDate);
    reconciliation.setTransactionDate(currentDate);
    if (cashCloseObj.has("paymentMethod")
        && !cashCloseObj.getJSONObject("paymentMethod").isNull("amountToKeep")) {
      reconciliation.setEndingBalance(BigDecimal
          .valueOf(cashCloseObj.getJSONObject("paymentMethod").getDouble("amountToKeep")));
    } else {
      reconciliation.setEndingBalance(new BigDecimal(0));
    }
    reconciliation.setStartingbalance(startingBalance);
    reconciliation.setDocumentStatus("CO");
    reconciliation.setProcessNow(false);
    reconciliation.setProcessed(true);

    return reconciliation;

  }

  // If reconciliation includes at least one cross store payment, return cross store organization,
  // otherwise return terminal organization
  private Organization getReconciliationOrganization(final OBPOSApplications posTerminal,
      final OBPOSAppPayment paymentType, final List<String> slaveCashupIds) {
    final Organization crossOrganization = posTerminal.getOrganization()
        .getOBRETCOCrossStoreOrganization();
    if (crossOrganization == null) {
      return posTerminal.getOrganization();
    }

    final OBCriteria<FIN_FinaccTransaction> query = getCashupTransactionsQuery(paymentType,
        slaveCashupIds);
    query.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_ORGANIZATION + ".id",
        crossOrganization.getId()));
    query.setMaxResults(1);

    if (query.uniqueResult() != null) {
      return crossOrganization;
    }

    return posTerminal.getOrganization();
  }

  private String getReconciliationDocumentNo(DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "",
        "FIN_Reconciliation", "", doctype == null ? "" : doctype.getId(), false, true);
  }

  private FIN_FinaccTransaction createDifferenceTransaction(OBPOSApplications terminal,
      OBPOSAppPayment payment, BigDecimal difference, Date currentDate, OBPOSAppCashup cashUp) {
    FIN_FinancialAccount account = payment.getFinancialAccount();
    GLItem glItem = null;
    if (payment.isOverrideconfiguration()) {
      glItem = payment.getCashDifferences();
    } else {
      glItem = payment.getPaymentMethod().getCashDifferences();
    }

    FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    Organization cashUpOrganization = cashUp.getPOSTerminal().getOrganization();
    transaction.setCurrency(account.getCurrency());
    transaction.setAccount(account);
    transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
    transaction.setGLItem(glItem);
    if (difference.compareTo(BigDecimal.ZERO) < 0) {
      transaction.setPaymentAmount(difference.abs());
      account.setCurrentBalance(account.getCurrentBalance().subtract(difference.abs()));
      transaction.setTransactionType("BPW");
    } else {
      transaction.setDepositAmount(difference);
      account.setCurrentBalance(account.getCurrentBalance().add(difference));
      transaction.setTransactionType("BPD");
    }
    transaction.setProcessed(true);
    transaction.setStatus("RPPC");
    transaction.setDescription("GL Item: " + glItem.getName());
    transaction.setObposAppCashup(cashUp);
    transaction.setDateAcct(OBMOBCUtils.stripTime(currentDate));
    transaction.setTransactionDate(OBMOBCUtils.stripTime(currentDate));
    if (cashUpOrganization != null) {
      transaction.setOrganization(cashUpOrganization);
    }

    return transaction;
  }

  private FIN_FinaccTransaction createTotalTransferTransactionPayment(OBPOSApplications terminal,
      FIN_Reconciliation reconciliation, OBPOSAppPayment paymentType,
      BigDecimal reconciliationTotal, Date currentDate, OBPOSAppCashup cashUp) {
    TerminalTypePaymentMethod paymentMethod = paymentType.getPaymentMethod();
    FIN_FinancialAccount account = paymentType.getFinancialAccount();
    GLItem glItem = null;
    if (paymentType.isOverrideconfiguration()) {
      glItem = paymentType.getGLItemForCashDropDeposit();
    } else {
      glItem = paymentMethod.getGlitemDropdep();
    }

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
    transaction.setStatus("RPPC");
    transaction.setDescription(
        String.format("GL Item: %s, Terminal: %s", glItem.getName(), terminal.getSearchKey()));
    transaction.setObposAppCashup(cashUp);
    transaction.setDateAcct(OBMOBCUtils.stripTime(currentDate));
    transaction.setTransactionDate(OBMOBCUtils.stripTime(currentDate));
    transaction.setReconciliation(reconciliation);

    account.setCurrentBalance(account.getCurrentBalance().subtract(reconciliationTotal));

    return transaction;

  }

  private FIN_FinaccTransaction createTotalTransferTransactionDeposit(OBPOSApplications terminal,
      FIN_Reconciliation reconciliation, OBPOSAppPayment paymentType,
      BigDecimal reconciliationTotal, Date currentDate, OBPOSAppCashup cashUp) {
    GLItem glItem = null;
    if (paymentType.isOverrideconfiguration()) {
      glItem = paymentType.getGLItemForCashDropDeposit();
    } else {
      glItem = paymentType.getPaymentMethod().getGlitemDropdep();
    }
    if (paymentType.getObretcoCmevents() == null) {
      throw new OBException("There is no close event defined for the payment method");
    }
    FIN_FinancialAccount accountFrom = paymentType.getFinancialAccount();
    FIN_FinancialAccount accountTo = paymentType.getObretcoCmevents().getFinancialAccount();

    String accountFromCurrency = accountFrom.getCurrency().getId();
    String accountToCurrency = accountTo.getCurrency().getId();
    BigDecimal conversionRate = new BigDecimal(1);
    if (!accountFromCurrency.equals(accountToCurrency)) {
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(accountFromCurrency);
      parameters.add(accountToCurrency);
      parameters.add(null);
      parameters.add(null);
      parameters.add(terminal.getClient().getId());
      parameters.add(terminal.getOrganization().getId());

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
        String.format("GL Item: %s, Terminal: %s", glItem.getName(), terminal.getSearchKey()));
    transaction.setObposAppCashup(cashUp);
    transaction.setDateAcct(OBMOBCUtils.stripTime(currentDate));
    transaction.setTransactionDate(OBMOBCUtils.stripTime(currentDate));

    accountTo.setCurrentBalance(accountTo.getCurrentBalance().add(reconciliationTotal));

    return transaction;

  }

  private OBPOSAppCashReconcil createCashUpReconciliation(OBPOSApplications posTerminal,
      OBPOSAppPayment paymentType, FIN_Reconciliation reconciliation, OBPOSAppCashup cashUp) {
    OBPOSAppCashReconcil recon = OBProvider.getInstance().get(OBPOSAppCashReconcil.class);
    recon.setId(reconciliation.getId());
    recon.setNewOBObject(true);
    recon.setOrganization(posTerminal.getOrganization());
    recon.setPaymentType(paymentType);
    recon.setReconciliation(reconciliation);
    recon.setCashUp(cashUp);
    return recon;
  }
}
