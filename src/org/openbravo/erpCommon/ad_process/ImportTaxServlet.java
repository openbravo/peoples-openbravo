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
 * All portions are Copyright (C) 2001-2008 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import org.openbravo.erpCommon.ad_actionButton.*;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ImportTaxServlet extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VariablesSecureApp vars = new VariablesSecureApp(request);

        String process = ImportData.processId(this, "ImportTaxes");
        if (vars.commandIn("DEFAULT")) {
            String strTabId = vars.getGlobalVariable("inpTabId",
                    "ImportTaxServlet|tabId");
            String strWindowId = vars.getGlobalVariable("inpwindowId",
                    "ImportTaxServlet|windowId");
            // String strKey = vars.getGlobalVariable("inpKey",
            // "ImportTaxServlet|key");
            String strKey = "00";
            String strDeleteOld = vars.getStringParameter("inpDeleteOld", "N");
            printPage(response, vars, process, strWindowId, strTabId, strKey,
                    strDeleteOld);
        } else if (vars.commandIn("SAVE")) {
            String strDeleteOld = vars.getStringParameter("inpDeleteOld", "N");
            String strRecord = vars.getGlobalVariable("inpKey",
                    "ImportTaxServlet|key");
            String strTabId = vars.getRequestGlobalVariable("inpTabId",
                    "ImportTaxServlet|tabId");
            String strWindowId = vars.getRequestGlobalVariable("inpwindowId",
                    "ImportTaxServlet|windowId");

            ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(
                    this, strTabId);
            String strWindowPath = "";
            String strTabName = "";
            if (tab != null && tab.length != 0) {
                strTabName = FormatUtilities.replace(tab[0].name);
                if (tab[0].help.equals("Y"))
                    strWindowPath = "../utility/WindowTree_FS.html?inpTabId="
                            + strTabId;
                else
                    strWindowPath = "../"
                            + FormatUtilities.replace(tab[0].description) + "/"
                            + strTabName + "_Relation.html";
            } else
                strWindowPath = strDefaultServlet;

            ImportTax tax = new ImportTax(this, process, strRecord,
                    strDeleteOld.equals("Y"));
            tax.startProcess(vars);
            // String strMessage = tax.getLog();
            // if (!strMessage.equals("")) vars.setSessionValue(strWindowId +
            // "|" + strTabName + ".message", strMessage);
            OBError myError = tax.getError();
            vars.setMessage(strTabId, myError);
            printPageClosePopUp(response, vars, strWindowPath);
        } else
            pageErrorPopUp(response);
    }

    void printPage(HttpServletResponse response, VariablesSecureApp vars,
            String strProcessId, String strWindowId, String strTabId,
            String strRecordId, String strDeleteOld) throws IOException,
            ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: process ImportTaxServlet");
        ActionButtonDefaultData[] data = null;
        String strHelp = "", strDescription = "";
        if (vars.getLanguage().equals("en_US"))
            data = ActionButtonDefaultData.select(this, strProcessId);
        else
            data = ActionButtonDefaultData.selectLanguage(this, vars
                    .getLanguage(), strProcessId);
        if (data != null && data.length != 0) {
            strDescription = data[0].description;
            strHelp = data[0].help;
        }
        String[] discard = { "" };
        if (strHelp.equals(""))
            discard[0] = new String("helpDiscard");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/ad_process/ImportTaxServlet")
                .createXmlDocument();
        xmlDocument.setParameter("language", "defaultLang=\""
                + vars.getLanguage() + "\";");
        xmlDocument.setParameter("directory", "var baseDirectory = \""
                + strReplaceWith + "/\";\n");
        xmlDocument.setParameter("theme", vars.getTheme());
        xmlDocument.setParameter("question", Utility.messageBD(this,
                "StartProcess?", vars.getLanguage()));
        xmlDocument.setParameter("description", strDescription);
        xmlDocument.setParameter("help", strHelp);
        xmlDocument.setParameter("windowId", strWindowId);
        xmlDocument.setParameter("tabId", strTabId);
        xmlDocument.setParameter("recordId", strRecordId);
        xmlDocument.setParameter("deleteOld", strDeleteOld);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    public String getServletInfo() {
        return "Servlet ImportTaxServlet";
    } // end of getServletInfo() method
}
