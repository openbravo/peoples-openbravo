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
import java.util.HashMap;

public class ReportProjectProfitabilityJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  public static String strTreeOrg = "";

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportProjectProfitabilityJR|Org", vars.getOrg());
      String strProject = vars.getGlobalVariable("inpcProjectId", "ReportProjectProfitabilityJR|Project", "");
      String strProjectType = vars.getGlobalVariable("inpProjectType", "ReportProjectProfitabilityJR|ProjectType", "");
      String strResponsible = vars.getGlobalVariable("inpResponsible", "ReportProjectProfitabilityJR|Responsible", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportProjectProfitabilityJR|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportProjectProfitabilityJR|DateTo", "");
	  String strDateFrom2 = vars.getGlobalVariable("inpDateFrom2", "ReportProjectProfitabilityJR|DateFrom2", "");
      String strDateTo2 = vars.getGlobalVariable("inpDateTo2", "ReportProjectProfitabilityJR|DateTo2", "");
      String strExpand = vars.getGlobalVariable("inpExpand", "ReportProjectProfitabilityJR|Expand", "Y");
      String strPartner = vars.getGlobalVariable("inpcBPartnerId", "ReportProjectProfitabilityJR|Partner", "");
      printPageDataSheet(response, vars, strOrg, strProject, strProjectType, strResponsible, strDateFrom, strDateTo, strExpand, strPartner, strDateFrom2, strDateTo2);
    } else if (vars.commandIn("FIND")) {
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportProjectProfitabilityJR|Org");
      String strProject = vars.getRequestGlobalVariable("inpcProjectId", "ReportProjectProfitabilityJR|Project");
      String strProjectType = vars.getRequestGlobalVariable("inpProjectType", "ReportProjectProfitabilityJR|ProjectType");
      String strResponsible = vars.getRequestGlobalVariable("inpResponsible", "ReportProjectProfitabilityJR|Responsible");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportProjectProfitabilityJR|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportProjectProfitabilityJR|DateTo");
	  String strDateFrom2 = vars.getRequestGlobalVariable("inpDateFrom2", "ReportProjectProfitabilityJR|DateFrom2");
      String strDateTo2 = vars.getRequestGlobalVariable("inpDateTo2", "ReportProjectProfitabilityJR|DateTo2");
      String strExpand = vars.getRequestGlobalVariable("inpExpand", "ReportProjectProfitabilityJR|Expand");
      String strPartner = vars.getRequestGlobalVariable("inpcBPartnerId", "ReportProjectProfitabilityJR|Partner");
      String strOutput="html";
      printPageDataHtml(response, vars, strOrg, strProject, strProjectType, strResponsible, strDateFrom, strDateTo, strExpand, strPartner, strDateFrom2, strDateTo2, strOutput);
    } else if (vars.commandIn("PDF")){
		String strOrg = vars.getRequestGlobalVariable("inpOrg", "ReportProjectProfitabilityJR|Org");
		String strProject = vars.getRequestGlobalVariable("inpcProjectId", "ReportProjectProfitabilityJR|Project");
		String strProjectType = vars.getRequestGlobalVariable("inpProjectType", "ReportProjectProfitabilityJR|ProjectType");
		String strResponsible = vars.getRequestGlobalVariable("inpResponsible", "ReportProjectProfitabilityJR|Responsible");
		String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportProjectProfitabilityJR|DateFrom");
		String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportProjectProfitabilityJR|DateTo");
		String strDateFrom2 = vars.getRequestGlobalVariable("inpDateFrom2", "ReportProjectProfitabilityJR|DateFrom2");
		String strDateTo2 = vars.getRequestGlobalVariable("inpDateTo2", "ReportProjectProfitabilityJR|DateTo2");
		String strExpand = vars.getRequestGlobalVariable("inpExpand", "ReportProjectProfitabilityJR|Expand");
		String strPartner = vars.getRequestGlobalVariable("inpcBPartnerId", "ReportProjectProfitabilityJR|Partner");
		String strOutput="pdf";
		printPageDataHtml(response, vars, strOrg, strProject, strProjectType, strResponsible, strDateFrom, strDateTo, strExpand, strPartner, strDateFrom2, strDateTo2, strOutput);
    } else pageError(response);
  }

  
  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strOrg, String strProject, String strProjectType, String strResponsible, String strDateFrom, String strDateTo, String strExpand, String strPartner, String strDateFrom2, String strDateTo2, String strOutput)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    
    String discard[]={"discard"};
    strTreeOrg = strOrg;
    if (strExpand.equals("Y")) treeOrg(vars, strOrg);
    ReportProjectProfitabilityData[] data = null;
    data = ReportProjectProfitabilityData.select(this, strDateFrom2, DateTimeData.nDaysAfter(this, strDateTo2,"1"), strTreeOrg, Utility.getContext(this, vars, "#User_Client", "ReportProjectProfitabilityJR"), strDateFrom , DateTimeData.nDaysAfter(this, strDateTo,"1"), strProjectType, strProject, strResponsible, strPartner);

    if (data == null || data.length == 0) {
      data = ReportProjectProfitabilityData.set();
      discard[0] = "discardAll";
    }
    
      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportProjectProfitabilityJR.jrxml";
      
      if (strOutput.equals("pdf")) response.setHeader("Content-disposition", "inline; filename=ReportProjectProfitabilityJR.pdf");

       
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("REPORT_TITLE", classInfo.name);
	
	renderJR(vars, response, strReportName, strOutput, parameters, data, null );
  }

  
   void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strOrg, String strProject, String strProjectType, String strResponsible, String strDateFrom, String strDateTo, String strExpand, String strPartner, String strDateFrom2, String strDateTo2)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
   
    XmlDocument xmlDocument;
    strTreeOrg = strOrg;
    if (strExpand.equals("Y")) treeOrg(vars, strOrg);
    
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportProjectProfitabilityJR").createXmlDocument();
     

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProjectProfitabilityJR", false, "", "", "",false, "ad_reports", strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportProjectProfitabilityJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportProjectProfitabilityJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportProjectProfitabilityJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportProjectProfitabilityJR");
      vars.removeMessage("ReportProjectProfitabilityJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");

    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	xmlDocument.setParameter("dateFrom2", strDateFrom2);
    xmlDocument.setParameter("dateFromdisplayFormat2", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat2", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo2", strDateTo2);
    xmlDocument.setParameter("dateTodisplayFormat2", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat2", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("orgid", strOrg);
    xmlDocument.setParameter("project", strProject);
    xmlDocument.setParameter("projecttype", strProjectType);
    xmlDocument.setParameter("responsible", strResponsible);
    xmlDocument.setParameter("partnerid", strPartner);
    xmlDocument.setParameter("expand", strExpand);


    try {
      ComboTableData comboTableData = null;

      comboTableData = new ComboTableData(vars, this, "TABLE", "Responsible_ID", "Responsible employee", "", Utility.getContext(this, vars, "#User_Org", "ReportProjectProfitabilityJR"), Utility.getContext(this, vars, "#User_Client", "ReportProjectProfitabilityJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectProfitabilityJR", strResponsible);
      xmlDocument.setData("reportResponsible","liststructure", comboTableData.select(false));
      comboTableData = null;

      comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportProjectProfitabilityJR"), Utility.getContext(this, vars, "#User_Client", "ReportProjectProfitabilityJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectProfitabilityJR", strOrg);
      xmlDocument.setData("reportAD_Org_ID","liststructure", comboTableData.select(false));
      comboTableData = null;

      comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Project_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportProjectProfitabilityJR"), Utility.getContext(this, vars, "#User_Client", "ReportProjectProfitabilityJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectProfitabilityJR", strProject);
      xmlDocument.setData("reportC_Project_ID","liststructure", comboTableData.select(false));
      comboTableData = null;

      comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_ProjectType_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportProjectProfitabilityJR"), Utility.getContext(this, vars, "#User_Client", "ReportProjectProfitabilityJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectProfitabilityJR", strProjectType);
      xmlDocument.setData("reportC_ProjectType_ID","liststructure", comboTableData.select(false));
      comboTableData = null;

    } catch (Exception e) {throw new ServletException(e);}

    out.println(xmlDocument.print());
    out.close();
  }
  
  public String getServletInfo() {
    return "Servlet ReportProjectProfitabilityJR. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method

  void treeOrg(VariablesSecureApp vars, String strOrg) throws ServletException{
    ReportProjectProfitabilityData[] dataOrg = ReportProjectProfitabilityData.selectOrg(this, strOrg, vars.getClient());
    for (int i = 0; i<dataOrg.length; i++) {
      strTreeOrg += "," + dataOrg[i].nodeId;
      if (dataOrg[i].issummary.equals("Y")) treeOrg(vars, dataOrg[i].nodeId);
    }
    return;
  }
}
