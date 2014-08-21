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

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.APRM_MatchingUtility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnMatchTransactionActionHandler extends BaseActionHandler {
  private static Logger log = LoggerFactory.getLogger(UnMatchTransactionActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    JSONObject errorMessage = new JSONObject();
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
            .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId());
    ConnectionProvider conn = new DalConnectionProvider();
    OBContext.setAdminMode(true);
    FIN_Reconciliation reconciliation = null;
    try {
      final JSONObject jsonData = new JSONObject(data);
      String strBankStatementLineId = jsonData.getString("bankStatementLineId");
      FIN_BankStatementLine bsline = OBDal.getInstance().get(FIN_BankStatementLine.class,
          strBankStatementLineId);

      reconciliation = bsline.getFinancialAccountTransaction().getReconciliation();
      if (reconciliation != null) {
        if (reconciliation.isProcessNow()) {
          APRM_MatchingUtility.wait(reconciliation);
        }
        reconciliation.setProcessNow(true);
        OBDal.getInstance().save(reconciliation);
        OBDal.getInstance().flush();
        OBDal.getInstance().getConnection().commit();
      }

      APRM_MatchingUtility.unmatch(bsline);

      final String strNewRefundPaymentMessage = OBMessageUtils
          .parseTranslation("@APRM_SuccessfullUnmatch@");

      errorMessage.put("severity", "success");
      errorMessage.put("title", "Success");
      errorMessage.put("text", strNewRefundPaymentMessage);
      result.put("message", errorMessage);
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
      reconciliation.setProcessNow(false);
      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();
    }
    return result;
  }
}