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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActiveInstanceProcess;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.System;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.xmlEngine.XmlDocument;

public class InstanceManagement extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPageActive(response, vars, ActivationKey.getInstance());
    } else if (vars.commandIn("SHOW_ACTIVATE")) {
      printPageNotActive(response, vars);
    } else if (vars.commandIn("ACTIVATE")) {
      activateRemote(vars);
      printPageClosePopUp(response, vars);
    } else if (vars.commandIn("SHOW_ACTIVATE_LOCAL")) {
      printPageActivateLocal(response, vars);
    } else if (vars.commandIn("INSTALLFILE")) {
      printPageInstallFile(response, vars);
    } else if (vars.commandIn("SHOW_DEACTIVATE")) {
      printPageDeactivate(response, vars);
    } else if (vars.commandIn("DEACTIVATE")) {
      printPageDeactivateProcess(response, vars);
    } else {
      pageError(response);
    }

  }

  private void printPageDeactivate(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/InstanceManagementDeactivate").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  private void printPageDeactivateProcess(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    OBError msg = new OBError();
    try {
      System sys = OBDal.getInstance().get(System.class, "0");
      sys.setActivationKey(null);
      sys.setInstanceKey(null);
      msg.setType("Success");
      msg.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      log4j.error(e);
      msg.setType("Error");
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), e.getMessage()));
    }
    vars.setMessage("InstanceManagement", msg);
    printPageClosePopUp(response, vars, "");

  }

  private void printPageInstallFile(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    FileItem fi = vars.getMultiFile("inpFile");
    OBError msg = new OBError();
    try {
      InputStream is = fi.getInputStream();

      // read the file in a String
      StringBuffer buf = new StringBuffer();
      byte[] b = new byte[1024];
      for (int n; (n = is.read(b)) != -1;) {
        buf.append(new String(b, 0, n));
      }

      System sys = OBDal.getInstance().get(System.class, "0");
      sys.setActivationKey(buf.toString());
      sys.setInstanceKey(vars.getStringParameter("publicKey"));
      msg.setType("Success");
      msg.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));

    } catch (Exception e) {
      log4j.error(e);
      msg.setType("Error");
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), e.getMessage()));
    }
    vars.setMessage("InstanceManagement", msg);
    printPageClosePopUp(response, vars, "");

  }

  private void printPageActivateLocal(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException {

    ActivationKey ak = ActivationKey.getInstance();
    String discard[] = { "", "" };

    if (ak.isOPSInstance()) {
      if (ak.hasExpired()) {
        // Renew
        discard[0] = "OPSActivate";
        discard[1] = "OPSRefresh";
      } else {
        // Refresh
        discard[0] = "OPSActivate";
        discard[1] = "OPSRefresh";
      }
    } else {
      // Activate
      discard[0] = "OPSRefresh";
      discard[1] = "OPSRenew";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/InstanceManagementActivateLocal", discard)
        .createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    if (ak.hasActivationKey()) {
      xmlDocument.setParameter("publicKey", ak.getPublicKey());
    }
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  private void printPageActive(HttpServletResponse response, VariablesSecureApp vars,
      ActivationKey activationKey) throws IOException, ServletException {
    response.setContentType("text/html; charset=UTF-8");
    String discard[] = { "", "", "", "", "", "" };
    if (activationKey.isOPSInstance()) {
      discard[0] = "CEInstance";
      if (activationKey.isSubscriptionConverted()) {
        discard[1] = "OPSActiveTitle";
        discard[2] = "OPSExpired";
        discard[3] = "OPSNoActiveYet";
      } else if (activationKey.hasExpired()) {
        discard[1] = "OPSActiveTitle";
        discard[2] = "OPSNoActiveYet";
        discard[3] = "OPSConverted";
        discard[4] = "OPSActive";
      } else if (activationKey.isNotActiveYet()) {
        discard[1] = "OPSExpired";
        discard[2] = "OPSActiveTitle";
        discard[3] = "OPSConverted";
        discard[4] = "OPSActive";
      } else {
        discard[1] = "OPSExpired";
        if (!activationKey.hasExpirationDate()) {
          discard[2] = "OPSExpirationTime";
        }
        discard[3] = "OPSConverted";
        discard[4] = "OPSNoActiveYet";
      }
    } else {
      discard[0] = "OPSInstance";
      discard[1] = "OPSActiveTitle";
      discard[2] = "OPSExpired";
      discard[3] = "OPSConverted";
      discard[4] = "OPSNoActiveYet";
      discard[5] = "OPSActive";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/InstanceManagement", discard).createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    // Interface parameters
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InstanceManagement", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InstanceManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "InstanceManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs
              .breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InstanceManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    // Message
    {
      OBError myMessage = null;
      if (activationKey.isActive() || activationKey.getErrorMessage() == null
          || activationKey.getErrorMessage().equals("")) {
        myMessage = vars.getMessage("InstanceManagement");
      } else {
        myMessage = new OBError();
        myMessage.setType(activationKey.getMessageType());
        myMessage.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), activationKey
            .getErrorMessage()));
      }

      vars.removeMessage("InstanceManagement");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    if (!activationKey.isOPSInstance())
      xmlDocument.setParameter("instanceInfo", Utility.messageBD(this, "OPSCommunityInstance",
          vars.getLanguage()).replace("\\n", "\n"));
    else
      xmlDocument.setParameter("instanceInfo", activationKey.toString(this, vars.getLanguage()));

    if (activationKey.hasExpirationDate()) {
      if (activationKey.getPendingDays() != null)
        xmlDocument.setParameter("OPSdaysLeft", activationKey.getPendingDays().toString());
      else
        xmlDocument.setParameter("OPSdaysLeft", Utility.messageBD(this, "OPSUnlimitedUsers",
            vars.getLanguage()).replace("\\n", "\n"));
    }

    String cacheMsg = Utility.messageBD(this, "OUTDATED_FILES_CACHED", vars.getLanguage()).replace(
        "\\n", "\n");
    cacheMsg = "var cacheMsg = \"" + cacheMsg + "\"";
    xmlDocument.setParameter("cacheMsg", cacheMsg);

    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  private void printPageNotActive(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    ActivationKey activationKey = ActivationKey.getInstance();
    response.setContentType("text/html; charset=UTF-8");
    String discard[] = { "", "" };
    if (activationKey.isOPSInstance()) {
      if (activationKey.hasExpired()) {
        // Renew
        discard[0] = "OPSActivate";
        discard[1] = "OPSRefresh";
      } else {
        // Refresh
        discard[0] = "OPSActivate";
        discard[1] = "OPSRefresh";
      }
    } else {
      // Activate
      discard[0] = "OPSRefresh";
      discard[1] = "OPSRenew";
    }

    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/InstanceManagementActivateOnline", discard)
        .createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InstanceManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InstanceManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "InstanceManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs
              .breadcrumb());
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
      Utility.fillSQLParameters(this, vars, null, comboTableData, "InstanceManagement",
          activationKey.isOPSInstance() ? activationKey.getProperty("purpose") : null);
      xmlDocument.setData("reportPurpose", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ServletException(ex);
    }

    if (activationKey.hasActivationKey()) {
      xmlDocument.setParameter("publicKey", activationKey.getPublicKey());
    }

    if (activationKey.isOPSInstance()) {
      xmlDocument.setParameter("paramSelPurpose", activationKey.getProperty("purpose"));
      xmlDocument.setParameter("instanceNo", activationKey.getProperty("instanceno"));
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
    params.put("instanceNo", vars.getStringParameter("instanceNo"));
    pb.setParams(params);

    OBError msg = new OBError();
    try {
      new ActiveInstanceProcess().execute(pb);
      msg = (OBError) pb.getResult();
      result = msg.getType().equals("Success");
    } catch (Exception e) {
      log4j.error("Error Activating instance", e);
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
