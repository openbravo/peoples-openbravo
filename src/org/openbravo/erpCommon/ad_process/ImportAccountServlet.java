/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SL
 * Contributions are Copyright (C) 2001-2009 Openbravo S.L.
 ******************************************************************************
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

public class ImportAccountServlet extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String process = ImportData.processId(this, "ImportAccount");
    String strTabId = vars.getGlobalVariable("inpTabId", "ImportAccountServlet|tabId");
    String strWindowId = vars.getGlobalVariable("inpwindowId", "ImportAccountServlet|windowId");
    String strDeleteOld = vars.getStringParameter("inpDeleteOld", "Y");
    String strCElementId = vars.getStringParameter("inpElementId", "");
    String strUpdateDefault = vars.getStringParameter("inpUpdateDefault", "Y");
    String strCreateNewCombination = vars.getStringParameter("inpCreateNewCombination", "Y");
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars, process, strWindowId, strTabId, strDeleteOld, strCElementId,
          strUpdateDefault, strCreateNewCombination);
    } else if (vars.commandIn("SAVE")) {
      strDeleteOld = vars.getStringParameter("inpDeleteOld", "N");
      strUpdateDefault = vars.getStringParameter("inpUpdateDefault", "N");
      strCreateNewCombination = vars.getStringParameter("inpCreateNewCombination", "N");

      String strWindowPath = Utility.getTabURL(this, strTabId, "R");
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      ImportAccount acc = new ImportAccount(this, process, strDeleteOld.equals("Y"), strCElementId,
          strUpdateDefault.equals("Y"), strCreateNewCombination.equals("Y"));
      acc.startProcess(vars);
      // String strMessage = acc.getLog();
      // if (!strMessage.equals("")) vars.setSessionValue(strWindowId +
      // "|" + strTabName + ".message", strMessage);
      OBError myError = acc.getError();
      vars.setMessage(strTabId, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strProcessId, String strWindowId, String strTabId, String strDeleteOld,
      String strCElementId, String strUpdateDefault, String strCreateNewCombination)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process ImportAccountServlet");
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
        "org/openbravo/erpCommon/ad_process/ImportAccountServlet").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("question", Utility.messageBD(this, "StartProcess?", vars
        .getLanguage()));
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("deleteOld", strDeleteOld);
    xmlDocument.setParameter("updateDefault", strUpdateDefault);
    xmlDocument.setParameter("createNewCombination", strCreateNewCombination);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Element_ID",
          "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), Utility
              .getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      xmlDocument.setData("reportC_ELEMENT", "liststructure", comboTableData.select(false));
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
    return "Servlet ImportAccountServlet";
  } // end of getServletInfo() method
}
