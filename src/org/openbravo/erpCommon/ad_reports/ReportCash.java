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

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportCash extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportCash|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportCash|DateTo", "");
      String strCashbook = vars.getGlobalVariable("inpcCashbookId", "ReportCash|Cashbook", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strCashbook);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportCash|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportCash|DateTo");
      String strCashbook = vars.getRequestGlobalVariable("inpcCashbookId", "ReportCash|Cashbook");
      printPageHtml(response, vars, strDateFrom, strDateTo, strCashbook);
    } else pageError(response);
  }

  void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strCashbook)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    
    XmlDocument xmlDocument=null;
    ReportCashData[] data=null;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strCashbook);
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportCashEdit").createXmlDocument();
      data = ReportCashData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportCash"), Utility.getContext(this, vars, "#User_Org", "ReportCash"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strCashbook);
      xmlDocument.setParameter("sumAmount", ReportCashData.BeginningBalance(this, Utility.getContext(this, vars, "#User_Client", "ReportCash"), Utility.getContext(this, vars, "#User_Org", "ReportCash"), strDateFrom, strCashbook));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setData("structure1", data);
      out.println(xmlDocument.print());
      out.close();
    }
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strCashbook)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    
    XmlDocument xmlDocument=null;

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportCash").createXmlDocument();
    xmlDocument.setParameter("sumAmount", ReportCashData.BeginningBalance(this, Utility.getContext(this, vars, "#User_Client", "ReportCash"), Utility.getContext(this, vars, "#User_Org", "ReportCash"), strDateFrom, strCashbook));


    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportCash", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 
    try {
      KeyMap key = new KeyMap(this, vars, "ReportCash.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportCash");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportCash.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportCash.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportCash");
      vars.removeMessage("ReportCash");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("cCashbook", strCashbook);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_CashBook_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportCash"), Utility.getContext(this, vars, "#User_Client", "ReportCash"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportCash", strCashbook);
      xmlDocument.setData("reportC_CASHBOOK","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportCash.";
  } // end of getServletInfo() method
}

