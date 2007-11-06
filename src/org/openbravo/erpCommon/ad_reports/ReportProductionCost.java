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

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportProductionCost extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (!Utility.hasProcessAccess(this, vars, "", "RV_ReportProductionCost")) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }

    if (vars.commandIn("DEFAULT")){
      String strdateFrom = vars.getGlobalVariable("inpDateFrom", "ReportProductionCost|dateFrom", "");
      String strdateTo = vars.getGlobalVariable("inpDateTo", "ReportProductionCost|dateTo", "");
      //String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId", "ReportProductionCost|bpartner", "");
      //String strCOrgId = vars.getGlobalVariable("inpCOrgId", "ReportProductionCost|orgID", "");
      String strmProductId = vars.getGlobalVariable("inpmProductId", "ReportProductionCost|mProductId", "");
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strmProductId);
    }else if (vars.commandIn("FIND")) {
      String strdateFrom = vars.getRequestGlobalVariable("inpDateFrom", "ReportProductionCost|dateFrom");
      String strdateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportProductionCost|dateTo");
      //String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId", "ReportProductionCost|bpartner");
      //String strCOrgId = vars.getRequestGlobalVariable("inpCOrgId", "ReportProductionCost|orgID");
      String strmProductId = vars.getRequestGlobalVariable("inpmProductId", "ReportProductionCost|mProductId");
      printPageDataSheet(response, vars, strdateFrom, strdateTo, strmProductId);
    } else if (vars.commandIn("OPEN"))  {
      String strdateFrom = vars.getRequiredStringParameter("inpDateFrom");
      String strdateTo = vars.getRequiredStringParameter("inpDateTo");
      String strmProductId = vars.getRequiredStringParameter("inpProduct");
      String strId = vars.getRequiredStringParameter("inpId");
      String strLevel = vars.getRequiredStringParameter("inpLevel");
      if (log4j.isDebugEnabled()) log4j.debug("***************************+: "+strdateFrom);
      if (log4j.isDebugEnabled()) log4j.debug("***************************+: "+strdateTo);
      if (log4j.isDebugEnabled()) log4j.debug("***************************+: "+strmProductId);
      if (log4j.isDebugEnabled()) log4j.debug("***************************+: "+strId);
      if (log4j.isDebugEnabled()) log4j.debug("***************************+: "+strLevel);

      printPageOpen(response, vars, strdateFrom, strdateTo, strmProductId, strId, strLevel);
    }else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strmProductId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    String discard[]={"discard"};
    XmlDocument xmlDocument=null;
    String strLevel = "0";
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportProductionCost").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportProductionCost", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    ReportProductionCostData[] data = null;

    if (strdateFrom.equals("") && strdateTo.equals("")){
      data = ReportProductionCostData.set();
      discard[0] = "sectionDetail";
    } else{
      data = ReportProductionCostData.select(this, strLevel, strdateFrom,  DateTimeData.nDaysAfter(this, strdateTo,"1"), strmProductId);
      if (data == null || data.length == 0){
        data = ReportProductionCostData.set();
        discard[0] = "sectionDetail";
      }
    }


    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportProductionCost.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportProductionCost");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportProductionCost.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportProductionCost.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportProductionCost");
      vars.removeMessage("ReportProductionCost");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFrom", strdateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strdateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    //    xmlDocument.setParameter("adOrgId", strCOrgId);
    //    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    //    xmlDocument.setParameter("paramBPartnerDescription", ReportProductionCostData.bPartnerDescription(this, strcBpartnerId));
    xmlDocument.setParameter("parammProductId", strmProductId);
    xmlDocument.setParameter("paramProductDescription", ReportProductionCostData.mProductDescription(this, strmProductId));

    //    xmlDocument.setData("structureOrganizacion", OrganizationComboData.select(this, vars.getRole()));
    xmlDocument.setData("structure", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }


  void printPageOpen(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strmProductId, String strId, String strLevel) throws IOException, ServletException {
    //Ajax response
    if (log4j.isDebugEnabled()) log4j.debug("Output: ajax");
    XmlDocument xmlDocument = null;
    String[] discard = {"discard", "discard", "discard", "discard", "discard"};
    ReportProductionCostData[] dataMaterial = ReportProductionCostData.selectMaterial(this, strId, strLevel, strdateFrom,  DateTimeData.nDaysAfter(this, strdateTo,"1"), strmProductId);
    if (dataMaterial == null || dataMaterial.length == 0){
      dataMaterial = ReportProductionCostData.set();
      discard[0] = "sectionMaterial";
    } 
    ReportProductionCostData[] dataMachine = ReportProductionCostData.selectMachine(this, strLevel, strdateFrom,  DateTimeData.nDaysAfter(this, strdateTo,"1"), strmProductId);
    if (dataMachine == null || dataMachine.length == 0) {
      dataMachine = ReportProductionCostData.set();
      discard[1] = "sectionMachine";
    } 
    ReportProductionCostData[] dataIndirect = ReportProductionCostData.selectIndirect(this, strLevel, strdateFrom,  DateTimeData.nDaysAfter(this, strdateTo,"1"), strmProductId);
    if (dataIndirect == null || dataIndirect.length == 0) {
      dataIndirect = ReportProductionCostData.set();
      discard[2] = "sectionIndirect";
    }
    ReportProductionCostData[] dataEmployee = ReportProductionCostData.selectEmployee(this, strLevel, strdateFrom, strdateTo, strmProductId);
    if (dataEmployee == null || dataEmployee.length == 0) {
      dataEmployee = ReportProductionCostData.set();
      discard[3] = "sectionEmployee";
    }
    ReportProductionCostData[] dataCostCenter = ReportProductionCostData.selectCostCenter(this, strLevel, strdateFrom, strdateTo, strmProductId);
    if (dataCostCenter == null || dataCostCenter.length == 0) {
      dataCostCenter = ReportProductionCostData.set();
      discard[4] = "sectionCostCenter";
    }

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportProductionCostSubreport", discard).createXmlDocument();
    //     xmlDocument.setData("structure1", data);
    xmlDocument.setData("structureMaterial", dataMaterial);
    xmlDocument.setData("structureMachine", dataMachine);
    xmlDocument.setData("structureIndirect", dataIndirect);
    xmlDocument.setData("structureEmployee", dataEmployee);
    xmlDocument.setData("structureCostCenter", dataCostCenter);

    response.setContentType("text/plain");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    //     xmlDocument.setData("structure", data);
    out.println(xmlDocument.print());
    out.close();
  }

  /*void printPageHtml(HttpServletResponse response, VariablesSecureApp vars, String strdateFrom, String strdateTo, String strcBpartnetId, String  strCOrgId, String strInvoiceRule, String strDetail) throws IOException, ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: print html");
    XmlDocument xmlDocument=null;
    ReportOrderNotInvoiceData[] data = null;
    if (!strDetail.equals("-1")) {
    String[] discard = {"selEliminar"};
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportOrderNotInvoice", discard).createXmlDocument();
    } else {
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportOrderNotInvoice").createXmlDocument();
    }
    data = ReportOrderNotInvoiceData.select(this, vars.getLanguage(),  Utility.getContext(this, vars, "#User_Client", "ReportOrderNotInvoice"), Utility.getContext(this, vars, "#User_Org", "ReportOrderNotInvoice"), strcBpartnetId, strCOrgId, strInvoiceRule, strdateFrom, DateTimeData.nDaysAfter(this, strdateTo,"1"));
    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
    }*/

  public String getServletInfo() {
    return "Servlet ReportProductionCost.";
  } // end of getServletInfo() method
}

