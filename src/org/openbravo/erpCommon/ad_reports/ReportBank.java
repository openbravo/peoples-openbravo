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

import org.openbravo.erpCommon.ad_combos.AccountNumberComboData;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportBank extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportBank|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportBank|DateTo", "");
      String strcbankaccount = vars.getGlobalVariable("inpmProductId", "ReportBank|C_Bankaccount_ID", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strcbankaccount);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportBank|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportBank|DateTo");
      String strcbankaccount = vars.getRequestGlobalVariable("inpcBankAccountId", "ReportBank|C_Bankaccount_ID");
      printPageDataHtml(response, vars, strDateFrom, strDateTo, strcbankaccount);
    } else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcbankaccount)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String strMessage="";
   
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportBank").createXmlDocument();
  
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBank", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 

    try {
      KeyMap key = new KeyMap(this, vars, "ReportBank.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportBank");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportBank.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportBank.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportBank");
      vars.removeMessage("ReportBank");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("cBankAccount", strcbankaccount);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramMessage", (strMessage.equals("")?"":"alert('" + strMessage + "');"));
    xmlDocument.setData("reportC_ACCOUNTNUMBER","liststructure",AccountNumberComboData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank")));
    
    out.println(xmlDocument.print());
    out.close();
  }
  
   void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strcbankaccount)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String strMessage="";
    String discard[]={"sectionAmount"};
    XmlDocument xmlDocument=null;
    ReportBankData[] data=null;
    if (strDateFrom.equals("") && strDateTo.equals("")) {
     xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportBank", discard).createXmlDocument();
     data = ReportBankData.set();
     if (vars.commandIn("FIND")) {
         strMessage=Utility.messageBD(this, "BothDatesCannotBeBlank", vars.getLanguage()); 
         log4j.warn("Both dates are blank");
       }
       ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBank", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
        toolbar.prepareSimpleToolBarTemplate();
        xmlDocument.setParameter("toolbar", toolbar.toString()); 
        
        xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
        xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
        xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
        xmlDocument.setParameter("theme", vars.getTheme());
        xmlDocument.setParameter("cBankAccount", strcbankaccount);
        xmlDocument.setParameter("dateFrom", strDateFrom);
        xmlDocument.setParameter("dateTo", strDateTo);
        xmlDocument.setParameter("paramMessage", (strMessage.equals("")?"":"alert('" + strMessage + "');"));
        xmlDocument.setData("reportC_ACCOUNTNUMBER","liststructure",AccountNumberComboData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank")));
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportBankEdit").createXmlDocument();
      data = ReportBankData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank"),strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strcbankaccount);
      xmlDocument.setParameter("sumAmount", ReportBankData.BeginningBalance(this, Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank"),strDateFrom, strcbankaccount));
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("theme", vars.getTheme());
    }

    
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportBank. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}

