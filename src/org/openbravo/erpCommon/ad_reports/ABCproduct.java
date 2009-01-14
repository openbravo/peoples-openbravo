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

public class ABCproduct extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VariablesSecureApp vars = new VariablesSecureApp(request);

        if (vars.commandIn("DEFAULT")) {
            String strFrom = vars.getGlobalVariable("inpFrom",
                    "ABCproduct|From", "");
            String strTo = vars.getGlobalVariable("inpTo", "ABCproduct|To", "");
            String strCategory = vars.getGlobalVariable("inpCategory",
                    "ABCproduct|Category", "");
            printPageDataSheet(response, vars, strFrom, strTo, strCategory);
        } else if (vars.commandIn("FIND")) {
            String strFrom = vars.getRequestGlobalVariable("inpFrom",
                    "ABCproduct|From");
            String strTo = vars.getRequestGlobalVariable("inpTo",
                    "ABCproduct|To");
            String strCategory = vars.getGlobalVariable("inpCategory",
                    "ABCproduct|Category", "");
            printPageDataSheet(response, vars, strFrom, strTo, strCategory);
        } else
            pageError(response);
    }

    void printPageDataSheet(HttpServletResponse response,
            VariablesSecureApp vars, String strFrom, String strTo,
            String strCategory) throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: dataSheet");
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        XmlDocument xmlDocument = null;
        ABCproductData[] data = null;
        if (strFrom.equals("") || strTo.equals("")) {
            String discard[] = { "selEliminar" };
            xmlDocument = xmlEngine.readXmlTemplate(
                    "org/openbravo/erpCommon/ad_reports/ABCproduct", discard)
                    .createXmlDocument();
            data = ABCproductData.set("0", "0");
        } else {
            xmlDocument = xmlEngine.readXmlTemplate(
                    "org/openbravo/erpCommon/ad_reports/ABCproduct")
                    .createXmlDocument();
            data = ABCproductData.select(this, strFrom, strTo, strCategory);
        }

        ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ABCproduct",
                false, "", "", "", false, "ad_reports", strReplaceWith, false,
                true);
        toolbar.prepareSimpleToolBarTemplate();
        xmlDocument.setParameter("toolbar", toolbar.toString());
        try {
            WindowTabs tabs = new WindowTabs(this, vars,
                    "org.openbravo.erpCommon.ad_reports.ABCbproduct");
            xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
            xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
            xmlDocument.setParameter("childTabContainer", tabs.childTabs());
            xmlDocument.setParameter("theme", vars.getTheme());
            NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
                    "ABCproduct.html", classInfo.id, classInfo.type,
                    strReplaceWith, tabs.breadcrumb());
            xmlDocument.setParameter("navigationBar", nav.toString());
            LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
                    "ABCproduct.html", strReplaceWith);
            xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        {
            OBError myMessage = vars.getMessage("ABCproduct");
            vars.removeMessage("ABCproduct");
            if (myMessage != null) {
                xmlDocument.setParameter("messageType", myMessage.getType());
                xmlDocument.setParameter("messageTitle", myMessage.getTitle());
                xmlDocument.setParameter("messageMessage", myMessage
                        .getMessage());
            }
        }

        xmlDocument
                .setParameter("calendar", vars.getLanguage().substring(0, 2));
        xmlDocument.setParameter("directory", "var baseDirectory = \""
                + strReplaceWith + "/\";\n");
        xmlDocument.setParameter("paramLanguage", "defaultLang=\""
                + vars.getLanguage() + "\";");
        xmlDocument.setParameter("from", strFrom);
        xmlDocument.setParameter("to", strTo);
        xmlDocument.setParameter("category", strCategory);

        try {
            ComboTableData comboTableData = new ComboTableData(vars, this,
                    "TABLEDIR", "M_Product_Category_ID", "", "", Utility
                            .getContext(this, vars, "#User_Org", "ABCproduct"),
                    Utility
                            .getContext(this, vars, "#User_Client",
                                    "ABCproduct"), 0);
            Utility.fillSQLParameters(this, vars, null, comboTableData,
                    "ABCproduct", "");
            xmlDocument.setData("reportCategory", "liststructure",
                    comboTableData.select(false));
            comboTableData = null;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        xmlDocument.setData("structure", data);
        out.println(xmlDocument.print());
        out.close();
    }

    public String getServletInfo() {
        return "Servlet ABCproduct. This Servlet was made by Eduardo Argal";
    } // end of getServletInfo() method
}
