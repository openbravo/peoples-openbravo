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
 * All portions are Copyright (C) 2001-2009 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportGeneralLedger extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId", "");
      String strDateFrom = vars
          .getGlobalVariable("inpDateFrom", "ReportGeneralLedger|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo", "");
      String strAmtFrom = vars.getNumericGlobalVariable("inpAmtFrom",
          "ReportGeneralLedger|AmtFrom", "");
      String strAmtTo = vars.getNumericGlobalVariable("inpAmtTo", "ReportGeneralLedger|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO", "");
      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvalueto);
      strcelementvaluefromdes = (strcelementvaluefromdes.equals("null")) ? ""
          : strcelementvaluefromdes;
      strcelementvaluetodes = (strcelementvaluetodes.equals("null")) ? "" : strcelementvaluetodes;
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", "", IsIDFilter.instance);
      String strAll = vars.getGlobalVariable("inpAll", "ReportGeneralLedger|All", "");
      String strHide = vars.getGlobalVariable("inpHideMatched", "ReportGeneralLedger|HideMatched",
          "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strHide,
          strcAcctSchemaId, strcelementvaluefromdes, strcelementvaluetodes);
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportGeneralLedger|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo");
      String strAmtFrom = vars.getNumericRequestGlobalVariable("inpAmtFrom",
          "ReportGeneralLedger|AmtFrom");
      String strAmtTo = vars.getNumericRequestGlobalVariable("inpAmtTo",
          "ReportGeneralLedger|AmtTo");
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO");
      String strcelementvaluefromdes = "", strcelementvaluetodes = "";
      if (!strcelementvaluefrom.equals(""))
        strcelementvaluefromdes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvaluefrom);
      if (!strcelementvalueto.equals(""))
        strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
            strcelementvalueto);
      vars.setSessionValue("inpElementValueIdFrom_DES", strcelementvaluefromdes);
      vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", IsIDFilter.instance);
      String strAll = vars.getStringParameter("inpAll");
      String strHide = vars.getStringParameter("inpHideMatched");
      if (log4j.isDebugEnabled())
        log4j.debug("inpAll: " + strAll);
      if (strAll.equals(""))
        vars.removeSessionValue("ReportGeneralLedger|All");
      else
        strAll = vars.getGlobalVariable("inpAll", "ReportGeneralLedger|All");
      if (strHide.equals(""))
        vars.removeSessionValue("ReportGeneralLedger|HideMatched");
      else
        strHide = vars.getGlobalVariable("inpHideMatched", "ReportGeneralLedger|HideMatched");
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - Find - strcBpartnerId= " + strcBpartnerId);
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - XLS - strcelementvaluefrom= "
            + strcelementvaluefrom);
      if (log4j.isDebugEnabled())
        log4j.debug("##################### DoPost - XLS - strcelementvalueto= "
            + strcelementvalueto);
      vars.setSessionValue("ReportGeneralLedger.initRecordNumber", "0");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strHide,
          strcAcctSchemaId, strcelementvaluefromdes, strcelementvaluetodes);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ReportGeneralLedger.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ReportGeneralLedger.initRecordNumber", strInitRecord);
      }
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      // if (initRecord == 0)
      // initRecord = 1; Removed by DAL 30/4/09
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ReportGeneralLedger.initRecordNumber", strInitRecord);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("PDF", "XLS")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportGeneralLedger|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo");
      String strAmtFrom = vars.getNumericRequestGlobalVariable("inpAmtFrom",
          "ReportGeneralLedger|AmteFrom");
      String strAmtTo = vars.getNumericRequestGlobalVariable("inpAmtTo",
          "ReportGeneralLedger|AmtTo");
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", IsIDFilter.instance);
      String strAll = vars.getStringParameter("inpAll");
      String strHide = vars.getStringParameter("inpHideMatched");
      printPageDataPDF(response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strHide,
          strcAcctSchemaId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strAll, String strHide, String strcAcctSchemaId, String strcelementvaluefromdes,
      String strcelementvaluetodes) throws IOException, ServletException {
    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
    // built limit/offset parameters for oracle/postgres
    String rowNum = "0";
    String oraLimit1 = null;
    String oraLimit2 = null;
    String pgLimit = null;
    if (intRecordRange != 0) {
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        rowNum = "ROWNUM";
        oraLimit1 = String.valueOf(initRecordNumber + intRecordRange);
        oraLimit2 = (initRecordNumber + 1) + " AND " + oraLimit1;
      } else {
        rowNum = "0";
        pgLimit = intRecordRange + " OFFSET " + initRecordNumber;
      }
    }
    log4j.debug("offset= " + initRecordNumber + " pageSize= " + intRecordRange);
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    if (log4j.isDebugEnabled())
      log4j.debug("Date From:" + strDateFrom + "- To:" + strDateTo + " - Schema:"
          + strcAcctSchemaId);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportGeneralLedgerData[][] subreport = null;
    ReportGeneralLedgerData[][] subreport2 = null;
    ReportGeneralLedgerData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    // String strTreeAccount = ReportTrialBalanceData.treeAccount(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strYearInitialDate = ReportGeneralLedgerData.yearInitialDate(this, vars
        .getSessionValue("#AD_SqlDateFormat"), strDateFrom, Utility.getContext(this, vars,
        "#User_Client", "ReportGeneralLedger"), strOrgFamily);
    if (strYearInitialDate.equals(""))
      strYearInitialDate = strDateFrom;

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportGeneralLedger", true, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    String strcBpartnerIdAux = strcBpartnerId;
    if (!strAll.equals(""))
      strcBpartnerId = "";
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      String discard[] = { "sectionAmount", "sectionPartner" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportGeneralLedger", discard).createXmlDocument();
      toolbar
          .prepareRelationBarTemplate(false, false,
              "submitCommandForm('XLS', false, frmMain, 'ReportGeneralLedgerExcel.xls', 'EXCEL');return false;");
      data = ReportGeneralLedgerData.set();
    } else {
      String[] discard = { "discard" };
      if (strcBpartnerId.equals("") && strAll.equals(""))
        discard[0] = "sectionPartner";
      else
        discard[0] = "sectionAmount";
      BigDecimal previousDebit = BigDecimal.ZERO;
      BigDecimal previousCredit = BigDecimal.ZERO;
      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strcelementvalueto.equals("")) {
          strcelementvalueto = strcelementvaluefrom;
          strcelementvaluetodes = ReportGeneralLedgerData.selectSubaccountDescription(this,
              strcelementvalueto);
          vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);

        }
        if (log4j.isDebugEnabled())
          log4j.debug("##################### strcelementvaluefrom= " + strcelementvaluefrom);
        if (log4j.isDebugEnabled())
          log4j.debug("##################### strcelementvalueto= " + strcelementvalueto);
        if (strHide.equals(""))
          data = ReportGeneralLedgerData.select(this, rowNum, (strcBpartnerId.equals("") && (strAll
              .equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
              : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strcelementvaluefrom,
              strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                  "ReportGeneralLedger"), strcAcctSchemaId, null, null, null, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId,
              strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                  : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", pgLimit, oraLimit1, oraLimit2,
              (strcBpartnerId.equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
        else
          data = ReportGeneralLedgerData
              .selectHiding(
                  this,
                  rowNum,
                  (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strDateFrom,
                  DateTimeData.nDaysAfter(this, strDateTo, "1"), strcelementvaluefrom,
                  strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                      "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                      "ReportGeneralLedger"), strcAcctSchemaId, null, null, null, strDateFrom,
                  DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId,
                  strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", pgLimit, oraLimit1,
                  oraLimit2, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                      : "C_BPARTNER_ID, ");
      } else {
        strcelementvalueto = "";
        strcelementvaluetodes = "";
        vars.setSessionValue("inpElementValueIdTo_DES", strcelementvaluetodes);
        if (strHide.equals(""))
          data = ReportGeneralLedgerData
              .selectAll(
                  this,
                  rowNum,
                  (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", Utility
                      .getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"), Utility
                      .getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
                  strcAcctSchemaId, null, null, null, strDateFrom, DateTimeData.nDaysAfter(this,
                      strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom, strAmtTo,
                  (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", pgLimit, oraLimit1,
                  oraLimit2, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                      : "C_BPARTNER_ID, ");
        else
          data = ReportGeneralLedgerData
              .selectAllHiding(
                  this,
                  rowNum,
                  (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strDateFrom,
                  DateTimeData.nDaysAfter(this, strDateTo, "1"), Utility.getContext(this, vars,
                      "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
                      "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, null, null, null,
                  strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily,
                  strcBpartnerId, strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll
                      .equals(""))) ? "" : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", pgLimit,
                  oraLimit1, oraLimit2, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                      : "C_BPARTNER_ID, ");
      }
      if (log4j.isDebugEnabled())
        log4j.debug("RecordNo: " + initRecordNumber);
      // In case this is not the first screen to show, initial balance may need to include amounts
      // of previous screen, so same sql -but from the beginning of the fiscal year- is executed

      ReportGeneralLedgerData[] dataTotal = null;
      if (data.length > 1) {
        if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("") && data != null) {
          if (strHide.equals(""))
            dataTotal = ReportGeneralLedgerData
                .select(
                    this,
                    rowNum,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ",
                    strcelementvaluefrom, strcelementvalueto, Utility.getContext(this, vars,
                        "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this,
                        vars, "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, data[0].id,
                    data[0].dateacctnumber + data[0].factAcctGroupId + data[0].factAcctId,
                    (strcBpartnerId.equals("") && strAll.equals("")) ? null : data[0].cBpartnerId,
                    strYearInitialDate, DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                    strOrgFamily, strcBpartnerId, strAmtFrom, strAmtTo,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null, null, null,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
          else
            dataTotal = ReportGeneralLedgerData
                .selectHiding(
                    this,
                    rowNum,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strDateFrom,
                    DateTimeData.nDaysAfter(this, strDateTo, "1"), strcelementvaluefrom,
                    strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                        "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                        "ReportGeneralLedger"), strcAcctSchemaId, data[0].id,
                    data[0].dateacctnumber + data[0].factAcctGroupId + data[0].factAcctId,
                    (strcBpartnerId.equals("") && strAll.equals("")) ? null : data[0].cBpartnerId,
                    strYearInitialDate, DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                    strOrgFamily, strcBpartnerId, strAmtFrom, strAmtTo,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null, null, null,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
        } else {
          if (strHide.equals(""))
            dataTotal = ReportGeneralLedgerData
                .selectAll(
                    this,
                    rowNum,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ",
                    Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
                    Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"),
                    strcAcctSchemaId,
                    // only account for first record of current page
                    data[0].id, data[0].dateacctnumber + data[0].factAcctGroupId
                        + data[0].factAcctId,
                    (strcBpartnerId.equals("") && strAll.equals("")) ? null : data[0].cBpartnerId,
                    strYearInitialDate, DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                    strOrgFamily, strcBpartnerId, strAmtFrom, strAmtTo,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null, null, null,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
          else
            dataTotal = ReportGeneralLedgerData
                .selectAllHiding(
                    this,
                    rowNum,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strDateFrom,
                    DateTimeData.nDaysAfter(this, strDateTo, "1"), Utility.getContext(this, vars,
                        "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this,
                        vars, "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, data[0].id,
                    data[0].dateacctnumber + data[0].factAcctGroupId + data[0].factAcctId,
                    (strcBpartnerId.equals("") && strAll.equals("")) ? null : data[0].cBpartnerId,
                    strYearInitialDate, DateTimeData.nDaysAfter(this, data[0].dateacct, "1"),
                    strOrgFamily, strcBpartnerId, strAmtFrom, strAmtTo,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                        : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null, null, null,
                    (strcBpartnerId.equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
        }
      }
      // Now dataTotal is covered adding debit and credit amounts
      for (int i = 0; dataTotal != null && i < dataTotal.length; i++) {
        previousDebit = previousDebit.add(new BigDecimal(dataTotal[i].amtacctdr));
        previousCredit = previousCredit.add(new BigDecimal(dataTotal[i].amtacctcr));
      }
      ArrayList<Object> list = new ArrayList<Object>();
      String toDatePlusOne = DateTimeData.nDaysAfter(this, strDateTo, "1");
      String strOld = "";
      int j = 0;
      ReportGeneralLedgerData[] subreportElement = new ReportGeneralLedgerData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(((strcBpartnerId.equals("") && strAll.equals("")) ? ""
            : data[i].cBpartnerId)
            + data[i].id)) {
          subreportElement = new ReportGeneralLedgerData[1];
          if (i == 0 && initRecordNumber > 0) {
            subreportElement = new ReportGeneralLedgerData[1];
            subreportElement[0] = new ReportGeneralLedgerData();
            subreportElement[0].totaldr = previousDebit.toPlainString();
            subreportElement[0].totalcr = previousCredit.toPlainString();
            subreportElement[0].total = previousDebit.subtract(previousCredit).toPlainString();
          } else if (strHide.equals(""))
            subreportElement = ReportGeneralLedgerData.selectTotal(this,
                (strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId,
                strcAcctSchemaId, data[i].id, strYearInitialDate, strDateFrom, strOrgFamily);
          else
            subreportElement = ReportGeneralLedgerData.selectTotalHiding(this, strDateFrom,
                toDatePlusOne, (strcBpartnerId.equals("") && strAll.equals("")) ? ""
                    : data[i].cBpartnerId, strcAcctSchemaId, data[i].id, strYearInitialDate,
                strDateFrom, strOrgFamily);
          data[i].totalacctdr = subreportElement[0].totaldr;
          data[i].totalacctcr = subreportElement[0].totalcr;
          data[i].totalacctsub = subreportElement[0].total;
          list.add(subreportElement);
          j++;
        }
        data[i].previousdebit = subreportElement[0].totaldr;
        data[i].previouscredit = subreportElement[0].totalcr;
        data[i].previoustotal = subreportElement[0].total;
        strOld = (((strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId) + data[i].id);
      }
      /*
       * subreport = new ReportGeneralLedgerData[j][]; list.toArray(subreport);
       */
      list = new ArrayList<Object>();
      String strTotal = "";
      int g = 0;
      subreportElement = new ReportGeneralLedgerData[1];
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strTotal.equals(((strcBpartnerId.equals("") && strAll.equals("")) ? ""
            : data[i].cBpartnerId)
            + data[i].id)) {
          subreportElement = new ReportGeneralLedgerData[1];
          if (strHide.equals(""))
            subreportElement = ReportGeneralLedgerData.selectTotal(this,
                (strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId,
                strcAcctSchemaId, data[i].id, strYearInitialDate, toDatePlusOne, strOrgFamily);
          else
            subreportElement = ReportGeneralLedgerData.selectTotalHiding(this, strDateFrom,
                toDatePlusOne, (strcBpartnerId.equals("") && strAll.equals("")) ? ""
                    : data[i].cBpartnerId, strcAcctSchemaId, data[i].id, strYearInitialDate,
                toDatePlusOne, strOrgFamily);
          g++;
        }
        data[i].finaldebit = subreportElement[0].totaldr;
        data[i].finalcredit = subreportElement[0].totalcr;
        data[i].finaltotal = subreportElement[0].total;
        strTotal = (((strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId) + data[i].id);
      }
      /*
       * subreport2 = new ReportGeneralLedgerData[g][]; list.toArray(subreport2);
       */

      boolean hasPrevious = !(data == null || data.length == 0 || initRecordNumber <= 1);
      boolean hasNext = !(data == null || data.length == 0 || data.length < intRecordRange);
      toolbar
          .prepareRelationBarTemplate(hasPrevious, hasNext,
              "submitCommandForm('XLS', true, frmMain, 'ReportGeneralLedgerExcel.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportGeneralLedger", discard).createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportGeneralLedger");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportGeneralLedger.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportGeneralLedger.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportGeneralLedger");
      vars.removeMessage("ReportGeneralLedger");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"), Utility
              .getContext(this, vars, "#User_Client", "ReportGeneralLedger"), '*');
      comboTableData.fillParameters(null, "ReportGeneralLedger", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amtFrom", strAmtFrom);
    xmlDocument.setParameter("amtTo", strAmtTo);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("paramElementvalueIdTo", strcelementvalueto);
    xmlDocument.setParameter("paramElementvalueIdFrom", strcelementvaluefrom);
    xmlDocument.setParameter("inpElementValueIdTo_DES", strcelementvaluetodes);
    xmlDocument.setParameter("inpElementValueIdFrom_DES", strcelementvaluefromdes);
    xmlDocument.setParameter("paramAll0", strAll.equals("") ? "0" : "1");
    xmlDocument.setParameter("paramHide0", strHide.equals("") ? "0" : "1");
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", SelectorUtilityData
        .selectBpartner(this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility
            .getContext(this, vars, "#User_Client", ""), strcBpartnerIdAux));
    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure", AccountingSchemaMiscData
        .selectC_ACCTSCHEMA_ID(this, Utility.getContext(this, vars, "#AccessibleOrgTree",
            "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
            "ReportGeneralLedger"), strcAcctSchemaId));

    if (log4j.isDebugEnabled())
      log4j.debug("data.length: " + data.length);

    if (strcBpartnerId.equals("") && strAll.equals(""))
      xmlDocument.setData("structure1", data);
    else
      xmlDocument.setData("structure2", data);

    /*
     * if (strcBpartnerId.equals("") && strAll.equals("")) xmlDocument.setDataArray("reportTotals",
     * "structure", subreport); else xmlDocument.setDataArray("reportTotals2", "structure",
     * subreport); if (strcBpartnerId.equals("") && strAll.equals(""))
     * xmlDocument.setDataArray("reportAll", "structure", subreport2); else
     * xmlDocument.setDataArray("reportAll2", "structure", subreport2);
     */

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataPDF(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strAll, String strHide, String strcAcctSchemaId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    response.setContentType("text/html; charset=UTF-8");
    ReportGeneralLedgerData[] data = null;
    ReportGeneralLedgerData[] subreport = null;
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = "";
    strOrgFamily = getFamily(strTreeOrg, strOrg);
    String strYearInitialDate = ReportGeneralLedgerData.yearInitialDate(this, vars
        .getSessionValue("#AD_SqlDateFormat"), strDateFrom, Utility.getContext(this, vars,
        "#User_Client", "ReportGeneralLedger"), strOrgFamily);
    if (strYearInitialDate.equals(""))
      strYearInitialDate = strDateFrom;

    if (!strAll.equals(""))
      strcBpartnerId = "";
    if (strDateFrom.equals("") && strDateTo.equals("")) {
      data = ReportGeneralLedgerData.set();
    } else {
      strOrgFamily = getFamily(strTreeOrg, strOrg);
      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strcelementvalueto.equals("")) {
          strcelementvalueto = strcelementvaluefrom;
        }

        if (strHide.equals(""))
          data = ReportGeneralLedgerData.select(this, "0", (strcBpartnerId.equals("") && (strAll
              .equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
              : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strcelementvaluefrom,
              strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                  "ReportGeneralLedger"), strcAcctSchemaId, null, null, null, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId,
              strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                  : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null, null, null, (strcBpartnerId
                  .equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
        else
          data = ReportGeneralLedgerData
              .selectHiding(
                  this,
                  "0",
                  (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strDateFrom,
                  DateTimeData.nDaysAfter(this, strDateTo, "1"), strcelementvaluefrom,
                  strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                      "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                      "ReportGeneralLedger"), strcAcctSchemaId, null, null, null, strDateFrom,
                  DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId,
                  strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null, null, null,
                  (strcBpartnerId.equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
      } else {
        if (strHide.equals(""))
          data = ReportGeneralLedgerData.selectAll(this, "0", (strcBpartnerId.equals("") && (strAll
              .equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
              : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", Utility.getContext(this,
              vars, "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
              "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, null, null, null,
              strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily,
              strcBpartnerId, strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll
                  .equals(""))) ? "" : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null, null,
              null, (strcBpartnerId.equals("") && (strAll.equals(""))) ? "" : "C_BPARTNER_ID, ");
        else
          data = ReportGeneralLedgerData
              .selectAllHiding(
                  this,
                  "0",
                  (strcBpartnerId.equals("") && (strAll.equals(""))) ? "TO_CHAR('0') AS C_BPARTNER_ID, TO_CHAR('0') AS PARTNER, "
                      : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME AS PARTNER, ", strDateFrom,
                  DateTimeData.nDaysAfter(this, strDateTo, "1"), Utility.getContext(this, vars,
                      "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
                      "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, null, null, null,
                  strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily,
                  strcBpartnerId, strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll
                      .equals(""))) ? "" : "FACT_ACCT.C_BPARTNER_ID, C_BPARTNER.NAME, ", null,
                  null, null, (strcBpartnerId.equals("") && (strAll.equals(""))) ? ""
                      : "C_BPARTNER_ID, ");
      }
    }
    String strOld = "";
    BigDecimal totalDebit = BigDecimal.ZERO;
    BigDecimal totalCredit = BigDecimal.ZERO;
    BigDecimal subTotal = BigDecimal.ZERO;
    subreport = new ReportGeneralLedgerData[data.length];
    for (int i = 0; data != null && i < data.length; i++) {
      if (!strOld.equals(((strcBpartnerId.equals("") && strAll.equals("")) ? ""
          : data[i].cBpartnerId)
          + data[i].id)) {
        if (strHide.equals(""))
          subreport = ReportGeneralLedgerData.selectTotal(this,
              (strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId,
              strcAcctSchemaId, data[i].id, strYearInitialDate, strDateFrom, strOrgFamily);
        else
          subreport = ReportGeneralLedgerData.selectTotalHiding(this, strDateFrom, DateTimeData
              .nDaysAfter(this, strDateTo, "1"),
              (strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId,
              strcAcctSchemaId, data[i].id, strYearInitialDate, strDateFrom, strOrgFamily);
        totalDebit = BigDecimal.ZERO;
        totalCredit = BigDecimal.ZERO;
        subTotal = BigDecimal.ZERO;
      }
      totalDebit = totalDebit.add(new BigDecimal(data[i].amtacctdr));
      data[i].totalacctdr = new BigDecimal(subreport[0].totaldr).add(totalDebit).toString();
      totalCredit = totalCredit.add(new BigDecimal(data[i].amtacctcr));
      data[i].totalacctcr = new BigDecimal(subreport[0].totalcr).add(totalCredit).toString();
      subTotal = subTotal.add(new BigDecimal(data[i].total));
      data[i].totalacctsub = new BigDecimal(subreport[0].total).add(subTotal).toString();
      strOld = (((strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId) + data[i].id);
    }

    String strOutput = vars.commandIn("PDF") ? "pdf" : "xls";
    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportGeneralLedger.jrxml";
    if (strOutput.equals("pdf"))
      response.setHeader("Content-disposition", "inline; filename=ReportGeneralLedgerPDF.pdf");

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

    JasperReport jasperReportLines;
    try {
      jasperReportLines = Utility.getTranslatedJasperReport(this, strBaseDesign
          + "/org/openbravo/erpCommon/ad_reports/ReportGeneralLedger_Previous.jrxml", vars
          .getLanguage(), strBaseDesign);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    parameters.put("SR_PREVIOUS", jasperReportLines);

    parameters.put("ShowPartner", new Boolean(!(strcBpartnerId.equals("") && strAll.equals(""))));
    StringBuilder strSubTitle = new StringBuilder();
    strSubTitle.append(Utility.messageBD(this, "DateFrom", strLanguage) + ": " + strDateFrom
        + " - " + Utility.messageBD(this, "DateTo", strLanguage) + ": " + strDateTo + " (");
    strSubTitle.append(ReportGeneralLedgerData.selectCompany(this, vars.getClient()) + " - ");
    strSubTitle.append(ReportGeneralLedgerData.selectOrganization(this, vars.getOrg()) + ")");
    parameters.put("REPORT_SUBTITLE", strSubTitle.toString());
    parameters.put("Previous", Utility.messageBD(this, "GL_Previous", strLanguage));
    parameters.put("Total", Utility.messageBD(this, "Total", strLanguage));
    String strDateFormat;
    strDateFormat = vars.getJavaDateFormat();
    SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
    Date date = null;
    try {
      date = dateFormat.parse(strYearInitialDate);
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }
    parameters.put("InitialYearDate", date);
    Date dateTo = null;
    try {
      dateTo = dateFormat.parse(strDateTo);
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }
    parameters.put("dateTo", dateTo);
    Date dateToPlus1 = null;
    try {
      dateToPlus1 = dateFormat.parse(DateTimeData.nDaysAfter(this, strDateTo, "1"));
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }
    parameters.put("dateToPlus1", dateToPlus1);
    Date dateFrom = null;
    try {
      dateFrom = dateFormat.parse(strDateFrom);
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }
    parameters.put("dateFrom", dateFrom);
    parameters.put("strDateFormat", strDateFormat);
    parameters.put("Org", strOrgFamily);
    parameters.put("isHiding", (strHide.equals("")) ? new Boolean(false) : new Boolean(true));

    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  public String getServletInfo() {
    return "Servlet ReportGeneralLedger. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
