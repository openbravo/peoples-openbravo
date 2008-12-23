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

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.utils.Replace;

import org.openbravo.erpCommon.utility.DateTimeData; //import org.openbravo.erpCommon.utility.ToolBar;

import org.openbravo.erpCommon.ad_combos.WarehouseComboData;
import org.openbravo.erpCommon.ad_combos.PriceListVersionComboData;

public class Product extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VariablesSecureApp vars = new VariablesSecureApp(request);

        if (vars.commandIn("DEFAULT")) {
            removePageSessionVariables(vars);
            String strNameValue = vars.getRequestGlobalVariable("inpNameValue",
                    "Product.name");
            String strIDValue = vars.getStringParameter("inpIDValue");
            if (!strIDValue.equals("")) {
                String strNameAux = ProductData.existsActual(this, vars
                        .getLanguage(), strNameValue, strIDValue);
                if (!strNameAux.equals(""))
                    strNameValue = strNameAux;
            }
            String strPriceList = vars.getRequestGlobalVariable("inpPriceList",
                    "Product.priceList");
            String strDate = vars.getRequestGlobalVariable("inpDate",
                    "Product.date");
            String windowId = vars.getRequestGlobalVariable("WindowID",
                    "Product.windowId");
            String strWarehouse = vars.getGlobalVariable("inpWarehouse",
                    "Product.warehouse", Utility.getContext(this, vars,
                            "M_Warehouse_ID", windowId));
            vars.removeSessionValue("Product.key");
            strNameValue = strNameValue + "%";
            vars.setSessionValue("Product.name", strNameValue);
            if (strPriceList.equals("")) {
                strPriceList = Utility.getContext(this, vars, "M_Pricelist_ID",
                        windowId);
                if (strPriceList.equals(""))
                    strPriceList = ProductData.priceListDefault(this, Utility
                            .getContext(this, vars, "#User_Client", "Product"),
                            Utility.getContext(this, vars, "#User_Org",
                                    "Product"));
                vars.setSessionValue("Product.priceList", strPriceList);
            }
            if (strDate.equals("")) {
                strDate = Utility.getContext(this, vars, "DateOrdered",
                        windowId);
                if (log4j.isDebugEnabled())
                    log4j.debug("DateOrdered:" + strDate);
            }
            if (strDate.equals("")) {
                strDate = Utility.getContext(this, vars, "DateInvoiced",
                        windowId);
                if (log4j.isDebugEnabled())
                    log4j.debug("DateInvoiced:" + strDate);
            }
            if (strDate.equals(""))
                strDate = DateTimeData.today(this);
            vars.setSessionValue("Product.date", strDate);

            String strPriceListVersion = getPriceListVersion(vars,
                    strPriceList, strDate);
            vars.setSessionValue("Product.priceListVersion",
                    strPriceListVersion);

            printPage(response, vars, "", strNameValue, strWarehouse,
                    strPriceList, strPriceListVersion, windowId, "paramName");
        } else if (vars.commandIn("KEY")) {
            removePageSessionVariables(vars);
            String strKeyValue = vars.getRequestGlobalVariable("inpNameValue",
                    "Product.key");
            String strIDValue = vars.getStringParameter("inpIDValue");
            if (!strIDValue.equals("")) {
                String strNameAux = ProductData.existsActualValue(this, vars
                        .getLanguage(), strKeyValue, strIDValue);
                if (!strNameAux.equals(""))
                    strKeyValue = strNameAux;
            }
            String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse",
                    "Product.warehouse");
            String strPriceList = vars.getRequestGlobalVariable("inpPriceList",
                    "Product.priceList");
            String strDate = vars.getRequestGlobalVariable("inpDate",
                    "Product.date");
            String windowId = vars.getRequestGlobalVariable("WindowID",
                    "Product.windowId");
            vars.removeSessionValue("Product.name");
            strKeyValue = strKeyValue + "%";
            vars.setSessionValue("Product.key", strKeyValue);
            if (strWarehouse.equals(""))
                strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID",
                        windowId);
            vars.setSessionValue("Product.warehouse", strWarehouse);
            if (strPriceList.equals("")) {
                strPriceList = Utility.getContext(this, vars, "M_Pricelist_ID",
                        windowId);
                if (strPriceList.equals(""))
                    strPriceList = ProductData.priceListDefault(this, Utility
                            .getContext(this, vars, "#User_Client", "Product"),
                            Utility.getContext(this, vars, "#User_Org",
                                    "Product"));
                vars.setSessionValue("Product.priceList", strPriceList);
            }

            if (strDate.equals("")) {
                strDate = Utility.getContext(this, vars, "DateOrdered",
                        windowId);
                if (log4j.isDebugEnabled())
                    log4j.debug("DateOrdered:" + strDate);
            }
            if (strDate.equals("")) {
                strDate = Utility.getContext(this, vars, "DateInvoiced",
                        windowId);
                if (log4j.isDebugEnabled())
                    log4j.debug("DateInvoiced:" + strDate);
            }
            if (strDate.equals(""))
                strDate = DateTimeData.today(this);
            vars.setSessionValue("Product.date", strDate);

            String strPriceListVersion = getPriceListVersion(vars,
                    strPriceList, strDate);
            vars.setSessionValue("Product.priceListVersion",
                    strPriceListVersion);
            ProductData[] data = ProductData.select(this, strWarehouse, "1",
                    strKeyValue + "%", "", Utility.getContext(this, vars,
                            "#User_Client", "Product"), Utility.getContext(
                            this, vars, "#User_Org", "Product"),
                    strPriceListVersion, "1", "", "");
            if (data != null && data.length == 1)
                printPageKey(response, vars, data, strWarehouse,
                        strPriceListVersion);
            else
                printPage(response, vars, strKeyValue, "", strWarehouse,
                        strPriceList, strPriceListVersion, windowId, "paramKey");
        } else if (vars.commandIn("STRUCTURE")) {
            printGridStructure(response, vars);
        } else if (vars.commandIn("DATA")) {
            if (vars.getStringParameter("newFilter").equals("1")) {
                removePageSessionVariables(vars);
            }
            String strKey = vars.getGlobalVariable("inpKey", "Product.key", "");
            String strName = vars.getGlobalVariable("inpName", "Product.name",
                    "");
            String strOrg = vars.getStringParameter("inpAD_Org_ID");
            String strWarehouse = vars.getGlobalVariable("inpWarehouse",
                    "Product.warehouse", "");
            String strPriceList = vars.getGlobalVariable("inpPriceList",
                    "Product.priceList", "");
            String strPriceListVersion = vars.getGlobalVariable(
                    "inpPriceListVersion", "Product.priceListVersion", "");

            String strNewFilter = vars.getStringParameter("newFilter");
            String strOffset = vars.getStringParameter("offset");
            String strPageSize = vars.getStringParameter("page_size");
            String strSortCols = vars.getStringParameter("sort_cols")
                    .toUpperCase();
            String strSortDirs = vars.getStringParameter("sort_dirs")
                    .toUpperCase();
            printGridData(response, vars, strKey, strName, strOrg,
                    strWarehouse, strPriceListVersion, strSortCols + " "
                            + strSortDirs, strOffset, strPageSize, strNewFilter);
        } else
            pageError(response);
    }

    private void removePageSessionVariables(VariablesSecureApp vars) {
        vars.removeSessionValue("Product.key");
        vars.removeSessionValue("Product.name");
        vars.removeSessionValue("Product.warehouse");
        vars.removeSessionValue("Product.priceList");
        vars.removeSessionValue("Product.priceListVersion");
    }

    String getPriceListVersion(VariablesSecureApp vars, String strPriceList,
            String strDate) throws IOException, ServletException {
        PriceListVersionComboData[] data = PriceListVersionComboData
                .selectActual(this, strPriceList, strDate, Utility.getContext(
                        this, vars, "#User_Client", "Product"));
        if (log4j.isDebugEnabled())
            log4j.debug("Selecting pricelistversion date:" + strDate
                    + " - pricelist:" + strPriceList);
        if (data == null || data.length == 0)
            return "";
        return data[0].mPricelistVersionId;
    }

    void printPage(HttpServletResponse response, VariablesSecureApp vars,
            String strKeyValue, String strNameValue, String strWarehouse,
            String strPriceList, String strPriceListVersion, String windowId,
            String focusedId) throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: Frame 1 of the products seeker");
        String[] discard = new String[1];
        if (windowId.equals("800004")) {
            discard[0] = new String("NotReducedSearch");
        } else {
            discard[0] = new String("ReducedSearch");
        }
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/info/Product", discard)
                .createXmlDocument();

        if (strKeyValue.equals("") && strNameValue.equals("")) {
            xmlDocument.setParameter("key", "%");
        } else {
            xmlDocument.setParameter("key", strKeyValue);
        }
        xmlDocument.setParameter("directory", "var baseDirectory = \""
                + strReplaceWith + "/\";\n");
        xmlDocument.setParameter("language", "defaultLang=\""
                + vars.getLanguage() + "\";");
        xmlDocument.setParameter("theme", vars.getTheme());
        xmlDocument.setParameter("name", strNameValue);
        xmlDocument.setParameter("warehouse", strWarehouse);
        xmlDocument.setParameter("priceListVersion", strPriceListVersion);

        // xmlDocument.setParameter("orgs",
        // vars.getStringParameter("inpAD_Org_ID"));

        xmlDocument.setParameter("jsFocusOnField", Utility
                .focusFieldJS(focusedId));

        xmlDocument.setParameter("grid", "20");
        xmlDocument.setParameter("grid_Offset", "");
        xmlDocument.setParameter("grid_SortCols", "1");
        xmlDocument.setParameter("grid_SortDirs", "ASC");
        xmlDocument.setParameter("grid_Default", "0");

        xmlDocument.setData("structure1", WarehouseComboData.selectFilter(this,
                Utility.getContext(this, vars, "#User_Client", "Product")));
        xmlDocument.setData("structure2", PriceListVersionComboData.select(
                this, strPriceList, Utility.getContext(this, vars,
                        "#User_Client", "Product")));

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
            ProductData[] data, String strWarehouse, String strPriceListVersion)
            throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: product seeker Frame Set");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/info/SearchUniqueKeyResponse")
                .createXmlDocument();

        xmlDocument.setParameter("script", generateResult(data, strWarehouse,
                strPriceListVersion));
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    String generateResult(ProductData[] data, String strWarehouse,
            String strPriceListVersion) throws IOException, ServletException {
        StringBuffer html = new StringBuffer();

        html.append("\nfunction validateSelector() {\n");
        html.append("var key = \"" + data[0].mProductId + "\";\n");
        html.append("var text = \""
                + Replace.replace(data[0].name, "\"", "\\\"") + "\";\n");
        html.append("var parameter = new Array(\n");
        html.append("new SearchElements(\"_UOM\", true, \"" + data[0].cUomId
                + "\"),\n");
        html.append("new SearchElements(\"_PSTD\", true, \"" + data[0].pricestd
                + "\"),\n");
        html.append("new SearchElements(\"_PLIM\", true, \""
                + data[0].pricelimit + "\"),\n");
        html.append("new SearchElements(\"_CURR\", true, \""
                + data[0].cCurrencyId + "\"),\n");
        html.append("new SearchElements(\"_PLIST\", true, \""
                + data[0].pricelist + "\")\n");
        html.append(");\n");
        html
                .append("parent.opener.closeSearch(\"SAVE\", key, text, parameter);\n");
        html.append("}\n");
        return html.toString();
    }

    void printGridStructure(HttpServletResponse response,
            VariablesSecureApp vars) throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: print page structure");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/utility/DataGridStructure")
                .createXmlDocument();

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
        String[] colNames = { "value", "name", "qtyavailable", "pricelist",
                "pricestd", "qtyonhand", "qtyordered", "margin", "pricelimit",
                "rowkey" };
        boolean[] colSortable = { true, true, false, false, false, false,
                false, false, false, false };
        // String[] gridNames = {"Key", "Name","Disp. Credit","Credit used",
        // "Contact", "Phone no.", "Zip", "City", "Income", "c_bpartner_id",
        // "c_bpartner_contact_id", "c_bpartner_location_id", "rowkey"};
        String[] colWidths = { "58", "129", "48", "95", "96", "124", "121",
                "48", "48", "0" };
        for (int i = 0; i < colNames.length; i++) {
            SQLReturnObject dataAux = new SQLReturnObject();
            dataAux.setData("columnname", colNames[i]);
            dataAux.setData("gridcolumnname", colNames[i]);
            dataAux.setData("adReferenceId", "AD_Reference_ID");
            dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
            dataAux.setData("isidentifier",
                    (colNames[i].equals("rowkey") ? "true" : "false"));
            dataAux.setData("iskey", (colNames[i].equals("rowkey") ? "true"
                    : "false"));
            dataAux.setData("isvisible", (colNames[i].endsWith("_id")
                    || colNames[i].equals("rowkey") ? "false" : "true"));
            String name = Utility.messageBD(this, "PS_"
                    + colNames[i].toUpperCase(), vars.getLanguage());
            dataAux.setData("name", (name.startsWith("PS_") ? colNames[i]
                    : name));
            dataAux.setData("type", "string");
            dataAux.setData("width", colWidths[i]);
            dataAux.setData("issortable", colSortable[i] ? "true" : "false");
            vAux.addElement(dataAux);
        }
        data = new SQLReturnObject[vAux.size()];
        vAux.copyInto(data);
        return data;
    }

    void printGridData(HttpServletResponse response, VariablesSecureApp vars,
            String strKey, String strName, String strOrg, String strWarehouse,
            String strPriceListVersion, String strOrderBy, String strOffset,
            String strPageSize, String strNewFilter) throws IOException,
            ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: print page rows");

        SQLReturnObject[] headers = getHeaders(vars);
        FieldProvider[] data = null;
        String type = "Hidden";
        String title = "";
        String description = "";
        String strNumRows = "0";

        if (headers != null) {
            try {
                if (strNewFilter.equals("1") || strNewFilter.equals("")) { // New
                                                                           // filter
                                                                           // or
                                                                           // first
                                                                           // load
                    strNumRows = ProductData.countRows(this, strKey, strName,
                            strPriceListVersion, Utility.getContext(this, vars,
                                    "#User_Client", "Product"), Utility
                                    .getContext(this, vars, "#User_Org",
                                            "Product"));
                    vars.setSessionValue("Product.numrows", strNumRows);
                } else {
                    strNumRows = vars.getSessionValue("Product.numrows");
                }

                // Filtering result
                if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
                    String oraLimit = strOffset
                            + " AND "
                            + String.valueOf(Integer.valueOf(strOffset)
                                    .intValue()
                                    + Integer.valueOf(strPageSize));
                    data = ProductData.select(this, strWarehouse, "ROWNUM",
                            strKey, strName, Utility.getContext(this, vars,
                                    "#User_Client", "Product"), Utility
                                    .getContext(this, vars, "#User_Org",
                                            "Product"), strPriceListVersion,
                            strOrderBy, oraLimit, "");
                } else {
                    String pgLimit = strPageSize + " OFFSET " + strOffset;
                    data = ProductData.select(this, strWarehouse, "1", strKey,
                            strName, Utility.getContext(this, vars,
                                    "#User_Client", "Product"), Utility
                                    .getContext(this, vars, "#User_Org",
                                            "Product"), strPriceListVersion,
                            strOrderBy, "", pgLimit);
                }
            } catch (ServletException e) {
                log4j.error("Error in print page data: " + e);
                e.printStackTrace();
                OBError myError = Utility.translateError(this, vars, vars
                        .getLanguage(), e.getMessage());
                if (!myError.isConnectionAvailable()) {
                    bdErrorAjax(response, "Error", "Connection Error",
                            "No database connection");
                    return;
                } else {
                    type = myError.getType();
                    title = myError.getTitle();
                    if (!myError.getMessage().startsWith("<![CDATA["))
                        description = "<![CDATA[" + myError.getMessage()
                                + "]]>";
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
        strRowsData.append("    <description>").append(description).append(
                "</description>\n");
        strRowsData.append("  </status>\n");
        strRowsData.append("  <rows numRows=\"").append(strNumRows).append(
                "\">\n");
        if (data != null && data.length > 0) {
            for (int j = 0; j < data.length; j++) {
                strRowsData.append("    <tr>\n");
                for (int k = 0; k < headers.length; k++) {
                    strRowsData.append("      <td><![CDATA[");
                    String columnname = headers[k].getField("columnname");

                    /*
                     * if (( (headers[k].getField("iskey").equals("false") &&
                     * !headers
                     * [k].getField("gridcolumnname").equalsIgnoreCase("keyname"
                     * )) || !headers[k].getField("iskey").equals("true")) &&
                     * !tableSQL.getSelectField(columnname + "_R").equals("")) {
                     * columnname += "_R"; }
                     */

                    if ((data[j].getField(columnname)) != null) {
                        if (headers[k].getField("adReferenceId").equals("32"))
                            strRowsData.append(strReplaceWith).append(
                                    "/images/");
                        strRowsData.append(data[j].getField(columnname)
                                .replaceAll("<b>", "").replaceAll("<B>", "")
                                .replaceAll("</b>", "").replaceAll("</B>", "")
                                .replaceAll("<i>", "").replaceAll("<I>", "")
                                .replaceAll("</i>", "").replaceAll("</I>", "")
                                .replaceAll("<p>", "&nbsp;").replaceAll("<P>",
                                        "&nbsp;").replaceAll("<br>", "&nbsp;")
                                .replaceAll("<BR>", "&nbsp;"));
                    } else {
                        if (headers[k].getField("adReferenceId").equals("32")) {
                            strRowsData.append(strReplaceWith).append(
                                    "/images/blank.gif");
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
