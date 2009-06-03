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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportInventory extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId", vars.getWarehouse());
      String strDetalle = vars.getStringParameter("inpDetalle", "");
      printPageDataSheet(response, vars, strmWarehouseId, strDetalle);
    } else if (vars.commandIn("FIND")) {
      String strDetalle = vars.getStringParameter("inpDetalle");
      String strmWarehouseId = vars.getStringParameter("inpmWarehouseId");
      printPageDataHtml(response, vars, strmWarehouseId, strDetalle);
    } else
      pageError(response);
  }

  private void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars,
      String strmWarehouseId, String strDetalle) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportInventoryData[] data = ReportInventoryData.selectSinDetalle(this, strmWarehouseId,
        Utility.getContext(this, vars, "#User_Client", "ReportInventory"), Utility.getContext(this,
            vars, "#AccessibleOrgTree", "ReportInventory"));

    if (data.length == 0) {
      String[] discard = { "sectionLocator" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportInventoryEdit", discard).createXmlDocument();
      xmlDocument.setData("structure1", ReportInventoryData.set());
    } else if (!strDetalle.equals("-1")) {
      String[] discard = { "reportDetalle" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportInventoryEdit", discard).createXmlDocument();
      xmlDocument.setData("structure1", data);
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportInventoryEdit").createXmlDocument();

      ReportInventoryData[][] subreport = new ReportInventoryData[data.length][];

      String strWarehouse = "";
      String strProduct = "";
      String strUom = "";
      int j = 0;
      boolean existsAtt = false;
      for (int i = 0; i < data.length; i++) {
        existsAtt = false;
        if (!(strWarehouse.equals(data[i].mLocatorId)) || !(strProduct.equals(data[i].mProductId))
            || !(strUom.equals(data[i].productUom))) {
          strWarehouse = data[i].mLocatorId;
          strProduct = data[i].mProductId;
          strUom = data[i].productUom;
          ReportInventoryData[] data2 = ReportInventoryData.select(this, data[i].mProductId,
              data[i].cUomId, data[i].mLocatorId, Utility.getContext(this, vars, "#User_Client",
                  "ReportInventory"), Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "ReportInventory"));
          for (int k = 0; k < data2.length; k++) {
            if (!data2[k].lot.equals("")) {
              subreport[j] = data2;
              existsAtt = true;
              break;
            }
          }
          if (!existsAtt) {
            subreport[j] = new ReportInventoryData[0];
          }
          j++;
        }
      }

      xmlDocument.setDataArray("reportDetalle", "structure4", subreport);
      xmlDocument.setData("structure1", data);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strmWarehouseId, String strDetalle) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_reports/ReportInventory").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportInventory", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.ReportInventory");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportInventory.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportInventory.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportInventory");
      vars.removeMessage("ReportInventory");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    // xmlDocument.setData("structure",
    // WarehouseComboData.selectFilter(this, Utility.getContext(this, vars,
    // "#User_Client", "ReportInventory")));

    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("Detalle", strDetalle);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInventory"), Utility
              .getContext(this, vars, "#User_Client", "ReportInventory"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportInventory",
          strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSEID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    out.println(xmlDocument.print());
    out.close();
  }

  /*
   * void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String
   * strmWarehouseId, String strDetalle) throws IOException, ServletException { if
   * (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * XmlDocument xmlDocument=null; ReportInventoryData [] data =
   * ReportInventoryData.selectSinDetalle(this, strmWarehouseId, Utility.getContext(this, vars,
   * "#User_Client", "ReportInventory"), Utility.getContext(this, vars, "#AccessibleOrgTree",
   * "ReportInventory"));
   * 
   * 
   * if (data.length == 0) { String[] discard = {"sectionLocator"}; xmlDocument
   * =xmlEngine.readXmlTemplate( "org/openbravo/erpCommon/ad_reports/ReportInventory",
   * discard).createXmlDocument(); xmlDocument.setData("structure1", ReportInventoryData.set()); }
   * else if(!strDetalle.equals("-1")){ String[] discard = {"reportDetalle"}; xmlDocument
   * =xmlEngine.readXmlTemplate( "org/openbravo/erpCommon/ad_reports/ReportInventory",
   * discard).createXmlDocument(); xmlDocument.setData("structure1", data); } else{ xmlDocument
   * =xmlEngine.readXmlTemplate( "org/openbravo/erpCommon/ad_reports/ReportInventory"
   * ).createXmlDocument();
   * 
   * 
   * ReportInventoryData [] [] subreport = new ReportInventoryData [data.length] [];
   * 
   * String strWarehouse = ""; String strProduct = ""; String strUom = ""; int j = 0; boolean
   * existsAtt = false; for (int i=0;i<data.length ;i++){ existsAtt = false; if
   * (!(strWarehouse.equals(data[i].mLocatorId))||!(strProduct
   * .equals(data[i].mProductId))||!(strUom.equals(data[i].productUom))) { strWarehouse =
   * data[i].mLocatorId; strProduct = data[i].mProductId; strUom = data[i].productUom;
   * ReportInventoryData [] data2 = ReportInventoryData.select(this, data[i].mProductId,
   * data[i].cUomId, data[i].mLocatorId, Utility.getContext(this, vars, "#User_Client",
   * "ReportInventory"), Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportInventory"));
   * for (int k=0;k<data2.length ;k++ ){ if (!data2[k].lot.equals("")){ subreport[j] = data2;
   * existsAtt = true; break; } } if (!existsAtt) { subreport[j] = new ReportInventoryData [0]; }
   * j++; } }
   * 
   * xmlDocument.setDataArray("reportDetalle","structure4",subreport);
   * xmlDocument.setData("structure1", data); } ToolBar toolbar = new ToolBar(this,
   * vars.getLanguage(), "ReportInventory", false, "", "", "",false, "ad_reports", strReplaceWith,
   * false, true); toolbar.prepareSimpleToolBarTemplate(); xmlDocument.setParameter("toolbar",
   * toolbar.toString()); try { WindowTabs tabs = new WindowTabs(this, vars,
   * "org.openbravo.erpCommon.ad_reports.ReportInventory");
   * xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
   * xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
   * xmlDocument.setParameter("childTabContainer", tabs.childTabs());
   * xmlDocument.setParameter("theme", vars.getTheme()); NavigationBar nav = new NavigationBar(this,
   * vars.getLanguage(), "ReportInventory.html", classInfo.id, classInfo.type, strReplaceWith,
   * tabs.breadcrumb()); xmlDocument.setParameter("navigationBar", nav.toString()); LeftTabsBar lBar
   * = new LeftTabsBar(this, vars.getLanguage(), "ReportInventory.html", strReplaceWith);
   * xmlDocument.setParameter("leftTabs", lBar.manualTemplate()); } catch (Exception ex) { throw new
   * ServletException(ex); } { OBError myMessage = vars.getMessage("ReportInventory");
   * vars.removeMessage("ReportInventory"); if (myMessage!=null) {
   * xmlDocument.setParameter("messageType", myMessage.getType());
   * xmlDocument.setParameter("messageTitle", myMessage.getTitle());
   * xmlDocument.setParameter("messageMessage", myMessage.getMessage()); } }
   * 
   * 
   * 
   * xmlDocument.setData("structure", WarehouseComboData.selectFilter(this, Utility.getContext(this,
   * vars, "#User_Client", "ReportInventory")));
   * 
   * xmlDocument.setParameter("warehouse", strmWarehouseId); xmlDocument.setParameter("directory",
   * "var baseDirectory = \"" + strReplaceWith + "/\";\n"); xmlDocument.setParameter("language",
   * "defaultLang=\"" + vars.getLanguage() + "\";"); xmlDocument.setParameter("Detalle",
   * strDetalle); out.println(xmlDocument.print()); out.close(); }
   */

  public String getServletInfo() {
    return "Servlet ReportInventory. This Servlet was made by Eduardo Argal";
  } // end of getServletInfo() method
}
