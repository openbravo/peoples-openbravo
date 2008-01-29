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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_reports;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.HashMap;


import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportToInvoiceConsignmentJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportToInvoiceConsignmentJR|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportToInvoiceConsignmentJR|DateTo", "");
      String strWarehouse = vars.getGlobalVariable("inpmWarehouseId","ReportToInvoiceConsignmentJR|Warehouse", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strWarehouse);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportToInvoiceConsignmentJR|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportToInvoiceConsignmentJR|DateTo");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId", "ReportToInvoiceConsignmentJR|Warehouse");
      printPagePDF(response, vars, strDateFrom, strDateTo, strWarehouse);
    } else pageErrorPopUp(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strWarehouse)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportToInvoiceConsignmentJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportToInvoiceConsignmentJR", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportToInvoiceConsignmentJR.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportToInvoiceConsignmentJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportToInvoiceConsignmentJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportToInvoiceConsignmentJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportToInvoiceConsignmentJR");
      vars.removeMessage("ReportToInvoiceConsignmentJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    } 

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("mWarehouseId", strWarehouse);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "M_Warehouse_ID", "M_Warehouse of Client", "", Utility.getContext(this, vars, "#User_Client",""), Utility.getContext(this, vars, "#AD_Client_ID", ""), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData,"", "");
      xmlDocument.setData("reportM_WAREHOUSEID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    out.println(xmlDocument.print());
    out.close();
  }

  void printPagePDF(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strWarehouse) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: PDF");

	response.setContentType("text/html; charset=UTF-8");

    ReportToInvoiceConsignmentData[] data = ReportToInvoiceConsignmentData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportToInvoiceConsignmentJR"), Utility.getContext(this, vars, "#User_Org", "ReportToInvoiceConsignmentJR"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strWarehouse);  

	 if (data == null && data.length == 0){
           data = ReportToInvoiceConsignmentData.set();
      }
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportToInvoiceConsignmentJR.jrxml";
      String strOutput="pdf";
      if (strOutput.equals("pdf")) response.setHeader("Content-disposition", "inline; filename=ReportToInvoiceConsignmentJR.pdf");

       
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("REPORT_TITLE", "Departure Movements of Consignment Material Report");
		renderJR(vars, response, strReportName, strOutput, parameters, data, null );
  }

  public String getServletInfo() {
    return "Servlet ReportToInvoiceConsignment. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
