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
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ProductMultiple extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VariablesSecureApp vars = new VariablesSecureApp(request);

        if (vars.commandIn("DEFAULT")) {
            // String strNameValue =
            // vars.getRequestGlobalVariable("inpNameValue",
            // "ProductMultiple.name");
            // vars.getRequestGlobalVariable("inpProductCategory",
            // "ProductMultiple.productCategory");
            // if (!strNameValue.equals(""))
            // vars.setSessionValue("ProductMultiple.name", strNameValue + "%");
            String strKeyValue = vars.getGlobalVariable("inpKey",
                    "ProductMultiple.key", "");
            String strNameValue = vars.getGlobalVariable("inpName",
                    "ProductMultiple.name", "");
            PrintPage(response, vars, strKeyValue, strNameValue);
        } else if (vars.commandIn("STRUCTURE")) {
            printGridStructure(response, vars);
        } else if (vars.commandIn("DATA")) {
            if (vars.getStringParameter("clear").equals("true")) {
                vars.removeSessionValue("ProductMultiple.key");
                vars.removeSessionValue("ProductMultiple.name");
                vars.removeSessionValue("ProductMultiple.productCategory");
                vars.removeSessionValue("ProductMultiple.adorgid");
            }
            String strKey = vars.getGlobalVariable("inpKey",
                    "ProductMultiple.key", "");
            String strName = vars.getGlobalVariable("inpName",
                    "ProductMultiple.name", "");
            String strProductCategory = vars
                    .getGlobalVariable("inpProductCategory",
                            "ProductMultiple.productCategory", "");
            String strOrg = vars.getGlobalVariable("inpAD_Org_ID",
                    "ProductMultiple.adorgid", "");
            String strNewFilter = vars.getStringParameter("newFilter");
            String strOffset = vars.getStringParameter("offset");
            String strPageSize = vars.getStringParameter("page_size");
            String strSortCols = vars.getStringParameter("sort_cols")
                    .toUpperCase();
            String strSortDirs = vars.getStringParameter("sort_dirs")
                    .toUpperCase();
            printGridData(response, vars, strKey, strName, strProductCategory,
                    strOrg, strSortCols + " " + strSortDirs, strOffset,
                    strPageSize, strNewFilter);
        } else
            pageError(response);
    }

    void PrintPage(HttpServletResponse response, VariablesSecureApp vars,
            String strKeyValue, String strNameValue) throws IOException,
            ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: Multiple products seeker Frame Set");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/info/ProductMultiple")
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
        try {
            ComboTableData comboTableData = new ComboTableData(vars, this,
                    "TABLEDIR", "M_Product_Category_ID", "", "", Utility
                            .getContext(this, vars, "#User_Org",
                                    "ProductMultiple"), Utility.getContext(
                            this, vars, "#User_Client", "ProductMultiple"), 0);
            Utility.fillSQLParameters(this, vars, null, comboTableData,
                    "ProductMultiple", vars.getSessionValue(
                            "ProductMultiple.productCategory", ""));
            xmlDocument.setData("reportM_Product_Category_ID", "liststructure",
                    comboTableData.select(false));
            comboTableData = null;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        xmlDocument.setParameter("category", vars.getSessionValue(
                "ProductMultiple.productCategory", ""));
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars,
            String strKeyValue, String strNameValue) throws IOException,
            ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: Frame 1 of the multiple products seeker");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/info/ProductMultiple_F1")
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
        try {
            ComboTableData comboTableData = new ComboTableData(vars, this,
                    "TABLEDIR", "M_Product_Category_ID", "", "", Utility
                            .getContext(this, vars, "#User_Org",
                                    "ProductMultiple"), Utility.getContext(
                            this, vars, "#User_Client", "ProductMultiple"), 0);
            Utility.fillSQLParameters(this, vars, null, comboTableData,
                    "ProductMultiple", "");
            xmlDocument.setData("reportM_Product_Category_ID", "liststructure",
                    comboTableData.select(false));
            comboTableData = null;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
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
        String[] colNames = { "Value", "Name", "Product_Category",
                "M_Product_ID", "RowKey" };
        String[] colWidths = { "109", "225", "465", "0", "0" };
        for (int i = 0; i < colNames.length; i++) {
            SQLReturnObject dataAux = new SQLReturnObject();
            dataAux.setData("columnname", colNames[i]);
            dataAux.setData("gridcolumnname", colNames[i]);
            dataAux.setData("adReferenceId", "AD_Reference_ID");
            dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
            dataAux.setData("isidentifier",
                    (colNames[i].equals("rowkey") ? "true" : "false"));
            dataAux.setData("iskey", (colNames[i].equals("RowKey") ? "true"
                    : "false"));
            dataAux.setData("isvisible", (colNames[i].equals("M_Product_ID")
                    || colNames[i].equalsIgnoreCase("RowKey") ? "false"
                    : "true"));
            String name = Utility.messageBD(this, "MPS_"
                    + colNames[i].toUpperCase(), vars.getLanguage());
            dataAux.setData("name", (name.startsWith("MPS_") ? colNames[i]
                    : name));
            dataAux.setData("type", "string");
            dataAux.setData("width", colWidths[i]);
            vAux.addElement(dataAux);
        }
        data = new SQLReturnObject[vAux.size()];
        vAux.copyInto(data);
        return data;
    }

    void printGridData(HttpServletResponse response, VariablesSecureApp vars,
            String strKey, String strName, String strProductCategory,
            String strOrg, String strOrderBy, String strOffset,
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
                    data = ProductMultipleData.select(this, "1", strKey,
                            strName, strProductCategory, Utility.getContext(
                                    this, vars, "#User_Client",
                                    "ProductMultiple"), Utility
                                    .getSelectorOrgs(this, vars, strOrg),
                            strOrderBy, "", "");
                    strNumRows = String.valueOf(data.length);
                    vars.setSessionValue("BusinessPartnerInfo.numrows",
                            strNumRows);
                } else {
                    strNumRows = vars
                            .getSessionValue("BusinessPartnerInfo.numrows");
                }

                // Filtering result
                if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
                    String oraLimit = (Integer.valueOf(strOffset) + 1)
                            + " AND "
                            + String.valueOf(Integer.valueOf(strOffset)
                                    .intValue()
                                    + Integer.valueOf(strPageSize));
                    data = ProductMultipleData.select(this, "1", strKey,
                            strName, strProductCategory, Utility.getContext(
                                    this, vars, "#User_Client",
                                    "ProductMultiple"), Utility
                                    .getSelectorOrgs(this, vars, strOrg),
                            strOrderBy, oraLimit, "");
                } else {
                    String pgLimit = strPageSize + " OFFSET " + strOffset;
                    data = ProductMultipleData.select(this, "1", strKey,
                            strName, strProductCategory, Utility.getContext(
                                    this, vars, "#User_Client",
                                    "ProductMultiple"), Utility
                                    .getSelectorOrgs(this, vars, strOrg),
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
        return "Servlet that presents the multiple products seeker";
    } // end of getServletInfo() method
}
