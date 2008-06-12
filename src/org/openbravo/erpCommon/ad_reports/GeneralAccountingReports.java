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

import org.openbravo.erpCommon.security.WarehouseData;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.ad_forms.AcctServerData;
import org.openbravo.erpCommon.businessUtility.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.utility.DateTimeData;

public class GeneralAccountingReports extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
   	  String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId", "ReportGeneralLedger|cAcctSchemaId", "");	
      String strAgno = vars.getGlobalVariable("inpAgno", "GeneralAccountingReports|agno", "");
      String strAgnoRef = vars.getGlobalVariable("inpAgnoRef", "GeneralAccountingReports|agnoRef", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "GeneralAccountingReports|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "GeneralAccountingReports|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef", "GeneralAccountingReports|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef", "GeneralAccountingReports|dateToRef", "");
      String strElementValue = vars.getGlobalVariable("inpcElementvalueId", "GeneralAccountingReports|C_ElementValue_ID", "");
      String strConImporte = vars.getGlobalVariable("inpConImporte", "GeneralAccountingReports|conImporte", "N");
      String strConCodigo = vars.getGlobalVariable("inpConCodigo", "GeneralAccountingReports|conCodigo", "N");
      String strOrg = vars.getGlobalVariable("inpOrganizacion", "GeneralAccountingReports|organizacion", vars.getOrg());
      String strLevel = vars.getGlobalVariable("inpLevel", "GeneralAccountingReports|level", "");
      printPageDataSheet(response, vars, strAgno, strAgnoRef, strDateFrom, strDateTo, strDateFromRef, strDateToRef, strElementValue, strConImporte, strOrg, strLevel, strConCodigo, strcAcctSchemaId);
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId", "ReportGeneralLedger|cAcctSchemaId");
      String strAgno = vars.getRequiredGlobalVariable("inpAgno", "GeneralAccountingReports|agno");
      String strAgnoRef = vars.getRequiredGlobalVariable("inpAgnoRef", "GeneralAccountingReports|agnoRef");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom", "GeneralAccountingReports|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "GeneralAccountingReports|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef", "GeneralAccountingReports|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef", "GeneralAccountingReports|dateToRef");
      String strElementValue = vars.getRequiredGlobalVariable("inpcElementvalueId", "GeneralAccountingReports|C_ElementValue_ID");
      String strConImporte = vars.getRequestGlobalVariable("inpConImporte", "GeneralAccountingReports|conImporte");
      String strConCodigo = vars.getRequestGlobalVariable("inpConCodigo", "GeneralAccountingReports|conCodigo");
      String strOrg = vars.getRequestGlobalVariable("inpOrganizacion", "GeneralAccountingReports|organizacion");
      String strLevel = vars.getRequestGlobalVariable("inpLevel", "GeneralAccountingReports|level");
      printPagePDF(response, vars, strAgno, strAgnoRef, strDateFrom, strDateTo, strDateFromRef, strDateToRef, strElementValue, strConImporte, strOrg, strLevel, strConCodigo, strcAcctSchemaId);
    } else pageError(response);
  }

  void printPagePDF(HttpServletResponse response, VariablesSecureApp vars, String strAgno, String strAgnoRef, String strDateFrom, String strDateTo, String strDateFromRef, String strDateToRef, String strElementValue, String strConImporte, String strOrg, String strLevel, String strConCodigo, String strcAcctSchemaId) throws IOException,ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: pdf");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/GeneralAccountingReportsPDF").createXmlDocument();

    GeneralAccountingReportsData[] strGroups = GeneralAccountingReportsData.selectGroups(this, strElementValue);
    strGroups[strGroups.length-1].pagebreak = "";

    String[][] strElementValueDes = new String[strGroups.length][];
    if (log4j.isDebugEnabled()) log4j.debug("strElementValue:"+strElementValue+" - strGroups.length:"+strGroups.length);
    for (int i=0; i<strGroups.length; i++) {  
      GeneralAccountingReportsData[] strElements = GeneralAccountingReportsData.selectElements(this, strGroups[i].id); 
      strElementValueDes[i] = new String[strElements.length];
      if (log4j.isDebugEnabled()) log4j.debug("strElements.length:"+strElements.length);
      for (int j=0; j<strElements.length; j++){
        strElementValueDes[i][j] = strElements[j].id;
      }
    }


    String strTreeOrg = GeneralAccountingReportsData.treeOrg(this, vars.getClient());
    AccountTree[] acct = new AccountTree[strGroups.length];

    AccountTreeData[][] elements = new AccountTreeData[strGroups.length][];

    WindowTreeData[] dataTree = WindowTreeData.selectTreeID(this, vars.getClient(), "EV");
    String TreeID = "";
    if (dataTree!=null && dataTree.length!=0) TreeID = dataTree[0].id;

    for (int i=0; i<strGroups.length; i++) {
      if (vars.getLanguage().equals("en_US")){
        elements[i] = AccountTreeData.select(this, strConCodigo, TreeID);
      } else {
        elements[i] = AccountTreeData.selectTrl(this, strConCodigo,vars.getLanguage(), TreeID );
      }
      AccountTreeData[] accounts = AccountTreeData.selectAcct(this, Utility.getContext(this, vars, "#User_Org", "GeneralAccountingReports"), Utility.getContext(this, vars, "#User_Client", "GeneralAccountingReports"), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strcAcctSchemaId, Tree.getMembers(this, strTreeOrg, strOrg), strAgno, strDateFromRef, DateTimeData.nDaysAfter(this, strDateToRef,"1"), strAgnoRef);

      {
        String strIncomeSummary = GeneralAccountingReportsData.incomesummary(this, strcAcctSchemaId);
        if (log4j.isDebugEnabled()) log4j.debug("*********** strIncomeSummary: " + strIncomeSummary);
        String strISyear = processIncomeSummary(strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), strAgno, strTreeOrg, strOrg, strcAcctSchemaId);
        if (log4j.isDebugEnabled()) log4j.debug("*********** strISyear: " + strISyear);
        String strISyearRef = processIncomeSummary(strDateFromRef, DateTimeData.nDaysAfter(this, strDateToRef,"1"), strAgnoRef, strTreeOrg, strOrg, strcAcctSchemaId);
        if (log4j.isDebugEnabled()) log4j.debug("*********** strISyearRef: " + strISyearRef);
        accounts = appendRecords(accounts,   strIncomeSummary, strISyear, strISyearRef);

      }
      acct[i] = new AccountTree(vars, this, elements[i], accounts, strElementValueDes[i]);
      if (acct[i]!=null) {
        acct[i].filter(strConImporte.equals("Y"), strLevel, false);
      } else if (log4j.isDebugEnabled()) log4j.debug("acct null!!!");
    }


    xmlDocument.setData("group", strGroups);

    /*
    xmlDocument.setParameter("agno", "Ejercicio "+strAgno);
    xmlDocument.setParameter("agno2", "Ejercicio "+strAgno);
    xmlDocument.setParameter("column", "Ejercicio "+strAgno);
    xmlDocument.setParameter("columnRef", "Ejercicio "+strAgnoRef);
    xmlDocument.setParameter("column1", "Ejercicio "+strAgno);
    xmlDocument.setParameter("columnRef1", "Ejercicio "+strAgnoRef);
    */
    xmlDocument.setParameter("agno", strAgno);
    xmlDocument.setParameter("agno2", strAgno);
    xmlDocument.setParameter("column", strAgno);
    xmlDocument.setParameter("columnRef", strAgnoRef);
    xmlDocument.setParameter("org", AcctServerData.selectOrgName(this,strOrg));
    xmlDocument.setParameter("column1", strAgno);
    xmlDocument.setParameter("columnRef1", strAgnoRef);
    xmlDocument.setParameter("companyName", GeneralAccountingReportsData.companyName(this, vars.getClient()));
    xmlDocument.setParameter("date", DateTimeData.today(this));
    if (strDateFrom.equals("")) strDateFrom = "01/01/"+strAgno;
    if (strDateTo.equals("")) strDateTo = "31/12/"+strAgno;
    if (strDateFromRef.equals("")) strDateFromRef = "01/01/"+strAgnoRef;
    if (strDateToRef.equals("")) strDateToRef = "31/12/"+strAgnoRef;
    xmlDocument.setParameter("period", strDateFrom + " - " + strDateTo);
    xmlDocument.setParameter("periodRef", strDateFromRef + " - " + strDateToRef);
    xmlDocument.setParameter("agnoInitial", strAgno);
    xmlDocument.setParameter("agnoRef", strAgnoRef);



    xmlDocument.setParameter("principalTitle", GeneralAccountingReportsData.rptTitle(this, strElementValue));


    AccountTreeData[][] trees= new AccountTreeData[strGroups.length][];

    for (int i=0; i<strGroups.length; i++)
      trees[i] = acct[i].getAccounts();

    xmlDocument.setDataArray("reportDetail","structure1",trees);

    String strResult = xmlDocument.print();
    renderFO(strResult, response);
  }

  AccountTreeData[] appendRecords(AccountTreeData[] data, String strIncomeSummary, String strISyear, String strISyearRef) throws ServletException {
    if (data==null || strIncomeSummary==null || strIncomeSummary.equals("") || strISyear==null || strISyear.equals("") || strISyearRef==null || strISyearRef.equals("")) return data;
    AccountTreeData[] data2 = new AccountTreeData[data.length + 1];
    boolean found = false;
    for (int i=0;i<data.length;i++) {
      if (data[i].id.equals(strIncomeSummary)) {
        found=true;
        double isYear = Double.valueOf(strISyear).doubleValue();
        double isYearRef = Double.valueOf(strISyearRef).doubleValue();
        data[i].qty = Double.toString(Double.valueOf(data[i].qty).doubleValue() + isYear);
        data[i].qtycredit = Double.toString(Double.valueOf(data[i].qtycredit).doubleValue() + isYear);
        data[i].qtyRef = Double.toString(Double.valueOf(data[i].qtyRef).doubleValue() + isYearRef);
        data[i].qtycreditRef = Double.toString(Double.valueOf(data[i].qtycreditRef).doubleValue() + isYearRef);
      }
      data2[i] = data[i];
    }
    if (!found) {
      data2[data2.length-1] = new AccountTreeData();
      data2[data2.length-1].id = strIncomeSummary;
      data2[data2.length-1].qty = strISyear;
      data2[data2.length-1].qtycredit = strISyear;
      data2[data2.length-1].qtyRef = strISyearRef;
      data2[data2.length-1].qtycreditRef = strISyearRef;
    } else return data;
    return data2;
  }

  String processIncomeSummary(String strDateFrom, String strDateTo, String strAgno, String strTreeOrg, String strOrg, String strcAcctSchemaId) throws ServletException, IOException {
    String strISRevenue = GeneralAccountingReportsData.selectPyG(this, "R", strDateFrom, strDateTo, strcAcctSchemaId, strAgno, Tree.getMembers(this, strTreeOrg, strOrg));
    String strISExpense = GeneralAccountingReportsData.selectPyG(this, "E", strDateFrom, strDateTo, strcAcctSchemaId, strAgno, Tree.getMembers(this, strTreeOrg, strOrg));
    BigDecimal totalRevenue = new BigDecimal(strISRevenue);
    BigDecimal totalExpense = new BigDecimal(strISExpense);
    BigDecimal total = totalRevenue.add(totalExpense);
    if (log4j.isDebugEnabled()) log4j.debug(total.toString());
    return total.toString();
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strAgno, String strAgnoRef, String strDateFrom, String strDateTo, String strDateFromRef, String strDateToRef, String strElementValue, String strConImporte, String strOrg, String strLevel, String strConCodigo, String strcAcctSchemaId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/GeneralAccountingReports").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GeneralAccountingReports", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.GeneralAccountingReports");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "GeneralAccountingReports.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GeneralAccountingReports.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GeneralAccountingReports");
      vars.removeMessage("GeneralAccountingReports");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("agno", strAgno);
    xmlDocument.setParameter("agnoRef", strAgnoRef);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRef", strDateFromRef);
    xmlDocument.setParameter("dateFromRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRef", strDateToRef);
    xmlDocument.setParameter("dateToRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("conImporte", strConImporte);
    xmlDocument.setParameter("conCodigo", strConCodigo);
    xmlDocument.setParameter("organizacion", strOrg);
    xmlDocument.setParameter("C_ElementValue_ID", strElementValue);
    xmlDocument.setParameter("level", strLevel);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setData("reportC_ACCTSCHEMA_ID","liststructure",ReportGeneralLedgerData.selectC_ACCTSCHEMA_ID(this, Utility.getContext(this, vars, "#User_Org", "GeneralAccountingReports"), Utility.getContext(this, vars, "#User_Client", "GeneralAccountingReports"), strcAcctSchemaId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_ElementValue level", "", Utility.getContext(this, vars, "#User_Org", "GeneralAccountingReports"), Utility.getContext(this, vars, "#User_Client", "GeneralAccountingReports"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "GeneralAccountingReports", "");
      xmlDocument.setData("reportLevel","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportAD_ORGID", "liststructure", GeneralAccountingReportsData.selectCombo(this, vars.getRole()));
    xmlDocument.setData("reportC_ElementValue_ID","liststructure", GeneralAccountingReportsData.selectRpt(this, Utility.getContext(this, vars, "#User_Org", "GeneralAccountingReports"), Utility.getContext(this, vars, "#User_Client", "GeneralAccountingReports"), strcAcctSchemaId));
    
    xmlDocument.setParameter("accountingReports", arrayDobleEntrada("arrAccountingReports", GeneralAccountingReportsData.selectRptDouble(this)));
    /*try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Acct_Rpt_ID", "", "", Utility.getContext(this, vars, "#User_Org", "GeneralAccountingReports"), Utility.getContext(this, vars, "#User_Client", "GeneralAccountingReports"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "GeneralAccountingReports", "");
      xmlDocument.setData("reportC_ElementValue_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }*/


    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet GeneralAccountingReportsData";
  } // end of getServletInfo() method
}

