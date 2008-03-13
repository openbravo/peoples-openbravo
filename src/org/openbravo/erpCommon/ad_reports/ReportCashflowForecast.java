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


public class ReportCashflowForecast extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strBankAccount = vars.getRequestGlobalVariable("inpcBankAccountId", "ReportCashflowForecast|AcctNo");
      String strDateFrom    = vars.getRequestGlobalVariable("inpDateFrom", "ReportCashflowForecast|DateFrom");
      String strBreakDate   = vars.getRequestGlobalVariable("inpBreakDate", "ReportCashflowForecast|BreakDate");

      printPageDataSheet(response, vars, strBankAccount, strDateFrom, strBreakDate, true);
    } else if (vars.commandIn("FIND")) {
      String strBankAccount = vars.getRequestGlobalVariable("inpcBankAccountId", "ReportCashflowForecast|AcctNo");
      String strDateFrom    = vars.getRequestGlobalVariable("inpDateFrom", "ReportCashflowForecast|DateFrom");
      String strBreakDate   = vars.getRequestGlobalVariable("inpBreakDate", "ReportCashflowForecast|BreakDate");

      printPageDataSheet(response, vars, strBankAccount, strDateFrom, strBreakDate, false);
    }else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strBankAccount, String strDateMax, String strBreakDate, boolean showDefault) throws IOException, ServletException {
    String[] discard = {"",""};
    XmlDocument xmlDocument = null;

    if (showDefault)
      discard[0] = "subrpt";
    else {
      if (strBreakDate.equals("")) discard[0] = "reportAccountDate";
      else discard[0] = "reportAccount";
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportCashflowForecast", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);

    //    ReportCashflowForecastData[] dataSummary = ReportCashflowForecastData.select(this,Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank"));
    ReportCashflowForecastData[] dataSummary = ReportCashflowForecastData.select(this,strDateMax,"",Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank"));

    if (!showDefault) {    
      ReportCashflowForecastData[][] data  = null;
      ReportCashflowForecastData[] dataAcct  = null;
      ReportCashflowForecastData[] dataDetail  = null;

      // dataAcct = ReportCashflowForecastData.select(this, strDateMax, strBankAccount);
      dataAcct = ReportCashflowForecastData.select(this,strDateMax,strBankAccount,Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank"));
      data = new ReportCashflowForecastData[dataAcct.length][];

      if (log4j.isDebugEnabled()) log4j.debug("length: "+dataAcct.length+" - bankaccount:"+strBankAccount);
      if (dataAcct.length==0) {
        discard[0] = "reportAccountDate";
        discard[1] = "reportAccount";
      } else {
        for (int i =0; i < dataAcct.length; i++){    
          if (strBreakDate.equals(""))
            dataDetail = ReportCashflowForecastData.selectLines(this, vars.getSqlDateFormat(), vars.getLanguage(), dataAcct[i].cBankaccountId, strDateMax, "2 DESC, 1");
          else
            dataDetail = ReportCashflowForecastData.selectLines(this, vars.getSqlDateFormat(), vars.getLanguage(), dataAcct[i].cBankaccountId, strDateMax, "1,2 DESC");
          if (log4j.isDebugEnabled()) log4j.debug("length: "+dataAcct.length+" bankacct:"+dataAcct[i].cBankaccountId+" lenght:"+dataDetail.length);
          data[i] = dataDetail;
        }
      }
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportCashflowForecast",discard).createXmlDocument();

      xmlDocument.setData("structureDetail",dataAcct);
      if (strBreakDate.equals(""))
        xmlDocument.setDataArray("reportAcct","structureAccount",data);
      else
        xmlDocument.setDataArray("reportAcctDate","structureAccount",data);
    } else 
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportCashflowForecast",discard).createXmlDocument();


    //      ReportCashflowForecastData.select(this,Utility.getContext(this, vars, "#User_Client", "ReportBank"), Utility.getContext(this, vars, "#User_Org", "ReportBank"));

    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 
    xmlDocument.setData("reportC_ACCOUNTNUMBER","liststructure",AccountNumberComboData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportCashflowForecast"), Utility.getContext(this, vars, "#User_Org", "ReportCashflowForecast")));
    xmlDocument.setParameter("cBankAccount", strBankAccount);
    xmlDocument.setParameter("dateFrom",strDateMax);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("finalDate",strDateMax);
    xmlDocument.setParameter("date",ReportCashflowForecastData.getDate(this, vars.getSqlDateFormat()));
    xmlDocument.setParameter("date1",ReportCashflowForecastData.getDate(this, vars.getSqlDateFormat()));
    xmlDocument.setParameter("breakDate",strBreakDate.equals("")?"0":"1");
    xmlDocument.setData("structureSummary",dataSummary);

   try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportCashflowForecast");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportCashflowForecast.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportCashflowForecast.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportCashflowForecast");
      vars.removeMessage("ReportCashflowForecast");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }


  public String getServletInfo() {
    return "Servlet ReportCashflowForecast";
  } // end of the getServletInfo() method
}
