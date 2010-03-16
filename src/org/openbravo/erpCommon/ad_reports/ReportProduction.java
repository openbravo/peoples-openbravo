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

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportProduction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportProduction|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportProduction|DateTo", "");
      String strRawMaterial = vars.getGlobalVariable("inpRawMaterial",
          "ReportProduction|RawMaterial", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strRawMaterial);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars
          .getRequestGlobalVariable("inpDateFrom", "ReportProduction|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportProduction|DateTo");
      String strRawMaterial = vars.getRequestGlobalVariable("inpRawMaterial",
          "ReportProduction|RawMaterial");
      printPagePDF(response, vars, strDateFrom, strDateTo, strRawMaterial);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strRawMaterial) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportProduction").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProduction", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportProduction");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportProduction.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportProduction.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportProduction");
      vars.removeMessage("ReportProduction");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("rawMaterial", strRawMaterial);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strRawMaterial) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportProductionPDF").createXmlDocument();

    String strTitle = "Informe de producción";
    if (!strDateFrom.equals(""))
      strTitle = strTitle + "desde el " + strDateFrom;
    if (!strDateTo.equals(""))
      strTitle = strTitle + " hasta el " + strDateTo;

    if (!strRawMaterial.equals("Y"))
      strRawMaterial = "N";

    ReportProductionData[] data = ReportProductionData.select(this, strRawMaterial, Utility
        .getContext(this, vars, "#User_Client", "ReportProduction"), Utility.getContext(this, vars,
        "#AccessibleOrgTree", "ReportProduction"), strDateFrom, DateTimeData.nDaysAfter(this,
        strDateTo, "1"));
    if (data == null || data.length == 0) {
      data = ReportProductionData.set();
    }
    ReportProductionData[] dataSummering = ReportProductionData.selectSummering(this, Utility
        .getContext(this, vars, "#User_Client", "ReportProduction"), Utility.getContext(this, vars,
        "#AccessibleOrgTree", "ReportProduction"), strDateFrom, DateTimeData.nDaysAfter(this,
        strDateTo, "1"));

    if (dataSummering == null || dataSummering.length == 0) {
      dataSummering = ReportProductionData.set();
    }

    xmlDocument.setParameter("title", strTitle);
    xmlDocument.setData("structure1", data);
    xmlDocument.setData("structure2", dataSummering);
    String strResult = xmlDocument.print();
    renderFO(strResult, response);
  }

  public String getServletInfo() {
    return "Servlet ReportProduction. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
