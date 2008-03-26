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
import org.openbravo.erpCommon.businessUtility.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.ad_combos.OrganizationComboData;

import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;


import java.util.*;

public class ReportGeneralLedgerJournal extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled()) log4j.debug("Command: "+vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId", "ReportGeneralLedger|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportGeneralLedgerJournal|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportGeneralLedgerJournal|DateTo", "");
      String strDocument = vars.getGlobalVariable("inpDocument", "ReportGeneralLedgerJournal|Document", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedgerJournal|Org", "0");
    //  String strRecord = vars.getGlobalVariable("inpRecord", "ReportGeneralLedgerJournal|Record");
     // String strTable = vars.getGlobalVariable("inpTable", "ReportGeneralLedgerJournal|Table");
      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strDocument, strOrg, strTable, strRecord, "", strcAcctSchemaId);
    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportGeneralLedgerJournal|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportGeneralLedgerJournal|Record");
      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportGeneralLedgerJournal.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", "", strTable, strRecord, "","");
    } else if (vars.commandIn("DIRECT2")) {
      String strFactAcctGroupId = vars.getGlobalVariable("inpFactAcctGroupId", "ReportGeneralLedgerJournal|FactAcctGroupId");
      setHistoryCommand(request, "DIRECT2");
      vars.setSessionValue("ReportGeneralLedgerJournal.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", "", "", "", strFactAcctGroupId, "");
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId", "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportGeneralLedgerJournal|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedgerJournal|DateTo");
      String strDocument = vars.getRequestGlobalVariable("inpDocument", "ReportGeneralLedgerJournal|Document");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedgerJournal|Org","0");
      vars.setSessionValue("ReportGeneralLedgerJournal.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strDocument, strOrg, "", "", "", strcAcctSchemaId);
    } else if (vars.commandIn("PDF","XLS")) {
      if (log4j.isDebugEnabled()) log4j.debug("PDF");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId", "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportGeneralLedgerJournal|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedgerJournal|DateTo");
      String strDocument = vars.getRequestGlobalVariable("inpDocument", "ReportGeneralLedgerJournal|Document");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedgerJournal|Org","0");
     // String strRecord = vars.getGlobalVariable("inpRecord", "ReportGeneralLedgerJournal|Record");
     // String strTable = vars.getGlobalVariable("inpTable", "ReportGeneralLedgerJournal|Table");
      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      vars.setSessionValue("ReportGeneralLedgerJournal.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(response, vars, strDateFrom, strDateTo, strDocument, strOrg, strTable, strRecord, "", strcAcctSchemaId);
    }else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedgerJournal.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedgerJournal");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("ReportGeneralLedgerJournal.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("ReportGeneralLedgerJournal.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedgerJournal.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedgerJournal");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("ReportGeneralLedgerJournal.initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strDocument, String strOrg, String strTable, String strRecord, String strFactAcctGroupId, String strcAcctSchemaId) throws IOException, ServletException {
	String  strRecordRange="500";
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportGeneralLedgerJournal.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    ReportGeneralLedgerJournalData[] data=null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportGeneralLedgerJournal", false, "", "", "imprimir();return false;",false, "ad_reports",  strReplaceWith, false,  true);
    if (vars.commandIn("DEFAULT","FIND")){
      String strTreeOrg = ReportGeneralLedgerJournalData.treeOrg(this, vars.getClient());
      String strOrgFamily = getFamily(strTreeOrg, strOrg);
      if (strRecord.equals("")) {
        data = ReportGeneralLedgerJournalData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strDocument, strcAcctSchemaId, strOrgFamily, initRecordNumber, intRecordRange);
        if (data!=null && data.length > 0) strPosition = ReportGeneralLedgerJournalData.selectCount(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strDocument, strcAcctSchemaId, strOrgFamily, data[0].dateacct, data[0].identifier);
      } else {
   	    data = ReportGeneralLedgerJournalData.selectDirect(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), strTable, strRecord, initRecordNumber, intRecordRange);
        if (data!=null && data.length > 0) strPosition = ReportGeneralLedgerJournalData.selectCountDirect(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"),strTable, strRecord, data[0].dateacct, data[0].identifier);        
      }
    }else if (vars.commandIn("DIRECT")){
      data = ReportGeneralLedgerJournalData.selectDirect(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), strTable, strRecord, initRecordNumber, intRecordRange);
      if (data!=null && data.length > 0) strPosition = ReportGeneralLedgerJournalData.selectCountDirect(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"),strTable, strRecord, data[0].dateacct, data[0].identifier);
    }else if (vars.commandIn("DIRECT2")){
      data = ReportGeneralLedgerJournalData.selectDirect2(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), strFactAcctGroupId, initRecordNumber, intRecordRange);
      if (data!=null && data.length > 0) strPosition = ReportGeneralLedgerJournalData.selectCountDirect2(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"),strFactAcctGroupId, data[0].dateacct, data[0].identifier);
    }
    if (data==null || data.length==0) {
      String discard[]={"sectionSchema"};
      toolbar.prepareRelationBarTemplate(false, false,"submitCommandForm('XLS', false, null, 'ReportGeneralLedgerJournal.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportGeneralLedgerJournal", discard).createXmlDocument();
      data = ReportGeneralLedgerJournalData.set("0");
      data[0].rownum = "0";
    } else {
      /*  String[] discard = {"withoutPrevious", "withoutNext"};
          if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
          if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
          xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportGeneralLedgerJournal", discard).createXmlDocument();
          */
      boolean hasPrevious = !(data==null || data.length==0 || initRecordNumber<=1);
      boolean hasNext     = !(data==null || data.length==0 || data.length<intRecordRange);
      toolbar.prepareRelationBarTemplate(hasPrevious, hasNext,"submitCommandForm('XLS', false, null, 'ReportGeneralLedgerJournal.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportGeneralLedgerJournal").createXmlDocument();
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_DocType DocBaseType", "", Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedgerJournal"), Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedgerJournal"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportGeneralLedgerJournal", strDocument);
      xmlDocument.setData("reportDocument","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportGeneralLedgerJournal");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportGeneralLedgerJournal.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportGeneralLedgerJournal.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportGeneralLedgerJournal");
      vars.removeMessage("ReportGeneralLedgerJournal");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("document", strDocument);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setData("reportAD_ORGID", "liststructure", GeneralAccountingReportsData.selectCombo(this, vars.getRole()));
    xmlDocument.setData("reportC_ACCTSCHEMA_ID","liststructure",ReportGeneralLedgerData.selectC_ACCTSCHEMA_ID(this, Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("groupId", strPosition);
    xmlDocument.setParameter("paramRecord", strRecord);
    xmlDocument.setParameter("paramTable", strTable);
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  void printPagePDF(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strDocument, String strOrg, String strTable, String strRecord, String strFactAcctGroupId, String strcAcctSchemaId) throws IOException, ServletException {

    ReportGeneralLedgerJournalData[] data=null;

    String strTreeOrg = ReportGeneralLedgerJournalData.treeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    
    if (strRecord.equals("")) 
      data = ReportGeneralLedgerJournalData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strDocument, strcAcctSchemaId, strOrgFamily);
    else 
      data = ReportGeneralLedgerJournalData.selectDirect(this, Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Org", "ReportGeneralLedger"), strTable, strRecord);
    
    String strSubtitle = Utility.messageBD(this, "CompanyName",vars.getLanguage())+": "+ ReportGeneralLedgerData.selectCompany(this, vars.getClient());
    
    if (strDateFrom.equals("")&&strDateTo.equals(""))
      strSubtitle += " - "+Utility.messageBD(this, "Period",vars.getLanguage())+": "+strDateFrom + " - " + strDateTo;

    String strOutput = vars.commandIn("PDF")?"pdf":"xls";
    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportGeneralLedgerJournal.jrxml";
    
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Title", classInfo.name);
    parameters.put("Subtitle",strSubtitle);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null );
  }

  
  public String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, (strChild==null||strChild.equals(""))?"0":strChild);
    /*    ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree, strChild);
          String strFamily = "";
          if(data!=null && data.length>0) {
          for (int i = 0;i<data.length;i++){
          if (i>0) strFamily = strFamily + ",";
          strFamily = strFamily + data[i].id;
          }
          return strFamily += "";
          }else return "'1'";*/
  }

  public String getServletInfo() {
    return "Servlet ReportGeneralLedgerJournal. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}

