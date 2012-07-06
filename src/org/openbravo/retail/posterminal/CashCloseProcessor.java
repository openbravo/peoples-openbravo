/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.json.JsonConstants;

public class CashCloseProcessor {

  public JSONObject processCashClose(JSONArray cashCloseInfo) throws JSONException {

    OBContext.setAdminMode(true);
    TriggerHandler.getInstance().disable();
    try {
      for (int i = 0; i < cashCloseInfo.length(); i++) {
        JSONObject cashCloseObj = cashCloseInfo.getJSONObject(i);
        BigDecimal reconciliationTotal = BigDecimal.valueOf(cashCloseObj.getDouble("expected"));
        BigDecimal difference = new BigDecimal(cashCloseObj.getString("difference"));
        String paymentTypeId = cashCloseObj.getString("paymentTypeId");
        OBPOSAppPayment paymentType = OBDal.getInstance().get(OBPOSAppPayment.class, paymentTypeId);

        OBPOSApplications posTerminal = paymentType.getObposApplications();

        FIN_Reconciliation reconciliation = createReconciliation(cashCloseObj, posTerminal,
            paymentType.getFinancialAccount());

        FIN_FinaccTransaction diffTransaction = null;
        if (!difference.equals(BigDecimal.ZERO)) {
          diffTransaction = createDifferenceTransaction(posTerminal, reconciliation, paymentType,
              difference);
          OBDal.getInstance().save(diffTransaction);
        }
        OBDal.getInstance().save(reconciliation);

        // if(paymentType.getPaymentMethod().isAutomatemovementtoother()){
        //
        // reconciliationTotal = reconciliationTotal.subtract(new
        // BigDecimal(paymentType.getPaymentMethod().getAmount()));

        FIN_FinaccTransaction paymentTransaction = createTotalTransferTransactionPayment(
            posTerminal, reconciliation, paymentType, reconciliationTotal);

        OBDal.getInstance().save(paymentTransaction);

        FIN_FinaccTransaction depositTransaction = createTotalTransferTransactionDeposit(
            posTerminal, reconciliation, paymentType, reconciliationTotal);

        OBDal.getInstance().save(depositTransaction);

        // }

        associateTransactions(paymentType, reconciliation);

      }
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } finally {
      OBDal.getInstance().rollbackAndClose();
      OBContext.restorePreviousMode();
      if (TriggerHandler.getInstance().isDisabled()) {
        TriggerHandler.getInstance().enable();
      }
    }

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }

  protected void associateTransactions(OBPOSAppPayment paymentType,
      FIN_Reconciliation reconciliation) {
    OBCriteria<FIN_FinaccTransaction> openTransactionsForAccount = OBDal.getInstance()
        .createCriteria(FIN_FinaccTransaction.class);
    openTransactionsForAccount.add(Restrictions.eq("account", paymentType.getFinancialAccount()));
    openTransactionsForAccount.add(Restrictions.isNull("reconciliation"));
    ScrollableResults transactions = openTransactionsForAccount.scroll();
    while (transactions.next()) {
      FIN_FinaccTransaction transaction = (FIN_FinaccTransaction) transactions.get(0);
      transaction.setStatus("RPPC");
      transaction.setReconciliation(reconciliation);
    }

  }

  protected FIN_Reconciliation createReconciliation(JSONObject cashCloseObj,
      OBPOSApplications posTerminal, FIN_FinancialAccount account) {

    BigDecimal startingBalance;
    OBCriteria<FIN_Reconciliation> reconciliationsForAccount = OBDal.getInstance().createCriteria(
        FIN_Reconciliation.class);
    reconciliationsForAccount.add(Restrictions.eq("account", account));
    reconciliationsForAccount.addOrderBy("creationDate", false);
    List<FIN_Reconciliation> reconciliations = reconciliationsForAccount.list();
    if (reconciliations.size() == 0) {
      startingBalance = account.getInitialBalance();
    } else {
      startingBalance = reconciliations.get(0).getEndingBalance();
    }

    FIN_Reconciliation reconciliation = OBProvider.getInstance().get(FIN_Reconciliation.class);
    reconciliation.setAccount(account);
    reconciliation.setDocumentNo(null);
    reconciliation.setDocumentType(posTerminal.getObposTerminaltype()
        .getDocumentTypeForReconciliations());
    reconciliation.setEndingDate(new Date());
    reconciliation.setTransactionDate(new Date());
    reconciliation.setEndingBalance(BigDecimal.ZERO);
    reconciliation.setStartingbalance(startingBalance);
    reconciliation.setDocumentStatus("CO");
    reconciliation.setProcessNow(false);
    reconciliation.setProcessed(true);

    return reconciliation;

  }

