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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportToInvoiceConsignment extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportToInvoiceConsignment|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportToInvoiceConsignment|DateTo",
          "");
      String strWarehouse = vars.getGlobalVariable("inpmWarehouseId",
          "ReportToInvoiceConsignment|Warehouse", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strWarehouse);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportToInvoiceConsignment|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportToInvoiceConsignment|DateTo");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportToInvoiceConsignment|Warehouse");
      printPagePDF(response, vars, strDateFrom, strDateTo, strWarehouse);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strWarehouse) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportToInvoiceConsignment").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportToInvoiceConsignment", false,
        "", "", "", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportToInvoiceConsignment");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportToInvoiceConsignment.html", classInfo.id, classInfo.type, strReplaceWith, tabs
              .breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportToInvoiceConsignment.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportToInvoiceConsignment");
      vars.removeMessage("ReportToInvoiceConsignment");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("mWarehouseId", strWarehouse);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "M_Warehouse_ID",
          "M_Warehouse of Client", "", Utility.getContext(this, vars, "#User_Client", ""), Utility
              .getContext(this, vars, "#AD_Client_ID", ""), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "", "");
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strWarehouse) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportToInvoiceConsignment_PDF").createXmlDocument();
    // Get user Client's base currency
    String strCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    ReportToInvoiceConsignmentData[] data = ReportToInvoiceConsignmentData.select(this,
        strCurrencyId,
        Utility.getContext(this, vars, "#User_Client", "ReportToInvoiceConsignment"), Utility
            .getContext(this, vars, "#AccessibleOrgTree", "ReportToInvoiceConsignment"),
        strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strWarehouse);
    String strTitle = "";
    if (!strDateFrom.equals("")) {
      strTitle = strTitle + " desde " + strDateFrom;
    }
    if (!strDateTo.equals("")) {
      strTitle = strTitle + " hasta " + strDateTo;
    }
    xmlDocument.setParameter("title", strTitle);
    xmlDocument.setData("structure1", data);
    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, response);

  }

  public String getServletInfo() {
    return "Servlet ReportToInvoiceConsignment. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
