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

public class ReportDebtPayment extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT", "DIRECT")) {
      String strcbankaccount = vars.getGlobalVariable("inpmProductId", "ReportDebtPayment|C_Bankaccount_ID", "");
      String strC_BPartner_ID = vars.getGlobalVariable("inpBpartnerId", "ReportDebtPayment|C_BPartner_ID", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportDebtPayment|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportDebtPayment|DateTo", "");
      String strCal1 = vars.getGlobalVariable("inpCal1", "ReportDebtPayment|Cal1", "");
      String strCalc2 = vars.getGlobalVariable("inpCalc2", "ReportDebtPayment|Cal2", "");
      String strPaymentRule = vars.getGlobalVariable("inpCPaymentRuleId", "ReportDebtPayment|PaymentRule", "");
      String strSettle = vars.getGlobalVariable("inpSettle", "ReportDebtPayment|Settle", "");
      String strConciliate = vars.getGlobalVariable("inpConciliate", "ReportDebtPayment|Conciliate", "");
      String strStatus = vars.getGlobalVariable("inpStatus", "ReportDebtPayment|Status", "");
      String strGroup = vars.getGlobalVariable("inpGroup", "ReportDebtPayment|Group", "isGroup");
      String strPending = "";
      String strReceipt = "";
      if (vars.commandIn("DIRECT")) {
        strReceipt = vars.getGlobalVariable("inpReceipt", "ReportDebtPayment|Receipt", "N");
        strPending = vars.getGlobalVariable("inpPending", "ReportDebtPayment|Pending", "");
      }
      else {
        strReceipt = vars.getGlobalVariable("inpReceipt", "ReportDebtPayment|Receipt", "Y");
        strPending = vars.getGlobalVariable("inpPending", "ReportDebtPayment|Pending", "isPending");
      }
      //String strEntry = vars.getGlobalVariable("inpEntry", "ReportDebtPayment|Entry","0");
      setHistoryCommand(request, "DIRECT");
      printPageDataSheet(response, vars, strC_BPartner_ID, strDateFrom, strDateTo, strCal1, strCalc2, strPaymentRule, strSettle, strConciliate, strReceipt, strPending, strcbankaccount, strStatus, strGroup);
    } else if (vars.commandIn("FIND")) {
      String strcbankaccount = vars.getRequestGlobalVariable("inpcBankAccountId", "ReportDebtPayment|C_Bankaccount_ID");
      String strC_BPartner_ID = vars.getRequestGlobalVariable("inpBpartnerId", "ReportDebtPayment|C_BPartner_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportDebtPayment|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportDebtPayment|DateTo");
      String strCal1 = vars.getRequestGlobalVariable("inpCal1", "ReportDebtPayment|Cal1");
      String strCalc2 = vars.getRequestGlobalVariable("inpCal2", "ReportDebtPayment|Cal2");
      String strPaymentRule = vars.getRequestGlobalVariable("inpCPaymentRuleId", "ReportDebtPayment|PaymentRule");
      String strSettle = vars.getRequestGlobalVariable("inpSettle", "ReportDebtPayment|Settle");
      String strConciliate = vars.getRequestGlobalVariable("inpConciliate", "ReportDebtPayment|Conciliate");
      String strPending = vars.getRequestGlobalVariable("inpPending", "ReportDebtPayment|Pending");
      String strGroup = vars.getRequestGlobalVariable("inpGroup", "ReportDebtPayment|Group");      
      String strStatus = vars.getRequestGlobalVariable("inpStatus", "ReportDebtPayment|Status");
      //String strReceipt = vars.getRequestGlobalVariable("inpReceipt", "ReportDebtPayment|Receipt");
      String strReceipt = vars.getStringParameter("inpReceipt").equals("")?"N":vars.getStringParameter("inpReceipt");
      vars.setSessionValue("ReportDebtPayment|Receipt", strReceipt);
      //String strEntry = vars.getGlobalVariable("inpEntry", "ReportDebtPayment|Entry","1");
      setHistoryCommand(request, "DIRECT");
      printPageDataSheet(response, vars, strC_BPartner_ID, strDateFrom, strDateTo, strCal1, strCalc2, strPaymentRule, strSettle, strConciliate, strReceipt, strPending, strcbankaccount, strStatus, strGroup);
    } else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strC_BPartner_ID,  String strDateFrom, String strDateTo, String strCal1, String strCalc2, String strPaymentRule, String strSettle, String strConciliate, String strReceipt, String strPending , String strcbankaccount, String strStatus, String strGroup)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[]={"discard", "discard2", "discard3", "discard4"};
    String strAux = "";
    if (log4j.isDebugEnabled()) log4j.debug("strGroup = " + strGroup);
    if (strPending.equals("") && strConciliate.equals("") && strSettle.equals("")) {
      strAux = "";
    } else {
      if (strPending.equals("isPending")) {
        strAux = "'P'";
      }
      if (strConciliate.equals("isConciliate")) {
        if (!strAux.equals("")) {
          strAux = strAux + ",";
        }
        strAux = strAux + "'C'";
      }
      if (strSettle.equals("isSettle")) {
        if (!strAux.equals("")) {
          strAux = strAux + ",";
        }
        strAux = strAux + "'A'";
      }
      strAux = "(" + strAux + ")";
    }
    XmlDocument xmlDocument;
    ReportDebtPaymentData[] data = null;
    if(!strGroup.equals("")) data= ReportDebtPaymentData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"), Utility.getContext(this, vars, "#User_Org", "ReportDebtPayment"), strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strCal1, strCalc2, strPaymentRule, strReceipt, strStatus, strAux, strcbankaccount);
    else data= ReportDebtPaymentData.selectNoBpartner(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"), Utility.getContext(this, vars, "#User_Org", "ReportDebtPayment"), strC_BPartner_ID, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strCal1, strCalc2, strPaymentRule, strReceipt, strStatus, strAux, strcbankaccount);
    if (data == null || data.length == 0) {
      data = ReportDebtPaymentData.set();
      discard[0] = "sectionBpartner";
      discard[1] = "sectionStatus2";
      discard[2] = "sectionTotal2";
      if(!strGroup.equals("")) {
    	  discard[3] = "sectionDetail2";
      }
      else {
    	  discard[3] = "sectionTotal";
      }
    }
    else {
	    if(!strGroup.equals("")){
	      discard[0] = "sectionDetail2";
	      discard[1] = "sectionStatus2";
	      discard[2] = "sectionTotal2";
	    } else {
	      discard[0] = "sectionBpartner";
	      discard[1] = "sectionTotal";
	    }
    }
    if (vars.commandIn("DEFAULT")) {
      discard[0] = "sectionBpartner";
      discard[1] = "sectionStatus2";
      discard[2] = "sectionTotal2";
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportDebtPayment", discard).createXmlDocument();
      data = ReportDebtPaymentData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportDebtPayment", discard).createXmlDocument();
    }
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportDebtPayment", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportDebtPayment.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportDebtPayment");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportDebtPayment.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportDebtPayment.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportDebtPayment");
      vars.removeMessage("ReportDebtPayment");
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
    xmlDocument.setData("reportC_ACCOUNTNUMBER","liststructure",AccountNumberComboData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"), Utility.getContext(this, vars, "#User_Org", "ReportDebtPayment")));
    xmlDocument.setParameter("paramBPartnerId", strC_BPartner_ID);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amountFrom", strCal1);
    xmlDocument.setParameter("amountTo", strCalc2);
    xmlDocument.setParameter("paymentRule", strPaymentRule);
    xmlDocument.setParameter("settle", strSettle);
    xmlDocument.setParameter("conciliate", strConciliate);
    xmlDocument.setParameter("pending", strPending);
    xmlDocument.setParameter("receipt", strReceipt);
    xmlDocument.setParameter("payable", strReceipt);
    xmlDocument.setParameter("status", strStatus);
    xmlDocument.setParameter("group", strGroup);
    if (log4j.isDebugEnabled()) log4j.debug("diacard = " + discard[0] + " - " + discard[1] + " - " + discard[2]);
    xmlDocument.setParameter("paramBPartnerDescription", ReportDebtPaymentData.bPartnerDescription(this, strC_BPartner_ID));
    if (log4j.isDebugEnabled()) log4j.debug("ListData.select PaymentRule:"+strPaymentRule);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "All_Payment Rule", "", Utility.getContext(this, vars, "#User_Org", "ReportDebtPayment"), Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportDebtPayment", strPaymentRule);
      xmlDocument.setData("reportPaymentRule","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    if (log4j.isDebugEnabled()) log4j.debug("ListData.select Status:"+strPaymentRule);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_DP_Management_Status", "", Utility.getContext(this, vars, "#User_Org", "ReportDebtPayment"), Utility.getContext(this, vars, "#User_Client", "ReportDebtPayment"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportDebtPayment", strStatus);
      xmlDocument.setData("reportStatus","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setData(!strGroup.equals("")?"structure1":"structure2", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportDebtPayment. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}