  protected FIN_FinaccTransaction createDifferenceTransaction(OBPOSApplications terminal,
      FIN_Reconciliation reconciliation, OBPOSAppPayment payment, BigDecimal difference) {
    FIN_FinancialAccount account = payment.getFinancialAccount();
    GLItem glItem = payment.getPaymentMethod().getCashDifferences();
    FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    transaction.setCurrency(account.getCurrency());
    transaction.setAccount(account);
    transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
    transaction.setGLItem(glItem);
    if (difference.compareTo(BigDecimal.ZERO) < 0) {
      transaction.setPaymentAmount(difference.abs());
      account.setCurrentBalance(account.getCurrentBalance().subtract(difference.abs()));
    } else {
      transaction.setDepositAmount(difference);
      account.setCurrentBalance(account.getCurrentBalance().add(difference));
    }
    transaction.setProcessed(true);
    transaction.setTransactionType("BPW");
    transaction.setStatus("RPPC");
    transaction.setDescription("GL Item: " + glItem.getName());
    transaction.setTransactionDate(new Date());
    transaction.setReconciliation(reconciliation);

    return transaction;
  }

  protected FIN_FinaccTransaction createTotalTransferTransactionPayment(OBPOSApplications terminal,
      FIN_Reconciliation reconciliation, OBPOSAppPayment paymentType, BigDecimal reconciliationTotal) {
    TerminalTypePaymentMethod paymentMethod = paymentType.getPaymentMethod();
    FIN_FinancialAccount account = paymentType.getFinancialAccount();
    GLItem glItem = paymentMethod.getGLItemForDrops();
    FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    transaction.setCurrency(account.getCurrency());
    transaction.setAccount(account);
    transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
    transaction.setGLItem(glItem);
    transaction.setPaymentAmount(reconciliationTotal);
    transaction.setProcessed(true);
    transaction.setTransactionType("BPW");
    transaction.setStatus("RPPC");
    transaction.setDescription("GL Item: " + glItem.getName());
    transaction.setTransactionDate(new Date());
    transaction.setReconciliation(reconciliation);

    account.setCurrentBalance(account.getCurrentBalance().subtract(reconciliationTotal));

    return transaction;

  }

  protected FIN_FinaccTransaction createTotalTransferTransactionDeposit(OBPOSApplications terminal,
      FIN_Reconciliation reconciliation, OBPOSAppPayment paymentType, BigDecimal reconciliationTotal) {
    GLItem glItem = paymentType.getPaymentMethod().getGLItemForDeposits();
    if (paymentType.getObretcoCmevents() == null) {
      throw new OBException("There is no close event defined for the payment method");
    }
    FIN_FinancialAccount account = paymentType.getObretcoCmevents().getFinancialAccount();
    FIN_FinaccTransaction transaction = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    transaction.setCurrency(account.getCurrency());
    transaction.setAccount(account);
    transaction.setLineNo(TransactionsDao.getTransactionMaxLineNo(account) + 10);
    transaction.setGLItem(glItem);
    transaction.setDepositAmount(reconciliationTotal);
    transaction.setProcessed(true);
    transaction.setTransactionType("BPW");
    transaction.setStatus("RDNC");
    transaction.setDescription("GL Item: " + glItem.getName());
    transaction.setTransactionDate(new Date());

    account.setCurrentBalance(account.getCurrentBalance().add(reconciliationTotal));

    return transaction;

  }
}
