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
 * All portions are Copyright (C) 2007 Openbravo SL 
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

import java.util.*;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ReportWarehousePartnerJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDate = vars.getGlobalVariable("inpDateFrom", "ReportWarehousePartnerJR|Date", DateTimeData.today(this));
      String strProductCategory = vars.getGlobalVariable("inpProductCategory", "ReportWarehousePartnerJR|productCategory", "");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN", "ReportWarehousePartnerJR|mProductId", "");
      String strX = vars.getGlobalVariable("inpX", "ReportWarehousePartnerJR|X", "");
      String strY = vars.getGlobalVariable("inpY", "ReportWarehousePartnerJR|Y", "");
      String strZ = vars.getGlobalVariable("inpZ", "ReportWarehousePartnerJR|Z", "");
      printPageDataSheet(response, vars, strDate, strProductCategory, strmProductId, strX, strY, strZ);
    } else if (vars.commandIn("FIND")) {
      String strDate = vars.getGlobalVariable("inpDateFrom", "ReportWarehousePartner|Date");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ReportWarehousePartnerJR|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN", "ReportWarehousePartnerJR|mProductId");
      String strX = vars.getRequestGlobalVariable("inpX", "ReportWarehousePartnerJR|X");
      String strY = vars.getRequestGlobalVariable("inpY", "ReportWarehousePartnerJR|Y");
      String strZ = vars.getRequestGlobalVariable("inpZ", "ReportWarehousePartnerJR|Z");
      setHistoryCommand(request, "FIND");
      printPageDataHtml(response, vars, strDate, strProductCategory, strmProductId, strX, strY, strZ);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strDate, String strProductCategory, String strmProductId, String strX, String strY, String strZ)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
   
    ReportWarehousePartnerData[] data=ReportWarehousePartnerData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportWarehouseControl"), Utility.getContext(this, vars, "#User_Org", "ReportWarehouseControl"), DateTimeData.nDaysAfter(this, strDate,"1"), strmProductId, strProductCategory, strX, strY, strZ);
    
    String strOutput = "html";
    String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportWarehousePartnerJR.jrxml";
    
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Title", classInfo.name);
    //parameters.put("Subtitle",strSubtitle);
    renderJR(vars, response, strReportName, strOutput, parameters, data, null );

  }
  
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDate, String strProductCategory, String strmProductId, String strX, String strY, String strZ)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportWarehousePartnerJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWarehousePartnerJR", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportWarehousePartnerJR.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportWarehousePartnerJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportWarehousePartnerJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportWarehousePartnerJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWarehousePartnerJR");
      vars.removeMessage("ReportWarehousePartnerJR");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }  

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("date", strDate);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("parameterX", strX);
    xmlDocument.setParameter("parameterY", strY);
    xmlDocument.setParameter("parameterZ", strZ);
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);

    xmlDocument.setData("reportMProductId_IN", "liststructure", ReportWarehousePartnerData.selectMproduct2(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strmProductId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportPricelist"), Utility.getContext(this, vars, "#User_Client", "ReportPricelist"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportPricelist", strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    
    out.println(xmlDocument.print());
    out.close();
  }
  
 /* void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDate, String strProductCategory, String strmProductId, String strX, String strY, String strZ)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportWarehousePartner").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportWarehousePartner", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportWarehousePartner.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportWarehousePartner");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportWarehousePartner.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportWarehousePartner.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportWarehousePartner");
      vars.removeMessage("ReportWarehousePartner");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }  

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("date", strDate);
    xmlDocument.setParameter("parameterX", strX);
    xmlDocument.setParameter("parameterY", strY);
    xmlDocument.setParameter("parameterZ", strZ);
    xmlDocument.setParameter("mProductCategoryId", strProductCategory);

    xmlDocument.setData("reportMProductId_IN", "liststructure", ReportWarehousePartnerData.selectMproduct2(this, Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), strmProductId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportPricelist"), Utility.getContext(this, vars, "#User_Client", "ReportPricelist"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportPricelist", strProductCategory);
      xmlDocument.setData("reportM_PRODUCT_CATEGORYID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    xmlDocument.setData("structure1", ReportWarehousePartnerData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ReportWarehouseControl"), Utility.getContext(this, vars, "#User_Org", "ReportWarehouseControl"), DateTimeData.nDaysAfter(this, strDate,"1"), strmProductId, strProductCategory, strX, strY, strZ));
    out.println(xmlDocument.print());
    out.close();
  }*/

  public String getServletInfo() {
    return "Servlet ReportWarehousePartner. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
