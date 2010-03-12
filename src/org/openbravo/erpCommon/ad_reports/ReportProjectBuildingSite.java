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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2009 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
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

public class ReportProjectBuildingSite extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strdateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportProjectBuildingSite|DateFrom", "");
      String strdateTo = vars
          .getGlobalVariable("inpDateTo", "ReportProjectBuildingSite|DateTo", "");
      String strcProjectId = vars.getGlobalVariable("inpcProjectId",
          "ReportProjectBuildingSite|cProjectId", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpcBPartnerId_IN",
          "ReportProjectBuildingSite|cBPartnerId_IN", "", IsIDFilter.instance);
      String strmCategoryId = vars.getInGlobalVariable("inpmProductCategoryId",
          "ReportProjectBuildingSite|mCategoryId", "", IsIDFilter.instance);
      String strProjectkind = vars.getInGlobalVariable("inpProjectkind",
          "ReportProjectBuildingSite|Projectkind", "", IsIDFilter.instance);
      String strProjectstatus = vars.getInGlobalVariable("inpProjectstatus",
          "ReportProjectBuildingSite|Projectstatus", "", IsIDFilter.instance);
      String strProjectphase = vars.getInGlobalVariable("inpProjectphase",
          "ReportProjectBuildingSite|Projectphase", "", IsIDFilter.instance);
      String strProduct = vars.getInGlobalVariable("inpmProductId_IN",
          "ReportProjectBuildingSite|mProductId_IN", "", IsIDFilter.instance);
      String strProjectpublic = vars.getGlobalVariable("inpProjectpublic",
          "ReportProjectBuildingSite|Projectpublic", "");
      String strSalesRep = vars.getGlobalVariable("inpSalesRepId",
          "ReportProjectBuildingSite|SalesRepId", "");
      String strcRegionId = vars.getInGlobalVariable("inpcRegionId",
          "ReportProjectBuildingSite|cRegionId", "", IsIDFilter.instance);
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcBpartnerId, strcProjectId,
          strmCategoryId, strProjectkind, strProjectphase, strProjectstatus, strProjectpublic,
          strcRegionId, strSalesRep, strProduct);
    } else if (vars.commandIn("FIND")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportProjectBuildingSite|DateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportProjectBuildingSite|DateTo");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportProjectBuildingSite|cProjectId");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportProjectBuildingSite|cBPartnerId_IN", IsIDFilter.instance);
      String strmCategoryId = vars.getRequestInGlobalVariable("inpmProductCategoryId",
          "ReportProjectBuildingSite|mCategoryId", IsIDFilter.instance);
      String strProjectkind = vars.getRequestInGlobalVariable("inpProjectkind",
          "ReportProjectBuildingSite|Projectkind", IsIDFilter.instance);
      String strProjectstatus = vars.getRequestInGlobalVariable("inpProjectstatus",
          "ReportProjectBuildingSite|Projectstatus", IsIDFilter.instance);
      String strProjectphase = vars.getRequestInGlobalVariable("inpProjectphase",
          "ReportProjectBuildingSite|Projectphase", IsIDFilter.instance);
      String strProduct = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportProjectBuildingSite|mProductId_IN", IsIDFilter.instance);
      String strProjectpublic = vars.getRequestGlobalVariable("inpProjectpublic",
          "ReportProjectBuildingSite|Projectpublic");
      String strSalesRep = vars.getRequestGlobalVariable("inpSalesRepId",
          "ReportProjectBuildingSite|SalesRepId");
      String strcRegionId = vars.getRequestInGlobalVariable("inpcRegionId",
          "ReportProjectBuildingSite|cRegionId", IsIDFilter.instance);
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strcBpartnerId, strcProjectId,
          strmCategoryId, strProjectkind, strProjectphase, strProjectstatus, strProjectpublic,
          strcRegionId, strSalesRep, strProduct);
    } else if (vars.commandIn("PDF")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportProjectBuildingSite|DateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportProjectBuildingSite|DateTo");
      String strcProjectId = vars.getRequestGlobalVariable("inpcProjectId",
          "ReportProjectBuildingSite|cProjectId");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportProjectBuildingSite|cBPartnerId_IN", IsIDFilter.instance);
      String strmCategoryId = vars.getRequestInGlobalVariable("inpmProductCategoryId",
          "ReportProjectBuildingSite|mCategoryId", IsIDFilter.instance);
      String strProjectkind = vars.getRequestInGlobalVariable("inpProjectkind",
          "ReportProjectBuildingSite|Projectkind", IsIDFilter.instance);
      String strProjectstatus = vars.getRequestInGlobalVariable("inpProjectstatus",
          "ReportProjectBuildingSite|Projectstatus", IsIDFilter.instance);
      String strProjectphase = vars.getRequestInGlobalVariable("inpProjectphase",
          "ReportProjectBuildingSite|Projectphase", IsIDFilter.instance);
      String strProduct = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportProjectBuildingSite|mProductId_IN", IsIDFilter.instance);
      String strProjectpublic = vars.getRequestGlobalVariable("inpProjectpublic",
          "ReportProjectBuildingSite|Projectpublic");
      String strSalesRep = vars.getRequestGlobalVariable("inpSalesRepId",
          "ReportProjectBuildingSite|SalesRepId");
      String strcRegionId = vars.getRequestInGlobalVariable("inpcRegionId",
          "ReportProjectBuildingSite|cRegionId", IsIDFilter.instance);
      printPageDataPDF(response, vars, strdateFrom, strdateTo, strcBpartnerId, strcProjectId,
          strmCategoryId, strProjectkind, strProjectphase, strProjectstatus, strProjectpublic,
          strcRegionId, strSalesRep, strProduct);
    } else
      pageErrorPopUp(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strcBpartnerId, String strcProjectId,
      String strmCategoryId, String strProjectkind, String strProjectphase,
      String strProjectstatus, String strProjectpublic, String strcRegionId, String strSalesRep,
      String strProduct) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    String discard[] = { "sectionPartner" };
    String strTitle = "";
    XmlDocument xmlDocument = null;
    if (vars.commandIn("DEFAULT")) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSite").createXmlDocument();

      ToolBar toolbar = new ToolBar(
          this,
          vars.getLanguage(),
          "ReportProjectBuildingSite",
          false,
          "",
          "",
          "openServletNewWindow('PDF', false, 'ReportProjectBuildingSitePDF.pdf', 'ReportProjectBuildingSite', null, false, '750', '1024', true);return false;",
          false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_reports.ReportProjectBuildingSite");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportProjectBuildingSite.html", classInfo.id, classInfo.type, strReplaceWith, tabs
                .breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportProjectBuildingSite.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportProjectBuildingSite");
        vars.removeMessage("ReportProjectBuildingSite");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("dateFrom", strdateFrom);
      xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTo", strdateTo);
      xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
      xmlDocument.setParameter("cProjectId", strcProjectId);
      xmlDocument.setParameter("projectName", ReportProjectBuildingSiteData.selectProject(this,
          strcProjectId));
      xmlDocument.setParameter("mProductCatId", strmCategoryId);
      xmlDocument.setParameter("cProjectKind", strProjectkind);
      xmlDocument.setParameter("cRegionId", strcRegionId);
      xmlDocument.setParameter("cProjectPhase", strProjectphase);
      xmlDocument.setParameter("cProjectStatus", strProjectstatus);
      xmlDocument.setParameter("cProjectPublic", strProjectpublic);
      xmlDocument.setParameter("salesRep", strSalesRep);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_kind",
            "Projectkind", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSite"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSite"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSite",
            strProjectkind);
        xmlDocument.setData("reportC_PROJECTKIND", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_phase",
            "Projectphase", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSite"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSite"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSite",
            strProjectphase);
        xmlDocument.setData("reportC_PROJECTKIND", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_status",
            "ProjectStatus", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSite"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSite"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSite",
            strProjectstatus);
        xmlDocument.setData("reportC_PROJECTSTATUS", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "C_Project_public",
            "ProjectPrivate", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSite"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSite"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportProjectBuildingSite",
            strProjectpublic);
        xmlDocument.setData("reportC_PROJECTPUBLIC", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_PRODUCT_CATEGORY_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSite"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSite"), 0);
        comboTableData.fillParameters(null, "ReportProjectBuildingSite", "");
        xmlDocument.setData("reportC_PRODUCTCATREGORY", "liststructure", comboTableData
            .select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID",
            "", "C_Region of Country", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "Account"), Utility.getContext(this, vars, "#User_Client",
                "ReportSalesOrderProvided"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvided",
            strcRegionId);
        xmlDocument.setData("reportC_REGIONID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "ReportProjectBuildingSite"), Utility.getContext(this, vars, "#User_Client",
                "ReportProjectBuildingSite"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesOrderProvided",
            strSalesRep);
        xmlDocument.setData("reportSALESREP", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setData("reportCBPartnerId_IN", "liststructure", SelectorUtilityData
          .selectBpartner(this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility
              .getContext(this, vars, "#User_Client", ""), strcBpartnerId));
      xmlDocument.setData("reportMProductId_IN", "liststructure", SelectorUtilityData
          .selectMproduct(this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility
              .getContext(this, vars, "#User_Client", ""), strProduct));
    } else {
      // Get user Client's base currency
      String strCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
      ReportProjectBuildingSiteData[] data = ReportProjectBuildingSiteData.select(this,
          strCurrencyId, Utility
              .getContext(this, vars, "#User_Client", "ReportProjectBuildingSite"), Utility
              .getContext(this, vars, "#AccessibleOrgTree", "ReportProjectBuildingSite"),
          strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"), strcBpartnerId,
          strcProjectId, strmCategoryId, strProjectkind, strProjectphase, strProjectstatus,
          strProjectpublic, strcRegionId, strSalesRep, strProduct);

      if (data == null || data.length == 0) {
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSitePop", discard)
            .createXmlDocument();
        xmlDocument.setData("structure1", ReportProjectBuildingSiteData.set());
      } else {
        xmlDocument = xmlEngine.readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSitePop").createXmlDocument();
        xmlDocument.setData("structure1", data);
      }
      if (!strProjectpublic.equals(""))
        strTitle += ", "
            + Utility.messageBD(this, "WithInitiativeType", vars.getLanguage())
            + " "
            + ReportProjectBuildingSiteData.selectProjectpublic(this, vars.getLanguage(),
                strProjectpublic);
      if (!strdateFrom.equals(""))
        strTitle += ", " + Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom;
      if (!strdateTo.equals(""))
        strTitle += " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;
      if (!strSalesRep.equals(""))
        strTitle += ", "
            + Utility.messageBD(this, "ForTheSalesRep", vars.getLanguage())
            + " "
            + ReportProjectBuildingSiteData.selectSalesRep(this, Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReportProjectBuildingSite"), Utility.getContext(this, vars,
                "#User_Client", "ReportProjectBuildingSite"), strSalesRep);
      xmlDocument.setParameter("title", strTitle);
    }
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataPDF(HttpServletResponse response, VariablesSecureApp vars,
      String strdateFrom, String strdateTo, String strcBpartnerId, String strcProjectId,
      String strmCategoryId, String strProjectkind, String strProjectphase,
      String strProjectstatus, String strProjectpublic, String strcRegionId, String strSalesRep,
      String strProduct) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: PDF");
    String discard[] = { "sectionPartner" };
    String strTitle = "";
    XmlDocument xmlDocument = null;

    // Get user Client's base currency
    String strCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    ReportProjectBuildingSiteData[] data = ReportProjectBuildingSiteData.select(this,
        strCurrencyId, Utility.getContext(this, vars, "#User_Client", "ReportProjectBuildingSite"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportProjectBuildingSite"),
        strdateFrom, DateTimeData.nDaysAfter(this, strdateTo, "1"), strcBpartnerId, strcProjectId,
        strmCategoryId, strProjectkind, strProjectphase, strProjectstatus, strProjectpublic,
        strcRegionId, strSalesRep, strProduct);

    if (data == null || data.length == 0) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSitePDF", discard)
          .createXmlDocument();
      xmlDocument.setData("structure1", ReportProjectBuildingSiteData.set());
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportProjectBuildingSitePDF").createXmlDocument();
      xmlDocument.setData("structure1", data);
    }
    if (!strProjectpublic.equals(""))
      strTitle += ", "
          + Utility.messageBD(this, "WithInitiativeType", vars.getLanguage())
          + " "
          + ReportProjectBuildingSiteData.selectProjectpublic(this, vars.getLanguage(),
              strProjectpublic);
    if (!strdateFrom.equals(""))
      strTitle += ", " + Utility.messageBD(this, "From", vars.getLanguage()) + " " + strdateFrom;
    if (!strdateTo.equals(""))
      strTitle += " " + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strdateTo;
    if (!strSalesRep.equals(""))
      strTitle += ", "
          + Utility.messageBD(this, "ForTheSalesRep", vars.getLanguage())
          + " "
          + ReportProjectBuildingSiteData.selectSalesRep(this, Utility.getContext(this, vars,
              "#AccessibleOrgTree", "ReportProjectBuildingSite"), Utility.getContext(this, vars,
              "#User_Client", "ReportProjectBuildingSite"), strSalesRep);

    xmlDocument.setData("structure1", data);
    String strResult = xmlDocument.print();
    if (log4j.isDebugEnabled())
      log4j.debug(strResult);
    renderFO(strResult, response);

  }

  public String getServletInfo() {
    return "Servlet ReportProjectBuildingSite. This Servlet was made by Eduardo Argal";
  } // end of getServletInfo() method
}
