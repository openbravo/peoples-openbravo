/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.actionHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.utility.APRM_MatchingUtility;
import org.openbravo.advpaymentmngt.utility.FIN_MatchedTransaction;
import org.openbravo.advpaymentmngt.utility.FIN_MatchingTransaction;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLineTemp;
import org.openbravo.model.financialmgmt.payment.FIN_ReconciliationLine_v;
import org.openbravo.model.financialmgmt.payment.MatchingAlgorithm;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchStatementOnLoadActionHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory
      .getLogger(MatchStatementOnLoadActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonResponse = new JSONObject();
    OBContext.setAdminMode(true);
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
            .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId());
    ConnectionProvider conn = new DalConnectionProvider();
    String strReconciliationId = "";
    try {
      JSONObject context = null;
      if (parameters.get("context") != null) {
        context = new JSONObject((String) parameters.get("context"));
      }
      String strFinancialAccountId = context.getString("inpfinFinancialAccountId");
      FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
          strFinancialAccountId);
      FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(OBDal.getInstance()
          .get(FIN_FinancialAccount.class, strFinancialAccountId), "N");
      boolean executeMatching = "true".equals(parameters.get("executeMatching")) ? true : false;
      int reconciledItems = 0;
      if (reconciliation != null) {
        strReconciliationId = reconciliation.getId();
        if (reconciliation.isProcessNow()) {
          APRM_MatchingUtility.wait(reconciliation);
        }
        reconciliation.setProcessNow(true);
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();
        OBDal.getInstance().getConnection().commit();

        List<FIN_FinaccTransaction> mixedLines = APRM_MatchingUtility
            .getManualReconciliationLines(reconciliation);
        if (mixedLines.size() > 0) {
          // Fix mixing Reconciliation and log the issue
          log.warn("Mixing Reconciliations: An error occured which left an inconsistent status for the current reconciliation: "
              + reconciliation.getIdentifier());
          OBContext.setAdminMode(false);
          try {
            for (FIN_FinaccTransaction mixedLine : mixedLines) {
              APRM_MatchingUtility.fixMixedLine(mixedLine);
              log.warn("Fixing Mixed Line (transaction appears as cleared but no bank statement line is linked to it): "
                  + mixedLine.getLineNo() + " - " + mixedLine.getIdentifier());
            }
            OBDal.getInstance().flush();
          } finally {
            OBContext.restorePreviousMode();
          }
        }
        // Check if problem remains
        mixedLines = APRM_MatchingUtility.getManualReconciliationLines(reconciliation);
        if (mixedLines.size() > 0) {
          OBDal.getInstance().rollbackAndClose();
          OBError message = Utility.translateError(conn, vars, vars.getLanguage(), Utility
              .parseTranslation(conn, vars, vars.getLanguage(), "@APRM_ReconciliationMixed@"));
          jsonResponse = new JSONObject();
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", message.getType().toLowerCase());
          errorMessage.put("title", message.getTitle());
          errorMessage.put("text", message.getMessage());
          jsonResponse.put("message", errorMessage);
          return jsonResponse;
        }
        OBContext.setAdminMode();
        try {
          getSnapShot(reconciliation);
          reconciledItems = reconciliation.getFINReconciliationLineVList().size();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
      if (MatchTransactionDao.getUnMatchedBankStatementLines(account).size() == 0
          && reconciledItems == 0) {
        OBError message = Utility.translateError(conn, vars, vars.getLanguage(),
            Utility.parseTranslation(conn, vars, vars.getLanguage(), "@APRM_NoStatementsToMatch@"));
        jsonResponse = new JSONObject();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", message.getType().toLowerCase());
        errorMessage.put("title", message.getTitle());
        errorMessage.put("text", message.getMessage());
        jsonResponse.put("message", errorMessage);
        return jsonResponse;
      } else {
        if (reconciliation == null) {
          reconciliation = MatchTransactionDao.addNewReconciliation(conn, vars,
              strFinancialAccountId);
          strReconciliationId = reconciliation.getId();
          if (reconciliation.isProcessNow()) {
            APRM_MatchingUtility.wait(reconciliation);
          }
          reconciliation.setProcessNow(true);
          OBDal.getInstance().save(reconciliation);
          OBDal.getInstance().flush();
          OBDal.getInstance().getConnection().commit();
          getSnapShot(reconciliation);
        } else {
          updateReconciliation(conn, vars, reconciliation.getId(), strFinancialAccountId, false);
        }
      }

      FIN_FinancialAccount financial = new AdvPaymentMngtDao().getObject(
          FIN_FinancialAccount.class, strFinancialAccountId);
      MatchingAlgorithm ma = financial.getMatchingAlgorithm();
      FIN_MatchingTransaction matchingTransaction = new FIN_MatchingTransaction(
          ma.getJavaClassName());
      List<FIN_BankStatementLine> bankLines = MatchTransactionDao.getMatchingBankStatementLines(
          strFinancialAccountId, strReconciliationId, "ALL", "N");
      List<FIN_FinaccTransaction> excluded = new ArrayList<FIN_FinaccTransaction>();

      // Change for scrollable results (update the previous method to return a scrollable results)
      for (FIN_BankStatementLine bankStatementLine : bankLines) {
        // boolean alreadyMatched = false;
        // String matchingType = bankStatementLine.getMatchingtype();
        FIN_FinaccTransaction transaction = bankStatementLine.getFinancialAccountTransaction();
        if (transaction == null) {
          FIN_MatchedTransaction matched = null;
          if (executeMatching) {
            // try to match if exception is thrown continue
            try {
              // long initMatchLine = System.currentTimeMillis();
              matched = matchingTransaction.match(bankStatementLine, excluded);
              // initMatch = initMatch + (System.currentTimeMillis() - initMatchLine);
              OBDal.getInstance().getConnection().commit();
            } catch (Exception e) {
              OBDal.getInstance().rollbackAndClose();
              bankStatementLine = OBDal.getInstance().get(FIN_BankStatementLine.class,
                  bankStatementLine.getId());
              // matchingType = bankStatementLine.getMatchingtype();
              transaction = bankStatementLine.getFinancialAccountTransaction();
              matched = new FIN_MatchedTransaction(null, FIN_MatchedTransaction.NOMATCH);
            }
          } else {
            matched = new FIN_MatchedTransaction(null, FIN_MatchedTransaction.NOMATCH);
          }
          transaction = matched.getTransaction();
          if (transaction != null) {
            APRM_MatchingUtility.matchBankStatementLine(bankStatementLine.getId(),
                transaction.getId(), strReconciliationId, matched.getMatchLevel());
          }
          // When hide flag checked then exclude matchings for transactions out of date range
          // if ("Y".equals(strHideDate)
          // && matched.getTransaction() != null
          // && matched.getTransaction().getTransactionDate()
          // .compareTo(reconciliation.getEndingDate()) > 0) {
          // transaction = null;
          // matched = new FIN_MatchedTransaction(null, FIN_MatchedTransaction.NOMATCH);
          // unmatch(bankStatementLine);
          // bankStatementLine = OBDal.getInstance().get(FIN_BankStatementLine.class,
          // bankStatementLine.getId());
          // }
          if (transaction != null) {
            excluded.add(transaction);
          }
          // matchingType = matched.getMatchLevel();
        }
        // else {
        // alreadyMatched = true;
        // }
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("Exception handling the match statement", e);

      try {
        jsonResponse = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonResponse.put("message", errorMessage);

      } catch (Exception ignore) {
      }
    } finally {
      OBContext.restorePreviousMode();
      FIN_Reconciliation reconciliation = OBDal.getInstance().get(FIN_Reconciliation.class,
          strReconciliationId);
      reconciliation.setProcessNow(false);
      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();
    }
    return jsonResponse;
  }

  private boolean updateReconciliation(ConnectionProvider conn, VariablesSecureApp vars,
      String strReconciliationId, String strFinancialAccountId, boolean process) {
    OBContext.setAdminMode(true);
    try {
      FIN_Reconciliation reconciliation = MatchTransactionDao.getObject(FIN_Reconciliation.class,
          strReconciliationId);
      FIN_FinancialAccount financialAccount = MatchTransactionDao.getObject(
          FIN_FinancialAccount.class, strFinancialAccountId);
      if (MatchTransactionDao.islastreconciliation(reconciliation)) {
        Date maxBSLDate = MatchTransactionDao.getBankStatementLineMaxDate(financialAccount);
        reconciliation.setEndingDate(maxBSLDate);
        reconciliation.setTransactionDate(maxBSLDate);
      } else {
        Date maxClearItemDate = getClearItemsMaxDate(reconciliation);
        reconciliation.setEndingDate(maxClearItemDate);
        reconciliation.setTransactionDate(maxClearItemDate);
      }
      reconciliation.setEndingBalance(MatchTransactionDao.getEndingBalance(reconciliation));

      if (!process) {
        reconciliation.setProcessed(false);
        reconciliation.setDocumentStatus("DR");
        reconciliation.setAPRMProcessReconciliation("P");
        reconciliation.setAprmProcessRec("P");
      }
      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();
      if (process) {
        // Process Reconciliation
        OBError myError = APRM_MatchingUtility.processReconciliation(conn, vars, "P",
            reconciliation);
        if (myError != null && myError.getType().equalsIgnoreCase("error")) {
          throw new OBException(myError.getMessage());
        }
      }
    } catch (Exception ex) {
      OBError menssage = Utility.translateError(conn, vars, vars.getLanguage(), ex.getMessage());
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return true;
  }

  private Date getClearItemsMaxDate(FIN_Reconciliation reconciliation) {
    OBCriteria<FIN_ReconciliationLine_v> obc = OBDal.getInstance().createCriteria(
        FIN_ReconciliationLine_v.class);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.add(Restrictions.eq(FIN_ReconciliationLine_v.PROPERTY_RECONCILIATION, reconciliation));
    obc.addOrder(Order.desc(FIN_ReconciliationLine_v.PROPERTY_TRANSACTIONDATE));
    obc.setMaxResults(1);
    return ((FIN_ReconciliationLine_v) obc.uniqueResult()).getTransactionDate();
  }

  private void getSnapShot(FIN_Reconciliation reconciliation) {
    if (reconciliation == null) {
      return;
    }
    OBContext.setAdminMode();
    try {
      // First remove old temp info if exists
      List<FIN_ReconciliationLineTemp> oldTempLines = reconciliation
          .getFINReconciliationLineTempList();
      for (FIN_ReconciliationLineTemp oldtempLine : oldTempLines) {
        OBDal.getInstance().remove(oldtempLine);
      }
      oldTempLines.clear();
      OBDal.getInstance().flush();
      // Now copy info taken from the reconciliation when first opened
      List<FIN_ReconciliationLine_v> reconciledlines = reconciliation
          .getFINReconciliationLineVList();
      for (FIN_ReconciliationLine_v reconciledLine : reconciledlines) {
        FIN_ReconciliationLineTemp lineTemp = OBProvider.getInstance().get(
            FIN_ReconciliationLineTemp.class);
        lineTemp.setClient(reconciledLine.getClient());
        lineTemp.setOrganization(reconciledLine.getOrganization());
        lineTemp.setReconciliation(reconciledLine.getReconciliation());
        lineTemp.setBankStatementLine(reconciledLine.getBankStatementLine());
        if (reconciledLine.getFinancialAccountTransaction() != null
            && reconciledLine.getFinancialAccountTransaction().isCreatedByAlgorithm()) {
          if (reconciledLine.getFinancialAccountTransaction().getFinPayment() != null
              && !reconciledLine.getFinancialAccountTransaction().getFinPayment()
                  .isCreatedByAlgorithm()) {
            lineTemp.setPayment(reconciledLine.getPayment());
          } else if (reconciledLine.getFinancialAccountTransaction() != null
              && reconciledLine.getFinancialAccountTransaction().getFinPayment() != null
              && reconciledLine.getFinancialAccountTransaction().getFinPayment()
                  .getFINPaymentDetailList().size() > 0
              && reconciledLine.getFinancialAccountTransaction().getFinPayment()
                  .getFINPaymentDetailList().get(0).getFINPaymentScheduleDetailList() != null
              && reconciledLine.getFinancialAccountTransaction().getFinPayment()
                  .getFINPaymentDetailList().get(0).getFINPaymentScheduleDetailList().size() > 0
              && (reconciledLine.getFinancialAccountTransaction().getFinPayment()
                  .getFINPaymentDetailList().get(0).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule() != null || reconciledLine
                  .getFinancialAccountTransaction().getFinPayment().getFINPaymentDetailList()
                  .get(0).getFINPaymentScheduleDetailList().get(0).getOrderPaymentSchedule() != null)) {
            lineTemp.setPaymentScheduleDetail(reconciledLine.getFinancialAccountTransaction()
                .getFinPayment().getFINPaymentDetailList().get(0).getFINPaymentScheduleDetailList()
                .get(0));
          }
        } else {
          lineTemp.setFinancialAccountTransaction(reconciledLine.getFinancialAccountTransaction());
        }
        if (reconciledLine.getFinancialAccountTransaction().getFinPayment() != null) {
          lineTemp.setPaymentDocumentno(reconciledLine.getFinancialAccountTransaction()
              .getFinPayment().getDocumentNo());
        }
        lineTemp
            .setMatched(reconciledLine.getBankStatementLine().getFinancialAccountTransaction() != null);
        lineTemp.setMatchlevel(reconciledLine.getBankStatementLine().getMatchingtype());
        OBDal.getInstance().save(lineTemp);
      }
      OBDal.getInstance().flush();
      OBDal.getInstance().getSession().clear();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
