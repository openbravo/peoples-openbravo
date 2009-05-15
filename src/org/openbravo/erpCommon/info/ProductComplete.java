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
package org.openbravo.erpCommon.info;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_combos.WarehouseComboData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class ProductComplete extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      removePageSessionVariables(vars);
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ProductComplete.name");
      String strIDValue = vars.getStringParameter("inpIDValue");
      // This if allows correctly filling the key and name fields and the
      // products selector when we open it from the adecuadamente cuando
      // lo abrimos desde la línea de albarán.
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActual(this, vars.getLanguage(), strNameValue,
            strIDValue);
        if (!strNameAux.equals(""))
          strNameValue = strNameAux;
      }
      String windowId = vars.getRequestGlobalVariable("WindowID", "ProductComplete.windowId");
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse",
          "ProductComplete.warehouse");
      if (strWarehouse.equals(""))
        strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("ProductComplete.warehouse", strWarehouse);
      String strBpartner = vars.getRequestGlobalVariable("inpBPartner", "ProductComplete.bpartner");
      vars.removeSessionValue("ProductComplete.key");
      if (!strNameValue.equals(""))
        strNameValue = strNameValue + "%";
      vars.setSessionValue("ProductComplete.name", strNameValue);
      String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String isSOTrx = strIsSOTrxTab;
      if (strIsSOTrxTab.equals(""))
        isSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ProductComplete.isSOTrx", isSOTrx);
      String strStore = vars.getStringParameter("inpWithStoreLines", isSOTrx);
      vars.setSessionValue("ProductComplete.withstorelines", strStore);

      printPage(response, vars, "", strNameValue, strWarehouse, strStore, strBpartner, "", "",
          "paramName");
    } else if (vars.commandIn("KEY")) {
      removePageSessionVariables(vars);
      String windowId = vars.getRequestGlobalVariable("WindowID", "ProductComplete.windowId");
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "ProductComplete.key");
      String strIDValue = vars.getStringParameter("inpIDValue");
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActualValue(this, vars.getLanguage(), strKeyValue,
            strIDValue);
        if (!strNameAux.equals(""))
          strKeyValue = strNameAux;
      }
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse",
          "ProductComplete.warehouse");
      if (strWarehouse.equals(""))
        strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("ProductComplete.warehouse", strWarehouse);
      String strBpartner = vars.getRequestGlobalVariable("inpBPartner", "ProductComplete.bpartner");
      String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String isSOTrx = strIsSOTrxTab;
      if (strIsSOTrxTab.equals(""))
        isSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ProductComplete.isSOTrx", isSOTrx);
      String strStore = vars.getStringParameter("inpWithStoreLines", isSOTrx);
      vars.removeSessionValue("ProductComplete.name");
      if (!strKeyValue.equals(""))
        strKeyValue = strKeyValue + "%";
      vars.setSessionValue("ProductComplete.key", strKeyValue);
      vars.setSessionValue("ProductComplete.withstorelines", strStore);

      ProductCompleteData[] data = null;
      String strClients = Utility.getContext(this, vars, "#User_Client", "ProductComplete");
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      String strOrgs = Utility.getSelectorOrgs(this, vars, strOrg);

      String isCalledFromProduction = "";
      if (windowId.equals("800051") || (windowId.equals("800052"))) {
        log4j
            .debug("selector called from process plan||work requirement using production=y filter");
        isCalledFromProduction = "production";
      }

      if (strStore.equals("Y")) {
        if (vars.getLanguage().equals("en_US"))
          data = ProductCompleteData.select(this, "1", strKeyValue, "", strWarehouse,
              isCalledFromProduction, vars.getRole(), strBpartner, strClients, "1", "", "");
        else
          data = ProductCompleteData.selecttrl(this, vars.getLanguage(), "1", strKeyValue, "",
              strWarehouse, isCalledFromProduction, vars.getRole(), strBpartner, strClients, "1",
              "", "");
      } else {
        if (vars.getLanguage().equals("en_US"))
          data = ProductCompleteData.selectNotStored(this, "1", strKeyValue, "", strBpartner,
              strClients, strOrgs, isCalledFromProduction, "1", "", "");
        else
          data = ProductCompleteData.selectNotStoredtrl(this, "1", vars.getLanguage(), strKeyValue,
              "", strBpartner, strClients, strOrgs, isCalledFromProduction, "1", "", "");
      }
      if (data != null && data.length == 1)
        printPageKey(response, vars, data, strWarehouse);
      else
        printPage(response, vars, strKeyValue, "", strWarehouse, strStore, strBpartner, strClients,
            strOrgs, "paramKey");
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      if (vars.getStringParameter("newFilter").equals("1")) {
        removePageSessionVariables(vars);
      }
      String strKey = vars.getGlobalVariable("inpKey", "ProductComplete.key", "");
      String strName = vars.getGlobalVariable("inpName", "ProductComplete.name", "");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "ProductComplete.warehouse", "");
      String strBpartner = vars.getGlobalVariable("inpBPartner", "ProductComplete.bpartner", "");
      String strStore = vars.getGlobalVariable("inpWithStoreLines",
          "ProductComplete.withstorelines", "");

      String windowId = vars.getGlobalVariable("WindowID", "ProductComplete.windowId", "");
      String isCalledFromProduction = "";
      if (windowId.equals("800051") || (windowId.equals("800052"))) {
        log4j
            .debug("selector called from process plan||work requirement using production=y filter");
        isCalledFromProduction = "production";
      }

      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getStringParameter("sort_cols").toUpperCase();
      String strSortDirs = vars.getStringParameter("sort_dirs").toUpperCase();
      String strClients = Utility.getContext(this, vars, "#User_Client", "ProductComplete");
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      String strOrgs = Utility.getSelectorOrgs(this, vars, strOrg);

      printGridData(response, vars, strKey, strName, strWarehouse, strBpartner, strStore,
          isCalledFromProduction, strOrgs, strClients, strSortCols + " " + strSortDirs, strOffset,
          strPageSize, strNewFilter);
    } else
      pageError(response);
  }

  private void removePageSessionVariables(VariablesSecureApp vars) {
    vars.removeSessionValue("ProductComplete.key");
    vars.removeSessionValue("ProductComplete.name");
    vars.removeSessionValue("ProductComplete.warehouse");
    vars.removeSessionValue("ProductComplete.bpartner");
    vars.removeSessionValue("ProductComplete.withstorelines");
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue,
      String strNameValue, String strWarehouse, String strStore, String strBpartner,
      String strClients, String strOrgs, String focusedId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the product seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/ProductComplete").createXmlDocument();

    if (strKeyValue.equals("") && strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strKeyValue);
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("name", strNameValue);
    xmlDocument.setParameter("warehouse", strWarehouse);
    xmlDocument.setParameter("store", strStore);
    xmlDocument.setParameter("bpartner", strBpartner);

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");

    xmlDocument.setParameter("jsFocusOnField", Utility.focusFieldJS(focusedId));

    xmlDocument.setData("structure1", WarehouseComboData.select(this, vars.getRole(), vars
        .getClient()));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      ProductCompleteData[] data, String strWarehouse) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Product seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data, strWarehouse));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(ProductCompleteData[] data, String strWarehouse) throws IOException,
      ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].mProductId + "\";\n");
    html.append("var text = \"" + FormatUtilities.replaceJS(data[0].name) + "\";\n");
    html.append("var parameter = new Array(\n");
    html.append("new SearchElements(\"_LOC\", true, \"" + data[0].mLocatorId + "\"),\n");
    html.append("new SearchElements(\"_ATR\", true, \"" + data[0].mAttributesetinstanceId
        + "\"),\n");
    html.append("new SearchElements(\"_PQTY\", true, \""
        + (data[0].qtyorder.equals("0") ? "" : data[0].qtyorder) + "\"),\n");
    html.append("new SearchElements(\"_PUOM\", true, \"" + data[0].cUom2Id + "\"),\n");
    html.append("new SearchElements(\"_QTY\", true, \"" + data[0].qty + "\"),\n");
    html.append("new SearchElements(\"_UOM\", true, \"" + data[0].cUom1Id + "\")\n");
    html.append(");\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text, parameter);\n");
    html.append("}\n");
    return html.toString();
  }

  void printGridStructure(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page structure");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridStructure").createXmlDocument();

    SQLReturnObject[] data = getHeaders(vars);
    String type = "Hidden";
    String title = "";
    String description = "";

    xmlDocument.setParameter("type", type);
    xmlDocument.setParameter("title", title);
    xmlDocument.setParameter("description", description);
    xmlDocument.setData("structure1", data);
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  private SQLReturnObject[] getHeaders(VariablesSecureApp vars) {
    SQLReturnObject[] data = null;
    Vector<SQLReturnObject> vAux = new Vector<SQLReturnObject>();
    String[] colNames = { "value", "name", "locator", "qty", "c_uom1", "attribute", "qtyorder",
        "c_uom2", "qty_ref", "quantityorder_ref", "rowkey" };
    boolean[] colSortable = { true, true, false, true, false, true, true, false, false, false,
        false };
    // String[] gridNames = {"Key", "Name","Disp. Credit","Credit used",
    // "Contact", "Phone no.", "Zip", "City", "Income", "c_bpartner_id",
    // "c_bpartner_contact_id", "c_bpartner_location_id", "rowkey"};
    String[] colWidths = { "73", "86", "166", "62", "32", "145", "104", "67", "97", "167", "0" };
    for (int i = 0; i < colNames.length; i++) {
      SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("isvisible",
          (colNames[i].endsWith("_id") || colNames[i].equals("rowkey") ? "false" : "true"));
      String name = Utility.messageBD(this, "PCS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("PCS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      dataAux.setData("issortable", colSortable[i] ? "true" : "false");
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  void printGridData(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String strName, String strWarehouse, String strBpartner, String strStore,
      String strIsCalledFromProduction, String strOrgs, String strClients, String strOrderBy,
      String strOffset, String strPageSize, String strNewFilter) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page rows");

    SQLReturnObject[] headers = getHeaders(vars);
    FieldProvider[] data = null;
    String type = "Hidden";
    String title = "";
    String description = "";
    String strNumRows = "0";
    int offset = Integer.valueOf(strOffset).intValue();
    int pageSize = Integer.valueOf(strPageSize).intValue();

    // strip out single '%' parameters used with like
    strKey = strKey.equals("%") ? "" : strKey;
    strName = strName.equals("%") ? "" : strName;

    if (headers != null) {
      try {
        if (strNewFilter.equals("1") || strNewFilter.equals("")) { // New
          // filter
          // or
          // first
          // load
          if (strStore.equals("Y")) {
            // countRows is the same in en_US and +trl case, so a
            // single countRows method is used
            strNumRows = ProductCompleteData.countRows(this, strKey, strName, strWarehouse,
                strIsCalledFromProduction, vars.getRole(), strBpartner, strClients);
          } else {
            if (vars.getLanguage().equals("en_US")) {
              strNumRows = ProductCompleteData.countRowsNotStored(this, strKey, strName,
                  strBpartner, strClients, strOrgs, strIsCalledFromProduction);
            } else {
              strNumRows = ProductCompleteData.countRowsNotStoredtrl(this, vars.getLanguage(),
                  strKey, strName, strBpartner, strClients, strOrgs, strIsCalledFromProduction);
            }
          }
          vars.setSessionValue("ProductComplete.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("ProductComplete.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit = (offset + 1) + " AND " + String.valueOf(offset + pageSize);
          // data = BusinessPartnerData.select(this, "ROWNUM",
          // Utility.getContext(this, vars, "#User_Client",
          // "BusinessPartner"), Utility.getSelectorOrgs(this, vars,
          // strOrg), strKey, strName, strContact, strZIP,
          // strProvincia,
          // (strBpartners.equals("costumer")?"clients":""),
          // (strBpartners.equals("vendor")?"vendors":""), strCity,
          // strOrderBy, oraLimit, "");}

          if (strStore.equals("Y")) {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.select(this, "ROWNUM", strKey, strName, strWarehouse,
                  strIsCalledFromProduction, vars.getRole(), strBpartner, strClients, strOrderBy,
                  oraLimit, "");
            else
              data = ProductCompleteData.selecttrl(this, vars.getLanguage(), "ROWNUM", strKey,
                  strName, strWarehouse, strIsCalledFromProduction, vars.getRole(), strBpartner,
                  strClients, strOrderBy, oraLimit, "");
          } else {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.selectNotStored(this, "ROWNUM", strKey, strName,
                  strBpartner, strClients, strOrgs, strIsCalledFromProduction, strOrderBy,
                  oraLimit, "");
            else
              data = ProductCompleteData.selectNotStoredtrl(this, "ROWNUM", vars.getLanguage(),
                  strKey, strName, strBpartner, strClients, strOrgs, strIsCalledFromProduction,
                  strOrderBy, oraLimit, "");
          }
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          // data = BusinessPartnerData.select(this, "1",
          // Utility.getContext(this, vars, "#User_Client",
          // "BusinessPartner"), Utility.getSelectorOrgs(this, vars,
          // strOrg), strKey, strName, strContact, strZIP,
          // strProvincia,
          // (strBpartners.equals("costumer")?"clients":""),
          // (strBpartners.equals("vendor")?"vendors":""), strCity,
          // strOrderBy, "", pgLimit);

          if (strStore.equals("Y")) {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.select(this, "1", strKey, strName, strWarehouse,
                  strIsCalledFromProduction, vars.getRole(), strBpartner, strClients, strOrderBy,
                  "", pgLimit);
            else
              data = ProductCompleteData.selecttrl(this, vars.getLanguage(), "1", strKey, strName,
                  strWarehouse, strIsCalledFromProduction, vars.getRole(), strBpartner, strClients,
                  strOrderBy, "", pgLimit);
          } else {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.selectNotStored(this, "1", strKey, strName, strBpartner,
                  strClients, strOrgs, strIsCalledFromProduction, strOrderBy, "", pgLimit);
            else
              data = ProductCompleteData.selectNotStoredtrl(this, "1", vars.getLanguage(), strKey,
                  strName, strBpartner, strClients, strOrgs, strIsCalledFromProduction, strOrderBy,
                  "", pgLimit);
          }

        }
      } catch (ServletException e) {
        log4j.error("Error in print page data: " + e);
        e.printStackTrace();
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorAjax(response, "Error", "Connection Error", "No database connection");
          return;
        } else {
          type = myError.getType();
          title = myError.getTitle();
          if (!myError.getMessage().startsWith("<![CDATA["))
            description = "<![CDATA[" + myError.getMessage() + "]]>";
          else
            description = myError.getMessage();
        }
      } catch (Exception e) {
        if (log4j.isDebugEnabled())
          log4j.debug("Error obtaining rows data");
        type = "Error";
        title = "Error";
        if (e.getMessage().startsWith("<![CDATA["))
          description = "<![CDATA[" + e.getMessage() + "]]>";
        else
          description = e.getMessage();
        e.printStackTrace();
      }
    }

    if (!type.startsWith("<![CDATA["))
      type = "<![CDATA[" + type + "]]>";
    if (!title.startsWith("<![CDATA["))
      title = "<![CDATA[" + title + "]]>";
    if (!description.startsWith("<![CDATA["))
      description = "<![CDATA[" + description + "]]>";
    StringBuffer strRowsData = new StringBuffer();
    strRowsData.append("<xml-data>\n");
    strRowsData.append("  <status>\n");
    strRowsData.append("    <type>").append(type).append("</type>\n");
    strRowsData.append("    <title>").append(title).append("</title>\n");
    strRowsData.append("    <description>").append(description).append("</description>\n");
    strRowsData.append("  </status>\n");
    strRowsData.append("  <rows numRows=\"").append(strNumRows).append("\">\n");
    if (data != null && data.length > 0) {
      for (int j = 0; j < data.length; j++) {
        strRowsData.append("    <tr>\n");
        for (int k = 0; k < headers.length; k++) {
          strRowsData.append("      <td><![CDATA[");
          String columnname = headers[k].getField("columnname");

          /*
           * if (( (headers[k].getField("iskey").equals("false") && !headers
           * [k].getField("gridcolumnname").equalsIgnoreCase("keyname" )) ||
           * !headers[k].getField("iskey").equals("true")) && !tableSQL.getSelectField(columnname +
           * "_R").equals("")) { columnname += "_R"; }
           */

          if ((data[j].getField(columnname)) != null) {
            if (headers[k].getField("adReferenceId").equals("32"))
              strRowsData.append(strReplaceWith).append("/images/");
            strRowsData.append(data[j].getField(columnname).replaceAll("<b>", "").replaceAll("<B>",
                "").replaceAll("</b>", "").replaceAll("</B>", "").replaceAll("<i>", "").replaceAll(
                "<I>", "").replaceAll("</i>", "").replaceAll("</I>", "")
                .replaceAll("<p>", "&nbsp;").replaceAll("<P>", "&nbsp;").replaceAll("<br>",
                    "&nbsp;").replaceAll("<BR>", "&nbsp;"));
          } else {
            if (headers[k].getField("adReferenceId").equals("32")) {
              strRowsData.append(strReplaceWith).append("/images/blank.gif");
            } else
              strRowsData.append("&nbsp;");
          }
          strRowsData.append("]]></td>\n");
        }
        strRowsData.append("    </tr>\n");
      }
    }
    strRowsData.append("  </rows>\n");
    strRowsData.append("</xml-data>\n");

    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(strRowsData.toString());
    out.print(strRowsData.toString());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the products seeker";
  } // end of getServletInfo() method
}
