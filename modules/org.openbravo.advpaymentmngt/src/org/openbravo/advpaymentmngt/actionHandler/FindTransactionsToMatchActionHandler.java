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

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.utility.APRM_MatchingUtility;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindTransactionsToMatchActionHandler extends BaseActionHandler {
  private static Logger log = LoggerFactory.getLogger(FindTransactionsToMatchActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    JSONObject errorMessage = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      final JSONObject jsonData = new JSONObject(data);
      final JSONObject params = jsonData.getJSONObject("_params");
      final JSONArray selection = params.getJSONObject("findtransactiontomatch").getJSONArray(
          "_selection");

      if (selection.length() > 0) {
        final String strBankLineId = params.getString("bankStatementLineId");
        final String strSelectedTransactionId = selection.getJSONObject(0).getString("id");

        final FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            jsonData.getString("inpfinFinancialAccountId"));
        final FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(account,
            "N");
        final FIN_BankStatementLine bankStatementLine = OBDal.getInstance().get(
            FIN_BankStatementLine.class, strBankLineId);
        final FIN_FinaccTransaction transaction = OBDal.getInstance().get(
            FIN_FinaccTransaction.class, strSelectedTransactionId);
        APRM_MatchingUtility.matchBankStatementLine(bankStatementLine, transaction, reconciliation,
            null);

      } else {
        // FIXME try to control this from the UI (disable Done if no record is selected)
        JSONArray actions = new JSONArray();
        JSONObject msg = new JSONObject();
        msg.put("msgType", "error");
        msg.put("msgTitle", "Error");
        msg.put("msgText", "No record selected");
        msg.put("force", true);
        JSONObject msgTotalAction = new JSONObject();
        msgTotalAction.put("showMsgInProcessView", msg);
        actions.put(msgTotalAction);

        result.put("responseActions", actions);
        result.put("retryExecution", true);
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      try {
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", "Error");
        errorMessage.put("text", message);
        result.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

}
