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
package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ImportBudgetServlet extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String process = ImportData.processId(this, "ImportBudget");
    if (vars.commandIn("DEFAULT")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "ImportBudgetServlet|tabId");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "ImportBudgetServlet|windowId");
      // String strKey = vars.getGlobalVariable("inpKey",
      // "ImportBudgetServlet|key");
      String strKey = "00";
      String strDeleteOld = vars.getStringParameter("inpDeleteOld", "Y");
      printPage(response, vars, process, strWindowId, strTabId, strKey, strDeleteOld);
    } else if (vars.commandIn("SAVE")) {
      String strDeleteOld = vars.getStringParameter("inpDeleteOld", "Y");
      String strRecord = vars.getGlobalVariable("inpKey", "ImportBudgetServlet|key");
      String strTabId = vars.getRequestGlobalVariable("inpTabId", "ImportBudgetServlet|tabId");
      String strBudget = vars.getRequestGlobalVariable("inpBudgetId",
          "ImportBudgetServlet|inpBudgetId");

      String strWindowPath = Utility.getTabURL(this, strTabId, "R");
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      ImportBudget b = new ImportBudget(this, process, strRecord, strBudget, strDeleteOld
          .equals("Y"));
      b.startProcess(vars);
      // String strMessage = b.getLog();
      // if (!strMessage.equals("")) vars.setSessionValue(strWindowId +
      // "|" + strTabName + ".message", strMessage);
      OBError myError = b.getError();
      vars.setMessage(strTabId, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strProcessId, String strWindowId, String strTabId, String strRecordId,
      String strDeleteOld) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process ImportBudgetServlet");
    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/ImportBudgetServlet").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("question", Utility.messageBD(this, "StartProcess?", vars
        .getLanguage()));
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("recordId", strRecordId);
    xmlDocument.setParameter("deleteOld", strDeleteOld);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Budget_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), Utility
              .getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      xmlDocument.setData("reportC_BUDGET", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ImportBudgetServlet";
  } // end of getServletInfo() method
}
