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


import org.openbravo.erpCommon.ad_combos.ProcessPlanComboData;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportWorkRequirementDaily extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (!Utility.hasProcessAccess(this, vars, "", "RV_ReportWorkRequirementDaily")) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }

    if (vars.commandIn("DEFAULT")) {
      String strStartDateFrom = vars.getGlobalVariable("inpStartDateFrom", "ReportWorkRequirementDaily|StartDateFrom", "");
      String strStartDateTo = vars.getGlobalVariable("inpStartDateTo", "ReportWorkRequirementDaily|StartDateTo", "");
      String strmaProcessPlan = vars.getGlobalVariable("inpmaProcessPlanId", "ReportWorkRequirementDaily|MA_ProcessPlan_ID", "");
      strStartDateTo = DateTimeData.today(this);
      strStartDateFrom = DateTimeData.today(this);
      printPageDataSheet(response, vars, strStartDateFrom, strStartDateTo, strmaProcessPlan);
    } else if (vars.commandIn("FIND")) {
      String strStartDateFrom = vars.getRequestGlobalVariable("inpStartDateFrom", "ReportWorkRequirementDaily|StartDateFrom");
      String strStartDateTo = vars.getRequestGlobalVariable("inpStartDateTo", "ReportWorkRequirementDaily|StartDateTo");
      String strmaProcessPlan = vars.getRequestGlobalVariable("inpmaProcessPlanId", "ReportWorkRequirementDaily|MA_ProcessPlan_ID");
      printPageDataHtml(response, vars, strStartDateFrom, strStartDateTo, strmaProcessPlan);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strStartDateFrom, String strStartDateTo, String strmaProcessPlan)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");

    ReportWorkRequirementDailyData[] data=null;

    data = ReportWorkRequirementDailyData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"), Utility.getContext(this, vars, "#User_Org", "ReportWorkRequirementDaily"), strStartDateFrom, strStartDateTo, strmaProcessPlan);
    for (int i=0; i<data.length; i++) {
      ReportWorkRequirementDailyData[] product = ReportWorkRequirementDailyData.producedproduct(this, data[i].wrpid);
      data[i].prodproduct = product[0].name;
      String strqty = ReportWorkRequirementDailyData.inprocess(this, data[i].wrid, data[i].productid);
      data[i].inprocess = strqty;
      if (strqty == "") {
        strqty = "0";
      }
    }

    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDailyEdit.jrxml";
    String strOutput ="html";
    String strTitle =classInfo.name;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_TITLE", strTitle);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null );

  }




  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strStartDateFrom, String strStartDateTo, String strmaProcessPlan)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDaily").createXmlDocument();
    
    
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWorkRequirementDaily", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportWorkRequirementDaily.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportWorkRequirementDaily");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportWorkRequirementDaily.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportWorkRequirementDaily.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWorkRequirementDaily");
      vars.removeMessage("ReportWorkRequirementDaily");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }  

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("maProcessPlan", strmaProcessPlan);
    xmlDocument.setParameter("startDateFrom", strStartDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("startDateTo", strStartDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setData("reportMA_PROCESSPLAN", "liststructure", ProcessPlanComboData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"), Utility.getContext(this, vars, "#User_Org", "ReportWorkRequirementDaily")));
    
    out.println(xmlDocument.print());
    out.close();
  }

 /* 
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strStartDateFrom, String strStartDateTo, String strmaProcessPlan)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    ReportWorkRequirementDailyData[] data=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportWorkRequirementDaily").createXmlDocument();
    data = ReportWorkRequirementDailyData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"), Utility.getContext(this, vars, "#User_Org", "ReportWorkRequirementDaily"), strStartDateFrom, strStartDateTo, strmaProcessPlan);
    for (int i=0; i<data.length; i++) {
      ReportWorkRequirementDailyData[] product = ReportWorkRequirementDailyData.producedproduct(this, data[i].wrpid);
      data[i].prodproduct = product[0].name;
      String strqty = ReportWorkRequirementDailyData.inprocess(this, data[i].wrid, data[i].productid);
      data[i].inprocess = strqty;
      if (strqty == "") {
        strqty = "0";
      }
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWorkRequirementDaily", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportWorkRequirementDaily.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportWorkRequirementDaily");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportWorkRequirementDaily.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportWorkRequirementDaily.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWorkRequirementDaily");
      vars.removeMessage("ReportWorkRequirementDaily");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }  

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("maProcessPlan", strmaProcessPlan);
    xmlDocument.setParameter("startDateFrom", strStartDateFrom);
    xmlDocument.setParameter("startDateTo", strStartDateTo);
    xmlDocument.setData("reportMA_PROCESSPLAN", "liststructure", ProcessPlanComboData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementDaily"), Utility.getContext(this, vars, "#User_Org", "ReportWorkRequirementDaily")));
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }
*/
  public String getServletInfo() {
    return "Servlet ReportWorkRequirementDaily.";
  } // end of getServletInfo() method
}
