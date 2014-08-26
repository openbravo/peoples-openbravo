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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollableResults;
import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.utility.APRM_MatchingUtility;
import org.openbravo.advpaymentmngt.utility.FIN_MatchedTransaction;
import org.openbravo.advpaymentmngt.utility.FIN_MatchingTransaction;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.MatchingAlgorithm;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchStatementOnLoadActionHandler extends BaseActionHandler {
  final private static Logger log = LoggerFactory
      .getLogger(MatchStatementOnLoadActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonResponse = new JSONObject();
    JSONArray actions = new JSONArray();

    String strReconciliationId = null;
    try {
      OBContext.setAdminMode(true);

      final JSONObject context = new JSONObject((String) parameters.get("context"));
      final String strFinancialAccountId = context.getString("Fin_Financial_Account_ID");
      boolean executeAutoMatchingAlgm = "true".equals(parameters.get("executeMatching")) ? true
          : false;

      final FIN_FinancialAccount financialAccount = OBDal.getInstance().get(
          FIN_FinancialAccount.class, strFinancialAccountId);

      /* Get the right reconciliation */
      FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(OBDal.getInstance()
          .get(FIN_FinancialAccount.class, strFinancialAccountId), "N");
      if (reconciliation == null) {
        // Create a new reconciliation
        reconciliation = APRM_MatchingUtility.addNewDraftReconciliation(financialAccount);

      } else {
        // Reuse last reconciliation
        APRM_MatchingUtility.fixMixedLines(reconciliation);
        APRM_MatchingUtility.updateReconciliation(reconciliation, financialAccount, false);
      }
      strReconciliationId = reconciliation.getId();

      if (executeAutoMatchingAlgm) {
        /* Verify we have something left to match */
        if (MatchTransactionDao.getUnMatchedBankStatementLines(financialAccount).isEmpty()) {
          actions = createMessage("@APRM_NoStatementsToMatch@", "warning");
          jsonResponse.put("responseActions", actions);
          return jsonResponse;
        }

        /* Run the automatic matching algorithm */
        int matchedLines = runAutoMatchingAlgorithm(strReconciliationId, strFinancialAccountId,
            financialAccount, reconciliation);
        if (matchedLines > 0) {
          actions = createMessage("@APRM_AutomaticMatchedLines@", "success", matchedLines);
        } else {
          actions = createMessage("@APRM_NoAutomaticMatchedLines@", "warning");
        }
        jsonResponse.put("responseActions", actions);
      }
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("Exception handling the match statement", e);

      try {
        jsonResponse = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        actions = createMessage(ex.getMessage(), "error");
        jsonResponse.put("responseActions", actions);
      } catch (Exception ignore) {
      }
    } finally {
      OBContext.restorePreviousMode();
      APRM_MatchingUtility.setNotProcessingReconciliation(strReconciliationId);
    }

    return jsonResponse;
  }

  private int runAutoMatchingAlgorithm(String strReconciliationId,
      final String strFinancialAccountId, final FIN_FinancialAccount financialAccount,
      FIN_Reconciliation reconciliation) throws InterruptedException, SQLException {
    APRM_MatchingUtility.setProcessingReconciliation(reconciliation);
    final MatchingAlgorithm ma = financialAccount.getMatchingAlgorithm();
    final FIN_MatchingTransaction matchingTransaction = new FIN_MatchingTransaction(
        ma.getJavaClassName());
    final ScrollableResults bankLinesSR = APRM_MatchingUtility
        .getPendingToBeMatchedBankStatementLines(strFinancialAccountId, strReconciliationId);
    final List<FIN_FinaccTransaction> excluded = new ArrayList<FIN_FinaccTransaction>();
    int matchedLines = 0;
    try {
      while (bankLinesSR.next()) {
        final FIN_BankStatementLine bankStatementLine = (FIN_BankStatementLine) bankLinesSR.get(0);

        FIN_MatchedTransaction matched;
        // try to match if exception is thrown continue
        try {
          matched = matchingTransaction.match(bankStatementLine, excluded);
        } catch (Exception e) {
          matched = new FIN_MatchedTransaction(null, FIN_MatchedTransaction.NOMATCH);
        }

        FIN_FinaccTransaction transaction = matched.getTransaction();
        if (transaction != null
            && APRM_MatchingUtility.matchBankStatementLine(bankStatementLine, transaction,
                reconciliation, matched.getMatchLevel())) {
          excluded.add(transaction);
          matchedLines++;
        }
      }
    } finally {
      bankLinesSR.close();
    }

    return matchedLines;
  }

  private JSONArray createMessage(final String messageSearchKey, final String msgType,
      Object... messageParams) throws JSONException {
    final OBError message = OBMessageUtils.translateError(messageSearchKey);

    final JSONObject msg = new JSONObject();
    msg.put("msgType", msgType);
    msg.put("msgTitle", message.getTitle());
    msg.put("msgText", String.format(message.getMessage(), messageParams));
    msg.put("force", true);

    final JSONObject msgTotalAction = new JSONObject();
    msgTotalAction.put("showMsgInProcessView", msg);

    final JSONArray actions = new JSONArray();
    actions.put(msgTotalAction);
    return actions;
  }
}
