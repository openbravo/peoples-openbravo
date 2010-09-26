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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.modules.ImportModule;
import org.openbravo.erpCommon.modules.ModuleTree;
import org.openbravo.erpCommon.modules.ModuleUtiltiy;
import org.openbravo.erpCommon.modules.UninstallModule;
import org.openbravo.erpCommon.modules.VersionUtility;
import org.openbravo.erpCommon.modules.VersionUtility.VersionComparator;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.DisabledModules;
import org.openbravo.erpCommon.obps.ActivationKey.CommercialModuleStatus;
import org.openbravo.erpCommon.obps.ActivationKey.LicenseClass;
import org.openbravo.erpCommon.obps.ActivationKey.SubscriptionStatus;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.services.webservice.Module;
import org.openbravo.services.webservice.ModuleDependency;
import org.openbravo.services.webservice.SimpleModule;
import org.openbravo.services.webservice.WebService3Impl;
import org.openbravo.services.webservice.WebService3ImplServiceLocator;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * This servlet is in charge of showing the Module Manager Console which have three tabs: *Installed
 * modules *Add Modules *Installation history
 * 
 * 
 */
public class ModuleManagement extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  public static final String UPDATE_ALL_RECORD_ID = "FFF";

  /**
   * Main method that controls the sent command
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPageInstalled(response, vars);
    } else if (vars.commandIn("APPLY")) {
      printPageApply(response, vars);
    } else if (vars.commandIn("ADD")) {
      final String searchText = vars.getGlobalVariable("inpSearchText", "ModuleManagemetAdd|text",
          "");
      printPageAdd(request, response, vars, searchText, true);
    } else if (vars.commandIn("ADD_NOSEARCH")) {
      final String searchText = vars.getGlobalVariable("inpSearchText", "ModuleManagemetAdd|text",
          "");
      printPageAdd(request, response, vars, searchText, false);
    } else if (vars.commandIn("ADD_SEARCH")) {
      final String searchText = vars.getRequestGlobalVariable("inpSearchText",
          "ModuleManagemetAdd|text");
      printPageAdd(request, response, vars, searchText, true);
    } else if (vars.commandIn("HISTORY")) {
      final String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ModuleManagement|DateFrom",
          "");
      final String strDateTo = vars.getGlobalVariable("inpDateTo", "ModuleManagement|DateTo", "");
      final String strUser = vars.getGlobalVariable("inpUser", "ModuleManagement|inpUser", "");
      printPageHistory(response, vars, strDateFrom, strDateTo, strUser);
    } else if (vars.commandIn("HISTORY_SEARCH")) {
      final String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ModuleManagement|DateFrom");
      final String strDateTo = vars
          .getRequestGlobalVariable("inpDateTo", "ModuleManagement|DateTo");
      final String strUser = vars.getRequestGlobalVariable("inpUser", "ModuleManagement|inpUser");
      printPageHistory(response, vars, strDateFrom, strDateTo, strUser);
    } else if (vars.commandIn("DETAIL")) {
      final String record = vars.getStringParameter("inpcRecordId");
      final boolean local = vars.getStringParameter("inpLocalInstall").equals("Y");
      printPageDetail(response, vars, record, local);
    } else if (vars.commandIn("INSTALL")) {
      final String record = vars.getStringParameter("inpcRecordId");

      printPageInstall1(response, request, vars, record, false, null, new String[0], ModuleUtiltiy
          .getSystemMaturityLevels(true));
    } else if (vars.commandIn("INSTALL2")) {
      printPageInstall2(response, vars);
    } else if (vars.commandIn("INSTALL3")) {
      printPageInstall3(response, vars);
    } else if (vars.commandIn("LICENSE")) {
      final String record = vars.getStringParameter("inpcRecordId");
      printLicenseAgreement(response, vars, record);
    } else if (vars.commandIn("LOCAL")) {
      printSearchFile(response, vars, null);
    } else if (vars.commandIn("INSTALLFILE")) {
      printPageInstallFile(response, request, vars);

    } else if (vars.commandIn("UNINSTALL")) {
      final String modules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);
      final UninstallModule um = new UninstallModule(this, vars.getSessionValue("#sourcePath"),
          vars);
      um.execute(modules);
      final OBError msg = um.getOBError();
      vars.setMessage("ModuleManagement|message", msg);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
      log4j.info(modules);
    } else if (vars.commandIn("DISABLE")) {
      disable(vars);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else if (vars.commandIn("ENABLE")) {
      enable(vars);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DEFAULT");
    } else if (vars.commandIn("SCAN")) {
      printScan(response, vars);
    } else if (vars.commandIn("UPDATE")) {
      final String updateModule = vars.getStringParameter("inpcUpdate");
      String[] modulesToUpdate;
      if (updateModule.equals("all")) {
        modulesToUpdate = getUpdateableModules();
      } else {
        modulesToUpdate = new String[1];
        modulesToUpdate[0] = updateModule;
      }

      // For update obtain just update maturity level
      printPageInstall1(response, request, vars, null, false, null, modulesToUpdate, ModuleUtiltiy
          .getSystemMaturityLevels(false));
    } else if (vars.commandIn("SETTINGS", "SETTINGS_ADD", "SETTINGS_REMOVE", "SETTINGS_SAVE")) {
      printPageSettings(response, request);
    } else {
      pageError(response);
    }
  }

  /**
   * Show the tab for installed modules, where it is possible to look for updates, uninstall and
   * apply changes-
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageInstalled(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Installed");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementInstalled").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");

    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.ModuleManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    {
      final OBError myMessage = vars.getMessage("ModuleManagement|message");
      vars.removeMessage("ModuleManagement|message");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    // //----
    final ModuleTree tree = new ModuleTree(this);
    tree.setLanguage(vars.getLanguage());
    tree.showNotifications(true);
    tree.setNotifications(getNotificationsHTML(vars.getLanguage()));

    // Obtains a tree for the installed modules
    xmlDocument.setParameter("moduleTree", tree.toHtml());

    // Obtains a box for display the modules descriptions
    xmlDocument.setParameter("moduleTreeDescription", tree.descriptionToHtml());

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Displays the pop-up to execute an ant task
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageApply(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    try {
      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/ApplyModule").createXmlDocument();
      final PrintWriter out = response.getWriter();
      response.setContentType("text/html; charset=UTF-8");
      out.println(xmlDocument.print());
      out.close();
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
    }
  }

  /**
   * Returns an HTML with the available notifications which can be: *Unapplied changes: rebuild
   * system *Available updates: install them
   * 
   * @param lang
   * @return
   */
  private String getNotificationsHTML(String lang) {
    String rt = "";
    try {
      // Check for rebuild system
      String total = ModuleManagementData.selectRebuild(this);
      if (!total.equals("0")) {
        rt = total
            + "&nbsp;"
            + Utility.messageBD(this, "ApplyModules", lang)
            + ", <a id=\"rebuildNow\" class=\"LabelLink_noicon\" href=\"#\" onclick=\"openServletNewWindow('DEFAULT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 700, 900);return false;\">"
            + Utility.messageBD(this, "RebuildNow", lang) + "</a>";
      }
      String restartTomcat = ModuleManagementData.selectRestartTomcat(this);
      // Check if last build was done but Tomcat wasn't restarted
      if (!restartTomcat.equals("0")) {
        rt = "<a class=\"LabelLink_noicon\" href=\"#\" onclick=\"openServletNewWindow('TOMCAT', false, '../ad_process/ApplyModules.html', 'BUTTON', null, true, 650, 900);return false;\">"
            + Utility.messageBD(this, "Restart_Tomcat", lang) + "</a>";
        return rt;

      }

      // Check for updates
      total = ModuleManagementData.selectUpdate(this);
      if (!total.equals("0")) {
        if (!rt.isEmpty()) {
          rt += "&nbsp;/&nbsp;";
        }
        rt += total
            + "&nbsp;"
            + Utility.messageBD(this, "UpdateAvailable", lang)
            + "&nbsp;"
            + "<a class=\"LabelLink_noicon\" href=\"#\" onclick=\"installUpdate('all'); return false;\">"
            + Utility.messageBD(this, "InstallUpdatesNow", lang) + "</a>";
      }
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
    }
    return rt;
  }

  /**
   * Displays the second tab: Add modules where it is possible to search and install modules
   * remotely or locally
   * 
   * @param request
   * @param response
   * @param vars
   * @param searchText
   * @param displaySearch
   * @throws IOException
   * @throws ServletException
   */
  private void printPageAdd(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String searchText, boolean displaySearch) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Installed");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementAdd").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.ModuleManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    {
      final OBError myMessage = vars.getMessage("ModuleManagement");
      vars.removeMessage("ModuleManagement");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    // //----

    xmlDocument.setParameter("inpSearchText", searchText);

    // In case the search results must be shown request and display them
    if (displaySearch)
      xmlDocument.setParameter("searchResults", getSearchResults(request, response, vars,
          searchText));

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Displays the third tab "Installation History" with a log of all installation actions
   * 
   * @param response
   * @param vars
   * @param strDateFrom
   * @param strDateTo
   * @param strUser
   * @throws IOException
   * @throws ServletException
   */
  private void printPageHistory(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strUser) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Installed");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementHistory").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    // Interface parameters
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.ModuleManagement");
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("inpUser", strUser);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "18", "AD_User_ID", "110", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ModuleManagement"), Utility
              .getContext(this, vars, "#User_Client", "ModuleManagement"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "ModuleManagement", strUser);
      xmlDocument.setData("reportUser", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    final ModuleManagementData data[] = ModuleManagementData.selectLog(this, vars.getLanguage(),
        strUser, strDateFrom, strDateTo);
    xmlDocument.setData("detail", data);

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Shows the detail pop-up for a module
   * 
   * @param response
   * @param vars
   * @param recordId
   * @throws IOException
   * @throws ServletException
   */
  private void printPageDetail(HttpServletResponse response, VariablesSecureApp vars,
      String recordId, boolean local) throws IOException, ServletException {
    Module module = null;
    if (!local) {
      try {
        // retrieve the module details from the webservice
        final WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
        final WebService3Impl ws = loc.getWebService3();
        module = ws.moduleDetail(recordId);
      } catch (final Exception e) {
        log4j.error(e.getMessage(), e);
        throw new ServletException(e);
      }
    } else {
      final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
      module = im.getModule(recordId);
    }

    final ModuleDependency[] dependencies = module.getDependencies();
    final ModuleDependency[] includes = module.getIncludes();

    final String discard[] = { "", "" };
    if (includes == null || includes.length == 0)
      discard[0] = "includeDiscard";
    if (dependencies == null || dependencies.length == 0)
      discard[1] = "dependDiscard";

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagementDetails", discard).createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("key", recordId);
    xmlDocument.setParameter("type", (module.getType() == null ? "M" : module.getType())
        .equals("M") ? "Module" : module.getType().equals("T") ? "Template" : "Pack");
    xmlDocument.setParameter("moduleName", module.getName());
    xmlDocument.setParameter("moduleVersion", module.getVersionNo());
    xmlDocument.setParameter("description", module.getDescription());
    xmlDocument.setParameter("help", module.getHelp());
    xmlDocument.setParameter("author", module.getAuthor());
    String url = module.getUrl();
    if (url == null || url.equals("")) {
      xmlDocument.setParameter("urlDisplay", "none");
    } else {
      xmlDocument.setParameter("urlLink", getLink(url));
      xmlDocument.setParameter("url", url);
    }
    xmlDocument.setParameter("license", module.getLicenseAgreement());

    if (dependencies != null && dependencies.length > 0) {
      xmlDocument.setData("dependencies", formatDeps4Display(dependencies, vars, this));
    }

    if (includes != null && includes.length > 0) {
      xmlDocument.setData("includes", formatDeps4Display(includes, vars, this));
    }

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String getLink(String url) {
    if (url == null || url.isEmpty()) {
      return "";
    }
    String link = url;
    if (!url.matches("^[a-z]+://.+")) {
      // url without protocol: infer http
      link = "http://" + url;
    }
    return link;
  }

  private static FieldProvider[] formatDeps4Display(ModuleDependency[] deps,
      VariablesSecureApp vars, ConnectionProvider conn) {
    @SuppressWarnings("unchecked")
    HashMap<String, String>[] res = new HashMap[deps.length];

    for (int i = 0; i < deps.length; i++) {
      res[i] = new HashMap<String, String>();
      res[i].put("moduleName", getDisplayString(deps[i], vars, conn));
    }
    return FieldProviderFactory.getFieldProviderArray(res);
  }

  private static String getDisplayString(ModuleDependency dep, VariablesSecureApp vars,
      ConnectionProvider conn) {

    final String DETAIL_MSG_DETAIL_BETWEEN = Utility.messageBD(conn, "MODULE_VERSION_BETWEEN", vars
        .getLanguage());

    final String DETAIL_MSG_OR_LATER = Utility.messageBD(conn, "MODULE_VERSION_OR_LATER", vars
        .getLanguage());

    final String VERSION = Utility.messageBD(conn, "VERSION", vars.getLanguage());

    String displayString = dep.getModuleName() + " " + VERSION + " ";

    if (dep.getVersionEnd() != null && dep.getVersionEnd().equals(dep.getVersionStart())) {
      displayString += dep.getVersionStart();
    } else if (dep.getVersionEnd() == null || dep.getVersionEnd().contains(".999999")) {
      // NOTE: dep.getVersionEnd() is .999999 from CR but null when installing from .obx
      displayString += DETAIL_MSG_OR_LATER.replace("@MODULE_VERSION@", dep.getVersionStart());
    } else {
      String tmp = DETAIL_MSG_DETAIL_BETWEEN.replace("@MIN_VERSION@", dep.getVersionStart());
      tmp = tmp.replace("@MAX_VERSION@", dep.getVersionEnd());
      displayString += tmp;
    }

    return displayString;
  }

  /**
   * A decision needs to be made when executing this method/pop-up:
   * 
   * a. The file is not a .obx file -> Display the search file pop-up again, with an error
   * indicating the file must be a .obx file.
   * 
   * b. The file is an .obx file but no need to update -> Display the same pop-up again with a
   * warning indicating the module is already the most recent version.
   * 
   * b. The .obx file is okay -> redirect to the moduleInstall1 pop-up.
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  private void printPageInstallFile(HttpServletResponse response, HttpServletRequest request,
      VariablesSecureApp vars) throws ServletException, IOException {
    final FileItem fi = vars.getMultiFile("inpFile");

    if (!fi.getName().toUpperCase().endsWith(".OBX")) {
      // We don't have a .obx file
      OBError message = new OBError();
      message.setType("Error");
      message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
      message.setMessage(Utility.messageBD(this, "MOD_OBX", vars.getLanguage()));

      printSearchFile(response, vars, message);

    } else {
      ImportModule im = new ImportModule(this, vars.getSessionValue("#sourcePath"), vars);
      try {
        if (im.isModuleUpdate(fi.getInputStream())) {
          vars.setSessionObject("ModuleManagementInstall|File", vars.getMultiFile("inpFile"));
          printPageInstall1(response, request, vars, null, true, fi.getInputStream(),
              new String[0], null);
        } else {
          OBError message = im.getOBError(this);
          printSearchFile(response, vars, message);
        }
      } catch (Exception e) {
        log4j.error(e.getMessage(), e);
        throw new ServletException(e);
      }
    }
  }

  /**
   * Shows the first pop-up for the installation process, where it is displayed the modules to
   * install/update and an error message in case it is not possible to install the selected one or a
   * warning message in case the selected version is not installable but it is possible to install
   * another one.
   * 
   * @param response
   * @param vars
   * @param recordId
   * @param islocal
   * @param obx
   * @param maturityLevels
   * @throws IOException
   * @throws ServletException
   */
  private void printPageInstall1(HttpServletResponse response, HttpServletRequest request,
      VariablesSecureApp vars, String recordId, boolean islocal, InputStream obx,
      String[] updateModules, HashMap<String, String> maturityLevels) throws IOException,
      ServletException {
    final String discard[] = { "", "", "", "", "", "", "warnMaturity" };
    Module module = null;

    // Remote installation is only allowed for heartbeat enabled instances
    if (!islocal && !HeartbeatProcess.isHeartbeatEnabled()) {
      String inpcRecordId = recordId;
      String command = "DEFAULT";

      if (updateModules != null && updateModules.length > 0 && !updateModules[0].equals("")) {
        if (updateModules.length == 1) {
          // User clicked "Install Now" from the module description
          inpcRecordId = updateModules[0];
        } else {
          inpcRecordId = UPDATE_ALL_RECORD_ID;
        }
        command = "UPDATE";
      }

      response.sendRedirect(strDireccion + "/ad_forms/Heartbeat.html?Command=" + command
          + "_MODULE&inpcRecordId=" + inpcRecordId);
      return;
    }

    if (!islocal && (updateModules == null || updateModules.length == 0)) {
      // if it is a remote installation get the module from webservice,
      // other case the obx file is passed as an InputStream
      try {
        if (HttpsUtils.isInternetAvailable()) {
          final WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
          final WebService3Impl ws = loc.getWebService3();
          module = ws.moduleDetail(recordId);
        }
      } catch (final Exception e) {
        log4j.error("Error obtaining module info", e);
      }
    } else {
      discard[4] = "core";
    }

    Module[] inst = null;
    Module[] upd = null;
    OBError message = null;
    boolean found = false;
    boolean check = false;
    // to hold (key,value) = (moduleId, minVersion)
    Map<String, String> minVersions = new HashMap<String, String>();

    VersionUtility.setPool(this);

    // Craete a new ImportModule instance which will be used to check
    // depencecies and to process the installation
    final ImportModule im = new ImportModule(this, vars.getSessionValue("#sourcePath"), vars);
    im.setInstallLocal(islocal);
    try {
      // check the dependenies and obtain the modules to install/update
      if (!islocal) {
        final String[] installableModules = { module != null ? module.getModuleVersionID() : "" };
        check = im.checkDependenciesId(installableModules, updateModules, maturityLevels);
      } else {
        check = im.checkDependenciesFile(obx);
      }

      if (check) { // dependencies are statisfied, show modules to install
        // installOrig includes also the module to install
        final Module[] installOrig = im.getModulesToInstall();

        if (installOrig == null || installOrig.length == 0)
          discard[0] = "modulesToinstall";
        else {
          if (!islocal && module != null) {
            inst = new Module[installOrig.length - 1]; // to remove
            // the module
            // itself
            // check if the version for the selected module is the
            // selected one
            int j = 0;
            for (int i = 0; i < installOrig.length; i++) {
              found = installOrig[i].getModuleID().equals(module.getModuleID());
              if (found && !module.getModuleVersionID().equals(installOrig[i].getModuleVersionID())) {

                message = new OBError();
                message.setType("Warning");
                message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
                message.setMessage(module.getName()
                    + " "
                    + module.getVersionNo()
                    + " "
                    + Utility.messageBD(this, "OtherModuleVersionToinstallOrigall", vars
                        .getLanguage()) + " " + installOrig[i].getVersionNo());
              }
              if (found) {
                module = installOrig[i];
              } else {
                inst[j] = installOrig[i];
                j++;
              }

            }
          } else {
            inst = installOrig;
          }
        }
        upd = im.getModulesToUpdate();
        // after all the checks, save the ImportModule object in session
        // to take it in next steps
        vars.setSessionObject("InstallModule|ImportModule", im);

        // calculate minimum required version of each extra module (installs & updates)
        minVersions = calcMinVersions(im);

        if (module == null) {
          // set the selected module for obx installation
          if (installOrig != null && installOrig.length > 0) {
            module = installOrig[0];
          } else {
            Module[] modsToUpdate = im.getModulesToUpdate();
            if (modsToUpdate != null && modsToUpdate.length > 0) {
              module = modsToUpdate[0];
            }
          }
        }
        // check commercial modules and show error page if not allowed to install
        if (!checkCommercialModules(im, minVersions, response, vars, module)) {
          return;
        }

        // Show warning message when installing/updating modules not in General availability level
        if (!islocal) {
          if (!"500".equals((String) module.getAdditionalInfo().get("maturity.level"))) {
            discard[6] = "";
          } else {
            if (inst != null) {
              for (Module m : inst) {
                if (!"500".equals((String) m.getAdditionalInfo().get("maturity.level"))) {
                  discard[6] = "";
                }
              }
            }
            if (upd != null) {
              for (Module m : upd) {
                if (!"500".equals((String) m.getAdditionalInfo().get("maturity.level"))) {
                  discard[6] = "";
                }
              }
            }
          }
        }

      } else { // Dependencies not satisfied, do not show continue button
        message = im.getCheckError();
        discard[5] = "discardContinue";

        if (message == null || message.getMessage() == null || message.getMessage().isEmpty()) {
          // No message: set generic one
          message = new OBError();
          message.setType("Error");
          message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
          message.setMessage(Utility.messageBD(this, "ModulesNotInstallable", vars.getLanguage()));
        }
      }
      if (upd == null || upd.length == 0)
        discard[1] = "updateModules";
      if (inst == null || inst.length == 0)
        discard[2] = "installModules";
      if ((upd == null || upd.length == 0) && (inst == null || inst.length == 0)
          && (module == null)) {
        discard[3] = "discardAdditional";
        discard[5] = "discardContinue";
      }
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
      message = new OBError();
      message.setType("Error");
      message.setTitle(Utility.messageBD(this, message.getType(), vars.getLanguage()));
      message.setMessage(e.toString());
    }

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallP1", discard).createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    if (inst != null && inst.length > 0) {
      xmlDocument.setData("installs", getModuleFieldProvider(inst, minVersions, false, vars
          .getLanguage(), islocal));
    }

    if (upd != null && upd.length > 0) {
      xmlDocument.setData("updates", getModuleFieldProvider(upd, minVersions, false, vars
          .getLanguage(), islocal));
    }

    xmlDocument.setParameter("inpLocalInstall", islocal ? "Y" : "N");

    if (!islocal && module != null) {
      xmlDocument.setParameter("key", recordId);
      xmlDocument.setParameter("moduleID", module.getModuleID());
      xmlDocument.setParameter("moduleName", module.getName());
      xmlDocument.setParameter("moduleVersion", module.getVersionNo());
      xmlDocument.setParameter("linkCore", module.getModuleVersionID());

      if (!check || "500".equals((String) module.getAdditionalInfo().get("maturity.level"))) {
        xmlDocument.setParameter("maturityStyle", "none");
      } else {
        xmlDocument.setParameter("maturityStyle", "yes");
        xmlDocument.setParameter("maturityLevel", (String) module.getAdditionalInfo().get(
            "maturity.name"));
      }
    }
    {
      if (message != null) {
        xmlDocument.setParameter("messageType", message.getType());
        xmlDocument.setParameter("messageTitle", message.getTitle());
        xmlDocument.setParameter("messageMessage", message.getMessage());
      }
    }
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private FieldProvider[] getModuleFieldProvider(Module[] inst, Map<String, String> minVersions,
      boolean installed, String lang, boolean islocal) {
    ArrayList<HashMap<String, String>> rt = new ArrayList<HashMap<String, String>>();

    for (Module module : inst) {
      HashMap<String, String> mod = new HashMap<String, String>();
      mod.put("name", module.getName());
      mod.put("versionNo", module.getVersionNo());
      mod.put("moduleVersionID", module.getModuleVersionID());

      if (installed) {
        if (minVersions != null && minVersions.get(module.getModuleID()) != null
            && !minVersions.get(module.getModuleID()).equals("")) {
          mod.put("versionNoMin", Utility.messageBD(this, "UpdateModuleNeed", lang) + " "
              + minVersions.get(module.getModuleID()));
        }
        mod.put("versionNoCurr", currentInstalledVersion(module.getModuleID()));
      } else {
        mod.put("versionNoMin", (minVersions.get(module.getModuleID()) == null ? module
            .getVersionNo() : minVersions.get(module.getModuleID())));
      }

      if (!islocal) {
        if ("500".equals((String) module.getAdditionalInfo().get("maturity.level"))) {
          mod.put("maturityStyle", "none");
        } else {
          mod.put("maturityStyle", "yes");
          mod.put("maturityLevel", (String) module.getAdditionalInfo().get("maturity.name"));
        }
      }
      rt.add(mod);
    }
    return FieldProviderFactory.getFieldProviderArray(rt);
  }

  private String currentInstalledVersion(String moduleId) {
    String currentVersion = "";
    org.openbravo.model.ad.module.Module mod = OBDal.getInstance().get(
        org.openbravo.model.ad.module.Module.class, moduleId);
    if (mod != null) {
      currentVersion = mod.getVersion();
    }
    return currentVersion;
  }

  /**
   * calculate minimum required version for each module in consistent set of (installs, updates)
   * returned by a checkConsistency call
   * 
   * @param im
   */
  private Map<String, String> calcMinVersions(ImportModule im) {
    // (key,value) = (moduleId, minRequiredVersion)
    Map<String, String> minVersions = new HashMap<String, String>();
    for (Module m : im.getModulesToInstall()) {
      calcMinVersionFromDeps(minVersions, m.getDependencies());
      if (m.getIncludes() != null) {
        calcMinVersionFromDeps(minVersions, m.getIncludes());
      }
    }
    for (Module m : im.getModulesToUpdate()) {
      calcMinVersionFromDeps(minVersions, m.getDependencies());
      if (m.getIncludes() != null) {
        calcMinVersionFromDeps(minVersions, m.getIncludes());
      }
    }

    // check and show:
    for (Module m : im.getModulesToInstall()) {
      log4j.debug("Install module " + m.getName() + " in version " + m.getVersionNo()
          + ", required is version >=" + minVersions.get(m.getModuleID()));
    }
    for (Module m : im.getModulesToUpdate()) {
      log4j.debug("Updating module " + m.getName() + " in version " + m.getVersionNo()
          + ", required is version >=" + minVersions.get(m.getModuleID()));
    }
    return minVersions;
  }

  /**
   * Utility method which processes a list of dependencies and fills a Map of (moduleID, minVersion)
   * 
   * @param minVersions
   *          in/out-parameter with map of (moduleID, minVersion)
   * @param deps
   *          array of dependency entries
   */
  private static void calcMinVersionFromDeps(Map<String, String> minVersions,
      ModuleDependency[] deps) {
    for (ModuleDependency md : deps) {
      String oldMinVersion = minVersions.get(md.getModuleID());
      VersionComparator vc = new VersionComparator();
      if (oldMinVersion == null || vc.compare(oldMinVersion, md.getVersionStart()) < 0) {
        minVersions.put(md.getModuleID(), md.getVersionStart());
      }
    }
  }

  /**
   * Verifies the commercial modules to be installed/updated and shows an error message with the
   * actions to be taken in case some of them cannot be installed due to license restrictions.
   */
  private boolean checkCommercialModules(ImportModule im, Map<String, String> minVersions,
      HttpServletResponse response, VariablesSecureApp vars, Module selectedModule)
      throws IOException {
    // get the list of all commercial modules that are not allowed in this instance
    List<Module> notAllowedModules = getNotAllowedModules(im.getModulesToInstall());
    notAllowedModules.addAll(getNotAllowedModules(im.getModulesToUpdate()));
    if (notAllowedModules.isEmpty()) {
      // all modules are allowed, continue the installation without error
      return true;
    }
    String discard[] = { "", "", "", "", "", "" };
    ActivationKey ak = ActivationKey.getInstance();

    // check if the only module is core as a dependency of another one
    String minCoreVersion = "";
    List<Module> modulesToAcquire = getModulesToAcquire((ArrayList<Module>) notAllowedModules);

    if (notAllowedModules.size() == 1 && !"0".equals(selectedModule.getModuleID())
        && "0".equals(notAllowedModules.get(0).getModuleID())) {
      discard[0] = "OBPSInstance-Canceled";
      discard[1] = "actionList";
      minCoreVersion = minVersions.get("0");
    } else {
      discard[0] = "installCore";
      // check canceled instance
      if (ak.getSubscriptionStatus() == SubscriptionStatus.CANCEL) {
        discard[1] = "actionList";

        // show all commercial modules, not only the ones that should be paid
        modulesToAcquire = notAllowedModules;
      } else {
        discard[1] = "OBPSInstance-Canceled";
      }

      // Decide license type, if any
      int maxTier = getMaxTier(notAllowedModules);
      LicenseClass licenseEdition = ak.getLicenseClass();

      if (!ak.isActive()) {
        if (maxTier == 1) {
          // show subscribe to Basic
          discard[2] = "subscribeSTD";
          discard[3] = "upgradeSTD";
        } else {
          // show subscribe to Standard
          discard[2] = "subscribeBAS";
          discard[3] = "upgradeSTD";
        }
      } else if (licenseEdition == LicenseClass.BASIC) {
        discard[2] = "subscribeBAS";
        discard[3] = "subscribeSTD";
        if (maxTier == 1) {
          // do not show license action
          discard[4] = "upgradeSTD";
        } else {
          // show upgrade to Standard
        }
      } else if (licenseEdition == LicenseClass.STD) {
        // do not show license action
        discard[2] = "subscribeBAS";
        discard[3] = "subscribeSTD";
        discard[4] = "upgradeSTD";
      }
    }

    if (modulesToAcquire.isEmpty()) {
      discard[5] = "acquireModules";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_ErrorCommercial", discard)
        .createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("minCoreVersion", minCoreVersion);
    xmlDocument.setData("notAllowedModules", FieldProviderFactory
        .getFieldProviderArray(modulesToAcquire));
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

    return false;
  }

  /**
   * Returns the list of modules which license must be acquired to be installed in the instance.
   * Currently it only removes core from the list of not allowed modules because it is not needed to
   * acquire a license for the module, just the Professional Subscription. It could be implemented
   * to remove also all the free commercial modules, in case this info came from CR.
   * 
   * @param notAllowedModules
   *          List with all the not allowed commercial modules
   * @return List with all the commercial modules that need a license to be acquired
   */
  @SuppressWarnings("unchecked")
  private List<Module> getModulesToAcquire(ArrayList<Module> notAllowedModules) {
    List<Module> rt = (List<Module>) notAllowedModules.clone();
    for (Module mod : rt) {
      if ("0".equals(mod.getModuleID())) {
        rt.remove(mod);
        break;
      }
    }
    return rt;
  }

  /**
   * Returns the maximum tier of the commercial modules passed as parameter.
   * 
   * @param modulesToCheck
   * @return The maximum tier of the modules (1 or 2)
   */
  private int getMaxTier(List<Module> modulesToCheck) {
    for (Module mod : modulesToCheck) {
      String modTier = (String) mod.getAdditionalInfo().get("tier");
      if ("2".equals(modTier)) {
        return 2;
      }
    }
    return 1;
  }

  /**
   * Checks from the array of commercial modules passed as parameter, which ones are not allowed to
   * be installed in the instance and returns this list.
   * 
   * @param modulesToCheck
   * @return List of modules that cannot be installed in the instance
   */
  private List<Module> getNotAllowedModules(Module[] modulesToCheck) {
    ArrayList<Module> notAllowedModules = new ArrayList<Module>();
    ActivationKey ak = ActivationKey.getInstance();
    for (Module mod : modulesToCheck) {
      if (mod.isIsCommercial()
          && ak.isModuleSubscribed(mod.getModuleID()) != CommercialModuleStatus.ACTIVE) {
        notAllowedModules.add(mod);
      }
    }
    return notAllowedModules;
  }

  /**
   * Shows the second installation pup-up with all the license agreements for the modules to
   * install/update
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageInstall2(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    Module[] inst = null;
    Module[] selected;

    // Obtain the session object with the modules to install/update
    final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
    final Module[] installOrig = im.getModulesToInstall();
    final Module[] upd = im.getModulesToUpdate();

    final String adModuleId = vars.getStringParameter("inpModuleID"); // selected
    // module
    // to
    // install
    final boolean islocal = im.getIsLocal();

    if (!islocal) {
      selected = new Module[1];
      inst = new Module[installOrig.length == 0 ? 0 : adModuleId.equals("") ? installOrig.length
          : installOrig.length - 1]; // to
      // remove
      // the
      // module
      // itself
      // check if the version for the selected module is the selected one
      int j = 0;
      for (int i = 0; i < installOrig.length; i++) {
        final boolean found = installOrig[i].getModuleID().equals(adModuleId);
        if (found) {
          selected[0] = installOrig[i];
        } else {
          inst[j] = installOrig[i];
          j++;
        }

      }
    } else {
      selected = installOrig;
    }

    final String discard[] = { "", "", "" };

    if (inst == null || inst.length == 0)
      discard[0] = "moduleIntallation";

    if (upd == null || upd.length == 0)
      discard[1] = "moduleUpdate";

    if (selected == null || selected.length == 0)
      discard[2] = "moduleSelected";

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallP2", discard).createXmlDocument();

    // Set positions to names in order to be able to use keyboard for
    // navigation in the box
    int position = 1;
    if (selected != null && selected.length > 0) {
      final FieldProvider[] fp = FieldProviderFactory.getFieldProviderArray(selected);
      for (int i = 0; i < fp.length; i++)
        FieldProviderFactory.setField(fp[i], "position", new Integer(position++).toString());
      xmlDocument.setData("selected", fp);
    }

    if (inst != null && inst.length > 0) {
      final FieldProvider[] fp = FieldProviderFactory.getFieldProviderArray(inst);
      for (int i = 0; i < fp.length; i++)
        FieldProviderFactory.setField(fp[i], "position", new Integer(position++).toString());
      xmlDocument.setData("installs", fp);
    }

    if (upd != null && upd.length > 0) {
      final FieldProvider[] fp = FieldProviderFactory.getFieldProviderArray(upd);
      for (int i = 0; i < fp.length; i++)
        FieldProviderFactory.setField(fp[i], "position", new Integer(position++).toString());
      xmlDocument.setData("updates", fp);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Shows the third pup-up for the installation process, in this popup the installation is executed
   * and afterwards a message is displayed with the success or fail information.
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printPageInstall3(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallP4").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    OBError message;
    if (im.getIsLocal())
      im.execute(((FileItem) vars.getSessionObject("ModuleManagementInstall|File"))
          .getInputStream());
    else
      im.execute();
    message = im.getOBError(this);

    {
      if (message != null) {
        xmlDocument.setParameter("messageType", message.getType());
        xmlDocument.setParameter("messageTitle", message.getTitle());
        xmlDocument.setParameter("messageMessage", message.getMessage());
      }
    }

    vars.removeSessionValue("ModuleManagementInstall|File");
    vars.removeSessionValue("InstallModule|ImportModule");

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Executes a search query in the web service and returns a HTML with the list of modules
   * retrieved from the query. This list is HTML with styles.
   * 
   * @param request
   * @param response
   * @param vars
   * @param text
   * @return
   */
  private String getSearchResults(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String text) {
    SimpleModule[] modules = null;
    try {
      if (!HttpsUtils.isInternetAvailable()) {
        final OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        message.setMessage(Utility.messageBD(this, "WSError", vars.getLanguage()));
        vars.setMessage("ModuleManagement", message);
        try {
          response.sendRedirect(strDireccion + request.getServletPath() + "?Command=ADD_NOSEARCH");
        } catch (final Exception ex) {
          log4j.error(ex.getMessage(), ex);
        }
      }
      final WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
      final WebService3Impl ws = loc.getWebService3();

      // Stub stub = (javax.xml.rpc.Stub) ws;
      // stub._setProperty(Stub.USERNAME_PROPERTY, "test");
      // stub._setProperty(Stub.PASSWORD_PROPERTY, "1");

      HashMap<String, String> maturitySearch = new HashMap<String, String>();
      maturitySearch.put("search.level", getSystemMaturity(false));
      modules = ws.moduleSearch(text, getInstalledModules(), maturitySearch);

    } catch (final Exception e) {
      final OBError message = new OBError();
      message.setType("Error");
      message.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      message.setMessage(Utility.messageBD(this, "WSError", vars.getLanguage()));
      vars.setMessage("ModuleManagement", message);
      log4j.error("Error searching modules", e);
      try {
        response.sendRedirect(strDireccion + request.getServletPath() + "?Command=ADD_NOSEARCH");
      } catch (final Exception ex) {
        log4j.error("error searching modules", ex);
      }
    }

    FieldProvider[] modulesBox = new FieldProvider[0];
    if (modules != null && modules.length > 0) {
      modulesBox = new FieldProvider[modules.length];
      int i = 0;
      for (SimpleModule mod : modules) {
        HashMap<String, String> moduleBox = new HashMap<String, String>();

        // set different icon depending on module type
        String icon = mod.getType();
        icon = (icon == null ? "M" : icon).equals("M") ? "Module" : icon.equals("T") ? "Template"
            : "Pack";

        // If there is no url, we need to hide the 'Visit Site' link and separator.
        String url = mod.getUrl();
        url = (url == null || url.equals("") ? "HIDDEN" : url);

        moduleBox.put("name", mod.getName());
        moduleBox.put("description", mod.getDescription());
        moduleBox.put("type", icon);
        moduleBox.put("help", mod.getHelp());
        moduleBox.put("url", getLink(url));
        moduleBox.put("moduleVersionID", mod.getModuleVersionID());
        moduleBox.put("commercialStyle", (mod.isIsCommercial() ? "true" : "none"));

        @SuppressWarnings("unchecked")
        HashMap<String, String> additioanlInfo = mod.getAdditionalInfo();
        if (additioanlInfo != null && !"500".equals(additioanlInfo.get("maturity.level"))) {
          // Display module's maturity in case it is not General availability (500)
          moduleBox.put("maturityStyle", "true");
          moduleBox.put("maturityLevel", additioanlInfo.get("maturity.name"));
        } else {
          moduleBox.put("maturityStyle", "none");
        }

        modulesBox[i] = FieldProviderFactory.getFieldProvider(moduleBox);
        i++;
      }
    }
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/modules/ModuleBox").createXmlDocument();

    xmlDocument.setData("structureBox", modulesBox);
    return xmlDocument.print();
  }

  /**
   * Returns String[] with the installed modules, this is used for perform the search in the
   * webservice and not to obtain in the list the already installed ones.
   * 
   * @return
   */
  private String[] getInstalledModules() {
    try {
      final ModuleManagementData data[] = ModuleManagementData.selectInstalled(this);
      if (data != null && data.length != 0) {
        final String[] rt = new String[data.length];
        for (int i = 0; i < data.length; i++)
          rt[i] = data[i].adModuleId;
        return rt;
      } else
        return new String[0];
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
      return (new String[0]);
    }
  }

  private String[] getUpdateableModules() {
    try {
      final ModuleManagementData data[] = ModuleManagementData.selectUpdateable(this);
      if (data != null && data.length != 0) {
        final String[] rt = new String[data.length];
        for (int i = 0; i < data.length; i++)
          rt[i] = data[i].adModuleVersionId;
        return rt;
      } else
        return new String[0];
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
      return (new String[0]);
    }
  }

  /**
   * This ajax call displays the license agreement for a module.
   * 
   * @param response
   * @param vars
   * @param record
   * @throws IOException
   * @throws ServletException
   */
  private void printLicenseAgreement(HttpServletResponse response, VariablesSecureApp vars,
      String record) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");

    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    final PrintWriter out = response.getWriter();
    final ImportModule im = (ImportModule) vars.getSessionObject("InstallModule|ImportModule");
    final Module[] inst = im.getModulesToInstall();
    final Module[] upd = im.getModulesToUpdate();

    int i = 0;
    boolean found = false;
    String agreement = "";
    while (!found && inst != null && i < inst.length) {
      if (found = inst[i].getModuleID().equals(record))
        agreement = inst[i].getLicenseAgreement();
      i++;
    }
    i = 0;
    while (!found && upd != null && i < upd.length) {
      if (found = upd[i].getModuleID().equals(record))
        agreement = upd[i].getLicenseAgreement();
      i++;
    }

    out.println(agreement);
    out.close();

  }

  /**
   * Displays the pop-up for the search locally file in order to look for an obx file and to install
   * it locally.
   * 
   * @param response
   * @param vars
   * @throws IOException
   * @throws ServletException
   */
  private void printSearchFile(HttpServletResponse response, VariablesSecureApp vars,
      OBError message) throws IOException, ServletException {
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/ModuleManagement_InstallLocal").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    if (message != null) {
      xmlDocument.setParameter("messageType", message.getType());
      xmlDocument.setParameter("messageTitle", message.getTitle());
      xmlDocument.setParameter("messageMessage", message.getMessage());
    }

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printScan(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");

    final HashMap<String, String> updates = ImportModule.scanForUpdates(this, vars);
    String up = "";
    for (final String node : updates.keySet())
      up += node + "," + updates.get(node) + "|";

    String notifications = getNotificationsHTML(vars.getLanguage());
    if (notifications.equals(""))
      notifications = Utility.messageBD(this, "NoUpdatesAvailable", vars.getLanguage());
    up = notifications + "|" + up + "|";
    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    final PrintWriter out = response.getWriter();
    out.println(up);
    out.close();
  }

  @SuppressWarnings("unchecked")
  private void printPageSettings(HttpServletResponse response, HttpServletRequest request)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String discard[] = { "", "" };
    OBError myMessage = null;
    try {
      OBContext.setAdminMode();
      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");

      if (vars.commandIn("SETTINGS_ADD", "SETTINGS_REMOVE")) {
        String moduleId;

        if (vars.commandIn("SETTINGS_ADD")) {
          moduleId = vars.getStringParameter("inpModule", IsIDFilter.instance);
        } else {
          moduleId = vars.getStringParameter("inpModuleId", IsIDFilter.instance);
        }

        org.openbravo.model.ad.module.Module mod = OBDal.getInstance().get(
            org.openbravo.model.ad.module.Module.class, moduleId);
        if (mod != null) {
          // do not update the audit info here, as its a local config change, which should not be
          // treated as 'local changes' by i.e. update.database
          try {
            OBInterceptor.setPreventUpdateInfoChange(true);
            if (vars.commandIn("SETTINGS_ADD")) {
              mod.setMaturityUpdate(vars.getStringParameter("inpModuleLevel"));
            } else {
              mod.setMaturityUpdate(null);
            }
            OBDal.getInstance().flush();
          } finally {
            OBInterceptor.setPreventUpdateInfoChange(false);
          }
        } else {
          log4j.error("Module does not exists ID:" + moduleId);
        }
      } else if (vars.commandIn("SETTINGS_SAVE")) {
        // Save global maturity levels
        sysInfo.setMaturitySearch(vars.getStringParameter("inpSearchLevel"));
        sysInfo.setMaturityUpdate(vars.getStringParameter("inpScanLevel"));

        // Save enforcement
        boolean warn = false;
        String warnMsg = "";
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
          String parameter = (String) e.nextElement();
          if (parameter.startsWith("inpEnforcement")) {
            String depId = parameter.replace("inpEnforcement", "");
            String value = vars.getStringParameter(parameter);
            org.openbravo.model.ad.module.ModuleDependency dep = OBDal.getInstance().get(
                org.openbravo.model.ad.module.ModuleDependency.class, depId);
            if (dep != null) {
              boolean save = true;
              if ("MINOR".equals(value)) {
                // Setting Minor version enforcement, check the configuration is still valid
                VersionComparator vc = new VersionComparator();
                if (dep.getLastVersion() == null
                    && vc.compare(dep.getFirstVersion(), dep.getDependentModule().getVersion()) != 0) {
                  save = false;
                  warn = true;
                  warnMsg += "<br/>"
                      + Utility.messageBD(this, "ModuleDependsButInstalled", vars.getLanguage())
                          .replace("@module@", dep.getDependentModule().getName()).replace(
                              "@version@", dep.getFirstVersion()).replace("@installed@",
                              dep.getDependentModule().getVersion());
                } else if (dep.getLastVersion() != null
                    && !(vc.compare(dep.getFirstVersion(), dep.getDependentModule().getVersion()) <= 0 && vc
                        .compare(dep.getLastVersion(), dep.getDependentModule().getVersion()) >= 0)) {
                  save = false;
                  warn = true;
                  warnMsg += "<br/>"
                      + Utility.messageBD(this, "ModuleDependsButInstalled", vars.getLanguage())
                          .replace("@module@", dep.getDependentModule().getName()).replace(
                              "@version@", dep.getFirstVersion() + " - " + dep.getLastVersion())
                          .replace("@installed@", dep.getDependentModule().getVersion());
                }
              }
              if (save) {
                if (value.equals(dep.getDependencyEnforcement())) {
                  // setting no instance enforcement in case the selected value is the default
                  dep.setInstanceEnforcement(null);
                } else {
                  dep.setInstanceEnforcement(value);
                }
              }
            }
          }
        }
        if (warn) {
          myMessage = new OBError();
          myMessage.setType("Warning");
          myMessage.setMessage(Utility.messageBD(this, "CannotSetMinorEnforcements", vars
              .getLanguage())
              + warnMsg);
        }
      }

      // Possible maturity levels are obtained from CR, obtain them once per session and store
      MaturityLevel levels = (MaturityLevel) vars.getSessionObject("SettingsModule|MaturityLevels");
      if (levels == null) {
        levels = new MaturityLevel();
        vars.setSessionObject("SettingsModule|MaturityLevels", levels);
      }

      // Populate module specific grid
      OBCriteria<org.openbravo.model.ad.module.Module> qModuleSpecific = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.module.Module.class);
      qModuleSpecific.add(Expression
          .isNotNull(org.openbravo.model.ad.module.Module.PROPERTY_MATURITYUPDATE));
      qModuleSpecific.addOrder(Order.asc(org.openbravo.model.ad.module.Module.PROPERTY_NAME));
      ArrayList<HashMap<String, String>> moduleSpecifics = new ArrayList<HashMap<String, String>>();
      List<org.openbravo.model.ad.module.Module> moduleSpecificList = qModuleSpecific.list();
      if (moduleSpecificList.isEmpty()) {
        discard[0] = "moduleTable";
      }
      for (org.openbravo.model.ad.module.Module module : moduleSpecificList) {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("id", module.getId());
        m.put("name", module.getName());
        m.put("level", levels.getLevelName(module.getMaturityUpdate()));
        moduleSpecifics.add(m);
      }

      // Populate combo of modules without specific setting
      OBCriteria<org.openbravo.model.ad.module.Module> qModule = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.module.Module.class);
      qModule.add(Expression.isNull(org.openbravo.model.ad.module.Module.PROPERTY_MATURITYUPDATE));
      qModule.addOrder(Order.asc(org.openbravo.model.ad.module.Module.PROPERTY_NAME));

      ArrayList<HashMap<String, String>> modules = new ArrayList<HashMap<String, String>>();
      List<org.openbravo.model.ad.module.Module> moduleList = qModule.list();
      if (moduleList.isEmpty()) {
        discard[0] = "assignModule";
      }
      for (org.openbravo.model.ad.module.Module module : moduleList) {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("id", module.getId());
        m.put("name", module.getName());
        modules.add(m);
      }

      // Dependencies table
      OBCriteria<org.openbravo.model.ad.module.ModuleDependency> qDeps = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.module.ModuleDependency.class);
      qDeps.add(Expression.eq(
          org.openbravo.model.ad.module.ModuleDependency.PROPERTY_USEREDITABLEENFORCEMENT, true));
      qDeps.addOrder(Order.asc(org.openbravo.model.ad.module.ModuleDependency.PROPERTY_MODULE));
      qDeps.addOrder(Order.asc(org.openbravo.model.ad.module.ModuleDependency.PROPERTY_ISINCLUDED));
      qDeps.addOrder(Order
          .asc(org.openbravo.model.ad.module.ModuleDependency.PROPERTY_DEPENDANTMODULENAME));
      List<org.openbravo.model.ad.module.ModuleDependency> deps = qDeps.list();

      if (deps.isEmpty()) {
        discard[1] = "enforcementTable";
      } else {
        discard[1] = "noEditableEnforcement";
      }

      FieldProvider fpDeps[] = new FieldProvider[deps.size()];
      FieldProvider fpEnforcements[][] = new FieldProvider[deps.size()][];
      int i = 0;
      String lastName = "";
      Boolean lastType = null;

      // Get the static text values once, not to query db each time for them
      OBCriteria<org.openbravo.model.ad.domain.List> qList = OBDal.getInstance().createCriteria(
          org.openbravo.model.ad.domain.List.class);
      qList.add(Expression.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE + ".id",
          "8BA0A3775CE14CE69989B6C09982FB2E"));
      qList.addOrder(Order.asc(org.openbravo.model.ad.domain.List.PROPERTY_SEQUENCENUMBER));
      SQLReturnObject[] fpEnforcementCombo = new SQLReturnObject[qList.list().size()];
      for (org.openbravo.model.ad.domain.List value : qList.list()) {
        SQLReturnObject val = new SQLReturnObject();
        val.setData("ID", value.getSearchKey());
        val.setData("NAME", Utility.getListValueName("Dependency Enforcement",
            value.getSearchKey(), vars.getLanguage()));
        fpEnforcementCombo[i] = val;
        i++;
      }
      String inclusionType = Utility.messageBD(this, "InclusionType", vars.getLanguage());
      String dependencyType = Utility.messageBD(this, "DependencyType", vars.getLanguage());
      String defaultStr = Utility.messageBD(this, "Default", vars.getLanguage());

      i = 0;
      for (org.openbravo.model.ad.module.ModuleDependency dep : deps) {
        HashMap<String, String> d = new HashMap<String, String>();

        d.put("baseModule", dep.getDependentModule().getName());
        d.put("currentVersion", dep.getDependentModule().getVersion());
        d.put("firstVersion", dep.getFirstVersion());
        d.put("lastVersion", dep.getLastVersion());
        d.put("depId", dep.getId());

        // Grouping by module and dependency
        String currentName = dep.getModule().getName();
        Boolean currentType = dep.isIncluded();
        if (lastName.equals(currentName)) {
          d.put("modName", "");
          if (!currentType.equals(lastType)) {
            d.put("depType", dep.isIncluded() ? inclusionType : dependencyType);
          } else {
            d.put("depType", "");
          }
        } else {
          d.put("modName", currentName);
          d.put("depType", dep.isIncluded() ? inclusionType : dependencyType);
          lastName = currentName;
          lastType = currentType;
        }

        d.put("selectedEnforcement", dep.getInstanceEnforcement() == null ? dep
            .getDependencyEnforcement() : dep.getInstanceEnforcement());
        fpDeps[i] = FieldProviderFactory.getFieldProvider(d);
        fpEnforcements[i] = getEnforcementCombo(dep, fpEnforcementCombo, defaultStr);
        i++;
      }

      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/ModuleManagementSettings", discard).createXmlDocument();

      xmlDocument.setData("moduleDetail", FieldProviderFactory
          .getFieldProviderArray(moduleSpecifics));
      xmlDocument.setData("moduleCombo", FieldProviderFactory.getFieldProviderArray(modules));

      // Populate maturity levels combos
      xmlDocument.setParameter("selectedScanLevel", sysInfo.getMaturityUpdate() == null ? "500"
          : sysInfo.getMaturityUpdate());
      xmlDocument.setData("reportScanLevel", "liststructure", levels.getCombo());
      xmlDocument.setParameter("selectedSearchLevel", sysInfo.getMaturitySearch() == null ? "500"
          : sysInfo.getMaturitySearch());
      xmlDocument.setData("reportSearchLevel", "liststructure", levels.getCombo());
      xmlDocument.setData("reportModuleLevel", "liststructure", levels.getCombo());

      // less and most mature values
      xmlDocument.setParameter("lessMature", levels.getLessMature());
      xmlDocument.setParameter("mostMature", levels.getMostMature());

      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");

      xmlDocument.setData("dependencyDetail", fpDeps);
      xmlDocument.setDataArray("reportEnforcementType", "liststructure", fpEnforcements);

      // Interface parameters
      final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ModuleManagement", false, "",
          "", "", false, "ad_forms", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        final WindowTabs tabs = new WindowTabs(this, vars,
            "org.openbravo.erpCommon.ad_forms.ModuleManagement");
        xmlDocument.setParameter("theme", vars.getTheme());
        final NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ModuleManagement.html", classInfo.id, classInfo.type, strReplaceWith, tabs
                .breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ModuleManagement.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (final Exception ex) {
        throw new ServletException(ex);
      }

      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      out.println(xmlDocument.print());
      out.close();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Obtains the combo used for enforcement, showing which is the default setting.
   */
  private FieldProvider[] getEnforcementCombo(org.openbravo.model.ad.module.ModuleDependency dep,
      SQLReturnObject[] fpEnforcementCombo, String defaultStr) {
    SQLReturnObject[] rt = new SQLReturnObject[fpEnforcementCombo.length];

    int i = 0;
    for (SQLReturnObject val : fpEnforcementCombo) {
      rt[i] = new SQLReturnObject();
      rt[i].setData("ID", val.getData("ID"));
      if (val.getData("ID").equals(dep.getDependencyEnforcement())) {
        rt[i].setData("NAME", val.getData("NAME") + " " + defaultStr);
      } else {
        rt[i].setData("NAME", val.getData("NAME"));
      }
      i++;
    }
    return rt;
  }

  private String getSystemMaturity(boolean updateLevel) {
    try {
      OBContext.setAdminMode();
      SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");
      if (updateLevel) {
        return sys.getMaturityUpdate();
      } else {
        return sys.getMaturitySearch();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Enable the passed in module
   */
  private void enable(VariablesSecureApp vars) throws ServletException {
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);
      ArrayList<String> notEnabledModules = new ArrayList<String>();
      enableDisableModule(OBDal.getInstance().get(org.openbravo.model.ad.module.Module.class,
          vars.getStringParameter("inpcRecordId")), true, notEnabledModules);
      finishEnabling(notEnabledModules, vars);
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  /**
   * Disables all the selected modules
   */
  private void disable(VariablesSecureApp vars) throws ServletException {
    String modules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);

    // check if disabling core
    if (modules.contains("'0'")) {
      OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage("Cannot disable core");
      vars.setMessage("ModuleManagement|message", msg);
      return;
    }
    String[] moduleIds = modules.replace("(", "").replace(")", "").replace(" ", "")
        .replace("'", "").split(",");
    ArrayList<String> notEnabledModules = new ArrayList<String>();
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);

      for (String moduleId : moduleIds) {
        org.openbravo.model.ad.module.Module module = OBDal.getInstance().get(
            org.openbravo.model.ad.module.Module.class, moduleId);
        enableDisableModule(module, false, notEnabledModules);
      }

      finishEnabling(notEnabledModules, vars);
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  /**
   * Enables or disables the module passed as parameter. In case it has other modules has inclusions
   * they are disabled/enabled recursively.
   * 
   * @param module
   *          Module to enable or disable
   * @param enable
   *          If true, the module will be enabled, if false, it will be disabled
   * @param notEnabledModules
   *          List of modules that couldn't be enabled because they are commercial and are not part
   *          of the instance license's subscribed modules
   */
  private void enableDisableModule(org.openbravo.model.ad.module.Module module, boolean enable,
      List<String> notEnabledModules) {
    if (module == null) {
      return;
    }

    if (enable
        && module.isCommercial()
        && ActivationKey.getInstance().isModuleSubscribed(module.getId()) == CommercialModuleStatus.NO_SUBSCRIBED) {
      log4j.warn("Cannot enable not subscribed commercial module " + module);
      notEnabledModules.add(module.getName());
    } else {
      log4j.info((enable ? "Enabling " : "Disabling ") + module.getName());
      module.setEnabled(enable);
    }
    if ("M".equals(module.getType())) {
      // Standard modules do not have inclusions
      return;
    }

    // For packs and templates enable/disable recursively all inclusions
    for (org.openbravo.model.ad.module.ModuleDependency dependency : module
        .getModuleDependencyList()) {
      if (dependency.isIncluded()) {
        enableDisableModule(dependency.getDependentModule(), enable, notEnabledModules);
      }
    }
  }

  /**
   * Finishes the enabling/disabling process. The actions it performs are:
   * <ul>
   * <li>In case the are no modules that couldn't be enabled, it reloads the disabled modules in
   * memory.
   * <li>If any module coudn't no be enabled, shows an error message and rolls back the transaction
   * </ul>
   */
  private void finishEnabling(List<String> notEnabledModules, VariablesSecureApp vars) {
    if (notEnabledModules == null || notEnabledModules.isEmpty()) {
      OBDal.getInstance().flush();
      DisabledModules.reload();
      return;
    }
    String msg = Utility.messageBD(this, "CannotEnableNonSubscribedModules", vars.getLanguage());
    for (String module : notEnabledModules) {
      msg += "<br/>" + module;
    }
    OBError err = new OBError();
    err.setType("Error");
    err.setMessage(msg);
    vars.setMessage("ModuleManagement|message", err);
    OBDal.getInstance().rollbackAndClose();
  }
}
