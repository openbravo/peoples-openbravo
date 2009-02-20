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
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
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
      String strAmtFrom = vars.getGlobalVariable("inpAmtFrom", "ReportGeneralLedger|AmtFrom", "");
      String strAmtTo = vars.getGlobalVariable("inpAmtTo", "ReportGeneralLedger|AmtTo", "");
      String strcelementvaluefrom = vars.getGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM", "");
      String strcelementvalueto = vars.getGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId", "");
      String strAll = vars.getGlobalVariable("inpAll", "ReportGeneralLedger|All", "");
      String strHide = vars.getGlobalVariable("inpHideMatched", "ReportGeneralLedger|HideMatched",
          "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strHide,
          strcAcctSchemaId);
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "ReportGeneralLedger|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportGeneralLedger|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportGeneralLedger|DateTo");
      String strAmtFrom = vars
          .getRequestGlobalVariable("inpAmtFrom", "ReportGeneralLedger|AmtFrom");
      String strAmtTo = vars.getRequestGlobalVariable("inpAmtTo", "ReportGeneralLedger|AmtTo");
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId");
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
          strcAcctSchemaId);
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
      if (initRecord == 0)
        initRecord = 1;
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
      String strAmtFrom = vars.getRequestGlobalVariable("inpAmtFrom",
          "ReportGeneralLedger|AmteFrom");
      String strAmtTo = vars.getRequestGlobalVariable("inpAmtTo", "ReportGeneralLedger|AmtTo");
      String strcelementvaluefrom = vars.getRequestGlobalVariable("inpcElementValueIdFrom",
          "ReportGeneralLedger|C_ElementValue_IDFROM");
      String strcelementvalueto = vars.getRequestGlobalVariable("inpcElementValueIdTo",
          "ReportGeneralLedger|C_ElementValue_IDTO");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportGeneralLedger|Org", "0");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportGeneralLedger|cBpartnerId");
      String strAll = vars.getStringParameter("inpAll");
      String strHide = vars.getStringParameter("inpHideMatched");
      printPageDataPDF(response, vars, strDateFrom, strDateTo, strAmtFrom, strAmtTo,
          strcelementvaluefrom, strcelementvalueto, strOrg, strcBpartnerId, strAll, strHide,
          strcAcctSchemaId);
    } else
      pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strAmtFrom, String strAmtTo,
      String strcelementvaluefrom, String strcelementvalueto, String strOrg, String strcBpartnerId,
      String strAll, String strHide, String strcAcctSchemaId) throws IOException, ServletException {
    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "ReportGeneralLedger");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ReportGeneralLedger.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
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
    String strTreeOrg = ReportTrialBalanceData.treeOrg(this, vars.getClient());
    String strTreeAccount = ReportTrialBalanceData.treeAccount(this, vars.getClient());
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
      // toolbar.prepareRelationBarTemplate(false, false);
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
        }
        if (log4j.isDebugEnabled())
          log4j.debug("##################### strcelementvaluefrom= " + strcelementvaluefrom);
        if (log4j.isDebugEnabled())
          log4j.debug("##################### strcelementvalueto= " + strcelementvalueto);
        if (strHide.equals(""))
          data = ReportGeneralLedgerData.select(this, strcelementvaluefrom, strcelementvalueto,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"), Utility
                  .getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId,
              strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily,
              strcBpartnerId, strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && (strAll
                  .equals(""))) ? "value" : "partner", initRecordNumber, intRecordRange);
        else
          data = ReportGeneralLedgerData.selectHiding(this, strcelementvaluefrom,
              strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                  "ReportGeneralLedger"), strcAcctSchemaId, strDateFrom, DateTimeData.nDaysAfter(
                  this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom, strAmtTo,
              (strcBpartnerId.equals("") && (strAll.equals(""))) ? "value" : "partner",
              initRecordNumber, intRecordRange);
      } else {
        if (strHide.equals(""))
          data = ReportGeneralLedgerData.selectAll(this, Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
              "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, strDateFrom, DateTimeData
              .nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom,
              strAmtTo, (strcBpartnerId.equals("") && strAll.equals("")) ? "value" : "partner",
              initRecordNumber, intRecordRange);
        else
          data = ReportGeneralLedgerData.selectAllHiding(this, Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
              "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, strDateFrom, DateTimeData
              .nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom,
              strAmtTo, (strcBpartnerId.equals("") && strAll.equals("")) ? "value" : "partner",
              initRecordNumber, intRecordRange);
      }
      if (log4j.isDebugEnabled())
        log4j.debug("RecordNo: " + initRecordNumber);
      // In case this is not the first screen to show, initial balance may need to include amounts
      // of previous screen, so same sql -but from the beginning of the fiscal year- is executed
      ReportGeneralLedgerData[] dataTotal = null;
      if (strcelementvaluefrom != null && !strcelementvaluefrom.equals("")) {
        if (strHide.equals(""))
          dataTotal = ReportGeneralLedgerData.select(this, strcelementvaluefrom,
              strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                  "ReportGeneralLedger"), strcAcctSchemaId, strYearInitialDate, DateTimeData
                  .nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom,
              strAmtTo, (strcBpartnerId.equals("") && (strAll.equals(""))) ? "value" : "partner");
        else
          dataTotal = ReportGeneralLedgerData.selectHiding(this, strcelementvaluefrom,
              strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                  "ReportGeneralLedger"), strcAcctSchemaId, strYearInitialDate, DateTimeData
                  .nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom,
              strAmtTo, (strcBpartnerId.equals("") && (strAll.equals(""))) ? "value" : "partner");
      } else {
        if (strHide.equals(""))
          dataTotal = ReportGeneralLedgerData.selectAll(this, Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
              "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, strYearInitialDate,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId,
              strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && strAll.equals("")) ? "value"
                  : "partner");
        else
          dataTotal = ReportGeneralLedgerData.selectAllHiding(this, Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
              "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, strYearInitialDate,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId,
              strAmtFrom, strAmtTo, (strcBpartnerId.equals("") && strAll.equals("")) ? "value"
                  : "partner");
      }
      // Now dataTotal is covered until the first record to show in this screen, so previous amounts
      // are calculated accurately
      String strOld = "";
      for (int i = 0; data != null && i < dataTotal.length; i++) {
        if (dataTotal[i].factAcctId.equals(data[0].factAcctId)) {
          if (!strOld.equals(((strcBpartnerId.equals("") && strAll.equals("")) ? ""
              : dataTotal[i].cBpartnerId)
              + dataTotal[i].id)) {
            previousDebit = BigDecimal.ZERO;
            previousCredit = BigDecimal.ZERO;
          }
          break;
        }
        if (!strOld.equals(((strcBpartnerId.equals("") && strAll.equals("")) ? ""
            : dataTotal[i].cBpartnerId)
            + dataTotal[i].id)) {
          previousDebit = BigDecimal.ZERO;
          previousCredit = BigDecimal.ZERO;
        }
        previousDebit = previousDebit.add(new BigDecimal(dataTotal[i].amtacctdr));
        previousCredit = previousCredit.add(new BigDecimal(dataTotal[i].amtacctcr));
        strOld = (((strcBpartnerId.equals("") && strAll.equals("")) ? "" : dataTotal[i].cBpartnerId) + dataTotal[i].id);
      }
      // And previous amounts (until DateFrom) is calculated through
      // ReportGeneralLedgerData.selectPrevious and, if necessary, amounts of previous screen are
      // added
      subreport = new ReportGeneralLedgerData[data.length][];
      strOld = "";
      int j = 0;
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strOld.equals(((strcBpartnerId.equals("") && strAll.equals("")) ? ""
            : data[i].cBpartnerId)
            + data[i].id)) {
          if (i == 0) {
            subreport[j] = new ReportGeneralLedgerData[1];
            subreport[j][0] = new ReportGeneralLedgerData();
            subreport[j][0].totaldr = previousDebit.toPlainString();
            subreport[j][0].totalcr = previousCredit.toPlainString();
            subreport[j][0].total = previousDebit.subtract(previousCredit).toPlainString();
          } else
            subreport[j] = ReportGeneralLedgerData.selectPrevious(this,
                (strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId,
                strcAcctSchemaId, data[i].id, strYearInitialDate, strDateFrom, strOrgFamily);
          data[i].totalacctdr = subreport[j][0].totaldr;
          data[i].totalacctcr = subreport[j][0].totalcr;
          data[i].totalacctsub = subreport[j][0].total;
          j++;
        }
        strOld = (((strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId) + data[i].id);
      }
      subreport2 = new ReportGeneralLedgerData[data.length][];
      String strTotal = "";
      int g = 0;
      for (int i = 0; data != null && i < data.length; i++) {
        if (!strTotal.equals(((strcBpartnerId.equals("") && strAll.equals("")) ? ""
            : data[i].cBpartnerId)
            + data[i].id)) {
          subreport2[g] = ReportGeneralLedgerData.selectTotal(this,
              (strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId,
              strcAcctSchemaId, data[i].id, strYearInitialDate, DateTimeData.nDaysAfter(this,
                  strDateTo, "1"), strOrgFamily);
          g++;
        }
        strTotal = (((strcBpartnerId.equals("") && strAll.equals("")) ? "" : data[i].cBpartnerId) + data[i].id);
      }
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
    xmlDocument.setData("reportAD_ORGID", "liststructure", GeneralAccountingReportsData
        .selectCombo(this, vars.getRole()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("cElementValueFrom", strcelementvaluefrom);
    xmlDocument.setParameter("cElementValueTo", strcelementvalueto);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amtFrom", strAmtFrom);
    xmlDocument.setParameter("amtTo", strAmtTo);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("InitialYearDate", strYearInitialDate);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("paramAll0", strAll.equals("") ? "0" : "1");
    xmlDocument.setParameter("paramHide0", strHide.equals("") ? "0" : "1");
    xmlDocument.setData("reportCBPartnerId_IN", "liststructure",
        ReportRefundInvoiceCustomerDimensionalAnalysesData.selectBpartner(this, Utility.getContext(
            this, vars, "#AccessibleOrgTree", ""), Utility.getContext(this, vars, "#User_Client",
            ""), strcBpartnerIdAux));
    xmlDocument.setData("reportC_ELEMENTVALUEFROM", "liststructure", ReportGeneralLedgerData
        .selectC_ElementValue_ID(this, Utility.getContext(this, vars, "#AccessibleOrgTree",
            "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
            "ReportGeneralLedger"), strcelementvaluefrom, strcAcctSchemaId));
    xmlDocument.setData("reportC_ELEMENTVALUETO", "liststructure", ReportGeneralLedgerData
        .selectC_ElementValue_ID(this, Utility.getContext(this, vars, "#AccessibleOrgTree",
            "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
            "ReportGeneralLedger"), strcelementvaluefrom, strcAcctSchemaId));
    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure", ReportGeneralLedgerData
        .selectC_ACCTSCHEMA_ID(this, Utility.getContext(this, vars, "#AccessibleOrgTree",
            "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
            "ReportGeneralLedger"), strcAcctSchemaId));

    xmlDocument.setParameter("accounFromArray", arrayDobleEntrada("arrAccountFrom",
        ReportGeneralLedgerData.selectC_ElementValue_ID_Double(this, Utility.getContext(this, vars,
            "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
            "#User_Client", "ReportGeneralLedger"), strcelementvaluefrom)));
    xmlDocument.setParameter("accounToArray", arrayDobleEntrada("arrAccountTo",
        ReportGeneralLedgerData.selectC_ElementValue_ID_Double(this, Utility.getContext(this, vars,
            "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
            "#User_Client", "ReportGeneralLedger"), strcelementvaluefrom)));

    if (log4j.isDebugEnabled())
      log4j.debug("data.length: " + data.length);

    if (strcBpartnerId.equals("") && strAll.equals(""))
      xmlDocument.setData("structure1", data);
    else
      xmlDocument.setData("structure2", data);
    if (strcBpartnerId.equals("") && strAll.equals(""))
      xmlDocument.setDataArray("reportTotals", "structure", subreport);
    else
      xmlDocument.setDataArray("reportTotals2", "structure", subreport);
    if (strcBpartnerId.equals("") && strAll.equals(""))
      xmlDocument.setDataArray("reportAll", "structure", subreport2);
    else
      xmlDocument.setDataArray("reportAll2", "structure", subreport2);

    out.println(xmlDocument.print());
    out.close();
  }

  void printPageDataPDF(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom,
      String strDateTo, String strAmtFrom, String strAmtTo, String strcelementvaluefrom,
      String strcelementvalueto, String strOrg, String strcBpartnerId, String strAll,
      String strHide, String strcAcctSchemaId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    ReportGeneralLedgerData[] data = null;
    ReportGeneralLedgerData[] subreport = null;
    String strTreeOrg = ReportTrialBalanceData.treeOrg(this, vars.getClient());
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
          data = ReportGeneralLedgerData.select(this, strcelementvaluefrom, strcelementvalueto,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"), Utility
                  .getContext(this, vars, "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId,
              strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strOrgFamily,
              strcBpartnerId, strAmtFrom, strAmtTo,
              (strcBpartnerId.equals("") && strAll.equals("")) ? "value" : "partner");
        else
          data = ReportGeneralLedgerData.selectHiding(this, strcelementvaluefrom,
              strcelementvalueto, Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "ReportGeneralLedger"), Utility.getContext(this, vars, "#User_Client",
                  "ReportGeneralLedger"), strcAcctSchemaId, strDateFrom, DateTimeData.nDaysAfter(
                  this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom, strAmtTo,
              (strcBpartnerId.equals("") && strAll.equals("")) ? "value" : "partner");
      } else {
        if (strHide.equals(""))
          data = ReportGeneralLedgerData.selectAll(this, Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
              "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, strDateFrom, DateTimeData
              .nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom,
              strAmtTo, (strcBpartnerId.equals("") && strAll.equals("")) ? "value" : "partner");
        else
          data = ReportGeneralLedgerData.selectAllHiding(this, Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ReportGeneralLedger"), Utility.getContext(this, vars,
              "#User_Client", "ReportGeneralLedger"), strcAcctSchemaId, strDateFrom, DateTimeData
              .nDaysAfter(this, strDateTo, "1"), strOrgFamily, strcBpartnerId, strAmtFrom,
              strAmtTo, (strcBpartnerId.equals("") && strAll.equals("")) ? "value" : "partner");
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
        subreport = ReportGeneralLedgerData.selectPrevious(this,
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

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

    JasperReport jasperReportLines;
    try {
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
          + "/org/openbravo/erpCommon/ad_reports/ReportGeneralLedger_Previous.jrxml");
      jasperReportLines = JasperCompileManager.compileReport(jasperDesignLines);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    parameters.put("ReportData", jasperReportLines);

    parameters.put("Title", classInfo.name);
    parameters.put("ShowPartner", new Boolean(!(strcBpartnerId.equals("") && strAll.equals(""))));
    parameters.put("Subtitle", Utility.messageBD(this, "GL_Previous", strLanguage));
    parameters.put("Final", Utility.messageBD(this, "Final", strLanguage));
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

    renderJR(vars, response, strReportName, strOutput, parameters, data, null);
  }

  public String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  public String getRange(String accountfrom, String accountto) throws IOException, ServletException {

    ReportGeneralLedgerData[] data = ReportGeneralLedgerData.selectRange(this, accountfrom,
        accountto);

    boolean bolFirstLine = true;
    String strText = "";
    for (int i = 0; i < data.length; i++) {
      if (bolFirstLine) {
        bolFirstLine = false;
        strText = data[i].name;
      } else {
        strText = data[i].name + "," + strText;
      }
    }
    return strText;
  }

  public String getServletInfo() {
    return "Servlet ReportGeneralLedger. This Servlet was made by Pablo Sarobe";
  } // end of getServletInfo() method
}
