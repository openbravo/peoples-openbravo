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
package org.openbravo.advpaymentmngt.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtility;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * @author openbravo
 * 
 */
public class Transactions extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "date", "bpartner", "paymentno", "description",
      "paidamount", "receivedamount", "cleared", "rowkey" };
  private static final RequestFilter columnFilter = new ValueListFilter(colNames);
  private static final RequestFilter directionFilter = new ValueListFilter("asc", "desc");

  // private static final String formClassName =
  // "org.openbravo.advpaymentmngt.ad_forms.Transactions";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String formClassName = this.getClass().getName();

    String windowId = "";
    String tabId = "";
    String tableId = "";
    String tabName = "";
    String windowName = "";
    String windowNameEnUS = "";
    String tabNameEnUS = "";

    OBContext.setAdminMode();
    try {
      List<Tab> data = TransactionsDao.getWindowData(formClassName);
      if (data == null || data.size() == 0) {
        throw new ServletException(formClassName + ": Error on window data");
      }
      Tab tab = data.get(0);
      windowId = tab.getWindow().getId();
      tabId = tab.getId();
      tableId = tab.getTable().getId();
      tabName = tab.getTable().getName();
      windowName = tab.getWindow().getName();
      tabNameEnUS = tab.getTable().getName();
      windowNameEnUS = tab.getWindow().getName();
    } finally {
      OBContext.restorePreviousMode();
    }

    if (vars.commandIn("DEFAULT")) {

      String strFinFinancialAccountId = vars.getGlobalVariable("inpfinFinancialAccountId", windowId
          + "|Fin_Financial_Account_ID", "");
      Boolean hideReconciledTrx = vars.getGlobalVariable("inpHideReconciled",
          windowId + "|hideReconciledTrx", "Y").equals("Y");
      if ("".equals(strFinFinancialAccountId))
        response.sendRedirect(strDireccion + "/" + FormatUtilities.replace(windowNameEnUS) + "/"
            + FormatUtilities.replace(tabNameEnUS) + "_Relation.html?Command=RELATION");
      else
        printPageDataSheet(response, vars, strFinFinancialAccountId, windowName, tabName, windowId,
            tabId, tableId, hideReconciledTrx);
    } else if (vars.commandIn("HIDERECONCILED")) {

      String strFinFinancialAccountId = vars.getGlobalVariable("inpfinFinancialAccountId", windowId
          + "|Fin_Financial_Account_ID", "");
      Boolean hideReconciledTrx = vars.getStringParameter("inpHideReconciled", "N").equals("Y");
      if (hideReconciledTrx)
        vars.setSessionValue(windowId + "|hideReconciledTrx", "Y");
      else
        vars.setSessionValue(windowId + "|hideReconciledTrx", "N");
      if ("".equals(strFinFinancialAccountId))
        response.sendRedirect(strDireccion + "/" + FormatUtilities.replace(windowNameEnUS) + "/"
            + FormatUtilities.replace(tabNameEnUS) + "_Relation.html?Command=RELATION");
      else
        printPageDataSheet(response, vars, strFinFinancialAccountId, windowName, tabName, windowId,
            tabId, tableId, hideReconciledTrx);
    } else if (vars.commandIn("NEW")) {
      String strFinFinancialAccountId = vars.getGlobalVariable("inpfinFinancialAccountId", windowId
          + "|Fin_Financial_Account_ID", "");
      Boolean hideReconciledTrx = vars.getGlobalVariable("inpHideReconciled",
          windowId + "|hideReconciledTrx", "Y").equals("Y");
      printPageDataSheet(response, vars, strFinFinancialAccountId, windowName, tabName, windowId,
          tabId, tableId, hideReconciledTrx);
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      String strFinancialAccountId = vars.getGlobalVariable("inpfinFinancialAccountId", windowId
          + "|Fin_Financial_Account_ID", "");

      Boolean hideReconciledTrx = vars.getSessionValue(windowId + "|hideReconciledTrx").equals("Y");
      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);
      printGridData(response, vars, strFinancialAccountId, hideReconciledTrx, strSortCols,
          strSortDirs, strOffset, strPageSize, strNewFilter);
    } else
      pageError(response);

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strFinFinancialAccountId, String windowName, String tabName, String windowId,
      String tabId, String tableId, Boolean hideReconciledTrx) throws IOException, ServletException {
    log4j.debug("Output: dataSheet");
    String strCommand = "EDIT";
    FieldProvider[] data = TransactionsDao.getAccTrxData(strFinFinancialAccountId);
    /*
     * if (data == null || data.length == 0) { throw new ServletException(formClassName +
     * ": Error when getting data"); }
     */
    String[] discard = { "", "" };
    int notMatchedItems = TransactionsDao.getPendingToMatchCount(new AdvPaymentMngtDao().getObject(
        FIN_FinancialAccount.class, strFinFinancialAccountId));
    FIN_FinancialAccount financialAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinFinancialAccountId);

    // Hide Match Using Imported Bank Statement button in Cash Accounts
    if ("C".equals(financialAccount.getType())) {
      discard[0] = "Match_Using_Imported_Bank_Statement";
      discard[1] = "ImportBankFile";
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_forms/Transactions", discard).createXmlDocument();

    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("command", strCommand);
    xmlDocument.setParameter("commandType", strCommand);
    xmlDocument.setParameter("windowName", windowName);
    xmlDocument.setParameter("tabName", tabName);
    xmlDocument.setParameter("windowId", windowId);
    xmlDocument.setParameter("tabId", tabId);
    xmlDocument.setParameter("tableId", tableId);
    xmlDocument.setParameter("finFinancialAccountId", strFinFinancialAccountId);
    xmlDocument.setParameter("hideReconciled", (hideReconciledTrx) ? "Y" : "N");
    xmlDocument.setParameter("KeyName", "");
    xmlDocument.setParameter("windowPath", Utility.getTabURL(this, tabId, "E"));

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
        "../FinancialAccount/Transactions5B9941AC1F6A4529A76FCA7CDA0A7D7A", (strCommand
            .equals("NEW") || (data == null || data.length == 0)),
        "document.frmMain.inpfinFinancialAccountId", "", "", "".equals("Y"), "FinancialAccount",
        strReplaceWith, true);

    toolbar.prepareRelationTemplateNoSearch(false, false, false, false, false);
    toolbar.prepareSimpleToolBarTemplate();

    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, tabId, windowId);
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "../FinancialAccount/Transactions5B9941AC1F6A4529A76FCA7CDA0A7D7A_Relation.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "../FinancialAccount/Transactions5B9941AC1F6A4529A76FCA7CDA0A7D7A_Relation.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      FIN_FinancialAccount account = new AdvPaymentMngtDao().getObject(FIN_FinancialAccount.class,
          strFinFinancialAccountId);
      String strLastReconcileDate = "", strLastRecBalance = "", strCurrentBalance = "";
      strCurrentBalance = financialAccount.getCurrentBalance().toString();
      xmlDocument.setParameter("account", account.getIdentifier());
      OBContext.setAdminMode();
      try {
        FIN_Reconciliation lastReconciliation = TransactionsDao.getLastReconciliation(account, "Y");
        if (lastReconciliation != null) {
          strLastReconcileDate = Utility.formatDate(lastReconciliation.getEndingDate(), vars
              .getJavaDateFormat());
          strLastRecBalance = lastReconciliation.getEndingBalance().toString();
        }
      } finally {
        OBContext.restorePreviousMode();
      }

      xmlDocument.setParameter("lastReconcileDate", strLastReconcileDate);
      xmlDocument.setParameter("lastRecBalance", strLastRecBalance);
      xmlDocument.setParameter("currentBalance", strCurrentBalance);
      xmlDocument.setParameter("itemNo", Integer.toString(notMatchedItems));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    OBError myMessage = vars.getMessage(tabId);
    vars.removeMessage(tabId);
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    // xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGridStructure(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    log4j.debug("Output: print page structure");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridStructure").createXmlDocument();

    SQLReturnObject[] data = getHeaders(vars);
    String type = "Hidden";
    String title = "";
    String description = "";

    xmlDocument.setParameter("type", type);
    xmlDocument.setParameter("title", title);
    xmlDocument.setParameter("description", description);
    xmlDocument.setData("structure1", data);
    xmlDocument.setParameter("backendPageSize", String.valueOf(TableSQLData.maxRowsPerGridPage));
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  private SQLReturnObject[] getHeaders(VariablesSecureApp vars) {
    SQLReturnObject[] data = null;
    Vector<SQLReturnObject> vAux = new Vector<SQLReturnObject>();
    // String[] colNames = { "date", "bpartner", "paymentno",
    // "description", "paidamount", "receivedamount", "cleared", "rowkey" };
    String[] colWidths = { "70", "170", "80", "300", "60", "60", "40", "0" }; // total 790
    for (int i = 0; i < colNames.length; i++) {
      SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("isvisible",
          (colNames[i].endsWith("_id") || colNames[i].equals("rowkey") ? "false" : "true"));
      String name = Utility.messageBD(this, "APRM_FATS_" + colNames[i].toUpperCase(), vars
          .getLanguage());
      dataAux.setData("name", (name.startsWith("APRM_FATS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, Boolean hideReconciledTrx, String strOrderCols,
      String strOrderDirs, String strOffset, String strPageSize, String strNewFilter)
      throws IOException {
    log4j.debug("Output: print page rows");
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    int page = 0;
    SQLReturnObject[] headers = getHeaders(vars);
    List<FIN_FinaccTransaction> finaccTransactions = null;
    String type = "Hidden";
    String title = "";
    String description = "";
    String strNumRows = "0";
    int offset = Integer.valueOf(strOffset).intValue();
    int pageSize = Integer.valueOf(strPageSize).intValue();

    if (headers != null) {
      try {
        // build sql orderBy clause from parameters
        String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);
        HashMap<String, String> orderByColsMap = new HashMap<String, String>();
        // String[] colNames = { "date", "bpartner", "paymentno",
        // "description", "paidamount", "receivedamount", "cleared", "rowkey" };
        orderByColsMap.put("date", "fatrx." + FIN_FinaccTransaction.PROPERTY_TRANSACTIONDATE);
        orderByColsMap.put("bpartner", "fatrx." + FIN_FinaccTransaction.PROPERTY_FINPAYMENT + "."
            + FIN_Payment.PROPERTY_BUSINESSPARTNER);
        orderByColsMap.put("paymentno", "fatrx." + FIN_FinaccTransaction.PROPERTY_FINPAYMENT + "."
            + FIN_Payment.PROPERTY_DOCUMENTNO);
        orderByColsMap.put("description", "fatrx." + FIN_FinaccTransaction.PROPERTY_DESCRIPTION);
        orderByColsMap.put("paidamount", "fatrx." + FIN_FinaccTransaction.PROPERTY_PAYMENTAMOUNT);
        orderByColsMap.put("receivedamount", "fatrx."
            + FIN_FinaccTransaction.PROPERTY_DEPOSITAMOUNT);
        orderByColsMap.put("cleared", "fatrx." + FIN_FinaccTransaction.PROPERTY_RECONCILIATION
            + "." + FIN_Reconciliation.PROPERTY_DOCUMENTNO);
        orderByColsMap.put("rowkey", "fatrx." + FIN_FinaccTransaction.PROPERTY_ID);
        for (int i = 0; i < colNames.length; i++)
          strOrderBy = strOrderBy.replace(colNames[i], orderByColsMap.get(colNames[i]));

        page = TableSQLData.calcAndGetBackendPage(vars, "Transactions.currentPage");
        if (vars.getStringParameter("movePage", "").length() > 0) {
          // on movePage action force executing countRows again
          strNewFilter = "";
        }
        int oldOffset = offset;
        offset = (page * TableSQLData.maxRowsPerGridPage) + offset;
        log4j.debug("relativeOffset: " + oldOffset + " absoluteOffset: " + offset);
        if (strNewFilter.equals("1") || strNewFilter.equals("")) { // New filter or first load
          int dbNumRows = dao.getTrxGridRowCount(dao.getObject(FIN_FinancialAccount.class,
              strFinancialAccountId), hideReconciledTrx, TableSQLData.maxRowsPerGridPage, offset);
          strNumRows = Integer.toString(dbNumRows);

          vars.setSessionValue("Transactions.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("ProjectData.numrows");
        }

        finaccTransactions = dao.getTrxGridRows(dao.getObject(FIN_FinancialAccount.class,
            strFinancialAccountId), hideReconciledTrx, pageSize, offset, strOrderBy);
        strNumRows = Integer.toString(finaccTransactions.size());
      } catch (ServletException e) {
        log4j.error("Error in print page data: " + e);
        e.printStackTrace();
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorAjax(response, "Error", "Connection Error", "No database connection");
          return;
        } else {
          type = myError.getType();
          title = myError.getTitle();
          if (!myError.getMessage().startsWith("<![CDATA["))
            description = "<![CDATA[" + myError.getMessage() + "]]>";
          else
            description = myError.getMessage();
        }
      } catch (Exception e) {
        log4j.debug("Error obtaining rows data");
        type = "Error";
        title = "Error";
        if (e.getMessage().startsWith("<![CDATA["))
          description = "<![CDATA[" + e.getMessage() + "]]>";
        else
          description = e.getMessage();
        e.printStackTrace();
      }
    }

    if (!type.startsWith("<![CDATA["))
      type = "<![CDATA[" + type + "]]>";
    if (!title.startsWith("<![CDATA["))
      title = "<![CDATA[" + title + "]]>";
    if (!description.startsWith("<![CDATA["))
      description = "<![CDATA[" + description + "]]>";
    StringBuffer strRowsData = new StringBuffer();
    strRowsData.append("<xml-data>\n");
    strRowsData.append("  <status>\n");
    strRowsData.append("    <type>").append(type).append("</type>\n");
    strRowsData.append("    <title>").append(title).append("</title>\n");
    strRowsData.append("    <description>").append(description).append("</description>\n");
    strRowsData.append("  </status>\n");
    strRowsData.append("  <rows numRows=\"").append(strNumRows).append(
        "\" backendPage=\"" + page + "\">\n");

    OBContext.setAdminMode();
    try {
      if (finaccTransactions != null && finaccTransactions.size() > 0) {
        for (FIN_FinaccTransaction finaccTrx : finaccTransactions) {
          strRowsData.append("    <tr>\n");
          for (int k = 0; k < headers.length; k++) {
            strRowsData.append("      <td><![CDATA[");
            // "date", "bpartner", "paymentno",
            // "description", "paidamount", "receivedamount", "cleared"
            String columnData = "";
            switch (k) {
            case 0: // date column
              columnData = Utility.formatDate(finaccTrx.getTransactionDate(), vars
                  .getJavaDateFormat());
              break;
            case 1: // bpartner
              if (finaccTrx.getFinPayment() != null)
                columnData = finaccTrx.getFinPayment().getBusinessPartner().getIdentifier();
              break;
            case 2: // paymentno
              if (finaccTrx.getFinPayment() != null)
                columnData = finaccTrx.getFinPayment().getDocumentNo();
              break;
            case 3: // description
              if (finaccTrx.getDescription() != null)
                columnData = finaccTrx.getDescription();
              break;
            case 4: // paidamount
              if (finaccTrx.getDepositAmount() != null)
                columnData = finaccTrx.getPaymentAmount().toString();
              break;
            case 5: // receivedamount
              if (finaccTrx.getPaymentAmount() != null)
                columnData = finaccTrx.getDepositAmount().toString();
              break;
            case 6: // cleared
              columnData = Utility.messageBD(myPool, ((finaccTrx.getStatus().equals("RPPC")) ? "Y"
                  : "N"), vars.getLanguage());
              break;
            case 7: // rowkey
              columnData = finaccTrx.getId().toString();
              break;
            }

            if (columnData != "") {
              if (headers[k].getField("adReferenceId").equals("32"))
                strRowsData.append(strReplaceWith).append("/images/");
              strRowsData.append(columnData.replaceAll("<b>", "").replaceAll("<B>", "").replaceAll(
                  "</b>", "").replaceAll("</B>", "").replaceAll("<i>", "").replaceAll("<I>", "")
                  .replaceAll("</i>", "").replaceAll("</I>", "").replaceAll("<p>", "&nbsp;")
                  .replaceAll("<P>", "&nbsp;").replaceAll("<br>", "&nbsp;").replaceAll("<BR>",
                      "&nbsp;"));
            } else {
              if (headers[k].getField("adReferenceId").equals("32")) {
                strRowsData.append(strReplaceWith).append("/images/blank.gif");
              } else
                strRowsData.append("&nbsp;");
            }
            strRowsData.append("]]></td>\n");
          }
          strRowsData.append("    </tr>\n");
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    strRowsData.append("  </rows>\n");
    strRowsData.append("</xml-data>\n");

    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(strRowsData.toString());
    out.print(strRowsData.toString());
    out.close();

  }

  public String getServletInfo() {
    return "Transactions Servlet";
  }

}
