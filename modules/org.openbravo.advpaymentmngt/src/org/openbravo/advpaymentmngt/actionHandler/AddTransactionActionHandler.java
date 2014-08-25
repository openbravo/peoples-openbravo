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

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_TransactionProcess;
import org.openbravo.advpaymentmngt.utility.APRM_MatchingUtility;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.UserDimension1;
import org.openbravo.model.financialmgmt.accounting.UserDimension2;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddTransactionActionHandler extends BaseProcessActionHandler {
  final private static Logger log = LoggerFactory.getLogger(AddPaymentActionHandler.class);
  private AdvPaymentMngtDao dao;

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonResponse = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      JSONObject request = new JSONObject(content);
      JSONObject params = request.getJSONObject("_params");
      String strFinBankStatementLineId = params.getString("bankStatementLineId");

      String strTabId = request.getString("inpTabId");
      String strFinancialAccountId = request.getString("Fin_Financial_Account_ID");

      String strTransactionType = params.getString("trxtype");
      String strTransactionDate = params.getString("trxdate");
      Date transactionDate = JsonUtils.createDateFormat().parse(strTransactionDate);

      String selectedPaymentsIds = "";
      if (params.has("fin_payment_id")) {
        selectedPaymentsIds = params.getString("fin_payment_id");
      }
      String strGLItemId = "";
      if (params.has("c_glitem_id")) {
        strGLItemId = params.getString("c_glitem_id");
      }

      String strDepositAmount = params.getString("depositamt");
      String strWithdrawalamt = params.getString("withdrawalamt");

      String strDescription = "";
      if (params.has("description")) {
        strDescription = params.getString("description");
      }
      createTransaction(strTabId, strFinancialAccountId, selectedPaymentsIds, strTransactionType,
          strGLItemId, transactionDate, strFinBankStatementLineId, strDepositAmount,
          strWithdrawalamt, strDescription, params);

    } catch (Exception e) {
      log.error("Error in process", e);
    }
    return jsonResponse;
  }

  private void createTransaction(String strTabId, String strFinancialAccountId,
      String selectedPaymentsIds, String strTransactionType, String strGLItemId,
      Date transactionDate, String strFinBankStatementLineId, String strDepositAmount,
      String strWithdrawalamt, String strDescription, JSONObject params) {
    dao = new AdvPaymentMngtDao();
    String strMessage = "";
    OBError msg = new OBError();
    OBContext.setAdminMode();
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    ConnectionProvider conn = new DalConnectionProvider(true);
    try {
      final FIN_BankStatementLine bankStatementLine = OBDal.getInstance().get(
          FIN_BankStatementLine.class, strFinBankStatementLineId);

      // SALES = DEPOSIT
      // PURCHASE = PAYMENT
      if (!strTransactionType.equals("BF") && (!selectedPaymentsIds.equals("null"))) { // Payment

        List<FIN_Payment> selectedPayments = FIN_Utility.getOBObjectList(FIN_Payment.class,
            selectedPaymentsIds);

        for (FIN_Payment p : selectedPayments) {
          BigDecimal depositAmt = FIN_Utility.getDepositAmount(p.isReceipt(),
              p.getFinancialTransactionAmount());
          BigDecimal paymentAmt = FIN_Utility.getPaymentAmount(p.isReceipt(),
              p.getFinancialTransactionAmount());

          String description = null;
          if (p.getDescription() != null) {
            description = p.getDescription().replace("\n", ". ");
          }

          FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(
              p.getOrganization(),
              OBDal.getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId),
              TransactionsDao.getTransactionMaxLineNo(OBDal.getInstance().get(
                  FIN_FinancialAccount.class, strFinancialAccountId)) + 10, p, description,
              transactionDate, null, p.isReceipt() ? "RDNC" : "PWNC", depositAmt, paymentAmt, null,
              null, null, p.isReceipt() ? "BPD" : "BPW", transactionDate, p.getCurrency(),
              p.getFinancialTransactionConvertRate(), p.getAmount());
          OBError processTransactionError = processTransaction(vars, conn, "P", finTrans);
          if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
            throw new OBException(processTransactionError.getMessage());
          }
          if (!"".equals(strFinBankStatementLineId)) {
            FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(OBDal
                .getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId), "N");
            APRM_MatchingUtility.matchBankStatementLine(bankStatementLine, finTrans,
                reconciliation, null);
          }
        }

        if (selectedPaymentsIds != null && selectedPayments.size() > 0) {
          strMessage = selectedPayments.size() + " " + "@RowsInserted@";
        }

      }
      if (!strTransactionType.equals("BF") && (!strGLItemId.equals("null"))) {// GL item
        // Accounting Dimensions
        final String strElement_OT = params.getString("ad_org_id");
        final Organization organization = OBDal.getInstance()
            .get(Organization.class, strElement_OT);

        final String strElement_BP = params.getString("c_bpartner_id");
        final BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
            strElement_BP);

        final String strElement_PR = params.getString("m_product_id");
        final Product product = OBDal.getInstance().get(Product.class, strElement_PR);

        final String strElement_PJ = params.getString("c_project_id");
        final Project project = OBDal.getInstance().get(Project.class, strElement_PJ);

        final String strElement_AY = params.getString("c_activity_id");
        final ABCActivity activity = OBDal.getInstance().get(ABCActivity.class, strElement_AY);

        final String strElement_SR = params.getString("c_salesregion_id");
        final SalesRegion salesRegion = OBDal.getInstance().get(SalesRegion.class, strElement_SR);

        final String strElement_MC = params.getString("c_campaign_id");
        final Campaign campaign = OBDal.getInstance().get(Campaign.class, strElement_MC);

        final String strElement_U1 = params.getString("user1_id");
        final UserDimension1 user1 = OBDal.getInstance().get(UserDimension1.class, strElement_U1);

        final String strElement_U2 = params.getString("user2_id");
        final UserDimension2 user2 = OBDal.getInstance().get(UserDimension2.class, strElement_U2);

        final String strElement_CC = params.getString("c_costcenter_id");
        final Costcenter costcenter = OBDal.getInstance().get(Costcenter.class, strElement_CC);

        BigDecimal glItemDepositAmt = new BigDecimal(strDepositAmount);
        BigDecimal glItemPaymentAmt = new BigDecimal(strWithdrawalamt);

        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        GLItem glItem = OBDal.getInstance().get(GLItem.class, strGLItemId);
        String description = strDescription.isEmpty() ? Utility.messageBD(conn, "APRM_GLItem",
            vars.getLanguage())
            + ": " + glItem.getName() : strDescription;
        boolean isReceipt = (glItemDepositAmt.compareTo(glItemPaymentAmt) >= 0);

        // Currency, Organization, paymentDate,
        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(organization, account,
            TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            transactionDate, glItem, isReceipt ? "RDNC" : "PWNC", glItemDepositAmt,
            glItemPaymentAmt, project, campaign, activity, isReceipt ? "BPD" : "BPW",
            transactionDate, null, null, null, businessPartner, product, salesRegion, user1, user2,
            costcenter);
        OBError processTransactionError = processTransaction(vars, conn, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {

          FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(OBDal
              .getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId), "N");
          APRM_MatchingUtility.matchBankStatementLine(bankStatementLine, finTrans, reconciliation,
              null);
        }
      }
      if (strTransactionType.equals("BF")) { // Bank Fee
        BigDecimal feeDepositAmt = new BigDecimal(strDepositAmount);
        BigDecimal feePaymentAmt = new BigDecimal(strWithdrawalamt);
        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        boolean isReceipt = (feeDepositAmt.compareTo(feePaymentAmt) >= 0);
        String description = strDescription.isEmpty() ? Utility.messageBD(conn, "APRM_BankFee",
            vars.getLanguage()) : strDescription;

        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(account.getOrganization(),
            account, TransactionsDao.getTransactionMaxLineNo(account) + 10, null, description,
            transactionDate, null, isReceipt ? "RDNC" : "PWNC", feeDepositAmt, feePaymentAmt, null,
            null, null, "BF", transactionDate, null, null, null);
        OBError processTransactionError = processTransaction(vars, conn, "P", finTrans);
        if (processTransactionError != null && "Error".equals(processTransactionError.getType())) {
          throw new OBException(processTransactionError.getMessage());
        }
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {
          FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(OBDal
              .getInstance().get(FIN_FinancialAccount.class, strFinancialAccountId), "N");
          APRM_MatchingUtility.matchBankStatementLine(bankStatementLine, finTrans, reconciliation,
              null);
        }
      }

    } catch (Exception e) {
      OBError newError = Utility.translateError(conn, vars, vars.getLanguage(),
          FIN_Utility.getExceptionMessage(e));
      throw new OBException(newError.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * It calls the Transaction Process for the given transaction and action.
   * 
   * @param vars
   *          VariablesSecureApp with the session data.
   * @param conn
   *          ConnectionProvider with the connection being used.
   * @param strAction
   *          String with the action of the process. {P, D, R}
   * @param transaction
   *          FIN_FinaccTransaction that needs to be processed.
   * @return a OBError with the result message of the process.
   * @throws Exception
   */
  private OBError processTransaction(VariablesSecureApp vars, ConnectionProvider conn,
      String strAction, FIN_FinaccTransaction transaction) throws Exception {
    ProcessBundle pb = new ProcessBundle("F68F2890E96D4D85A1DEF0274D105BCE", vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("action", strAction);
    parameters.put("Fin_FinAcc_Transaction_ID", transaction.getId());
    pb.setParams(parameters);
    OBError myMessage = null;
    new FIN_TransactionProcess().execute(pb);
    myMessage = (OBError) pb.getResult();
    return myMessage;
  }

}