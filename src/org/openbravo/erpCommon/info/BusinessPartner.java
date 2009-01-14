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
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class BusinessPartner extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VariablesSecureApp vars = new VariablesSecureApp(request);

        if (vars.commandIn("DEFAULT")) {

            clearSessionValue(vars);

            String strWindowId = vars.getStringParameter("WindowID");
            String strNameValue = vars.getRequestGlobalVariable("inpNameValue",
                    "BusinessPartner.name");
            String strIDValue = vars.getStringParameter("inpIDValue");
            String strKeyValue = vars.getGlobalVariable("inpKey",
                    "BusinessPartner.key", "");
            if (!strIDValue.equals("")) {
                String strNameAux = BusinessPartnerData.existsActual(this,
                        strNameValue, strIDValue);
                if (!strNameAux.equals(""))
                    strNameValue = strNameAux;
            }

            vars.removeSessionValue("BusinessPartner.key");

            String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
            String strBpartner = strIsSOTrxTab;
            if (strIsSOTrxTab.equals(""))
                strBpartner = Utility.getContext(this, vars, "isSOTrx",
                        strWindowId);
            String strSelected = "all";
            if (strBpartner.equals("Y"))
                strSelected = "customer";
            else if (strBpartner.equals("N"))
                strSelected = "vendor";
            else
                strSelected = "all";
            vars.setSessionValue("BusinessPartner.bpartner", strSelected);
            if (!strNameValue.equals(""))
                vars
                        .setSessionValue("BusinessPartner.name", strNameValue
                                + "%");
            printPage(response, vars, strKeyValue, strNameValue.concat("%"),
                    strSelected, "paramName");
        } else if (vars.commandIn("KEY")) {
            String strWindowId = vars.getStringParameter("WindowID");
            String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
            String strKeyValue = vars.getRequestGlobalVariable("inpNameValue",
                    "BusinessPartner.key");
            String strIDValue = vars.getStringParameter("inpIDValue");
            String strOrg = vars.getStringParameter("inpAD_Org_ID");
            if (!strIDValue.equals("")) {
                String strNameAux = BusinessPartnerData.existsActualValue(this,
                        strKeyValue, strIDValue);
                if (!strNameAux.equals(""))
                    strKeyValue = strNameAux;
            }
            vars.removeSessionValue("BusinessPartner.name");
            if (!strKeyValue.equals(""))
                vars.setSessionValue("BusinessPartner.key", strKeyValue + "%");
            String strBpartner = strIsSOTrxTab;
            if (strIsSOTrxTab.equals(""))
                strBpartner = Utility.getContext(this, vars, "isSOTrx",
                        strWindowId);
            String strSelected = "all";
            if (strBpartner.equals("Y"))
                strSelected = "customer";
            else if (strBpartner.equals("N"))
                strSelected = "vendor";
            else
                strSelected = "all";
            vars.setSessionValue("BusinessPartner.bpartner", strSelected);
            BusinessPartnerData[] data = BusinessPartnerData.selectKey(this,
                    Utility.getContext(this, vars, "#User_Client",
                            "BusinessPartner"), Utility.getSelectorOrgs(this,
                            vars, strOrg),
                    (strSelected.equals("customer") ? "clients" : ""),
                    (strSelected.equals("vendor") ? "vendors" : ""),
                    strKeyValue + "%");
            if (data != null && data.length == 1) {
                printPageKey(response, vars, data);
            } else
                printPage(response, vars, strKeyValue + "%", "", strSelected,
                        "paramKey");
        } else if (vars.commandIn("STRUCTURE")) {
            printGridStructure(response, vars);
        } else if (vars.commandIn("DATA")) {
            if (vars.getStringParameter("clear").equals("true")) {
                clearSessionValue(vars);
            }
            String strKey = vars.getGlobalVariable("inpKey",
                    "BusinessPartner.key", "");
            String strName = vars.getGlobalVariable("inpName",
                    "BusinessPartner.name", "");
            String strOrg = vars.getGlobalVariable("inpAD_Org_ID",
                    "BusinessPartner.adorgid", "");
            String strContact = vars.getGlobalVariable("inpContact",
                    "BusinessPartner.contact", "");
            String strZIP = vars.getGlobalVariable("inpZIP",
                    "BusinessPartner.zip", "");
            String strProvincia = vars.getGlobalVariable("inpProvincia",
                    "BusinessPartner.provincia", "");
            String strBpartners = vars.getGlobalVariable("inpBpartner",
                    "BusinessPartner.bpartner", "all"); // all
            String strCity = vars.getGlobalVariable("inpCity",
                    "BusinessPartner.city", "");
            String strNewFilter = vars.getStringParameter("newFilter");
            String strOffset = vars.getStringParameter("offset");
            String strPageSize = vars.getStringParameter("page_size");
            String strSortCols = vars.getStringParameter("sort_cols")
                    .toUpperCase();
            String strSortDirs = vars.getStringParameter("sort_dirs")
                    .toUpperCase();
            printGridData(response, vars, strKey, strName, strOrg, strContact,
                    strZIP, strProvincia, strBpartners, strCity, strSortCols
                            + " " + strSortDirs, strOffset, strPageSize,
                    strNewFilter);
        } else
            pageError(response);
    }

    void printPage(HttpServletResponse response, VariablesSecureApp vars,
            String strKeyValue, String strNameValue, String strBpartners,
            String focusedId) throws IOException, ServletException {

        if (log4j.isDebugEnabled())
            log4j.debug("Output: Frame 1 of business partners seeker");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/info/BusinessPartner")
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
        xmlDocument.setParameter("clients", strBpartners);
        xmlDocument.setParameter("vendors", strBpartners);
        xmlDocument.setParameter("all", strBpartners);
        xmlDocument.setParameter("orgs", vars
                .getStringParameter("inpAD_Org_ID"));

        xmlDocument.setParameter("grid", "20");
        xmlDocument.setParameter("grid_Offset", "");
        xmlDocument.setParameter("grid_SortCols", "1");
        xmlDocument.setParameter("grid_SortDirs", "ASC");
        xmlDocument.setParameter("grid_Default", "0");

        xmlDocument.setParameter("jsFocusOnField", Utility
                .focusFieldJS(focusedId));

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
            BusinessPartnerData[] data) throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: business partners seeker Frame Set");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/info/SearchUniqueKeyResponse")
                .createXmlDocument();

        xmlDocument.setParameter("script", generateResult(data));
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    String generateResult(BusinessPartnerData[] data) throws IOException,
            ServletException {
        StringBuffer html = new StringBuffer();

        html.append("\nfunction validateSelector() {\n");
        html.append("var key = \"" + data[0].cBpartnerId + "\";\n");
        html.append("var text = \""
                + Replace.replace(data[0].name, "\"", "\\\"") + "\";\n");
        html.append("var parameter = new Array(\n");
        html.append("new SearchElements(\"_LOC\", true, \""
                + data[0].cBpartnerLocationId + "\"),\n");
        html.append("new SearchElements(\"_CON\", true, \""
                + data[0].cBpartnerContactId + "\")\n");
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
        String[] colNames = { "value", "name", "so_creditavailable",
                "so_creditused", "contact", "phone", "pc", "city", "income",
                "c_bpartner_id", "c_bpartner_contact_id",
                "c_bpartner_location_id", "rowkey" };
        boolean[] colSortable = { true, true, true, true, false, false, false,
                false, false, false, false, false, false };
        String[] colWidths = { "98", "172", "50", "83", "104", "63", "43",
                "100", "63", "0", "0", "0", "0" };
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
            String name = Utility.messageBD(this, "BPS_"
                    + colNames[i].toUpperCase(), vars.getLanguage());
            dataAux.setData("name", (name.startsWith("BPS_") ? colNames[i]
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
            String strKey, String strName, String strOrg, String strContact,
            String strZIP, String strProvincia, String strBpartners,
            String strCity, String strOrderBy, String strOffset,
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
                    strNumRows = BusinessPartnerData.countRows(this, Utility
                            .getContext(this, vars, "#User_Client",
                                    "BusinessPartner"), Utility
                            .getSelectorOrgs(this, vars, strOrg), (strKey
                            .equals("%") ? "" : strKey),
                            (strName.equals("%") ? "" : strName), (strContact
                                    .equals("%") ? "" : strContact), (strZIP
                                    .equals("%") ? "" : strZIP), (strProvincia
                                    .equals("%") ? "" : strProvincia),
                            (strBpartners.equals("customer") ? "clients" : ""),
                            (strBpartners.equals("vendor") ? "vendors" : ""),
                            (strCity.equals("%") ? "" : strCity));
                    vars.setSessionValue("BusinessPartnerInfo.numrows",
                            strNumRows);
                } else {
                    strNumRows = vars
                            .getSessionValue("BusinessPartnerInfo.numrows");
                }

                // Filtering result
                if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
                    String oraLimit = strOffset
                            + " AND "
                            + String.valueOf(Integer.valueOf(strOffset)
                                    .intValue()
                                    + Integer.valueOf(strPageSize));
                    data = BusinessPartnerData.select(this, "ROWNUM", Utility
                            .getContext(this, vars, "#User_Client",
                                    "BusinessPartner"), Utility
                            .getSelectorOrgs(this, vars, strOrg), (strKey
                            .equals("%") ? "" : strKey),
                            (strName.equals("%") ? "" : strName), (strBpartners
                                    .equals("customer") ? "clients" : ""),
                            (strBpartners.equals("vendor") ? "vendors" : ""),
                            strOrderBy, oraLimit, "", (strZIP.equals("%") ? ""
                                    : strZIP), (strCity.equals("%") ? ""
                                    : strCity), (strProvincia.equals("%") ? ""
                                    : strProvincia),
                            (strContact.equals("%") ? "" : strContact));
                } else {
                    String pgLimit = strPageSize + " OFFSET " + strOffset;
                    data = BusinessPartnerData.select(this, "1", Utility
                            .getContext(this, vars, "#User_Client",
                                    "BusinessPartner"), Utility
                            .getSelectorOrgs(this, vars, strOrg), (strKey
                            .equals("%") ? "" : strKey),
                            (strName.equals("%") ? "" : strName), (strBpartners
                                    .equals("customer") ? "clients" : ""),
                            (strBpartners.equals("vendor") ? "vendors" : ""),
                            strOrderBy, "", pgLimit, (strZIP.equals("%") ? ""
                                    : strZIP), (strCity.equals("%") ? ""
                                    : strCity), (strProvincia.equals("%") ? ""
                                    : strProvincia),
                            (strContact.equals("%") ? "" : strContact));
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

    private void clearSessionValue(VariablesSecureApp vars) {
        vars.removeSessionValue("BusinessPartner.key");
        vars.removeSessionValue("BusinessPartner.name");
        vars.removeSessionValue("BusinessPartner.adorgid");
        vars.removeSessionValue("BusinessPartner.contact");
        vars.removeSessionValue("BusinessPartner.zip");
        vars.removeSessionValue("BusinessPartner.provincia");
        vars.removeSessionValue("BusinessPartner.bpartner");
        vars.removeSessionValue("BusinessPartner.city");
    }

    public String getServletInfo() {
        return "Servlet that presents the business partners seeker";
    } // end of getServletInfo() method
}
