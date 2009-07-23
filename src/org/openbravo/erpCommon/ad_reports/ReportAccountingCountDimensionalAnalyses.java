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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_combos.OrganizationComboData;
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
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportAccountingCountDimensionalAnalyses extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final RequestFilter columnNameFilter = new RequestFilter() {
    @Override
    public boolean accept(String value) {
      for (int i = 0; i < value.length(); i++) {
        int c = value.codePointAt(i);
        if (Character.isLetter(c) || Character.isDigit(c) || value.charAt(i) == '_') {
          return true;
        }
      }
      return false;
    }
  };

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));
    if (vars.commandIn("DEFAULT", "DEFAULT_COMPARATIVE")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportAccountingCountDimensionalAnalyses|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportAccountingCountDimensionalAnalyses|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef",
          "ReportAccountingCountDimensionalAnalyses|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef",
          "ReportAccountingCountDimensionalAnalyses|dateToRef", "");
      String strAccountingCount = vars.getGlobalVariable("inpAccountingCount",
          "ReportAccountingCountDimensionalAnalyses|accountingCount", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportAccountingCountDimensionalAnalyses|cBpartnerId", "", IsIDFilter.instance);
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportAccountingCountDimensionalAnalyses|mProductId", "", IsIDFilter.instance);
      String strShown = vars.getInGlobalVariable("inpShown",
          "ReportAccountingCountDimensionalAnalyses|shown", "", columnNameFilter);
      String strOrg = vars.getGlobalVariable("inpOrg",
          "ReportAccountingCountDimensionalAnalyses|org", "0");
      String strcProjectId = vars.getGlobalVariable("inpcProjectId",
          "ReportAccountingCountDimensionalAnalyses|project", "");
      String strComparative = "Y";
      String strLevel = vars.getStringParameter("inpLevel", "0");
      String strColumnLevel = "";
      if (vars.commandIn("DEFAULT_COMPARATIVE"))
        strComparative = vars.getRequestGlobalVariable("inpComparative",
            "ReportAccountingCountDimensionalAnalyses|comparative");
      else
        strComparative = vars.getGlobalVariable("inpComparative",
            "ReportAccountingCountDimensionalAnalyses|comparative", "Y");
      String strPeriod = vars.getGlobalVariable("inpPeriod",
          "ReportAccountingCountDimensionalAnalyses|period", "without");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo,
          strAccountingCount, strcBpartnerId, strmProductId, strShown, strDateFromRef,
          strDateToRef, strOrg, strcProjectId, strPeriod, strLevel, strColumnLevel);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_HTML_COMPARATIVE")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportAccountingCountDimensionalAnalyses|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportAccountingCountDimensionalAnalyses|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportAccountingCountDimensionalAnalyses|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportAccountingCountDimensionalAnalyses|dateToRef");
      String strAccountingCount = vars.getRequestGlobalVariable("inpAccountingCount",
          "ReportAccountingCountDimensionalAnalyses|accountingCount");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportAccountingCountDimensionalAnalyses|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportAccountingCountDimensionalAnalyses|mProductId", IsIDFilter.instance);
      String strShown = vars.getRequestInGlobalVariable("inpShown",
          "ReportAccountingCountDimensionalAnalyses|shown", columnNameFilter);
      String strOrg = vars.getGlobalVariable("inpOrg",
          "ReportAccountingCountDimensionalAnalyses|org", "0");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportAccountingCountDimensionalAnalyses|project");
      // String strComparative = vars.getStringParameter("inpComparative",
      // "N");
      String strComparative = vars.getGlobalVariable("inpComparative",
          "ReportAccountingCountDimensionalAnalyses|comparative", "N");
      String strLevel = "0";
      String strColumnLevel = "";
      // String strPeriod = vars.getRequiredStringParameter("inpPeriod");
      String strPeriod = vars.getRequestGlobalVariable("inpPeriod",
          "ReportAccountingCountDimensionalAnalyses|period");
      printPageDataHtml(response, vars, strComparative, strDateFrom, strDateTo, strAccountingCount,
          strcBpartnerId, strmProductId, strShown, strDateFromRef, strDateToRef, strOrg,
          strcProjectId, strPeriod, strLevel, strColumnLevel);
    } else if (vars.commandIn("OPEN")) {
      String strDateFrom = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|dateFrom");
      String strDateTo = vars.getSessionValue("ReportAccountingCountDimensionalAnalyses|dateTo");
      String strDateFromRef = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|dateFromRef");
      String strDateToRef = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|dateToRef");
      // String strAccountingCount =
      // vars.getRequestGlobalVariable("inpAccountingCount",
      // "ReportAccountingCountDimensionalAnalyses|accountingCount");
      String strAccountingCount = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|accountingCount");
      String strcBpartnerId = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|cBpartnerId");
      String strmProductId = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|mProductId");
      String strShown = vars.getSessionValue("ReportAccountingCountDimensionalAnalyses|shown");
      String strOrg = vars.getSessionValue("ReportAccountingCountDimensionalAnalyses|org");
      if (strOrg.equals(""))
        strOrg = "0";
      String strcProjectId = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|project");
      String strComparative = vars
          .getSessionValue("ReportAccountingCountDimensionalAnalyses|comparative");
      String strLevel = vars.getRequiredStringParameter("inpLevel");
      String strColumnLevel = vars.getStringParameter("inpColumnLevel");
      String strPeriod = vars.getSessionValue("ReportAccountingCountDimensionalAnalyses|period");
      // String strPeriod="without";
      // String strPeriod = "without";
      // //vars.getRequestGlobalVariable("inpPeriod",
      // "ReportAccountingCountDimensionalAnalyses|period");
      printPageOpen(response, vars, strComparative, strDateFrom, strDateTo, strAccountingCount,
          strcBpartnerId, strmProductId, strShown, strDateFromRef, strDateToRef, strOrg,
          strcProjectId, strPeriod, strLevel, strColumnLevel);
    } else if (vars.commandIn("DIRECT")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportAccountingCountDimensionalAnalyses|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportAccountingCountDimensionalAnalyses|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportAccountingCountDimensionalAnalyses|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportAccountingCountDimensionalAnalyses|dateToRef");
      String strAccountingCount = vars.getRequestGlobalVariable("inpAccountingCount",
          "ReportAccountingCountDimensionalAnalyses|accountingCount");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportAccountingCountDimensionalAnalyses|cBpartnerId", IsIDFilter.instance);
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportAccountingCountDimensionalAnalyses|mProductId", IsIDFilter.instance);
      String strShown = vars.getStringParameter("inpShown");
      String strOrg = vars.getGlobalVariable("inpOrg",
          "ReportAccountingCountDimensionalAnalyses|org", "0");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportAccountingCountDimensionalAnalyses|project");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      String strLevel = vars.getRequiredStringParameter("inpLevel");
      String strColumnLevel = vars.getStringParameter("inpColumnLevel");
      String strPeriod = vars.getRequiredStringParameter("inpPeriod");
      printPageDataSheet(response, vars, strComparative, strDateFrom, strDateTo,
          strAccountingCount, strcBpartnerId, strmProductId, strShown, strDateFromRef,
          strDateToRef, strOrg, strcProjectId, strPeriod, strLevel, strColumnLevel);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strComparative, String strDateFrom, String strDateTo, String strAccountingCount,
      String strcBpartnerId, String strmProductId, String strShown, String strDateFromRef,
      String strDateToRef, String strOrg, String strcProjectId, String strPeriod, String strLevel,
      String strColumnLevel) throws IOException, ServletException {
    // Find response
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet - period:" + strPeriod + " strAccountingCount:"
          + strAccountingCount);
    String discard1[] = { "discard", "discard", "discard", "discard", "discard" };
    String discard[] = { "discard", "discard", "discard", "discard" };
    Vector<Object> vectorArray = createVector();

    XmlDocument xmlDocument = null;
    String strGroupBy = "";
    String strFilter = "";

    String[] arrayString = { "", "", "", "", "", "" };
    ReportAccountingCountDimensionalAnalysesData[] data = null;
    ReportAccountingCountDimensionalAnalysesData[] dataPeriod = null;

    // ReportAccountingCountDimensionalAnalysesData[] dataList =
    // createList(vars, strShown);
    if (strAccountingCount.equals("")) {
      discard1[0] = "selEliminarDetail1";
      discard1[1] = "selEliminarDetail2";
      discard1[2] = "selEliminarDetail3";
      discard1[3] = "selEliminarDetail4";
      data = ReportAccountingCountDimensionalAnalysesData.set();
    } else {
      arrayString = createArrayString(vectorArray, strShown, strColumnLevel);
      strFilter = createFilter(vars, vectorArray, strShown, arrayString[1]);
      strGroupBy = createGroupby(vectorArray, strShown, arrayString[1]);
      if (strPeriod.equals("without")) {
        if (strComparative.equals("Y")) {
          discard1[0] = "selEliminarDetail1";
          discard1[1] = "selEliminarDetail2";
          discard1[2] = "selEliminarDetail4";
          data = ReportAccountingCountDimensionalAnalysesData.selectComparative(this, "'"
              + arrayString[0] + "'", "''", arrayString[2], arrayString[1], vars
              .getStringParameter("inpNivel1", "0"), vars.getStringParameter("inpNivel2", "0"),
              vars.getStringParameter("inpNivel3", "0"), vars.getStringParameter("inpNivel4", "0"),
              arrayString[3], Utility.getContext(this, vars, "#User_Client",
                  "ReportAccountingCountDimensionalAnalyses"), strAccountingCount, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData
                  .getTreeOrg(this, vars.getClient()), strOrg), strcBpartnerId, strmProductId,
              strcProjectId, strFilter, strGroupBy, strDateFromRef, DateTimeData.nDaysAfter(this,
                  strDateToRef, "1"), arrayString[4], arrayString[5]);
        } else {
          discard1[0] = "selEliminarDetail2";
          discard1[1] = "selEliminarDetail3";
          discard1[2] = "selEliminarDetail4";
          data = ReportAccountingCountDimensionalAnalysesData.select(this, "'" + arrayString[0]
              + "'", "''", arrayString[2], arrayString[1], vars
              .getStringParameter("inpNivel1", "0"), vars.getStringParameter("inpNivel2", "0"),
              vars.getStringParameter("inpNivel3", "0"), vars.getStringParameter("inpNivel4", "0"),
              arrayString[3], Utility.getContext(this, vars, "#User_Client",
                  "ReportAccountingCountDimensionalAnalyses"), strAccountingCount, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData
                  .getTreeOrg(this, vars.getClient()), strOrg), strcBpartnerId, strmProductId,
              strcProjectId, strFilter, strGroupBy, arrayString[4], arrayString[5]);
        }
        if (data == null || data.length == 0)
          ReportAccountingCountDimensionalAnalysesData.set();
      } else if (strPeriod.equals("month")) {

        dataPeriod = ReportAccountingCountDimensionalAnalysesData.selectDate(this, strDateFrom,
            strDateTo);
        if (!strComparative.equals("Y")) {
          discard1[0] = "selEliminarDetail1";
          discard1[1] = "selEliminarDetail3";
          discard1[2] = "selEliminarDetail4";
          if (log4j.isDebugEnabled())
            log4j.debug("Output: dataSheet");
          if (log4j.isDebugEnabled())
            log4j.debug("Level1 = " + vars.getStringParameter("inpNivel1", "0"));
          if (log4j.isDebugEnabled())
            log4j.debug("Level2 = " + vars.getStringParameter("inpNivel2", "0"));
          if (log4j.isDebugEnabled())
            log4j.debug("Level3 = " + vars.getStringParameter("inpNivel3", "0"));
          if (log4j.isDebugEnabled())
            log4j.debug("Level4 = " + vars.getStringParameter("inpNivel4", "0"));
          data = ReportAccountingCountDimensionalAnalysesData.selectPeriod(this, "'"
              + arrayString[0] + "'", "''", arrayString[2], arrayString[1], vars
              .getStringParameter("inpNivel1", "0"), vars.getStringParameter("inpNivel2", "0"),
              vars.getStringParameter("inpNivel3", "0"), vars.getStringParameter("inpNivel4", "0"),
              arrayString[3], Utility.getContext(this, vars, "#User_Client",
                  "ReportAccountingCountDimensionalAnalyses"), strAccountingCount, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData
                  .getTreeOrg(this, vars.getClient()), strOrg), strcBpartnerId, strmProductId,
              strcProjectId, strFilter, strGroupBy, strDateTo, arrayString[4], arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();
        } else {
          discard1[0] = "selEliminarDetail1";
          discard1[1] = "selEliminarDetail3";
          discard1[2] = "selEliminarDetail2";
          data = ReportAccountingCountDimensionalAnalysesData.selectPeriodComparative(this, "'"
              + arrayString[0] + "'", "''", arrayString[2], arrayString[1], vars
              .getStringParameter("inpNivel1", "0"), vars.getStringParameter("inpNivel2", "0"),
              vars.getStringParameter("inpNivel3", "0"), vars.getStringParameter("inpNivel4", "0"),
              arrayString[3], Utility.getContext(this, vars, "#User_Client",
                  "ReportAccountingCountDimensionalAnalyses"), strAccountingCount, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData
                  .getTreeOrg(this, vars.getClient()), strOrg), strcBpartnerId, strmProductId,
              strcProjectId, strFilter, strGroupBy, strDateFromRef, DateTimeData.nDaysAfter(this,
                  strDateToRef, "1"), strDateTo, arrayString[4], arrayString[5]);
        }
        if (data == null || data.length == 0)
          data = ReportAccountingCountDimensionalAnalysesData.set();
      } else {
        dataPeriod = ReportAccountingCountDimensionalAnalysesData.selectQuarter(this, strDateFrom,
            strDateTo);
        if (!strComparative.equals("Y")) {
          discard1[0] = "selEliminarDetail1";
          discard1[1] = "selEliminarDetail3";
          discard1[2] = "selEliminarDetail4";
          data = ReportAccountingCountDimensionalAnalysesData.selectPeriodQuarter(this, "'"
              + arrayString[0] + "'", "''", arrayString[2], arrayString[1], vars
              .getStringParameter("inpNivel1", "0"), vars.getStringParameter("inpNivel2", "0"),
              vars.getStringParameter("inpNivel3", "0"), vars.getStringParameter("inpNivel4", "0"),
              arrayString[3], Utility.getContext(this, vars, "#User_Client",
                  "ReportAccountingCountDimensionalAnalyses"), strAccountingCount, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData
                  .getTreeOrg(this, vars.getClient()), strOrg), strcBpartnerId, strmProductId,
              strcProjectId, strFilter, strGroupBy, strDateTo, arrayString[4], arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();
        } else {
          discard1[0] = "selEliminarDetail1";
          discard1[1] = "selEliminarDetail3";
          discard1[2] = "selEliminarDetail2";
          data = ReportAccountingCountDimensionalAnalysesData.selectPeriodComparativeQuarter(this,
              "'" + arrayString[0] + "'", "''", arrayString[2], arrayString[1], vars
                  .getStringParameter("inpNivel1", "0"), vars.getStringParameter("inpNivel2", "0"),
              vars.getStringParameter("inpNivel3", "0"), vars.getStringParameter("inpNivel4", "0"),
              arrayString[3], Utility.getContext(this, vars, "#User_Client",
                  "ReportAccountingCountDimensionalAnalyses"), strAccountingCount, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData
                  .getTreeOrg(this, vars.getClient()), strOrg), strcBpartnerId, strmProductId,
              strcProjectId, strFilter, strGroupBy, strDateFromRef, DateTimeData.nDaysAfter(this,
                  strDateToRef, "1"), strDateTo, arrayString[4], arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();
        }
      }

    }

    xmlDocument = xmlEngine
        .readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportAccountingCountDimensionalAnalysesEdit",
            discard1).createXmlDocument();

    xmlDocument.setParameter("title", " From: " + strDateFrom + " - To: " + strDateTo);

    xmlDocument.setParameter("eliminar2", discard[1]);
    xmlDocument.setParameter("eliminar3", discard[2]);
    xmlDocument.setParameter("eliminar4", discard[3]);

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("constante", "100");

    if (strPeriod.equals("month") || strPeriod.equals("quarter")) {
      xmlDocument.setData("structure", dataPeriod);
      if (strComparative.equals("Y"))
        xmlDocument.setData("structureComparativePeriod", data);
      else
        xmlDocument.setData("structurePeriod", data);
    } else {
      if (strComparative.equals("Y"))
        xmlDocument.setData("structureComparative", data);
      else
        xmlDocument.setData("structureNoperiod", data);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String
   * strComparative, String strDateFrom, String strDateTo, String strAccountingCount, String
   * strcBpartnerId, String strmProductId, String strShown, String strDateFromRef, String
   * strDateToRef, String strOrg, String strcProjectId, String strPeriod, String strLevel, String
   * strColumnLevel) throws IOException, ServletException { //Find response if
   * (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet"); String[] arrayVector = {"", "", "",
   * "", "", ""}; Vector<Object> vectorArray = createVector();
   * 
   * XmlDocument xmlDocument=null; String strTitle = ""; String strGroupBy = ""; String strFilter =
   * "";
   * 
   * 
   * String[] arrayString = {"", "", "", "", "", ""};
   * 
   * ReportAccountingCountDimensionalAnalysesData[] dataList = createList(vars, strShown);
   * 
   * 
   * xmlDocument =xmlEngine.readXmlTemplate(
   * "org/openbravo/erpCommon/ad_reports/ReportAccountingCountDimensionalAnalysesFilter"
   * ).createXmlDocument();
   * 
   * ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
   * "ReportAccountingCountDimensionalAnalysesFilter", false, "", "", "",false, "ad_reports",
   * strReplaceWith, false, true); toolbar.prepareSimpleToolBarTemplate();
   * xmlDocument.setParameter("toolbar", toolbar.toString());
   * 
   * 
   * xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
   * xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
   * xmlDocument.setParameter("calendarLanguage", vars.getLanguage().substring(0,2));
   * xmlDocument.setParameter("dateFrom", strDateFrom);
   * xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
   * xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
   * xmlDocument.setParameter("dateTo", strDateTo); xmlDocument.setParameter("dateTodisplayFormat",
   * vars.getSessionValue("#AD_SqlDateFormat")); xmlDocument.setParameter("dateTosaveFormat",
   * vars.getSessionValue("#AD_SqlDateFormat")); xmlDocument.setParameter("dateFromRef",
   * strDateFromRef); xmlDocument.setParameter("dateFromRefdisplayFormat",
   * vars.getSessionValue("#AD_SqlDateFormat")); xmlDocument.setParameter("dateFromRefsaveFormat",
   * vars.getSessionValue("#AD_SqlDateFormat")); xmlDocument.setParameter("dateToRef",
   * strDateToRef); xmlDocument.setParameter("dateToRefdisplayFormat",
   * vars.getSessionValue("#AD_SqlDateFormat")); xmlDocument.setParameter("dateToRefsaveFormat",
   * vars.getSessionValue("#AD_SqlDateFormat")); xmlDocument.setParameter("accounting",
   * strAccountingCount); xmlDocument.setParameter("adOrgId", strOrg);
   * xmlDocument.setParameter("comparative", strComparative); xmlDocument.setParameter("period",
   * strPeriod); xmlDocument.setParameter("cProjectId", strcProjectId);
   * xmlDocument.setParameter("projectName",
   * ReportAccountingCountDimensionalAnalysesData.selectProject(this, strcProjectId));
   * xmlDocument.setParameter("withoutPeriod", strPeriod); xmlDocument.setParameter("monthPeriod",
   * strPeriod); xmlDocument.setParameter("quarterPeriod", strPeriod);
   * 
   * 
   * xmlDocument.setData("reportCBPartnerId_IN", "liststructure",
   * ReportRefundInvoiceCustomerDimensionalAnalysesData.selectBpartner(this,
   * Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility.getContext(this, vars,
   * "#User_Client", ""), strcBpartnerId)); xmlDocument.setData("reportMProductId_IN",
   * "liststructure", ReportRefundInvoiceCustomerDimensionalAnalysesData.selectMproduct(this,
   * Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility.getContext(this, vars,
   * "#User_Client", ""), strmProductId)); xmlDocument.setData("reportAD_ORGID", "liststructure",
   * OrganizationComboData.selectCombo(this, vars.getRole()));
   * 
   * try { WindowTabs tabs = new WindowTabs(this, vars,
   * "org.openbravo.erpCommon.ad_reports.ReportAccountingCountDimensionalAnalyses" );
   * xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
   * xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
   * xmlDocument.setParameter("childTabContainer", tabs.childTabs());
   * xmlDocument.setParameter("theme", vars.getTheme()); NavigationBar nav = new NavigationBar(this,
   * vars.getLanguage(), "ReportAccountingCountDimensionalAnalyses.html", classInfo.id,
   * classInfo.type, strReplaceWith, tabs.breadcrumb()); xmlDocument.setParameter("navigationBar",
   * nav.toString()); LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
   * "ReportAccountingCountDimensionalAnalyses.html", strReplaceWith);
   * xmlDocument.setParameter("leftTabs", lBar.manualTemplate()); } catch (Exception ex) { throw new
   * ServletException(ex); } { OBError myMessage =
   * vars.getMessage("ReportAccountingCountDimensionalAnalyses");
   * vars.removeMessage("ReportAccountingCountDimensionalAnalyses"); if (myMessage!=null) {
   * xmlDocument.setParameter("messageType", myMessage.getType());
   * xmlDocument.setParameter("messageTitle", myMessage.getTitle());
   * xmlDocument.setParameter("messageMessage", myMessage.getMessage()); } }
   * 
   * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "Account_ID",
   * "C_ElementValue (Accounts)", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
   * "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
   * Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", strAccountingCount);
   * xmlDocument.setData("reportAccount_ID","liststructure", comboTableData.select(false));
   * comboTableData = null; } catch (Exception ex) { throw new ServletException(ex); }
   * 
   * xmlDocument.setData("structure2", dataList);
   * 
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * out.println(xmlDocument.print()); out.close(); }
   */

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strComparative, String strDateFrom, String strDateTo, String strAccountingCount,
      String strcBpartnerId, String strmProductId, String strShown, String strDateFromRef,
      String strDateToRef, String strOrg, String strcProjectId, String strPeriod, String strLevel,
      String strColumnLevel) throws IOException, ServletException {
    // Find response
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet strAccountingCount:" + strAccountingCount);

    XmlDocument xmlDocument = null;
    String discard1[] = { "discard", "discard", "discard", "discard" };

    ReportAccountingCountDimensionalAnalysesData[] dataList = createList(vars, strShown);
    if (!strComparative.equals("Y")) {
      discard1[0] = "selEliminarHeader1";
    }

    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportAccountingCountDimensionalAnalysesFilter",
        discard1).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
        "ReportAccountingCountDimensionalAnalysesFilter", false, "", "", "", false, "ad_reports",
        strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("calendarLanguage", vars.getLanguage().substring(0, 2));
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
    xmlDocument.setParameter("accounting", strAccountingCount);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("comparative", strComparative);
    xmlDocument.setParameter("period", strPeriod);
    xmlDocument.setParameter("cProjectId", strcProjectId);
    xmlDocument.setParameter("projectName", ReportAccountingCountDimensionalAnalysesData
        .selectProject(this, strcProjectId));
    xmlDocument.setParameter("withoutPeriod", strPeriod);
    xmlDocument.setParameter("monthPeriod", strPeriod);
    xmlDocument.setParameter("quarterPeriod", strPeriod);

    xmlDocument.setData("reportCBPartnerId_IN", "liststructure", SelectorUtilityData
        .selectBpartner(this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility
            .getContext(this, vars, "#User_Client", ""), strcBpartnerId));
    xmlDocument.setData("reportMProductId_IN", "liststructure", SelectorUtilityData.selectMproduct(
        this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility.getContext(this,
            vars, "#User_Client", ""), strmProductId));
    xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this,
        vars.getRole()));

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportAccountingCountDimensionalAnalyses");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportAccountingCountDimensionalAnalyses.html", classInfo.id, classInfo.type,
          strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportAccountingCountDimensionalAnalyses.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportAccountingCountDimensionalAnalyses");
      vars.removeMessage("ReportAccountingCountDimensionalAnalyses");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "Account_ID",
          "C_ElementValue (Accounts)", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", strAccountingCount);
      xmlDocument.setData("reportAccount_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structure2", dataList);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageOpen(HttpServletResponse response, VariablesSecureApp vars,
      String strComparative, String strDateFrom, String strDateTo, String strAccountingCount,
      String strcBpartnerId, String strmProductId, String strShown, String strDateFromRef,
      String strDateToRef, String strOrg, String strcProjectId, String strPeriod, String strLevel,
      String strColumnLevel) throws IOException, ServletException {
    // Ajax response
    if (log4j.isDebugEnabled())
      log4j.debug("Output: open - period: " + strPeriod + " strAccountingCount:"
          + strAccountingCount);
    Vector<Object> vectorArray = createVector();
    String strGroupBy = "";
    String strFilter = "";
    XmlDocument xmlDocument = null;

    ReportAccountingCountDimensionalAnalysesData[] data = null;
    if (strAccountingCount.equals("")) {
      data = ReportAccountingCountDimensionalAnalysesData.set();
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("creating array");
      String[] arrayString = createArrayString(vectorArray, strShown, strColumnLevel);
      if (log4j.isDebugEnabled())
        log4j.debug("array created, length:" + arrayString.length + "creating filter");
      strFilter = createFilter(vars, vectorArray, strShown, arrayString[1]);
      if (log4j.isDebugEnabled())
        log4j.debug("filter created:" + strFilter + "\ncreating group by");
      strGroupBy = createGroupby(vectorArray, strShown, arrayString[1]);
      if (log4j.isDebugEnabled())
        log4j.debug("comparative:" + strComparative);
      if (strPeriod.equals("without")) {
        if (!strComparative.equals("Y")) {

          xmlDocument = xmlEngine.readXmlTemplate(
              "org/openbravo/erpCommon/ad_reports/ReportAccountingNoComparativeNoPeriod")
              .createXmlDocument();
          if (log4j.isDebugEnabled())
            log4j.debug("inpLevel" + strLevel + ":"
                + vars.getStringParameter("inpNivel" + strLevel));
          data = ReportAccountingCountDimensionalAnalysesData.select(this, "'" + arrayString[0]
              + "'", vars.getStringParameter("inpNivel" + strLevel, "0"), arrayString[2],
              arrayString[1], vars.getStringParameter("inpNivel1", "0"), vars.getStringParameter(
                  "inpNivel2", "0"), vars.getStringParameter("inpNivel3", "0"), vars
                  .getStringParameter("inpNivel4", "0"), arrayString[3], Utility.getContext(this,
                  vars, "#User_Client", "ReportAccountingCountDimensionalAnalyses"),
              strAccountingCount, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree
                  .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              strcBpartnerId, strmProductId, strcProjectId, strFilter, strGroupBy, arrayString[4],
              arrayString[5]);
          if (data == null || data.length == 0) {
            if (log4j.isDebugEnabled())
              log4j.debug("No data");
            data = ReportAccountingCountDimensionalAnalysesData.set();
          }
        } else {
          xmlDocument = xmlEngine.readXmlTemplate(
              "org/openbravo/erpCommon/ad_reports/ReportAccountingComparativeNoPeriod")
              .createXmlDocument();

          data = ReportAccountingCountDimensionalAnalysesData.selectComparative(this, "'"
              + arrayString[0] + "'", vars.getStringParameter("inpNivel" + strLevel, "0"),
              arrayString[2], arrayString[1], vars.getStringParameter("inpNivel1", "0"), vars
                  .getStringParameter("inpNivel2", "0"), vars.getStringParameter("inpNivel3", "0"),
              vars.getStringParameter("inpNivel4", "0"), arrayString[3], Utility.getContext(this,
                  vars, "#User_Client", "ReportAccountingCountDimensionalAnalyses"),
              strAccountingCount, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree
                  .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              strcBpartnerId, strmProductId, strcProjectId, strFilter, strGroupBy, strDateFromRef,
              DateTimeData.nDaysAfter(this, strDateToRef, "1"), arrayString[4], arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();

        }

      } else if (strPeriod.equals("month")) {
        if (!strComparative.equals("Y")) {
          xmlDocument = xmlEngine.readXmlTemplate(
              "org/openbravo/erpCommon/ad_reports/ReportAccountingNoComparativePeriod")
              .createXmlDocument();

          data = ReportAccountingCountDimensionalAnalysesData.selectPeriod(this, "'"
              + arrayString[0] + "'", vars.getStringParameter("inpNivel" + strLevel, "0"),
              arrayString[2], arrayString[1], vars.getStringParameter("inpNivel1", "0"), vars
                  .getStringParameter("inpNivel2", "0"), vars.getStringParameter("inpNivel3", "0"),
              vars.getStringParameter("inpNivel4", "0"), arrayString[3], Utility.getContext(this,
                  vars, "#User_Client", "ReportAccountingCountDimensionalAnalyses"),
              strAccountingCount, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree
                  .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              strcBpartnerId, strmProductId, strcProjectId, strFilter, strGroupBy, strDateTo,
              arrayString[4], arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();
        } else {
          xmlDocument = xmlEngine.readXmlTemplate(
              "org/openbravo/erpCommon/ad_reports/ReportAccountingComparativePeriod")
              .createXmlDocument();
          data = ReportAccountingCountDimensionalAnalysesData.selectPeriodComparative(this, "'"
              + arrayString[0] + "'", vars.getStringParameter("inpNivel" + strLevel, "0"),
              arrayString[2], arrayString[1], vars.getStringParameter("inpNivel1", "0"), vars
                  .getStringParameter("inpNivel2", "0"), vars.getStringParameter("inpNivel3", "0"),
              vars.getStringParameter("inpNivel4", "0"), arrayString[3], Utility.getContext(this,
                  vars, "#User_Client", "ReportAccountingCountDimensionalAnalyses"),
              strAccountingCount, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree
                  .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              strcBpartnerId, strmProductId, strcProjectId, strFilter, strGroupBy, strDateFromRef,
              DateTimeData.nDaysAfter(this, strDateToRef, "1"), strDateTo, arrayString[4],
              arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();
        }
      } else {
        if (!strComparative.equals("Y")) {
          xmlDocument = xmlEngine.readXmlTemplate(
              "org/openbravo/erpCommon/ad_reports/ReportAccountingNoComparativePeriod")
              .createXmlDocument();

          data = ReportAccountingCountDimensionalAnalysesData.selectPeriodQuarter(this, "'"
              + arrayString[0] + "'", vars.getStringParameter("inpNivel" + strLevel, "0"),
              arrayString[2], arrayString[1], vars.getStringParameter("inpNivel1", "0"), vars
                  .getStringParameter("inpNivel2", "0"), vars.getStringParameter("inpNivel3", "0"),
              vars.getStringParameter("inpNivel4", "0"), arrayString[3], Utility.getContext(this,
                  vars, "#User_Client", "ReportAccountingCountDimensionalAnalyses"),
              strAccountingCount, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree
                  .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              strcBpartnerId, strmProductId, strcProjectId, strFilter, strGroupBy, strDateTo,
              arrayString[4], arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();
        } else {
          xmlDocument = xmlEngine.readXmlTemplate(
              "org/openbravo/erpCommon/ad_reports/ReportAccountingComparativePeriod")
              .createXmlDocument();
          data = ReportAccountingCountDimensionalAnalysesData.selectPeriodComparativeQuarter(this,
              "'" + arrayString[0] + "'", vars.getStringParameter("inpNivel" + strLevel, "0"),
              arrayString[2], arrayString[1], vars.getStringParameter("inpNivel1", "0"), vars
                  .getStringParameter("inpNivel2", "0"), vars.getStringParameter("inpNivel3", "0"),
              vars.getStringParameter("inpNivel4", "0"), arrayString[3], Utility.getContext(this,
                  vars, "#User_Client", "ReportAccountingCountDimensionalAnalyses"),
              strAccountingCount, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree
                  .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
              strcBpartnerId, strmProductId, strcProjectId, strFilter, strGroupBy, strDateFromRef,
              DateTimeData.nDaysAfter(this, strDateToRef, "1"), strDateTo, arrayString[4],
              arrayString[5]);
          if (data == null || data.length == 0)
            data = ReportAccountingCountDimensionalAnalysesData.set();
        }
      }

    }
    response.setContentType("text/plain");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug("data.lenght:" + data.length);
    xmlDocument.setData("structure", data);
    out.println(xmlDocument.print());
    out.close();

    if (log4j.isDebugEnabled())
      log4j.debug("AJAX exit: " + xmlDocument.print());

  }

  private Vector<Object> createVector() throws IOException, ServletException {
    // Dimension main vector. Here you must put new dimensions
    ReportAccountingCountDimensionalAnalysesData[] data = ReportAccountingCountDimensionalAnalysesData
        .selectDimension(this, "RV_ReportAccountingCountDimensionalAnaly");
    Vector<Object> vectorArray = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      String[] arrayAux = { "", "", "", "", "", "" };
      arrayAux[0] = data[i].columnname;
      arrayAux[1] = data[i].line;
      arrayAux[2] = data[i].description;
      arrayAux[3] = data[i].joinGroup1;
      arrayAux[4] = data[i].tablename;
      arrayAux[5] = data[i].joinGroup2;
      vectorArray.addElement(arrayAux);
    }
    return vectorArray;
  }

  private ReportAccountingCountDimensionalAnalysesData[] createList(VariablesSecureApp vars,
      String strShown) throws IOException, ServletException {
    // Dimension filter lists creation
    ReportAccountingCountDimensionalAnalysesData[] dataDimension = ReportAccountingCountDimensionalAnalysesData
        .selectDimension(this, "RV_ReportAccountingCountDimensionalAnaly");
    ReportAccountingCountDimensionalAnalysesData[] data = ReportAccountingCountDimensionalAnalysesData
        .selectShown(this);
    int intContador = 0;
    if (!strShown.equals("")) {
      if (strShown.startsWith("("))
        strShown = strShown.substring(1, strShown.length() - 1);
      if (!strShown.equals("")) {
        strShown = Replace.replace(strShown, "'", "");
        strShown = Replace.replace(strShown, " ", "");
        StringTokenizer st = new StringTokenizer(strShown, ",", false);
        String strAux = "";
        while (st.hasMoreTokens()) {
          strAux = st.nextToken();
          data[intContador].id = strAux;
          data[intContador].name = ReportAccountingCountDimensionalAnalysesData
              .selectDimensionName(this, vars.getLanguage(), strAux);
          intContador++;
        }
      }
    } else {
      for (int i = 0; i < dataDimension.length; i++) {
        data[i].id = dataDimension[i].columnname;
        data[i].name = ReportAccountingCountDimensionalAnalysesData.selectDimensionName(this, vars
            .getLanguage(), dataDimension[i].columnname);
      }
    }
    return data;

  }

  private String[] createArrayString(Vector<Object> vectorArray, String strShown,
      String strColumnLevel) throws IOException, ServletException {
    // Method to create the array string which say sqlc fields to obtain.
    if (log4j.isDebugEnabled())
      log4j.debug("createArrayString strShown:" + strShown + " - level:" + strColumnLevel);
    String[] arrayString = null;
    int intContador = 0;
    if (strShown.startsWith("("))
      strShown = strShown.substring(1, strShown.length() - 1);
    if (!strShown.equals("")) {
      strShown = Replace.replace(strShown, "'", "");
      strShown = Replace.replace(strShown, " ", "");
      StringTokenizer st = new StringTokenizer(strShown, ",", false);
      String strAux = "";
      while (st.hasMoreTokens() && !strAux.equals(strColumnLevel)) {
        strAux = st.nextToken();
        intContador++;
      }
      strAux = st.nextToken();
      intContador++;
      int intCount = 0;
      arrayString = (String[]) (vectorArray.elementAt(intCount));
      while (!arrayString[0].equals(strAux) && arrayString[0] != null) {
        intCount++;
        arrayString = (String[]) (vectorArray.elementAt(intCount));
      }

      arrayString[1] = String.valueOf(intContador);
    }

    return arrayString;
  }

  private String createFilter(VariablesSecureApp vars, Vector<Object> vectorArray, String strShown,
      String strLevel) throws IOException, ServletException {
    // Method which creates filter to sqlc.
    if (log4j.isDebugEnabled())
      log4j.debug("Filter params: strShown:" + strShown + " - level:" + strLevel);
    String strFilter = "";
    String[] arrayAux = inicializarArray();
    if (strShown.startsWith("("))
      strShown = strShown.substring(1, strShown.length() - 1);
    if (!strShown.equals("")) {
      strShown = Replace.replace(strShown, "'", "");
      strShown = Replace.replace(strShown, " ", "");
      StringTokenizer st = new StringTokenizer(strShown, ",", false);
      String strAux = "";
      if (!strLevel.equals("1")) {
        for (int i = 0; i < Integer.valueOf(strLevel).intValue() - 1; i++) {
          strAux = st.nextToken();
          int j = 0;
          while (!strAux.equals(arrayAux[0])) {
            arrayAux = (String[]) (vectorArray.elementAt(j));
            j++;
          }
          strFilter += " AND " + arrayAux[3] + " = '"
              + vars.getStringParameter("inpNivel" + (i + 1)) + "'";

        }
      }
    }
    return strFilter;
  }

  private String createGroupby(Vector<Object> vectorArray, String strShown, String strLevel)
      throws IOException, ServletException {
    // Method that cretates group by sentence to sqlc.
    String strGroupBy = "";
    String[] arrayAux = inicializarArray();
    if (strShown.startsWith("("))
      strShown = strShown.substring(1, strShown.length() - 1);
    if (!strShown.equals("")) {
      strShown = Replace.replace(strShown, "'", "");
      strShown = Replace.replace(strShown, " ", "");
      StringTokenizer st = new StringTokenizer(strShown, ",", false);
      String strAux = "";
      for (int i = 0; i < Integer.valueOf(strLevel).intValue(); i++) {
        strAux = st.nextToken();
        int j = 0;
        while (!strAux.equals(arrayAux[0])) {
          arrayAux = (String[]) (vectorArray.elementAt(j));
          j++;
        }
        strGroupBy += ", " + arrayAux[3];
      }
    }
    return strGroupBy;
  }

  private String[] inicializarArray() throws IOException, ServletException {
    String[] defaultArray = { "", "", "", "", "", "" };
    return defaultArray;
  }

  public String getServletInfo() {
    return "Servlet ReportAccountingCountDimensionalAnalyses. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
