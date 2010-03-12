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
 * All portions are Copyright (C) 2008-2009 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.OrgTree;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.modules.ModuleReferenceDataOrgTree;
import org.openbravo.erpCommon.modules.ModuleUtiltiy;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class InitialOrgSetup extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final String SALTO_LINEA = "<br>\n";
  private String C_Currency_ID = "";
  private String AD_User_U_Name = "";
  private String AD_User_U_ID = "";
  private String AD_Client_ID = "";
  private String AD_Org_ID = "";
  private String C_AcctSchema_ID = "";
  private String client = "";
  private String strError = "";
  private StringBuffer strSummary = new StringBuffer();
  private AcctSchema m_AcctSchema;
  private boolean m_hasProject;
  private boolean m_hasMCampaign;
  private boolean m_hasSRegion;
  private boolean isOK = true;
  private String AD_Tree_Org_ID = "", AD_Tree_BPartner_ID = "", AD_Tree_Project_ID = "",
      AD_Tree_SalesRegion_ID = "", AD_Tree_Product_ID = "", AD_Tree_Account_ID = "";
  private static StringBuffer m_info = new StringBuffer();
  private final String CompiereSys = "N"; // Should NOT be changed

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("OK")) {
      final String strModules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);
      m_info.delete(0, m_info.length());
      final String strResult = process(request, response, vars, strModules);
      log4j.debug("InitialOrgSetup - after processFile");
      printPageResult(response, vars, strResult);
    } else if (vars.commandIn("CANCEL")) {
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    final ModuleReferenceDataOrgTree tree = new ModuleReferenceDataOrgTree(this, vars.getClient(),
        false, true);
    XmlDocument xmlDocument = null;
    final String[] discard = { "selEliminar" };
    if (tree.getData() == null || tree.getData().length == 0)
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/InitialOrgSetup")
          .createXmlDocument();
    else
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/InitialOrgSetup",
          discard).createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialOrgSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialOrgSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    {
      vars.removeMessage("InitialOrgSetup");
      final OBError myMessage = vars.getMessage("InitialOrgSetup");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      xmlDocument.setParameter("moduleTree", tree.toHtml());
      xmlDocument.setParameter("moduleTreeDescription", tree.descriptionToHtml());

      xmlDocument.setParameter("paramLocationId", "");
      xmlDocument.setParameter("paramLocationDescription", "");
      // xmlDocument.setParameter("region", arrayDobleEntrada("arrRegion",
      // RegionComboData.selectTotal(this)));
      xmlDocument.setData("reportCurrency", "liststructure", MonedaComboData.select(this));
      xmlDocument.setData("reportOrgType", "liststructure", InitialOrgSetupData.selectOrgType(this,
          vars.getLanguage(), vars.getClient()));
      xmlDocument.setData("reportParentOrg", "liststructure", InitialOrgSetupData.selectParentOrg(
          this, vars.getLanguage(), vars.getClient()));

      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void printPageResult(HttpServletResponse response, VariablesSecureApp vars,
      String strResult) throws IOException, ServletException {
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/Resultado").createXmlDocument();

    xmlDocument.setParameter("resultado", strResult);

    final ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialOrgSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      final WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialOrgSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      final NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      final LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialOrgSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }
    OBError myMessage = new OBError();
    myMessage.setTitle("");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - before setMessage");
    if (strError != null && !strError.equals("")) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), strError);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - isOK: " + isOK);
    if (isOK)
      myMessage.setType("Success");
    else
      myMessage.setType("Error");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - Message Type: " + myMessage.getType());
    vars.setMessage("InitialOrgSetup", myMessage);
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - after setMessage");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private synchronized String process(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strModules) throws IOException {
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - strModules - " + strModules);
    Connection conn = null;
    isOK = true;
    strSummary = new StringBuffer();
    strSummary.append(Utility.messageBD(this, "ReportSummary", vars.getLanguage())).append(
        SALTO_LINEA);
    AD_Client_ID = vars.getClient();
    final String strOrganization = vars.getStringParameter("inpOrganization");
    final String strOrgUser = vars.getStringParameter("inpOrgUser");
    C_Currency_ID = vars.getStringParameter("inpCurrency");
    final String strCreateAccounting = vars.getStringParameter("inpCreateAccounting");
    final String strOrgType = vars.getStringParameter("inpOrgType");
    final String strParentOrg = vars.getStringParameter("inpParentOrg");
    final String strcLocationId = vars.getStringParameter("inpcLocationId");
    final boolean bProduct = isTrue(vars.getStringParameter("inpProduct"));
    final boolean bBPartner = isTrue(vars.getStringParameter("inpBPartner"));
    final boolean bProject = isTrue(vars.getStringParameter("inpProject"));
    final boolean bCampaign = isTrue(vars.getStringParameter("inpCampaign"));
    final boolean bSalesRegion = isTrue(vars.getStringParameter("inpSalesRegion"));
    final boolean bIsSystemInstalation = isTrue(vars.getStringParameter("inpSystem"));
    if (bIsSystemInstalation)
      client = vars.getClient();
    try {
      conn = this.getTransactionConnection();
      if (InitialOrgSetupData.updateUser2(conn, this, strOrgUser) != 0) {
        m_info.append("Duplicate UserOrg").append(SALTO_LINEA);
        strError = Utility.messageBD(this, "Duplicate Organization Username", vars.getLanguage());
        isOK = false;
        releaseRollbackConnection(conn);
        return m_info.toString();
      }
      releaseCommitConnection(conn);
    } catch (final Exception err) {
      log4j.warn(err);
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
    }
    /*
     * // verify that organization type and parent selection makes sense try { conn =
     * this.getTransactionConnection(); final String strIsLE = InitialOrgSetupData.isLE(this,
     * strOrgType); final String strIsTransactionAllowed = InitialOrgSetupData
     * .isTransactionAllowed(this, strOrgType); if (!strIsLE.equals("Y") &&
     * strIsTransactionAllowed.equals("Y")) { if (InitialOrgSetupData.verifyIsLE(this,
     * strParentOrg).equals( "N")) { m_info.append("Parent organization not correct").append(
     * SALTO_LINEA); strError = Utility.messageBD(this, "ParentOrganizationNotCorrect",
     * vars.getLanguage()); isOK = false; releaseRollbackConnection(conn); return m_info.toString();
     * } } releaseCommitConnection(conn); } catch (final Exception err) { log4j.warn(err); try {
     * releaseRollbackConnection(conn); } catch (final Exception ignored) { } }
     */
    try {
      m_info.append(SALTO_LINEA).append("*****************************************************")
          .append(SALTO_LINEA);
      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "StartingOrg", vars.getLanguage()))
          .append(SALTO_LINEA);
      final boolean prevMode = OBContext.getOBContext().setInAdministratorMode(true);
      try {
        if (!createOrg(request, vars, strOrganization, strOrgType, strParentOrg, strOrgUser,
            strcLocationId)) {
          releaseRollbackConnection(conn);
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "createOrgFailed", vars.getLanguage())).append(SALTO_LINEA);
          strSummary.append(SALTO_LINEA).append(
              Utility.messageBD(this, "createOrgFailed", vars.getLanguage())).append(SALTO_LINEA);
          isOK = false;
          return m_info.toString();
        }
      } finally {
        OBContext.getOBContext().setInAdministratorMode(prevMode);
      }
    } catch (final Exception err) {
      m_info.append(SALTO_LINEA).append(
          Utility.messageBD(this, "createOrgFailed", vars.getLanguage())).append(SALTO_LINEA);
      strSummary.append(SALTO_LINEA).append(
          Utility.messageBD(this, "createOrgFailed", vars.getLanguage())).append(SALTO_LINEA);
      strError = err.toString();
      strError = strError.substring((strError.lastIndexOf("@ORA-") > 0 ? strError
          .lastIndexOf("@ORA-") : 0), strError.length());
      isOK = false;
      log4j.error("Error in createOrg", err);
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
    }
    m_info.append(SALTO_LINEA).append("*****************************************************")
        .append(SALTO_LINEA);
    if (strCreateAccounting.equals("")) {
      m_info.append(SALTO_LINEA).append(
          Utility.messageBD(this, "SkippingAccounting", vars.getLanguage())).append(SALTO_LINEA);
    } else {
      final AccountingValueData av = new AccountingValueData(vars, "inpFile", true, "C");
      final FieldProvider[] avData = av.getFieldProvider();
      if (avData.length == 0)
        m_info.append(SALTO_LINEA).append(
            Utility.messageBD(this, "SkippingAccounting", vars.getLanguage())).append(SALTO_LINEA);
      else {
        try {
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "StartingAccounting", vars.getLanguage()))
              .append(SALTO_LINEA);
          if (!createAccounting(vars, strOrganization, C_Currency_ID, InitialOrgSetupData.currency(
              this, C_Currency_ID), bProduct, bBPartner, bProject, bCampaign, bSalesRegion, avData)) {
            releaseRollbackConnection(conn);
            m_info.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
                SALTO_LINEA);
            strSummary.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
                SALTO_LINEA);
            isOK = false;
            return m_info.toString();
          }
        } catch (final Exception err) {
          log4j.warn(err);
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          strSummary.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          strError = err.toString();
          strError = strError.substring((strError.lastIndexOf("@ORA-") > 0 ? strError
              .lastIndexOf("@ORA-") : 0), strError.length());
          log4j.debug("InitialOrgSetup - after strError: " + strError);
          isOK = false;
          try {
            releaseRollbackConnection(conn);
          } catch (final Exception ignored) {
          }
        }
      }
    }

    // ==============================================================================================

    if (strModules != null && !strModules.equals("")) {
      try {
        m_info.append(SALTO_LINEA).append("*****************************************************")
            .append(SALTO_LINEA);
        m_info.append(SALTO_LINEA).append(
            Utility.messageBD(this, "StartingReferenceData", vars.getLanguage())).append(
            SALTO_LINEA);
        final boolean prevMode = OBContext.getOBContext().setInAdministratorMode(true);
        String strReferenceData = "";
        try {
          strReferenceData = createReferenceData(vars, strOrganization, AD_Client_ID, strModules,
              bProduct, bBPartner, bProject, bCampaign, bSalesRegion, strCreateAccounting);
        } finally {
          OBContext.getOBContext().setInAdministratorMode(prevMode);
        }
        if (!strReferenceData.equals("")) {
          releaseRollbackConnection(conn);
          strError = Utility.messageBD(this, "CreateReferenceDataFailed", vars.getLanguage());
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateReferenceDataFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          strSummary.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateReferenceDataFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          isOK = false;
          strReferenceData = "";
          return m_info.toString();
        }
      } catch (final Exception err) {
        log4j.warn(err);
        m_info.append(SALTO_LINEA).append(
            Utility.messageBD(this, "CreateReferenceDataFailed", vars.getLanguage())).append(
            SALTO_LINEA);
        strSummary.append(SALTO_LINEA).append(
            Utility.messageBD(this, "CreateReferenceDataFailed", vars.getLanguage())).append(
            SALTO_LINEA);
        strError = err.toString();
        strError = strError.substring((strError.lastIndexOf("@ORA-") > 0 ? strError
            .lastIndexOf("@ORA-") : 0), strError.length());
        log4j.debug("InitialOrgSetup - after strError: " + strError);
        isOK = false;
        try {
          releaseRollbackConnection(conn);
        } catch (final Exception ignored) {
        }
      }
    }
    // ==============================================================================================
    log4j.debug("InitialOrgSetup - after createEntities");
    if (isOK)
      strError = Utility.messageBD(this, "Success", vars.getLanguage());
    log4j.debug("InitialOrgSetup - after strError");
    strSummary.append(m_info.toString());
    m_info = strSummary;
    return m_info.toString();
  }

  private boolean isTrue(String s) {
    if (s == null || s.equals(""))
      return false;
    else
      return true;
  }

  private boolean createOrg(HttpServletRequest request, VariablesSecureApp vars, String orgName,
      String strOrgType, String strParentOrg, String userOrg, String strcLocationId)
      throws ServletException {

    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      // info header
      m_info.append(SALTO_LINEA);
      // Standard columns
      String name = null;

      // * Create Organization

      vars.setSessionValue("#CompiereSys", CompiereSys);
      vars.setSessionValue("AD_Client_ID", AD_Client_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createOrg - CLIENT_ID: " + AD_Client_ID);

      // * Create Org

      AD_Org_ID = SequenceIdData.getUUID();
      name = orgName;
      if (name == null || name.length() == 0)
        name = "newOrg";
      if (InitialOrgSetupData.insertOrg(conn, this, AD_Client_ID, AD_Org_ID, name, strOrgType) != 1) {
        final String err = "InitialOrgSetup - createOrg - Org NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      final String strTree = InitialOrgSetupData.selectOrgTree(this, vars.getClient());
      InitialOrgSetupData.updateTreeNode(conn, this, strParentOrg, strTree, AD_Org_ID);
      // Info
      if (InitialClientSetupData.updateOrgInfo(conn, this, strcLocationId, AD_Org_ID) != 1) {
        final String err = "InitialOrgSetup - createOrg - Location NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      m_info.append(Utility.messageBD(this, "AD_Org_ID", vars.getLanguage())).append("=").append(
          name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createOrg - m_info: " + m_info.toString());
      // * Load Roles
      // * - Admin
      final String stradRoleId = InitialOrgSetupData.selectAdminRole(conn, this, AD_Client_ID);

      // * Create Users
      // * - Org

      name = userOrg;
      if (name == null || name.length() == 0)
        name = orgName + "Org";
      AD_User_U_ID = SequenceIdData.getUUID();
      AD_User_U_Name = name;
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createOrg - AD_User_U_Name : " + AD_User_U_Name);
      if (InitialOrgSetupData.insertUser(conn, this, AD_Client_ID, AD_User_U_ID, name,
          FormatUtilities.sha1Base64(name), vars.getLanguage()) != 1) {
        final String err = "InitialOrgSetup - createOrg - Org User A NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createOrg - USER INSERTED " + name);
      // Info
      m_info.append(Utility.messageBD(this, "AD_User_ID", vars.getLanguage())).append("=").append(
          name).append("/").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createOrg - m_info: " + m_info.toString());
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      OBContext.getOBContext().addWritableOrganization(AD_Org_ID);
      vars
          .setSessionValue("#USER_ORG", vars.getSessionValue("#USER_ORG") + ", '" + AD_Org_ID + "'");
      vars.setSessionValue("#ORG_CLIENT", vars.getSessionValue("#ORG_CLIENT") + ", '" + AD_Org_ID
          + "'");
      OrgTree tree = new OrgTree(this, AD_Client_ID);
      vars.setSessionObject("#CompleteOrgTree", tree);
      OrgTree accessibleTree = tree.getAccessibleTree(this, vars.getRole());
      vars.setSessionValue("#AccessibleOrgTree", accessibleTree.toString());
      // * Create User-Role

      // OrgUser - User
      if (InitialOrgSetupData.insertUserRoles(conn, this, AD_Client_ID, AD_User_U_ID, stradRoleId) != 1)
        log4j.warn("InitialOrgSetup - createOrg - UserRole OrgUser+Org NOT inserted");
      releaseCommitConnection(conn);
    } catch (final Exception e) {
      m_info.append(e).append(SALTO_LINEA);
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      try {
        conn = this.getTransactionConnection();
      } catch (final Exception ignored) {
      }
      return false;
    }
    m_info.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateOrgSuccess", vars.getLanguage())).append(SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateOrgSuccess", vars.getLanguage())).append(SALTO_LINEA);
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - createOrg - m_info last: " + m_info.toString());
    // Add images
    Organization newOrganization = OBDal.getInstance().get(Organization.class, AD_Org_ID);
    Client organizationClient = OBDal.getInstance().get(Client.class, AD_Client_ID);
    if (organizationClient.getClientInformationList().get(0).getYourCompanyDocumentImage() != null) {
      Image yourCompanyDocumentImage = OBProvider.getInstance().get(Image.class);
      yourCompanyDocumentImage.setClient(organizationClient);
      yourCompanyDocumentImage.setOrganization(newOrganization);
      yourCompanyDocumentImage.setBindaryData(organizationClient.getClientInformationList().get(0)
          .getYourCompanyDocumentImage().getBindaryData());
      yourCompanyDocumentImage.setName(organizationClient.getClientInformationList().get(0)
          .getYourCompanyDocumentImage().getName());
      newOrganization.getOrganizationInformationList().get(0).setYourCompanyDocumentImage(
          yourCompanyDocumentImage);
      yourCompanyDocumentImage.setOrganization(newOrganization);
      OBDal.getInstance().save(yourCompanyDocumentImage);
    }
    OBDal.getInstance().save(newOrganization);
    OBDal.getInstance().flush();
    return true;
  }

  private boolean save(Connection conn, VariablesSecureApp vars, String C_Element_ID,
      AccountingValueData[] data) throws ServletException {
    final String strAccountTree = InitialOrgSetupData.selectTree(this, AD_Client_ID);
    for (int i = 0; i < data.length; i++) {
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - DATA LENGTH : " + data.length + ", POSICION : " + i
            + ", DEFAULT_ACCT: " + data[i].defaultAccount);
      data[i].cElementValueId = SequenceIdData.getUUID();
      final String IsDocControlled = data[i].accountDocument.equals("Yes") ? "Y" : "N";
      final String C_ElementValue_ID = data[i].cElementValueId;
      final String IsSummary = data[i].accountSummary.equals("Yes") ? "Y" : "N";
      String accountType = "";
      String accountSign = "";
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - AccountType debug");
      if (!data[i].accountType.equals("")) {
        final String s = data[i].accountType.toUpperCase().substring(0, 1);
        if (s.equals("A") || s.equals("L") || s.equals("O") || s.equals("E") || s.equals("R")
            || s.equals("M"))
          accountType = s;
        else
          accountType = "E";
        if (log4j.isDebugEnabled())
          log4j.debug("InitialOrgSetup - save - Not is account type");
      } else {
        accountType = "E";
        if (log4j.isDebugEnabled())
          log4j.debug("InitialOrgSetup - save - Is account type");
      }
      if (!data[i].accountSign.equals("")) {
        final String s = data[i].accountSign.toUpperCase().substring(0, 1);
        if (s.equals("D") || s.equals("C"))
          accountSign = s;
        else
          accountSign = "N";
      } else
        accountSign = "N";
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - ACCOUNT VALUE : " + data[i].accountValue
            + " ACCOUNT NAME : " + data[i].accountName + " DEFAULT_ACCOUNT: "
            + data[i].defaultAccount);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - C_ElementValue_ID: " + C_ElementValue_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - C_Element_ID: " + C_Element_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - data[i].accountValue: " + data[i].accountValue);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - data[i].accountName: " + data[i].accountName);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - data[i].accountDescription: "
            + data[i].accountDescription);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - save - accountType: " + accountType);

      if (!data[i].accountValue.equals("")) {
        try {
          if (InitialOrgSetupData.insertElementValue(conn, this, C_ElementValue_ID, C_Element_ID,
              AD_Client_ID, AD_Org_ID, data[i].accountValue, data[i].accountName,
              data[i].accountDescription, accountType, accountSign, IsDocControlled, IsSummary,
              data[i].elementLevel) != 1) {
            log4j.warn("InitialOrgSetup - save - Natural Account not added");
            data[i].cElementValueId = "";
            return false;
          } else {
            final String strParent = InitialOrgSetupData.selectParent(conn, this,
                data[i].accountParent, AD_Client_ID, C_Element_ID);
            InitialOrgSetupData.updateTreeNode(conn, this, (strParent != null && !strParent
                .equals("")) ? strParent : "0", strAccountTree, C_ElementValue_ID, AD_Client_ID);
          }
        } catch (final ServletException e) {
          log4j.warn("InitialOrgSetup - save - Natural Account not added");
          data[i].cElementValueId = "";
          return false;
        }
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - save - NATURAL ACCOUNT ADDED");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - save - m_info last: " + m_info.toString());
    return updateOperands(conn, vars, data, C_Element_ID);
  }// save

  private boolean updateOperands(Connection conn, VariablesSecureApp vars,
      AccountingValueData[] data, String C_Element_ID) throws ServletException {
    boolean OK = true;
    for (int i = 0; i < data.length; i++) {
      final String[][] strOperand = operandProcess(data[i].operands);
      String strSeqNo = "10";
      for (int j = 0; strOperand != null && j < strOperand.length; j++) {
        final String C_ElementValue_Operand_ID = SequenceIdData.getUUID();
        final String strAccount = InitialClientSetupData.selectAccount(conn, this,
            strOperand[j][0], C_Element_ID);
        final String strElementValue = InitialClientSetupData.selectAccount(conn, this,
            data[i].accountValue, C_Element_ID);
        if (strAccount != null && !strAccount.equals("")) {
          InitialClientSetupData.insertOperands(conn, this, C_ElementValue_Operand_ID,
              (strOperand[j][1].equals("+") ? "1" : "-1"), strElementValue, strAccount, strSeqNo,
              AD_Client_ID, vars.getUser());
          strSeqNo = nextSeqNo(strSeqNo);
        } else {
          if (log4j.isDebugEnabled())
            log4j.error("Operand not inserted - Value = " + strOperand[j][0]);
          OK = false;
        }
      }
    }
    return OK;
  }

  private String[][] operandProcess(String strOperand) {
    if (strOperand == null || strOperand.equals(""))
      return null;
    final StringTokenizer st = new StringTokenizer(strOperand, "+-", true);
    final StringTokenizer stNo = new StringTokenizer(strOperand, "+-", false);
    int no = stNo.countTokens();
    final String[][] strResult = new String[no][2];
    no = 0; // Token No
    int i = 0; // Array position
    strResult[0][1] = "+";
    while (st.hasMoreTokens()) {
      if (i % 2 != 1) {
        strResult[no][0] = st.nextToken().trim();
        no++;
      } else
        strResult[no][1] = st.nextToken().trim();
      i++;
    }
    // strResult = filterArray(strResult);
    return strResult;
  } // operandProcess

  private String nextSeqNo(String oldSeqNo) {
    final BigDecimal seqNo = new BigDecimal(oldSeqNo);
    final String SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  private AccountingValueData[] parseData(FieldProvider[] data) throws ServletException {
    AccountingValueData[] result = null;
    final Vector<Object> vec = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      final AccountingValueData dataAux = new AccountingValueData();
      dataAux.accountValue = data[i].getField("accountValue");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountValue: " + dataAux.accountValue);
      dataAux.accountName = data[i].getField("accountName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountName: " + dataAux.accountName);
      dataAux.accountDescription = data[i].getField("accountDescription");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountDescription: " + dataAux.accountDescription);
      dataAux.accountType = data[i].getField("accountType");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountType: " + dataAux.accountType);
      dataAux.accountSign = data[i].getField("accountSign");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountSign: " + dataAux.accountSign);
      dataAux.accountDocument = data[i].getField("accountDocument");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountDocument: " + dataAux.accountDocument);
      dataAux.accountSummary = data[i].getField("accountSummary");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountSummary: " + dataAux.accountSummary);
      dataAux.defaultAccount = data[i].getField("defaultAccount");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.defaultAccount: " + dataAux.defaultAccount);
      dataAux.accountParent = data[i].getField("accountParent");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.accountParent: " + dataAux.accountParent);
      dataAux.elementLevel = data[i].getField("elementLevel");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.elementLevel: " + dataAux.elementLevel);
      dataAux.balanceSheet = data[i].getField("balanceSheet");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.operands: " + dataAux.operands);
      dataAux.operands = data[i].getField("operands");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.balanceSheet: " + dataAux.balanceSheet);
      dataAux.balanceSheetName = data[i].getField("balanceSheetName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.balanceSheetName: " + dataAux.balanceSheetName);
      dataAux.uS1120BalanceSheet = data[i].getField("uS1120BalanceSheet");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.uS1120BalanceSheet: " + dataAux.uS1120BalanceSheet);
      dataAux.uS1120BalanceSheetName = data[i].getField("uS1120BalanceSheetName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.uS1120BalanceSheetName: "
            + dataAux.uS1120BalanceSheetName);
      dataAux.profitAndLoss = data[i].getField("profitAndLoss");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.profitAndLoss: " + dataAux.profitAndLoss);
      dataAux.profitAndLossName = data[i].getField("profitAndLossName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.profitAndLossName: " + dataAux.profitAndLossName);
      dataAux.uS1120IncomeStatement = data[i].getField("uS1120IncomeStatement");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.uS1120IncomeStatement: "
            + dataAux.uS1120IncomeStatement);
      dataAux.uS1120IncomeStatementName = data[i].getField("uS1120IncomeStatementName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.uS1120IncomeStatementName: "
            + dataAux.uS1120IncomeStatementName);
      dataAux.cashFlow = data[i].getField("cashFlow");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.cashFlow: " + dataAux.cashFlow);
      dataAux.cashFlowName = data[i].getField("cashFlowName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.cashFlowName: " + dataAux.cashFlowName);
      dataAux.cElementValueId = data[i].getField("cElementValueId");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - dataAux.cElementValueId: " + dataAux.cElementValueId);
      vec.addElement(dataAux);
    }
    result = new AccountingValueData[vec.size()];
    vec.copyInto(result);
    return result;
  }// parseData

  private String getC_ElementValue_ID(AccountingValueData[] data, String key) {
    if (data == null || data.length == 0)
      return "";
    for (int i = 0; i < data.length; i++)
      if (data[i].defaultAccount.equalsIgnoreCase(key) && !data[i].defaultAccount.equals(""))
        return data[i].cElementValueId;
    return "";
  } // getC_ElementValue_ID

  private boolean createAccounting(VariablesSecureApp vars, String strOrganization,
      String newC_Currency_ID, String curName, boolean hasProduct, boolean hasBPartner,
      boolean hasProject, boolean hasMCampaign, boolean hasSRegion, FieldProvider[] avData)
      throws ServletException {
    //
    C_Currency_ID = newC_Currency_ID;
    m_hasProject = hasProject;
    m_hasMCampaign = hasMCampaign;
    m_hasSRegion = hasSRegion;

    Connection conn = null;
    String name = null;
    String C_Element_ID = null;
    String C_ElementValue_ID = null;
    String GAAP = null;
    String CostingMethod = null;
    AccountingValueData[] data = null;
    try {
      conn = this.getTransactionConnection();

      // Standard variables
      m_info.append(SALTO_LINEA);

      // Create Account Elements
      AD_Tree_Account_ID = InitialOrgSetupData.selectEVTree(conn, this, AD_Client_ID);
      C_Element_ID = SequenceIdData.getUUID();
      name = strOrganization + " " + Utility.messageBD(this, "Account_ID", vars.getLanguage());
      if (InitialOrgSetupData.insertElement(conn, this, AD_Client_ID, AD_Org_ID, C_Element_ID,
          name, AD_Tree_Account_ID) != 1) {
        final String err = "InitialOrgSetup - createAccounting - Acct Element NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createAccounting - ELEMENT INSERTED :" + C_Element_ID);
      m_info.append(Utility.messageBD(this, "C_Element_ID", vars.getLanguage())).append("=")
          .append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createAccounting - m_info last: " + m_info.toString());

      // Create Account Values
      data = parseData(avData);
      final boolean errMsg = save(conn, vars, C_Element_ID, data);
      if (!errMsg) {
        releaseRollbackConnection(conn);
        final String err = "InitialOrgSetup - createAccounting - Acct Element Values NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        return false;
      } else
        m_info.append(Utility.messageBD(this, "C_ElementValue_ID", vars.getLanguage())).append(
            " # ").append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createAccounting - m_info last: " + m_info.toString());

      // * Create AccountingSchema
      C_ElementValue_ID = getC_ElementValue_ID(data, "DEFAULT_ACCT");
      C_AcctSchema_ID = SequenceIdData.getUUID();
      //
      GAAP = "US"; // AD_Reference_ID=123
      CostingMethod = "A"; // AD_Reference_ID=122
      name = strOrganization + " " + GAAP + "/" + CostingMethod + "/" + curName;
      //
      if (InitialOrgSetupData.insertAcctSchema(conn, this, AD_Client_ID, AD_Org_ID,
          C_AcctSchema_ID, name, GAAP, CostingMethod, C_Currency_ID) != 1) {
        final String err = "InitialOrgSetup - createAccounting - AcctSchema NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialOrgSetup - createAccounting - ACCT SCHEMA INSERTED");
      // Info
      m_info.append(Utility.messageBD(this, "C_AcctSchema_ID", vars.getLanguage())).append("=")
          .append(name).append(SALTO_LINEA);
    } catch (final NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (final SQLException ex2) {
      throw new ServletException(ex2.getMessage());
    } catch (final Exception ex3) {
      throw new ServletException(ex3.getMessage());
    }

    // * Create AccountingSchema Elements (Structure)

    FieldProvider[] data1 = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_AcctSchema ElementType", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "InitialOrgSetup"),
          Utility.getContext(this, vars, "#User_Client", "InitialOrgSetup"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "InitialOrgSetup", "");
      data1 = comboTableData.select(false);
      comboTableData = null;
    } catch (final Exception ex) {
      throw new ServletException(ex);
    }

    try {
      for (int i = 0; i < data1.length; i++) {
        final String ElementType = data1[i].getField("id");
        name = data1[i].getField("name");
        //
        String IsMandatory = "";
        String IsBalanced = "N";
        String SeqNo = "";
        String C_AcctSchema_Element_ID = "";

        if (ElementType.equals("OO")) {
          C_AcctSchema_Element_ID = SequenceIdData.getUUID();
          IsMandatory = "Y";
          IsBalanced = "Y";
          SeqNo = "10";
        } else if (ElementType.equals("AC")) {
          C_AcctSchema_Element_ID = SequenceIdData.getUUID();
          IsMandatory = "Y";
          SeqNo = "20";
        } else if (ElementType.equals("PR") && hasProduct) {
          C_AcctSchema_Element_ID = SequenceIdData.getUUID();
          IsMandatory = "N";
          SeqNo = "30";
        } else if (ElementType.equals("BP") && hasBPartner) {
          C_AcctSchema_Element_ID = SequenceIdData.getUUID();
          IsMandatory = "N";
          SeqNo = "40";
        } else if (ElementType.equals("PJ") && hasProject) {
          C_AcctSchema_Element_ID = SequenceIdData.getUUID();
          IsMandatory = "N";
          SeqNo = "50";
        } else if (ElementType.equals("MC") && hasMCampaign) {
          C_AcctSchema_Element_ID = SequenceIdData.getUUID();
          IsMandatory = "N";
          SeqNo = "60";
        } else if (ElementType.equals("SR") && hasSRegion) {
          C_AcctSchema_Element_ID = SequenceIdData.getUUID();
          IsMandatory = "N";
          SeqNo = "70";
        }
        // Not OT, LF, LT, U1, U2, AY
        if (log4j.isDebugEnabled())
          log4j.debug("InitialOrgSetup - createAccounting - C_ElementValue_ID: "
              + C_ElementValue_ID);

        if (!IsMandatory.equals("")) {
          if (InitialOrgSetupData.insertAcctSchemaElement(conn, this, AD_Client_ID, AD_Org_ID,
              C_AcctSchema_Element_ID, C_AcctSchema_ID, ElementType, name, SeqNo, IsMandatory,
              IsBalanced) == 1)
            m_info.append(Utility.messageBD(this, "C_AcctSchema_Element_ID", vars.getLanguage()))
                .append("=").append(name).append(SALTO_LINEA);
          else
            m_info.append(Utility.messageBD(this, "C_AcctSchema_Element_ID", vars.getLanguage()))
                .append("=").append(name).append(" NOT inserted").append(SALTO_LINEA);

          // Default value for mandatory elements: OO and AC
          if (ElementType.equals("OO")) {
            if (InitialOrgSetupData.updateAcctSchemaElement(conn, this, AD_Org_ID,
                C_AcctSchema_Element_ID) != 1) {
              log4j
                  .warn("InitialOrgSetup - createAccounting - Default Org in AcctSchamaElement NOT updated");
              m_info
                  .append(
                      "InitialOrgSetup - createAccounting - Default Org in AcctSchamaElement NOT updated")
                  .append(SALTO_LINEA);
            }
          }
          if (ElementType.equals("AC")) {
            if (InitialOrgSetupData.updateAcctSchemaElement2(conn, this, C_ElementValue_ID,
                C_Element_ID, C_AcctSchema_Element_ID) != 1) {
              log4j
                  .warn("InitialOrgSetup - createAccounting - Default Account in AcctSchamaElement NOT updated");
              m_info
                  .append(
                      "InitialOrgSetup - createAccounting - Default Account in AcctSchamaElement NOT updated")
                  .append(SALTO_LINEA);
            }
          }
        }
      }
    } catch (final Exception e1) {
      log4j.warn("InitialOrgSetup - createAccounting - Elements", e1);
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      throw new ServletException(e1);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - createAccounting - ACCT SCHEMA ELEMENTS INSERTED");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - createAccounting - m_info last: " + m_info.toString());
    try {
      // Create AcctSchema
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();

      m_AcctSchema = new AcctSchema(this, C_AcctSchema_ID);
      if (InitialOrgSetupData.insertAcctSchemaGL(conn, this, AD_Client_ID, AD_Org_ID,
          C_AcctSchema_ID, getAcct(conn, data, "SUSPENSEBALANCING_ACCT"), getAcct(conn, data,
              "SUSPENSEERROR_ACCT"), getAcct(conn, data, "CURRENCYBALANCING_ACCT"), getAcct(conn,
              data, "RETAINEDEARNING_ACCT"), getAcct(conn, data, "INCOMESUMMARY_ACCT"), getAcct(
              conn, data, "INTERCOMPANYDUETO_ACCT"),
          getAcct(conn, data, "INTERCOMPANYDUEFROM_ACCT"), getAcct(conn, data, "PPVOFFSET_ACCT")) != 1) {
        final String err = "InitialOrgSetup - createAccounting - GL Accounts NOT inserted";
        log4j.warn(err);
        m_info.append(err);
        return false;
      }

      final String C_AcctSchema_Default_ID = SequenceIdData.getUUID();
      if (InitialOrgSetupData.insertAcctSchemaDEFAULT(conn, this, AD_Client_ID, AD_Org_ID,
          C_AcctSchema_ID, getAcct(conn, data, "W_INVENTORY_ACCT"), getAcct(conn, data,
              "W_DIFFERENCES_ACCT"), getAcct(conn, data, "W_REVALUATION_ACCT"), getAcct(conn, data,
              "W_INVACTUALADJUST_ACCT"), getAcct(conn, data, "P_REVENUE_ACCT"), getAcct(conn, data,
              "P_EXPENSE_ACCT"), getAcct(conn, data, "P_ASSET_ACCT"), getAcct(conn, data,
              "P_COGS_ACCT"), getAcct(conn, data, "P_PURCHASEPRICEVARIANCE_ACCT"), getAcct(conn,
              data, "P_INVOICEPRICEVARIANCE_ACCT"), getAcct(conn, data, "P_TRADEDISCOUNTREC_ACCT"),
          getAcct(conn, data, "P_TRADEDISCOUNTGRANT_ACCT"),
          getAcct(conn, data, "C_RECEIVABLE_ACCT"), getAcct(conn, data, "C_PREPAYMENT_ACCT"),
          getAcct(conn, data, "V_LIABILITY_ACCT"),
          getAcct(conn, data, "V_LIABILITY_SERVICES_ACCT"),
          getAcct(conn, data, "V_PREPAYMENT_ACCT"), getAcct(conn, data, "PAYDISCOUNT_EXP_ACCT"),
          getAcct(conn, data, "PAYDISCOUNT_REV_ACCT"), getAcct(conn, data, "WRITEOFF_ACCT"),
          getAcct(conn, data, "UNREALIZEDGAIN_ACCT"), getAcct(conn, data, "UNREALIZEDLOSS_ACCT"),
          getAcct(conn, data, "REALIZEDGAIN_ACCT"), getAcct(conn, data, "REALIZEDLOSS_ACCT"),
          getAcct(conn, data, "WITHHOLDING_ACCT"), getAcct(conn, data, "E_PREPAYMENT_ACCT"),
          getAcct(conn, data, "E_EXPENSE_ACCT"), getAcct(conn, data, "PJ_ASSET_ACCT"), getAcct(
              conn, data, "PJ_WIP_ACCT"), getAcct(conn, data, "T_EXPENSE_ACCT"), getAcct(conn,
              data, "T_LIABILITY_ACCT"), getAcct(conn, data, "T_RECEIVABLES_ACCT"), getAcct(conn,
              data, "T_DUE_ACCT"), getAcct(conn, data, "T_CREDIT_ACCT"), getAcct(conn, data,
              "B_INTRANSIT_ACCT"), getAcct(conn, data, "B_ASSET_ACCT"), getAcct(conn, data,
              "B_EXPENSE_ACCT"), getAcct(conn, data, "B_INTERESTREV_ACCT"), getAcct(conn, data,
              "B_INTERESTEXP_ACCT"), getAcct(conn, data, "B_UNIDENTIFIED_ACCT"), getAcct(conn,
              data, "B_SETTLEMENTGAIN_ACCT"), getAcct(conn, data, "B_SETTLEMENTLOSS_ACCT"),
          getAcct(conn, data, "B_REVALUATIONGAIN_ACCT"), getAcct(conn, data,
              "B_REVALUATIONLOSS_ACCT"), getAcct(conn, data, "B_PAYMENTSELECT_ACCT"), getAcct(conn,
              data, "B_UNALLOCATEDCASH_ACCT"), getAcct(conn, data, "CH_EXPENSE_ACCT"), getAcct(
              conn, data, "CH_REVENUE_ACCT"), getAcct(conn, data, "UNEARNEDREVENUE_ACCT"), getAcct(
              conn, data, "NOTINVOICEDRECEIVABLES_ACCT"), getAcct(conn, data,
              "NOTINVOICEDREVENUE_ACCT"), getAcct(conn, data, "NOTINVOICEDRECEIPTS_ACCT"), getAcct(
              conn, data, "CB_ASSET_ACCT"), getAcct(conn, data, "CB_CASHTRANSFER_ACCT"), getAcct(
              conn, data, "CB_DIFFERENCES_ACCT"), getAcct(conn, data, "CB_EXPENSE_ACCT"), getAcct(
              conn, data, "CB_RECEIPT_ACCT"), C_AcctSchema_Default_ID, getAcct(conn, data,
              "A_DEPRECIATION_ACCT"), getAcct(conn, data, "A_ACCUMDEPRECIATION_ACCT"), getAcct(
              conn, data, "A_DISPOSAL_LOSS"), getAcct(conn, data, "A_DISPOSAL_GAIN")) != 1) {
        final String err = "InitialOrgSetup - createAccounting - Default Accounts NOT inserted";
        log4j.warn(err);
        m_info.append(err);
        return false;
      }
      InitialOrgSetupData.insertOrgAcctSchema(conn, this, AD_Client_ID, AD_Org_ID, vars.getUser(),
          C_AcctSchema_ID);
      releaseCommitConnection(conn);
    } catch (final Exception ex) {
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }
      throw new ServletException(ex.getMessage());
    }
    m_info.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateAccountingSuccess", vars.getLanguage())).append(SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateAccountingSuccess", vars.getLanguage())).append(SALTO_LINEA);
    return true;
  } // createAccounting

  /**
   * Returns the error. "" if there is no error
   * 
   * @param vars
   * @param strOrganization
   * @param strClient
   * @param strModules
   * @param hasProduct
   * @param hasBPartner
   * @param hasProject
   * @param hasMCampaign
   * @param hasSRegion
   * @param strCreateAccounting
   * @return the error. "" if there is no error
   */
  private String createReferenceData(VariablesSecureApp vars, String strOrganization,
      String strClient, String strModules, boolean hasProduct, boolean hasBPartner,
      boolean hasProject, boolean hasMCampaign, boolean hasSRegion, String strCreateAccounting)
      throws ServletException, IOException {
    if (strModules != null && !strModules.equals("")) {
      // Remove ( ) characters from the In string as it causes a failure
      if (strModules.charAt(0) == '(')
        strModules = strModules.substring(1, strModules.length());
      if (strModules.charAt(strModules.length() - 1) == ')')
        strModules = strModules.substring(0, strModules.length() - 1);

      // import coa
      if (!strCreateAccounting.equals("")) {
        InitialOrgSetupData[] dataCOA = InitialOrgSetupData.selectCOAModules(this, strModules);
        try {
          ModuleUtiltiy.orderModuleByDependency(dataCOA);
        } catch (Exception e) {
          log4j.error("Error ordering modules", e);
        }

        final DataImportService myData = DataImportService.getInstance();
        for (int i = 0; i < dataCOA.length; i++) {
          final String strPath = vars.getSessionValue("#SOURCEPATH") + "/modules" + dataCOA[i].path;
          final FileInputStream in = new FileInputStream(strPath);
          final AccountingValueData av = new AccountingValueData(vars, in, true, "C");
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "StartingAccounting", vars.getLanguage()))
              .append(SALTO_LINEA);
          if (!createAccounting(vars, strOrganization, C_Currency_ID, InitialOrgSetupData.currency(
              this, C_Currency_ID), hasProduct, hasBPartner, hasProject, hasMCampaign, hasSRegion,
              av.getFieldProvider())) {
            m_info.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
                SALTO_LINEA);
          } else {
            if (!InitialOrgSetupData.existsOrgModule(this, strClient, AD_Org_ID,
                dataCOA[i].adModuleId)) {
              InitialOrgSetupData.insertOrgModule(this, strClient, AD_Org_ID, vars.getUser(),
                  dataCOA[i].adModuleId, dataCOA[i].version);
            }
            m_info.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateReferenceDataSuccess", vars.getLanguage())).append(
                SALTO_LINEA);
            strSummary.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateReferenceDataSuccess", vars.getLanguage())).append(
                SALTO_LINEA);
          }
        }
      }
      // import rd
      InitialOrgSetupData[] data = InitialOrgSetupData.selectRDModules(this, strModules);
      try {
        ModuleUtiltiy.orderModuleByDependency(data);
      } catch (Exception e) {
        log4j.error("Error ordering modules", e);
      }
      if (data != null && data.length != 0) {
        final DataImportService myData = DataImportService.getInstance();
        StringBuffer strError = new StringBuffer("");

        for (int j = 0; j < data.length; j++) {

          final String strPath = vars.getSessionValue("#SOURCEPATH") + "/modules/"
              + data[j].javapackage + "/referencedata/standard";
          File datasetFile = new File(strPath + "/" + Utility.wikifiedName(data[j].datasetname)
              + ".xml");
          if (!datasetFile.exists()) {
            continue;
          }

          final String strXml = Utility.fileToString(datasetFile.getPath());
          final ImportResult myResult = myData.importDataFromXML(OBDal.getInstance().get(
              Client.class, strClient), OBDal.getInstance().get(Organization.class, AD_Org_ID),
              strXml, OBDal.getInstance().get(Module.class, data[j].adModuleId));
          m_info.append(SALTO_LINEA).append("File: ").append(datasetFile.getName()).append(":")
              .append(SALTO_LINEA);
          if (myResult.getLogMessages() != null && !myResult.getLogMessages().equals("")
              && !myResult.getLogMessages().equals("null")) {
            m_info.append(SALTO_LINEA).append("LOG:").append(SALTO_LINEA);
            m_info.append(SALTO_LINEA).append(
                myResult.getLogMessages().replaceAll("\\\n", SALTO_LINEA)).append(SALTO_LINEA);
          }
          if (myResult.getWarningMessages() != null && !myResult.getWarningMessages().equals("")
              && !myResult.getWarningMessages().equals("null")) {
            m_info.append(SALTO_LINEA).append("WARNINGS:").append(SALTO_LINEA);
            m_info.append(SALTO_LINEA).append(
                myResult.getWarningMessages().replaceAll("\\\n", SALTO_LINEA)).append(SALTO_LINEA);
          }
          if (myResult.getErrorMessages() != null && !myResult.getErrorMessages().equals("")
              && !myResult.getErrorMessages().equals("null")) {
            m_info.append(SALTO_LINEA).append("ERRORS:").append(SALTO_LINEA);
            m_info.append(SALTO_LINEA).append(
                myResult.getErrorMessages().replaceAll("\\\n", SALTO_LINEA)).append(SALTO_LINEA);
          }
          if (myResult.getErrorMessages() != null && !myResult.getErrorMessages().equals("")
              && !myResult.getErrorMessages().equals("null"))
            strError = strError.append(myResult.getErrorMessages());

          if (!strError.toString().equals(""))
            return strError.toString();
          else {
            if (!InitialOrgSetupData
                .existsOrgModule(this, strClient, AD_Org_ID, data[j].adModuleId)) {
              InitialOrgSetupData.insertOrgModule(this, strClient, AD_Org_ID, vars.getUser(),
                  data[j].adModuleId, data[j].version);
            }
            m_info.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateReferenceDataSuccess", vars.getLanguage())).append(
                SALTO_LINEA);
            strSummary.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateReferenceDataSuccess", vars.getLanguage())).append(
                SALTO_LINEA);
          }
        }
        return "";
      } else
        return "WrongModules";
    } else
      return "NoModules";
  }

  /**
   * Returns the modules {@link FieldProvider} ordered taking into account dependencies
   * 
   * @param modules
   * @return
   */
  private InitialOrgSetupData[] orderModuleByDependency(InitialOrgSetupData[] modules) {
    if (modules == null || modules.length == 0)
      return null;
    final ArrayList<String> list = new ArrayList<String>();
    for (int i = 0; i < modules.length; i++) {
      list.add(modules[i].adModuleId);
    }
    List<String> orderList = null;
    try {
      orderList = ModuleUtiltiy.orderByDependency(list);
    } catch (Exception e) {
      log4j.error("Error ordering modules", e);
      orderList = list;
    }
    final InitialOrgSetupData[] rt = new InitialOrgSetupData[orderList.size()];
    for (int i = 0; i < orderList.size(); i++) {
      int j = 0;
      while (j < modules.length && !modules[j].adModuleId.equals(orderList.get(i)))
        j++;
      rt[i] = modules[j];
    }
    return rt;
  }

  private String getAcct(Connection conn, AccountingValueData[] data, String key)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - getAcct - " + key);
    final String C_ElementValue_ID = getC_ElementValue_ID(data, key);
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - getAcct - C_ElementValue_ID: " + C_ElementValue_ID);
    final Account vc = Account.getDefault(m_AcctSchema, true);
    vc.Account_ID = C_ElementValue_ID;
    vc.save(conn, this, AD_Client_ID, "");// BEFORE, HERE IT WAS 0
    String C_ValidCombination_ID = vc.C_ValidCombination_ID;
    if (C_ValidCombination_ID.equals("")) {
      log4j.warn("InitialOrgSetup - getAcct - C_ElementValue_ID: " + C_ElementValue_ID);
      log4j.warn("InitialOrgSetup - getAcct - no account for " + key);
      C_ValidCombination_ID = "";// HERE IT WAS 0
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - getAcct - " + key + "-- valid combination:"
          + C_ValidCombination_ID);
    return C_ValidCombination_ID;
  }

}
