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

public class ReportWorkRequirementJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strStartDateFrom = vars.getGlobalVariable("inpStartDateFrom", "ReportWorkRequirementJR|StartDateFrom", "");
      String strStartDateTo = vars.getGlobalVariable("inpStartDateTo", "ReportWorkRequirementJR|StartDateTo", "");
      String strEndDateFrom = vars.getGlobalVariable("inpEndDateFrom", "ReportWorkRequirementJR|EndDateFrom", "");
      String strEndDateTo = vars.getGlobalVariable("inpEndDateTo", "ReportWorkRequirementJR|EndDateTo", "");
      String strmaProcessPlan = vars.getGlobalVariable("inpmaProcessPlanId", "ReportWorkRequirementJR|MA_ProcessPlan_ID", "");
      printPageDataSheet(response, vars, strStartDateFrom, strStartDateTo, strEndDateFrom, strEndDateTo, strmaProcessPlan);
    } else if (vars.commandIn("FIND")) {
      String strStartDateFrom = vars.getRequestGlobalVariable("inpStartDateFrom", "ReportWorkRequirementJR|StartDateFrom");
      String strStartDateTo = vars.getRequestGlobalVariable("inpStartDateTo", "ReportWorkRequirementJR|StartDateTo");
      String strEndDateFrom = vars.getRequestGlobalVariable("inpEndDateFrom", "ReportWorkRequirementJR|EndDateFrom");
      String strEndDateTo = vars.getRequestGlobalVariable("inpEndDateTo", "ReportWorkRequirementJR|EndDateTo");
      String strmaProcessPlan = vars.getRequestGlobalVariable("inpmaProcessPlanId", "ReportWorkRequirementJR|MA_ProcessPlan_ID");
      printPageDataHtml(response, vars, strStartDateFrom, strStartDateTo, strEndDateFrom, strEndDateTo, strmaProcessPlan);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strStartDateFrom, String strStartDateTo, String strEndDateFrom, String strEndDateTo, String strmaProcessPlan)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    ReportWorkRequirementJRData[] data=null;
    data = ReportWorkRequirementJRData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementJR"), Utility.getContext(this, vars, "#User_Org", "ReportWorkRequirementJR"), strStartDateFrom, strStartDateTo, strEndDateFrom, strEndDateTo, strmaProcessPlan);
    for (int i=0; i<data.length; i++) {
      String strqty = ReportWorkRequirementJRData.inprocess(this, data[i].wrid, data[i].productid);
      if (strqty==null || strqty.equals("")) strqty="0";
      data[i].inprocess = strqty; 
    }
    String strReportPath = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportWorkRequirementJR.jrxml";
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_TITLE", classInfo.name);
    renderJR(vars, response, strReportPath, "html", parameters, data, null);
  }
    
    void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strStartDateFrom, String strStartDateTo, String strEndDateFrom, String strEndDateTo, String strmaProcessPlan)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    
    XmlDocument xmlDocument=null;
    
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportWorkRequirementJR").createXmlDocument();
    
    

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWorkRequirementJR", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportWorkRequirementJR.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportWorkRequirementJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportWorkRequirementJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportWorkRequirementJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWorkRequirementJR");
      vars.removeMessage("ReportWorkRequirementJR");
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
    xmlDocument.setParameter("endDateFrom", strEndDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("endDateTo", strEndDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setData("reportMA_PROCESSPLAN", "liststructure", ProcessPlanComboData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportWorkRequirementJR"), Utility.getContext(this, vars, "#User_Org", "ReportWorkRequirementJR")));

    out.println(xmlDocument.print());
    out.close();
    }
    
    public String getServletInfo() {
      return "Servlet ReportWorkRequirementJR.";
    } // end of getServletInfo() method
  }

