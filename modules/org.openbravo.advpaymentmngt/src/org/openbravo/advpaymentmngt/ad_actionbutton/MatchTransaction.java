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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.utility.FIN_MatchedTransaction;
import org.openbravo.advpaymentmngt.utility.FIN_MatchingTransaction;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.MatchingAlgorithm;
import org.openbravo.xmlEngine.XmlDocument;

public class MatchTransaction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "MatchTransaction.adOrgId");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "MatchTransaction.adWindowId");
      String strTabId = vars.getGlobalVariable("inpTabId", "MatchTransaction.adTabId");
      String strFinancialAccountId = vars.getGlobalVariable("inpfinFinancialAccountId",
          "MatchTransaction.finFinancialAccountId");
      String strPaymentTypeFilter = vars.getGlobalVariable("inpPaymentTypeFilter",
          "MatchTransaction.paymentTypeFilter", "ALL");
      String strShowCleared = vars.getGlobalVariable("inpShowCleared",
          "MatchTransaction.showCleared", "N");

      FIN_Reconciliation reconciliation = MatchTransactionDao.getReconciliationPending();
      if (reconciliation == null) {
        reconciliation = MatchTransactionDao
            .addNewReconciliation(this, vars, strFinancialAccountId);
      }

      printPage(response, vars, strOrgId, strWindowId, strTabId, strPaymentTypeFilter,
          strFinancialAccountId, reconciliation.getId(), strShowCleared);
    } else if (vars.commandIn("GRID")) {
      String strFinancialAccountId = vars.getRequestGlobalVariable("inpfinFinancialAccountId",
          "MatchTransaction.finFinancialAccountId");
      String strReconciliationId = vars.getRequestGlobalVariable("inpfinReconciliationId",
          "MatchTransaction.finReconciliationId");
      String strPaymentTypeFilter = vars.getRequestGlobalVariable("inpPaymentTypeFilter",
          "MatchTransaction.paymentTypeFilter");
      String strShowCleared = vars.getRequestGlobalVariable("inpShowCleared",
          "MatchTransaction.showCleared");
      if (strShowCleared.equals("")) {
        strShowCleared = "N";
        vars.setSessionValue("MatchTransaction.showCleared", strShowCleared);
      }

      /*
       * String message = checkReconciliationPending(vars, strReconciliationId, strTabId); if
       * (message != null && message.length() != 0) throw new ServletException(message);
       */

      printGrid(response, vars, strPaymentTypeFilter, strFinancialAccountId, strReconciliationId,
          strShowCleared);
    } else if (vars.commandIn("UNMATCH")) {
      String strUnmatchBankStatementLineId = vars
          .getRequiredStringParameter("inpFinBankStatementLineId");
      unMatchBankStatementLine(response, strUnmatchBankStatementLineId);

    } else if (vars.commandIn("SAVE", "RECONCILE")) {
      OBContext.setAdminMode();
      try {
        String strFinancialAccountId = vars.getRequiredStringParameter("inpfinFinancialAccountId");
        // String strRecords = vars.getRequiredInParameter("inpRecordId", IsIDFilter.instance);
        String strReconciliationId = vars.getRequiredStringParameter("inpfinReconciliationId");
        String strTabId = vars.getGlobalVariable("inpTabId", "MatchTransaction.adTabId");
        String message = "";
        // checkReconciliationPending(vars, strReconciliationId, strTabId);
        if (message == null || message.length() == 0) {

          String strRecordsChecked = vars.getInParameter("inpBankStatementLineId",
              IsIDFilter.instance);
          List<FIN_BankStatementLine> items = FIN_Utility.getOBObjectList(
              FIN_BankStatementLine.class, strRecordsChecked);

          for (FIN_BankStatementLine item : items) {
            String strTransaction = vars.getStringParameter("inpFinancialTransactionId_"
                + item.getId(), "");
            String strMatchingType = vars.getStringParameter("inpMatchingType_" + item.getId(),
                FIN_MatchedTransaction.NOMATCH);
            if (strTransaction == null || strTransaction.equalsIgnoreCase("")) {
              item.setFinancialAccountTransaction(null);
              item.setMatchingtype(FIN_MatchedTransaction.NOMATCH);
            } else {
              FIN_FinaccTransaction transactionLine = MatchTransactionDao.getObject(
                  FIN_FinaccTransaction.class, strTransaction);
              transactionLine.setReconciliation(MatchTransactionDao.getObject(
                  FIN_Reconciliation.class, strReconciliationId));
              item.getBankStatement().setFINReconciliation(
                  MatchTransactionDao.getObject(FIN_Reconciliation.class, strReconciliationId));
              if (isInArray(strRecordsChecked, item.getId()))
                transactionLine.setStatus("RPPC");
              else {
                boolean isReceipt = true;
                if (transactionLine.getFinPayment() != null)
                  isReceipt = transactionLine.getFinPayment().isReceipt();
                else
                  isReceipt = (transactionLine.getDepositAmount().compareTo(
                      transactionLine.getPaymentAmount()) >= 0);
                transactionLine.setStatus((isReceipt) ? "RDNC" : "PWNC");
              }
              OBDal.getInstance().save(transactionLine);
              OBDal.getInstance().flush();
              item.setFinancialAccountTransaction(transactionLine);
              item.setMatchingtype(strMatchingType);
              if (transactionLine.getFinPayment() != null)
                item.setBusinessPartner(transactionLine.getFinPayment().getBusinessPartner());
            }
            OBDal.getInstance().save(item);
            OBDal.getInstance().flush();
          }
          if (updateReconciliation(vars, strReconciliationId, strFinancialAccountId, strTabId, vars
              .commandIn("RECONCILE"))) {
            OBError msg = new OBError();
            msg.setType("Success");
            msg.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
            vars.setMessage(strTabId, msg);
          }
        }
        String strWindowPath = Utility.getTabURL(this, strTabId, "R");
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;

        printPageClosePopUp(response, vars, strWindowPath);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }

  private boolean updateReconciliation(VariablesSecureApp vars, String strReconciliationId,
      String strFinancialAccountId, String strTabId, boolean process) {
    try {
      FIN_Reconciliation reconciliation = MatchTransactionDao.getObject(FIN_Reconciliation.class,
          strReconciliationId);
      reconciliation.setEndingBalance(reconciliation.getStartingbalance().add(
          MatchTransactionDao.getClearedLinesAmount(strReconciliationId)));
      if (process && !MatchTransactionDao.checkAllLinesCleared(strFinancialAccountId))
        // FIXME : Well-formed error message
        throw new OBException("Not all the transaction lines has been cleared");
      // FIXME : There must be more actions to do, in order to get the reconciliation processed
      // For example, change the DocumentStatus to completed
      reconciliation.setProcessed(process);
      reconciliation.setDocumentStatus("CO");
      OBDal.getInstance().save(reconciliation);
      OBDal.getInstance().flush();
    } catch (Exception ex) {
      OBError menssage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      vars.setMessage(strTabId, menssage);
      return false;
    }
    return true;
  }

  private boolean isInArray(String inString, String value) {
    if (inString.indexOf(value) == -1)
      return false;
    return true;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strWindowId, String strTabId, String strPaymentTypeFilter,
      String strFinancialAccountId, String reconciliationId, String strShowCleared)
      throws IOException, ServletException {

    log4j
        .debug("Output: Match using imported Bank Statement Lines pressed on Financial Account || Transaction tab");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/MatchTransaction").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", strOrgId);
    xmlDocument.setParameter("financialAccountId", strFinancialAccountId);
    xmlDocument.setParameter("reconciliationId", reconciliationId);
    xmlDocument.setParameter("paramPaymentTypeFilter", strPaymentTypeFilter);
    xmlDocument.setParameter("showCleared", strShowCleared);
    xmlDocument.setParameter("jsDateFormat", "var sc_JsDateFormat =\"" + vars.getJsDateFormat()
        + "\";");
    // Check if There is a matching algorithm for the given financial account
    FIN_FinancialAccount financial = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);
    try {
      new FIN_MatchingTransaction(financial.getMatchingAlgorithm().getJavaClassName());
    } catch (Exception ex) {
      OBDal.getInstance().rollbackAndClose();
      OBError message = Utility.translateError(this, vars, vars.getLanguage(), Utility
          .parseTranslation(this, vars, vars.getLanguage(), "@APRM_MissingMatchingAlgorithm@"));
      vars.setMessage(strTabId, message);
      printPageClosePopUp(response, vars, Utility.getTabURL(this, strTabId, "R"));
      return;
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "0CC268ED2E8D4B0397A0DCBBFA2237DE", "", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "MatchTransaction"), Utility.getContext(this, vars,
              "#User_Client", "MatchTransaction"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "MatchTransaction", "");
      xmlDocument.setData("reportPaymentTypeFilter", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars,
      String strPaymentTypeFilter, String strFinancialAccountId, String strReconciliationId,
      String strShowCleared) throws IOException, ServletException {
    log4j.debug("Output: Grid Match using imported Bank Statement Lines");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_actionbutton/MatchTransactionGrid").createXmlDocument();

    FieldProvider[] data = getMatchedBankStatementLinesData(vars, strFinancialAccountId,
        strReconciliationId, strPaymentTypeFilter, strShowCleared);

    xmlDocument.setData("structure", data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void unMatchBankStatementLine(HttpServletResponse response,
      String strUnmatchBankStatementLineId) throws IOException, ServletException {

    OBContext.setAdminMode();
    try {
      FIN_BankStatementLine bsline = OBDal.getInstance().get(FIN_BankStatementLine.class,
          strUnmatchBankStatementLineId);

      FIN_FinaccTransaction finTrans = bsline.getFinancialAccountTransaction();
      if (finTrans != null) {
        bsline.getFinancialAccountTransaction().setReconciliation(null);
        bsline.setFinancialAccountTransaction(null);
      }
      bsline.setMatchingtype(null);
      OBDal.getInstance().save(bsline);
      OBDal.getInstance().flush();

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println("");
      out.close();
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private FieldProvider[] getMatchedBankStatementLinesData(VariablesSecureApp vars,
      String strFinancialAccountId, String strReconciliationId, String strPaymentTypeFilter,
      String strShowCleared) throws ServletException {
    FIN_FinancialAccount financial = new AdvPaymentMngtDao().getObject(FIN_FinancialAccount.class,
        strFinancialAccountId);
    MatchingAlgorithm ma = financial.getMatchingAlgorithm();
    FIN_MatchingTransaction matchingTransaction = new FIN_MatchingTransaction(ma.getJavaClassName());

    List<FIN_BankStatementLine> bankLines = MatchTransactionDao.getMatchingBankStatementLines(
        strFinancialAccountId, strReconciliationId, strPaymentTypeFilter, strShowCleared);

    FIN_BankStatementLine[] FIN_BankStatementLines = new FIN_BankStatementLine[0];
    FIN_BankStatementLines = bankLines.toArray(FIN_BankStatementLines);
    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(bankLines);

    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {
        final String COLOR_STRONG = "#66CC00";
        final String COLOR_WEAK = "#99CC66";
        final String COLOR_WHITE = "white";
        String matchingType = FIN_BankStatementLines[i].getMatchingtype();
        FIN_FinaccTransaction transaction = FIN_BankStatementLines[i]
            .getFinancialAccountTransaction();
        if (transaction == null) {
          FIN_MatchedTransaction matched = matchingTransaction.match(FIN_BankStatementLines[i]);
          transaction = matched.getTransaction();
          matchingType = matched.getMatchLevel();
        }

        FieldProviderFactory.setField(data[i], "rownum", Integer.toString(i + 1));
        FieldProviderFactory.setField(data[i], "yes", "Y");
        FieldProviderFactory.setField(data[i], "finBankLineId", FIN_BankStatementLines[i].getId());
        FieldProviderFactory.setField(data[i], "bankLineTransactionDate", Utility.formatDate(
            FIN_BankStatementLines[i].getTransactionDate(), vars.getJavaDateFormat()));
        FieldProviderFactory.setField(data[i], "bankLineBusinessPartner", FIN_BankStatementLines[i]
            .getBpartnername());
        FieldProviderFactory.setField(data[i], "bankLineReferenceNo", FIN_BankStatementLines[i]
            .getReferenceNo());
        FieldProviderFactory.setField(data[i], "bankLineAmount", FIN_BankStatementLines[i]
            .getDramount().subtract(FIN_BankStatementLines[i].getCramount()).toString());
        FieldProviderFactory.setField(data[i], "matchStyle", matchingType
            .equals(FIN_MatchedTransaction.STRONG) ? COLOR_STRONG : ((matchingType
            .equals(FIN_MatchedTransaction.WEAK)) ? COLOR_WEAK : COLOR_WHITE));
        FieldProviderFactory.setField(data[i], "matchingType", matchingType);

        if (transaction != null) {
          FieldProviderFactory.setField(data[i], "disabled", "N");
          // Auto Matching or already matched
          FieldProviderFactory.setField(data[i], "checked",
              (matchingType.equals(FIN_MatchedTransaction.STRONG) || transaction
                  .getReconciliation() != null) ? "Y" : "N");
          FieldProviderFactory.setField(data[i], "finTransactionId", transaction.getId());
          FieldProviderFactory.setField(data[i], "transactionDate", Utility.formatDate(transaction
              .getTransactionDate(), vars.getJavaDateFormat()));
          FieldProviderFactory.setField(data[i], "transactionBPartner",
              transaction.getFinPayment() != null ? transaction.getFinPayment()
                  .getBusinessPartner().getName() : "");
          FieldProviderFactory.setField(data[i], "transactionReferenceNo", transaction
              .getFinPayment() != null ? transaction.getFinPayment().getReferenceNo() : "");
          FieldProviderFactory.setField(data[i], "transactionAmount", transaction
              .getDepositAmount().subtract(transaction.getPaymentAmount()).toString());
        } else {
          FieldProviderFactory.setField(data[i], "disabled", "Y");
          FieldProviderFactory.setField(data[i], "checked", "N");
          FieldProviderFactory.setField(data[i], "finTransactionId", "");
          FieldProviderFactory.setField(data[i], "transactionDate", "");
          FieldProviderFactory.setField(data[i], "transactionBPartner", "");
          FieldProviderFactory.setField(data[i], "transactionReferenceNo", "");
          FieldProviderFactory.setField(data[i], "transactionAmount", "");
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  public String checkReconciliationNotProcessed(VariablesSecureApp vars,
      String strReconciliationId, String strTabId) {
    FIN_Reconciliation reconciliation = MatchTransactionDao.getObject(FIN_Reconciliation.class,
        strReconciliationId);
    OBContext.setAdminMode();
    try {
      String text = "Closed or Invalid Reconciliation";
      if (reconciliation != null && !reconciliation.isNewOBObject() && reconciliation.isProcessed()) {
        OBError menssage = Utility.translateError(this, vars, vars.getLanguage(), text);
        vars.setMessage(strTabId, menssage);
        return text;
      }
      return "";
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getServletInfo() {
    return "This servlet match imported bank statement lines for a financial account";
  }
}
