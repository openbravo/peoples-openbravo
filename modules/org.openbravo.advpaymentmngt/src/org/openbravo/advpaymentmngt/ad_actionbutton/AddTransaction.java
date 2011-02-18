/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.xmlEngine.XmlDocument;

public class AddTransaction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private AdvPaymentMngtDao dao;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "AddTransaction|Org");
      String strWindowId = vars.getRequestGlobalVariable("inpwindowId", "AddTransaction|windowId");
      String strTabId = vars.getRequestGlobalVariable("inpTabId", "AddTransaction|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);

      printPage(response, vars, strOrgId, strWindowId, strTabId, strFinancialAccountId,
          strFinBankStatementLineId);

    } else if (vars.commandIn("GRID")) {
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      boolean strIsReceipt = "RCIN".equals(vars.getStringParameter("inpDocumentType"));
      String strFromDate = vars.getStringParameter("inpDateFrom");
      String strToDate = vars.getStringParameter("inpDateTo");

      printGrid(response, strFinancialAccountId, strFromDate, strToDate, strIsReceipt);

    } else if (vars.commandIn("SAVE")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "AddTransaction|tabId");
      String strFinancialAccountId = vars.getStringParameter("inpFinFinancialAccountId", "");
      String selectedPaymentsIds = vars.getInParameter("inpPaymentId", IsIDFilter.instance);
      String strTransactionType = vars.getStringParameter("inpTransactionType");
      String strTransactionDate = vars.getStringParameter("inpMainDate", "");

      String strGLItemId = vars.getStringParameter("inpGLItemId", "");
      String strGLItemDebitAmount = vars.getStringParameter("inpDebitAmountGLItem", "");
      String strGLItemCreditAmount = vars.getStringParameter("inpCreditAmountGLItem", "");

      String strFeeDebitAmount = vars.getStringParameter("inpDebitAmount", "");
      String strFeeCreditAmount = vars.getStringParameter("inpCreditAmount", "");

      String strFinBankStatementLineId = vars.getStringParameter("inpFinBankStatementLineId", "",
          IsIDFilter.instance);

      saveAndCloseWindow(response, vars, strTabId, strFinancialAccountId, selectedPaymentsIds,
          strTransactionType, strGLItemId, strGLItemDebitAmount, strGLItemCreditAmount,
          strFeeDebitAmount, strFeeCreditAmount, strTransactionDate, strFinBankStatementLineId);
    }

  }

  private void saveAndCloseWindow(HttpServletResponse response, VariablesSecureApp vars,
      String strTabId, String strFinancialAccountId, String selectedPaymentIds,
      String strTransactionType, String strGLItemId, String strGLItemDebitAmount,
      String strGLItemCreditAmount, String strFeeDebitAmount, String strFeeCreditAmount,
      String strTransactionDate, String strFinBankStatementLineId) throws IOException,
      ServletException {

    dao = new AdvPaymentMngtDao();
    String strMessage = "";
    OBError msg = new OBError();
    OBContext.setAdminMode();
    try {
      // SALES = DEPOSIT = DEBIT
      // PURCHASE = PAYMENT = CREDIT
      if (strTransactionType.equals("P")) { // Payment

        List<FIN_Payment> selectedPayments = FIN_Utility.getOBObjectList(FIN_Payment.class,
            selectedPaymentIds);

        for (FIN_Payment p : selectedPayments) {
          BigDecimal debitAmt = BigDecimal.ZERO;
          BigDecimal creditAmt = BigDecimal.ZERO;

          if (p.isReceipt()) {
            if (p.getAmount().compareTo(BigDecimal.ZERO) == -1)
              creditAmt = p.getAmount().abs();
            else
              debitAmt = p.getAmount();
          } else {
            if (p.getAmount().compareTo(BigDecimal.ZERO) == -1)
              debitAmt = p.getAmount().abs();
            else
              creditAmt = p.getAmount();
          }
          String description = null;
          if (p.getDescription() != null) {
            description = p.getDescription().replace("\n", ". ");
          }

          FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(p.getOrganization(), p
              .getCurrency(), p.getAccount(), TransactionsDao.getTransactionMaxLineNo(p
              .getAccount()) + 10, p, description, FIN_Utility.getDate(strTransactionDate), null, p
              .isReceipt() ? "RDNC" : "PWNC", debitAmt, creditAmt, null, null, null,
              p.isReceipt() ? "BPD" : "BPW", FIN_Utility.getDate(strTransactionDate));

          TransactionsDao.process(finTrans);
          if (!"".equals(strFinBankStatementLineId)) {
            FIN_Reconciliation reconciliation = MatchTransactionDao.getReconciliationPending();
            FIN_BankStatementLine bsline = dao.getObject(FIN_BankStatementLine.class,
                strFinBankStatementLineId);
            bsline.setMatchingtype("AD");
            bsline.setFinancialAccountTransaction(finTrans);
            if (finTrans.getFinPayment() != null)
              bsline.setBusinessPartner(finTrans.getFinPayment().getBusinessPartner());
            finTrans.setReconciliation(reconciliation);
            OBDal.getInstance().save(bsline);
            OBDal.getInstance().save(finTrans);
            OBDal.getInstance().flush();
          }
        }

        if (selectedPaymentIds != null && selectedPayments.size() > 0) {
          strMessage = selectedPayments.size() + " " + "@RowsInserted@";
        }

      } else if (strTransactionType.equals("GL")) { // GL Item
        BigDecimal glItemDebitAmt = new BigDecimal(strGLItemDebitAmount);
        BigDecimal glItemCreditAmt = new BigDecimal(strGLItemCreditAmount);

        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        GLItem glItem = OBDal.getInstance().get(GLItem.class, strGLItemId);
        String description = Utility.messageBD(this, "APRM_GLItem", vars.getLanguage()) + ": "
            + glItem.getName();
        boolean isReceipt = (glItemDebitAmt.compareTo(glItemCreditAmt) >= 0);

        // Currency, Organization, paymentDate,
        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(account.getOrganization(),
            account.getCurrency(), account, TransactionsDao.getTransactionMaxLineNo(account) + 10,
            null, description, FIN_Utility.getDate(strTransactionDate), glItem, isReceipt ? "RDNC"
                : "PWNC", glItemDebitAmt, glItemCreditAmt, null, null, null, isReceipt ? "BPD"
                : "BPW", FIN_Utility.getDate(strTransactionDate));

        TransactionsDao.process(finTrans);
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {
          FIN_Reconciliation reconciliation = MatchTransactionDao.getReconciliationPending();
          FIN_BankStatementLine bsline = dao.getObject(FIN_BankStatementLine.class,
              strFinBankStatementLineId);
          bsline.setMatchingtype("AD");
          bsline.setFinancialAccountTransaction(finTrans);
          finTrans.setReconciliation(reconciliation);
          OBDal.getInstance().save(bsline);
          OBDal.getInstance().save(finTrans);
          OBDal.getInstance().flush();
        }

      } else if (strTransactionType.equals("F")) { // Fee
        BigDecimal feeDebitAmt = new BigDecimal(strFeeDebitAmount);
        BigDecimal feeCreditAmt = new BigDecimal(strFeeCreditAmount);
        FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
            strFinancialAccountId);
        boolean isReceipt = (feeDebitAmt.compareTo(feeCreditAmt) >= 0);

        FIN_FinaccTransaction finTrans = dao.getNewFinancialTransaction(account.getOrganization(),
            account.getCurrency(), account, TransactionsDao.getTransactionMaxLineNo(account) + 10,
            null, Utility.messageBD(this, "APRM_BankFee", vars.getLanguage()), FIN_Utility
                .getDate(strTransactionDate), null, isReceipt ? "RDNC" : "PWNC", feeDebitAmt,
            feeCreditAmt, null, null, null, "BF", FIN_Utility.getDate(strTransactionDate));

        TransactionsDao.process(finTrans);
        strMessage = "1 " + "@RowsInserted@";
        if (!"".equals(strFinBankStatementLineId)) {
          FIN_Reconciliation reconciliation = MatchTransactionDao.getReconciliationPending();
          FIN_BankStatementLine bsline = dao.getObject(FIN_BankStatementLine.class,
              strFinBankStatementLineId);
          bsline.setMatchingtype("AD");
          bsline.setFinancialAccountTransaction(finTrans);
          finTrans.setReconciliation(reconciliation);
          OBDal.getInstance().save(bsline);
          OBDal.getInstance().save(finTrans);
          OBDal.getInstance().flush();
        }

      }

      // Message
      msg.setType("Success");
      msg.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), strMessage));
      vars.setMessage(strTabId, msg);
      msg = null;
      if ("".equals(strFinBankStatementLineId))
        printPageClosePopUpAndRefreshParent(response, vars);
      else {
        log4j.debug("Output: PopUp Response");
        final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/base/secureApp/PopUp_Close_Refresh").createXmlDocument();
        xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
        response.setContentType("text/html; charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
      }

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strWindowId, String strTabId, String strFinancialAccountId,
      String strBankStatementLineId) throws IOException, ServletException {

    log4j.debug("Output: Add Transaction pressed on Financial Account || Transaction tab");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddTransaction").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("mainDate", DateTimeData.today(this));
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", strOrgId);
    xmlDocument.setParameter("finFinancialAccountId", strFinancialAccountId);
    xmlDocument.setParameter("finBankStatementLineId", strBankStatementLineId);

    xmlDocument.setParameter("debitAmount", BigDecimal.ZERO.toString());
    xmlDocument.setParameter("creditAmount", BigDecimal.ZERO.toString());
    xmlDocument.setParameter("debitAmountGLItem", BigDecimal.ZERO.toString());
    xmlDocument.setParameter("creditAmountGLItem", BigDecimal.ZERO.toString());

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGrid(HttpServletResponse response, String strFinancialAccountId,
      String strFromDate, String strToDate, boolean isReceipt) throws IOException, ServletException {
    dao = new AdvPaymentMngtDao();

    log4j.debug("Output: Grid with transactions not reconciled");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/AddTransactionGrid").createXmlDocument();

    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);

    // Payments not deposited/withdrawal
    // Not stored in Fin_Finacc_Transaction table
    final FieldProvider[] data = dao.getPaymentsNotDeposited(account, FIN_Utility
        .getDate(strFromDate), FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strToDate, "1")),
        isReceipt);

    xmlDocument.setData("structure", (data == null) ? set() : data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();
    empty.put("paymentId", "");
    empty.put("paymentInfo", "");
    empty.put("paymentDescription", "");
    empty.put("paymentDate", "");
    empty.put("debitAmount", "");
    empty.put("creditAmount", "");
    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  public String getServletInfo() {
    return "This servlet adds transaction for a financial account";
  }

}
