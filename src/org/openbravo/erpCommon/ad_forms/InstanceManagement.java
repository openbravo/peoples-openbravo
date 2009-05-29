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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.ops.ActivationKey;
import org.openbravo.erpCommon.ops.ActiveInstanceProcess;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.xmlEngine.XmlDocument;

public class InstanceManagement extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      ActivationKey activationKey = new ActivationKey();
      if (!activationKey.isInstanceActive())
        printPageNotActive(response, vars);
      else
        printPageActive(response, vars, activationKey);
    } else if (vars.commandIn("ACTIVATE")) {
      if (activateRemote(vars)) {
        ActivationKey activationKey = new ActivationKey();
        if (!activationKey.isInstanceActive())
          printPageNotActive(response, vars);
        else
          printPageActive(response, vars, activationKey);
      } else
        printPageNotActive(response, vars);
    } else
      pageError(response);

  }

  private void printPageActive(HttpServletResponse response, VariablesSecureApp vars,
      ActivationKey activationKey) throws IOException, ServletException {
    // TODO Auto-generated method stub

  }

  private void printPageNotActive(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/InstanceManagementNotActive").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InstanceManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InstanceManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    // Message
    {
      final OBError myMessage = vars.getMessage("InstanceManagement");
      vars.removeMessage("InstanceManagement");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    // Purpose combo
    try {
      ComboTableData comboTableData = new ComboTableData(this, "LIST", "", "InstancePurpose", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "InstanceManagement"), Utility
              .getContext(this, vars, "#User_Client", "InstanceManagement"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "InstanceManagement", null);
      xmlDocument.setData("reportPurpose", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ServletException(ex);
    }

    out.println(xmlDocument.print());
    out.close();
  }

  private boolean activateRemote(VariablesSecureApp vars) {
    boolean result = false;
    ProcessBundle pb = new ProcessBundle(null, vars);

    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("publicKey", vars.getStringParameter("publicKey"));
    params.put("purpose", vars.getStringParameter("purpose"));
    pb.setParams(params);

    OBError msg = null;
    try {
      new ActiveInstanceProcess().execute(pb);
      msg = (OBError) pb.getResult();
      result = msg.getType().equals("Success");
    } catch (Exception e) {
      log4j.error(e);
      msg.setType("Error");
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), e.getMessage()));
      e.printStackTrace();
      result = false;
    }

    msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), msg.getMessage()));
    vars.setMessage("InstanceManagement", msg);
    return result;

  }
}
