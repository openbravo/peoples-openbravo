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

import java.util.HashMap;


public class ReportGuaranteeDateJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

	if (!Utility.hasProcessAccess(this, vars, "", "ReportGuaranteeDateJR")) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }

    if (vars.commandIn("DEFAULT")) {
      String strDate = vars.getGlobalVariable("inpDate", "ReportGuaranteeDateJR|date", "");
      String strcBpartnerId = vars.getGlobalVariable("inpcBPartnerId", "ReportGuaranteeDateJR|cBpartnerId", "");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId","");
      printPageDataSheet(response, vars, strDate, strcBpartnerId, strmWarehouseId);
    } else if (vars.commandIn("FIND")) {
      String strDate = vars.getRequestGlobalVariable("inpDate", "ReportGuaranteeDateJR|date");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpcBPartnerId", "ReportGuaranteeDateJR|cBpartnerId");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId");
      setHistoryCommand(request, "FIND");
      printPageDataHtml(response, vars, strDate, strcBpartnerId, strmWarehouseId);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strDate, String strcBpartnerId, String strmWarehouseId)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");

    //XmlDocument xmlDocument=null;
    ReportGuaranteeDateData[] data = null;
	String discard[] = {"discard"};
      data = ReportGuaranteeDateData.select(this, Utility.getContext(this, vars, "#User_Client", "ReportGuaranteeDateJR"), Utility.getContext(this, vars, "#User_Org", "ReportGuaranteeDateJR"), DateTimeData.nDaysAfter(this, strDate,"1"), strcBpartnerId, strmWarehouseId);

  if (data == null && data.length == 0){
        discard[0] = "selEliminar";
        data = ReportGuaranteeDateData.set();
      }

      String strReportName = "@basedesign@/org/openbravo/erpCommon/ad_reports/ReportGuaranteeDateJR.jrxml";
      String strOutput="html";
      if (strOutput.equals("pdf")) response.setHeader("Content-disposition", "inline; filename=ReportGuaranteeDateJR.pdf");

       
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("REPORT_TITLE", classInfo.name);
		parameters.put("ReportData", strDate);
	
	renderJR(vars, response, strReportName, strOutput, parameters, data, null );

  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strDate, String strcBpartnerId, String strmWarehouseId)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    
   
    
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportGuaranteeDateJR").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportGuaranteeDateJR", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      KeyMap key = new KeyMap(this, vars, "ReportGuaranteeDateJR.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportGuaranteeDateJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportGuaranteeDateJR.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportGuaranteeDateJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportGuaranteeDateJR");
      vars.removeMessage("ReportGuaranteeDateJR");
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
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramBPartnerId", strcBpartnerId);
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("bPartnerDescription", ReportGuaranteeDateData.selectBpartner(this, strcBpartnerId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportGuaranteeDateJR"), Utility.getContext(this, vars, "#User_Client", "ReportGuaranteeDateJR"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportGuaranteeDateJR", strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSEID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
   

    out.println(xmlDocument.print());
    out.close();
  }
  


  public String getServletInfo() {
    return "Servlet ReportGuaranteeDateJR. This Servlet was made by Jon Alegria";
  } // end of getServletInfo() method
}
