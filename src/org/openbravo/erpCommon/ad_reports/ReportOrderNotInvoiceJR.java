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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
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

import java.util.*;

import org.openbravo.erpCommon.ad_combos.OrganizationComboData;
import org.openbravo.erpCommon.utility.ComboTableData;
//import org.openbravo.erpCommon.ad_combos.AdOrgTreeComboData;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportOrderNotInvoiceJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")){
      String strdateFrom = vars.getGlobalVariable("inpDateFrom", "ReportOrderNotInvoiceJR|dateFrom", "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportOrderNotInvoiceJR|dateTo", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId", "ReportOrderNotInvoiceJR|bpartner", "");
      String strCOrgId = vars.getGlobalVariable("inpOrg", "ReportOrderNotInvoiceJR|Org", "");
      String strInvoiceRule = vars.getGlobalVariable("inpInvoiceRule", "ReportOrderNotInvoiceJR|invoiceRule", "");
      String strDetail = vars.getStringParameter("inpDetail", "0");
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcBpartnetId, strCOrgId, strInvoiceRule, strDetail);
    }else if (vars.commandIn("FIND")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportOrderNotInvoiceJR|dateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportOrderNotInvoiceJR|dateTo");
      String strcBpartnetId = vars.getRequestGlobalVariable("inpcBPartnerId", "ReportOrderNotInvoiceJR|bpartner");
      String strCOrgId = vars.getRequestGlobalVariable("inpOrg", "ReportOrderNotInvoiceJR|Org");
      String strInvoiceRule = vars.getRequestGlobalVariable("inpInvoiceRule", "ReportOrderNotInvoiceJR|invoiceRule");
      String strDetail = vars.getStringParameter("inpDetail", "0");
      printPageHtml(response, vars, strdateFrom, strdateTo, strcBpartnetId, strCOrgId, strInvoiceRule, strDetail);
    }  else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnetId, String  strCOrgId, String strInvoiceRule, String strDetail) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportOrderNotInvoiceFilterJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportOrderNotInvoiceJR", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportOrderNotInvoiceJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportOrderNotInvoiceJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportOrderNotInvoiceJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportOrderNotInvoiceJR");
      vars.removeMessage("ReportOrderNotInvoiceJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strdateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strdateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("detail", strDetail);
    xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);
    xmlDocument.setParameter("paramBPartnerDescription", ReportOrderNotInvoiceData.bPartnerDescription(this, strcBpartnetId));
    xmlDocument.setParameter("invoiceRule", strInvoiceRule);
    xmlDocument.setParameter("adOrgId", strCOrgId);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_Order InvoiceRule", "", Utility.getContext(this, vars, "#User_Org", "ReportOrderNotInvoiceFilterJR"), Utility.getContext(this, vars, "#User_Client", "ReportOrderNotInvoiceJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportOrderNotInvoiceJR", strInvoiceRule);
      xmlDocument.setData("reportInvoiceRule","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    //xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));
    }
    xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnetId, String  strCOrgId, String strInvoiceRule, String strDetail) throws IOException, ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: print html");
 
    ReportOrderNotInvoiceData[] data = null;
    data = ReportOrderNotInvoiceData.select(this, vars.getLanguage(),  Utility.getContext(this, vars, "#User_Client", "ReportOrderNotInvoiceJR"), Utility.getContext(this, vars, "#User_Org", "ReportOrderNotInvoiceJR"), strcBpartnetId, strCOrgId, strInvoiceRule, strdateFrom, DateTimeData.nDaysAfter(this, strdateTo,"1"));
    
    String strOutput = "html";
    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportOrderNotInvoiceJR.jrxml";
    
    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " "+strdateFrom+" " + Utility.messageBD(this, "To", vars.getLanguage()) + " "+strdateTo;
    
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_TITLE", classInfo.name);
    parameters.put("REPORT_SUBTITLE", strSubTitle);
    parameters.put("Detail",new Boolean(strDetail.equals("-1")));
    renderJR(vars, response, strReportName, strOutput, parameters, data, null );
    
  }

  public String getServletInfo() {
    return "Servlet ReportOrderNotInvoiceFilter. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}

