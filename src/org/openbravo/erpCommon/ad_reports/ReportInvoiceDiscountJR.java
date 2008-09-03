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

public class ReportInvoiceDiscountJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")){
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportInvoiceDiscountJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportInvoiceDiscountJR|dateTo", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN", "ReportInvoiceDiscountJR|partner", "");
      String strDiscount = vars.getGlobalVariable("inpDiscount", "ReportInvoiceDiscountJR|discount", "N");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcBpartnerId, strDiscount);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportInvoiceDiscountJR|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportInvoiceDiscountJR|dateTo", "");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN", "ReportInvoiceDiscountJR|partner");
      String strDiscount = vars.getRequestGlobalVariable("inpDiscount", "ReportInvoiceDiscountJR|discount");
      printPageDataHtml(response, vars, strDateFrom, strDateTo, strcBpartnerId, strDiscount);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId, String strDiscount) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
 
    if (strDiscount.equals("")) strDiscount = "N";

	 String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportInvoiceDiscountJR.jrxml";
	 String strOutput = "html";
      if (strOutput.equals("pdf")) response.setHeader("Content-disposition", "inline; filename=ReportInvoiceDiscountEdit.pdf");
      ReportInvoiceDiscountData[] data =  ReportInvoiceDiscountData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportInvoiceDiscountJR"), Utility.getContext(this, vars, "#User_Org", "ReportInvoiceDiscountJR"), strDateFrom,DateTimeData.nDaysAfter(this, strDateTo,"1"), strcBpartnerId, (strDiscount.equals("N"))?"":"discount");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("REPORT_TITLE", classInfo.name);
	  String strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " "+strDateFrom+" " + Utility.messageBD(this, "To", vars.getLanguage()) + " "+strDateTo;
	  parameters.put("REPORT_SUBTITLE", strSubTitle);
    
      renderJR(vars, response, strReportName, strOutput, parameters, data, null );

  }
  
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId, String strDiscount) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    
    XmlDocument xmlDocument=null;
    if (strDiscount.equals("")) strDiscount = "N";
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoiceDiscountJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInvoiceDiscountReportInvoiceDiscountJR", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportInvoiceDiscountJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportInvoiceDiscountJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportInvoiceDiscountJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoiceDiscountJR");
      vars.removeMessage("ReportInvoiceDiscountJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("discount", strDiscount);
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", ReportInvoiceDiscountData.selectBpartner(this, Utility.getContext(this, vars, "#User_Org", "ReportInvoiceDiscountJR"), Utility.getContext(this, vars, "#User_Client", "ReportInvoiceDiscountJR"), strcBpartnerId));
    
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  
  /*void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcBpartnerId, String strDiscount) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    String discard[]={"discard"};
    XmlDocument xmlDocument=null;
    if (strDiscount.equals("")) strDiscount = "N";
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportInvoiceDiscount").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInvoiceDiscountReportInvoiceDiscount", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportInvoiceDiscount");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportInvoiceDiscount.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportInvoiceDiscount.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInvoiceDiscount");
      vars.removeMessage("ReportInvoiceDiscount");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("discount", strDiscount);
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", ReportInvoiceDiscountData.selectBpartner(this, Utility.getContext(this, vars, "#User_Org", "ReportInvoiceDiscount"), Utility.getContext(this, vars, "#User_Client", "ReportInvoiceDiscount"), strcBpartnerId));
    xmlDocument.setData("structure1", ReportInvoiceDiscountData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportInvoiceDiscount"), Utility.getContext(this, vars, "#User_Org", "ReportInvoiceDiscount"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strcBpartnerId, (strDiscount.equals("N"))?"":"discount"));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }*/


  public String getServletInfo() {
    return "Servlet ReportInvoiceDiscount. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}

