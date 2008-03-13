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

public class ReportExpense extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportExpense|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportExpense|dateTo", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId", "ReportExpense|cBpartnerId", "");
      String strPartner = vars.getGlobalVariable("inpPartner", "ReportExpense|partner", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strPartner);
    } else if (vars.commandIn("DIRECT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportExpense|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportExpense|dateTo", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId", "ReportExpense|cBpartnerId", "");
      String strPartner = vars.getGlobalVariable("inpPartner", "ReportExpense|partner", "");
      setHistoryCommand(request, "DIRECT");
      printPageDataHtml(response, vars, strDateFrom, strDateTo, strcBpartnerId, strPartner);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportExpense|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportExpense|dateTo");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId", "ReportExpense|cBpartnerId");
      String strPartner = vars.getRequestGlobalVariable("inpPartner", "ReportExpense|partner");
      setHistoryCommand(request, "DIRECT");
      printPageDataHtml(response, vars, strDateFrom, strDateTo, strcBpartnerId, strPartner);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId, String strPartner)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    ReportExpenseData[] data1 = null;
   
    if (vars.commandIn("DEFAULT") && strDateFrom.equals("") && strDateTo.equals("") && strcBpartnerId.equals("") && strPartner.equals("")){
      /*discard[0] = "sectionPartner";
      data1 = ReportExpenseData.set();
      strDateFrom = DateTimeData.weekBefore(this);
      strDateTo = DateTimeData.today(this);*/
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strPartner);
    } else {
      data1 = ReportExpenseData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportExpense"), Utility.getContext(this, vars, "#User_Org", "ReportExpense"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strcBpartnerId, strPartner);
    }
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportExpenseEdit").createXmlDocument();


    


    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setData("structure1", data1);

    out.println(xmlDocument.print());
    out.close();
  }
  
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId, String strPartner)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;   
   
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportExpense").createXmlDocument();


    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportExpense", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportExpense");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportExpense.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportExpense.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportExpense");
      vars.removeMessage("ReportExpense");
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
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("bPartnerDescription", ReportExpenseData.selectBpartner(this, strcBpartnerId));
    xmlDocument.setParameter("partner", strPartner);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_BPartner_ID", "C_BPartner Employee w Address", "", Utility.getContext(this, vars, "#User_Org", "ReportExpense"), Utility.getContext(this, vars, "#User_Client", "ReportExpense"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportExpense", "");
      xmlDocument.setData("reportC_BPartner_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

   

    out.println(xmlDocument.print());
    out.close();
  }
  
  /*void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId, String strPartner)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    ReportExpenseData[] data1 = null;
    String discard[] = {"discard"};
    if (vars.commandIn("DEFAULT") && strDateFrom.equals("") && strDateTo.equals("") && strcBpartnerId.equals("") && strPartner.equals("")){
      discard[0] = "sectionPartner";
      data1 = ReportExpenseData.set();
      strDateFrom = DateTimeData.weekBefore(this);
      strDateTo = DateTimeData.today(this);
    } else {
      data1 = ReportExpenseData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportExpense"), Utility.getContext(this, vars, "#User_Org", "ReportExpense"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strcBpartnerId, strPartner);
    }
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportExpense", discard).createXmlDocument();


    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportExpense", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportExpense");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportExpense.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportExpense.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportExpense");
      vars.removeMessage("ReportExpense");
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
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("bPartnerDescription", ReportExpenseData.selectBpartner(this, strcBpartnerId));
    xmlDocument.setParameter("partner", strPartner);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_BPartner_ID", "C_BPartner Employee w Address", "", Utility.getContext(this, vars, "#User_Org", "ReportExpense"), Utility.getContext(this, vars, "#User_Client", "ReportExpense"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportExpense", "");
      xmlDocument.setData("reportC_BPartner_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structure1", data1);

    out.println(xmlDocument.print());
    out.close();
  }*/

  public String getServletInfo() {
    return "Servlet ReportExpense. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
