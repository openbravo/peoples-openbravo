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

public class ReportShipper extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strFrom = vars.getGlobalVariable("inpFrom", "ReportShipper|From", "");
      String strTo = vars.getGlobalVariable("inpTo", "ReportShipper|To", "");
      String strShipper = vars.getGlobalVariable("inpShipper", "ReportShipper|Shipper", "");
      String strSale = vars.getGlobalVariable("inpSale", "ReportShipper|Sale", "N");
      String strPurchase = vars.getGlobalVariable("inpPurchase", "ReportShipper|Purchase", "N");
      String strDetail = vars.getGlobalVariable("inpDetail", "ReportShipper|Detail", "N");
      printPageDataSheet(response, vars, strFrom, strTo, strShipper, strSale, strPurchase, strDetail);
    } else if (vars.commandIn("FIND")) {
      String strFrom = vars.getRequestGlobalVariable("inpFrom", "ReportShipper|From");
      String strTo = vars.getRequestGlobalVariable("inpTo", "ReportShipper|To");
      String strShipper = vars.getRequestGlobalVariable("inpShipper", "ReportShipper|Shipper");
      String strSale = vars.getRequestGlobalVariable("inpSale", "ReportShipper|Sale");
      String strPurchase = vars.getRequestGlobalVariable("inpPurchase", "ReportShipper|Purchase");
      String strDetail = vars.getRequestGlobalVariable("inpDetail", "ReportShipper|Detail");
      printPageDataSheet(response, vars, strFrom, strTo, strShipper, strSale, strPurchase, strDetail);
    } else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strFrom, String strTo, String strShipper, String strSale, String strPurchase, String strDetail)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    ReportShipperData[] data=null;

    String discard[]= {""};
    if (!strDetail.equals("Y")) discard[0] = "reportLine";
    if (vars.commandIn("DEFAULT")) discard[0] = "selEliminar";


    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/ReportShipper", discard).createXmlDocument();

    String strIsSOTrx = "";
    if (strSale.equals("Y") && !strPurchase.equals("Y")) strIsSOTrx = "Y";
    else if (!strSale.equals("Y") && strPurchase.equals("Y")) strIsSOTrx = "N";
    else if (!strSale.equals("Y") && !strPurchase.equals("Y")) strIsSOTrx = "D";

    if (log4j.isDebugEnabled()) log4j.debug("****data passed from: " + strFrom + " to: " +strTo + " shiper: " + strShipper + " isso " + strIsSOTrx + " det " + strDetail);
    data = ReportShipperData.select(this, vars.getLanguage(), strFrom, strTo, strShipper, strIsSOTrx);

    ReportShipperData[][] dataLine = new ReportShipperData[0][0];
    if (data != null && data.length > 0) {
      dataLine = new ReportShipperData[data.length][];

      for (int i=0; i<data.length ; i++) {
        if (log4j.isDebugEnabled()) log4j.debug("shipment " + data[i].shipmentid);
        dataLine[i] = ReportShipperData.selectLine(this, vars.getLanguage(), data[i].shipmentid);
        //if (RawMaterialData[i] == null || RawMaterialData[i].length == 0) RawMaterialData[i] = ReportRawMaterialData.set();
      }
    } //else dataLine[0] = ReportShipperData.set();


    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportShipper", false, "", "", "",false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      KeyMap key = new KeyMap(this, vars, "ReportShipper.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.ReportShipper");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportShipper.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportShipper.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportShipper");
      vars.removeMessage("ReportShipper");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    } 

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramFrom", strFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramTo", strTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramSale", strSale);
    xmlDocument.setParameter("paramPurchase", strPurchase);
    xmlDocument.setParameter("paramDetalle", strDetail);

    xmlDocument.setParameter("paramShipper", strShipper);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Shipper_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ReportShipper"), Utility.getContext(this, vars, "#User_Client", "ReportShipper"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportShipper", strShipper);
      xmlDocument.setData("reportShipper","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    xmlDocument.setData("structure", data);
    xmlDocument.setDataArray("reportLine","structureLine", dataLine);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ReportShipper.";
  } // end of getServletInfo() method
}

