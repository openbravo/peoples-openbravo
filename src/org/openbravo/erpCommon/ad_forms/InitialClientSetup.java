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
package org.openbravo.erpCommon.ad_forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.modules.ModuleReferenceDataClientTree;
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
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class InitialClientSetup extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final String SALTO_LINEA = "<br>\n";
  private String C_Currency_ID = "";
  private String clientName = "";
  private String AD_User_ID = "";
  private String AD_User_Name = "";
  private String AD_User_U_Name = "";
  private String AD_User_U_ID = "";
  private String AD_Client_ID = "";
  private String C_AcctSchema_ID = "";
  private String client = "1000000";
  private String strError = "";
  private String C_Calendar_ID = null;
  private StringBuffer strSummary = new StringBuffer();
  private AcctSchema m_AcctSchema;
  private boolean m_hasProject;
  private boolean m_hasMCampaign;
  private boolean m_hasSRegion;
  private boolean isOK = true;
  private String AD_Tree_Org_ID = "", AD_Tree_BPartner_ID = "", AD_Tree_Project_ID = "",
      AD_Tree_SalesRegion_ID = "", AD_Tree_Product_ID = "", AD_Tree_Account_ID = "";
  private static StringBuffer m_info = new StringBuffer();
  private static StringBuffer m_infoOperands = new StringBuffer();
  private final String CompiereSys = "N"; // Should NOT be changed

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("OK")) {
      String strModules = vars.getInStringParameter("inpNodes", IsIDFilter.instance);
      m_info.delete(0, m_info.length());
      String strResultado = process(request, response, vars, strModules);
      log4j.debug("InitialClientSetup - after processFile");
      printPageResultado(response, vars, strResultado);
    } else if (vars.commandIn("CANCEL")) {
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    ModuleReferenceDataClientTree tree = new ModuleReferenceDataClientTree(this, true);
    XmlDocument xmlDocument = null;
    String[] discard = { "selEliminar" };
    if (tree.getData() == null || tree.getData().length == 0)
      xmlDocument = xmlEngine
          .readXmlTemplate("org/openbravo/erpCommon/ad_forms/InitialClientSetup")
          .createXmlDocument();
    else
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/InitialClientSetup", discard).createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialClientSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialClientSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialClientSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialClientSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      vars.removeMessage("InitialClientSetup");
      OBError myMessage = vars.getMessage("InitialClientSetup");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
      xmlDocument.setParameter("moduleTree", tree.toHtml());
      xmlDocument.setParameter("moduleTreeDescription", tree.descriptionToHtml());

      xmlDocument.setData("reportCurrency", "liststructure", MonedaComboData.select(this));
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void printPageResultado(HttpServletResponse response, VariablesSecureApp vars,
      String strResultado) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/Resultado").createXmlDocument();

    xmlDocument.setParameter("resultado", strResultado);

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialClientSetup", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.InitialClientSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialClientSetup.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialClientSetup.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    OBError myMessage = new OBError();
    myMessage.setTitle("");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - before setMessage");
    if (strError != null && !strError.equals("")) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), strError);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - isOK: " + isOK);
    if (isOK)
      myMessage.setType("Success");
    else
      myMessage.setType("Error");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - Message Type: " + myMessage.getType());
    vars.setMessage("InitialClientSetup", myMessage);
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - after setMessage");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String process(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strModules) throws IOException {
    if (log4j.isDebugEnabled())
      log4j.debug("InitialOrgSetup - strModules - " + strModules);
    Connection conn = null;
    isOK = true;
    strSummary = new StringBuffer();
    strSummary.append(Utility.messageBD(this, "ReportSummary", vars.getLanguage())).append(
        SALTO_LINEA);
    String strClient = vars.getStringParameter("inpClient");
    String strClientUser = vars.getStringParameter("inpClientUser");
    String strCurrency = vars.getStringParameter("inpCurrency");
    C_Currency_ID = strCurrency;
    boolean bProduct = isTrue(vars.getStringParameter("inpProduct"));
    boolean bBPartner = isTrue(vars.getStringParameter("inpBPartner"));
    boolean bProject = isTrue(vars.getStringParameter("inpProject"));
    boolean bCampaign = isTrue(vars.getStringParameter("inpCampaign"));
    boolean bSalesRegion = isTrue(vars.getStringParameter("inpSalesRegion"));
    boolean bIsSystemInstalation = isTrue(vars.getStringParameter("inpSystem"));
    boolean bCreateAccounting = isTrue(vars.getStringParameter("inpCreateAccounting"));
    if (bIsSystemInstalation)
      client = vars.getClient();
    try {
      conn = this.getTransactionConnection();
      if (InitialClientSetupData.updateClient2(conn, this, strClient) != 0) {
        m_info.append("Duplicate Client name").append(SALTO_LINEA);
        strError = Utility.messageBD(this, "Duplicate client name", vars.getLanguage());
        releaseRollbackConnection(conn);
        isOK = false;
        return m_info.toString();
      } else if (InitialClientSetupData.updateUser2(conn, this, strClientUser) != 0) {
        m_info.append("Duplicate UserClient").append(SALTO_LINEA);
        strError = Utility.messageBD(this, "Duplicate Client Username", vars.getLanguage());
        isOK = false;
        releaseRollbackConnection(conn);
        return m_info.toString();
      }
      releaseCommitConnection(conn);
    } catch (Exception err) {
      log4j.warn(err);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
    }
    try {
      m_info.append(SALTO_LINEA).append("*****************************************************")
          .append(SALTO_LINEA);
      m_info.append(SALTO_LINEA).append(
          Utility.messageBD(this, "StartingClient", vars.getLanguage())).append(SALTO_LINEA);
      conn = this.getTransactionConnection();
      if (!createClient(conn, vars, strClient, strClientUser)) {
        releaseRollbackConnection(conn);
        m_info.append(SALTO_LINEA).append(
            Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
        strSummary.append(SALTO_LINEA).append(
            Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
        isOK = false;
        return m_info.toString();
      }
    } catch (Exception err) {
      m_info.append(SALTO_LINEA).append(
          Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
      strSummary.append(SALTO_LINEA).append(
          Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
      strError = err.toString();
      strError = strError.substring((strError.lastIndexOf("@ORA-") > 0 ? strError
          .lastIndexOf("@ORA-") : 0), strError.length());
      isOK = false;
      log4j.warn(err);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
    }
    m_info.append(SALTO_LINEA).append("*****************************************************")
        .append(SALTO_LINEA);
    if (bCreateAccounting == false) {
      m_info.append(SALTO_LINEA).append(
          Utility.messageBD(this, "SkippingAccounting", vars.getLanguage())).append(SALTO_LINEA);
    } else {
      AccountingValueData av = new AccountingValueData(vars, "inpFile", true, "C");
      FieldProvider[] avData = (av != null) ? av.getFieldProvider() : null;
      if (avData != null && avData.length != 0) {
        try {
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "StartingAccounting", vars.getLanguage()))
              .append(SALTO_LINEA);
          conn = this.getTransactionConnection();
          if (!createAccounting(conn, vars, strCurrency, InitialClientSetupData.currency(this,
              strCurrency), bProduct, bBPartner, bProject, bCampaign, bSalesRegion, avData)) {
            releaseRollbackConnection(conn);
            m_info.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
                SALTO_LINEA);
            m_info.append(SALTO_LINEA).append(m_infoOperands).append(SALTO_LINEA);
            strSummary.append(SALTO_LINEA).append(
                Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
                SALTO_LINEA);
            isOK = false;
            return m_info.toString();
          }
        } catch (Exception err) {
          log4j.warn(err);
          m_info.append(m_infoOperands).append(SALTO_LINEA);
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          strSummary.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          strError = err.toString();
          strError = strError.substring((strError.lastIndexOf("@ORA-") > 0 ? strError
              .lastIndexOf("@ORA-") : 0), strError.length());
          log4j.debug("InitialClientSetup - after strError: " + strError);
          isOK = false;
          try {
            releaseRollbackConnection(conn);
          } catch (Exception ignored) {
          }
        }
      }
    }
    try {
      m_info.append(SALTO_LINEA).append("*****************************************************")
          .append(SALTO_LINEA);
      m_info.append(SALTO_LINEA).append(
          Utility.messageBD(this, "StartingDocumentTypes", vars.getLanguage())).append(SALTO_LINEA);
      if (!createDocumentTypes(vars)) {
        releaseRollbackConnection(conn);
        m_info.append(SALTO_LINEA).append(
            Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(
            SALTO_LINEA);
        strSummary.append(SALTO_LINEA).append(
            Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(
            SALTO_LINEA);
        isOK = false;
        return m_info.toString();
      }
    } catch (Exception err) {
      log4j.warn(err);
      m_info.append(SALTO_LINEA).append(
          Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(
          SALTO_LINEA);
      strSummary.append(SALTO_LINEA).append(
          Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(
          SALTO_LINEA);
      strError = err.toString();
      strError = strError.substring((strError.lastIndexOf("@ORA-") > 0 ? strError
          .lastIndexOf("@ORA-") : 0), strError.length());
      log4j.debug("InitialClientSetup - after strError: " + strError);
      isOK = false;
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
    }

    // ==============================================================================================
    if (!strModules.equals("")) {
      try {
        m_info.append(SALTO_LINEA).append("*****************************************************")
            .append(SALTO_LINEA);
        m_info.append(SALTO_LINEA).append(
            Utility.messageBD(this, "StartingReferenceData", vars.getLanguage())).append(
            SALTO_LINEA);
        conn = this.getTransactionConnection();
        String strReferenceData = createReferenceData(conn, vars, AD_Client_ID, strModules,
            strCurrency, bProduct, bBPartner, bProject, bCampaign, bSalesRegion, bCreateAccounting);
        if (strReferenceData != null && !strReferenceData.equals("")) {
          releaseRollbackConnection(conn);
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateReferenceDataFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          m_info.append(SALTO_LINEA).append(
              Utility.messageBD(this, strReferenceData, vars.getLanguage())).append(SALTO_LINEA);
          strSummary.append(SALTO_LINEA).append(
              Utility.messageBD(this, "CreateReferenceDataFailed", vars.getLanguage())).append(
              SALTO_LINEA);
          strSummary.append(SALTO_LINEA).append(
              Utility.messageBD(this, strReferenceData, vars.getLanguage())).append(SALTO_LINEA);
          isOK = false;
          strError = strReferenceData;
          return m_info.toString();
        }
      } catch (Exception err) {
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
        log4j.debug("InitialClientSetup - after strError: " + strError);
        isOK = false;
        try {
          releaseRollbackConnection(conn);
        } catch (Exception ignored) {
        }
      }
    }

    // ==============================================================================================

    log4j.debug("InitialClientSetup - after createEntities");
    if (isOK)
      strError = Utility.messageBD(this, "Success", vars.getLanguage());
    log4j.debug("InitialClientSetup - after strError");
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

  private boolean createClient(Connection conn, VariablesSecureApp vars, String m_ClientName,
      String userClient) throws ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - createClient");
    clientName = m_ClientName;
    if (clientName.equals(""))
      clientName = "newClient";

    try {
      // info header
      m_info.append(SALTO_LINEA);
      // Standard columns
      String name = null;

      // * Create Client

      vars.setSessionValue("#CompiereSys", CompiereSys);
      AD_Client_ID = SequenceIdData.getUUID();
      vars.setSessionValue("AD_Client_ID", AD_Client_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - CLIENT_ID: " + AD_Client_ID);

      if (InitialClientSetupData.insertClient(conn, this, AD_Client_ID, clientName, C_Currency_ID) != 1) {
        String err = "InitialClientSetup - createClient - Client NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      // Info - Client
      m_info.append(Utility.messageBD(this, "AD_Client_ID", vars.getLanguage())).append("=")
          .append(clientName).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - M_INFO: " + m_info.toString());

      // * Create Trees
      // Get TreeTypes & Name
      FieldProvider[] data = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "AD_TreeType Type", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "InitialClientSetup"), Utility.getContext(this, vars, "#User_Client",
                "InitialClientSetup"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "InitialClientSetup", "");
        data = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - LIST COUNT: " + data.length);

      // Tree IDs

      try {
        int i = 0;
        while (i < data.length) {
          String value = data[i].getField("id");
          String AD_Tree_ID = "0";
          if (value.equals("OO")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            AD_Tree_Org_ID = AD_Tree_ID;
          } else if (value.equals("BP")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            AD_Tree_BPartner_ID = AD_Tree_ID;
          } else if (value.equals("PJ")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            AD_Tree_Project_ID = AD_Tree_ID;
          } else if (value.equals("SR")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            AD_Tree_SalesRegion_ID = AD_Tree_ID;
          } else if (value.equals("PR")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            AD_Tree_Product_ID = AD_Tree_ID;
          } else if (value.endsWith("EV")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            AD_Tree_Account_ID = AD_Tree_ID;
          } else if (value.endsWith("TR")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            // Not added to clientinfo
          } else if (value.endsWith("AR")) {
            AD_Tree_ID = SequenceIdData.getUUID();
            // Not added to clientinfo
          } else if (!value.equals("MM")) { // No Menu
            AD_Tree_ID = SequenceIdData.getUUID();
          }
          //
          if (!AD_Tree_ID.equals("0")) {
            name = clientName + " " + data[i].getField("name");
            if (InitialClientSetupData
                .insertTree(conn, this, AD_Client_ID, AD_Tree_ID, name, value) == 1) {
              m_info.append(Utility.messageBD(this, "AD_Client_ID", vars.getLanguage()))
                  .append("=").append(name).append(SALTO_LINEA);
            } else
              log4j.warn("InitialClientSetup - createClient - Tree NOT created: " + name);
          }
          if (log4j.isDebugEnabled())
            log4j.debug("InitialClientSetup - createClient - VALUE " + i + ": " + value
                + " ,AD_Tree_ID: " + AD_Tree_ID);
          i++;
        }
      } catch (ServletException e1) {
        log4j.warn("InitialClientSetup - createClient - Trees");
      }
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      // Get Primary Tree
      String AD_Tree_Menu_ID = "10"; // hardcoded
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - AD_Client_ID: " + AD_Client_ID
            + ", AD_Tree_Menu_ID: " + AD_Tree_Menu_ID + ", AD_Tree_Org_ID: " + AD_Tree_Org_ID
            + ", AD_Tree_BPartner_ID: " + AD_Tree_BPartner_ID + ", AD_Tree_Project_ID: "
            + AD_Tree_Project_ID + ", AD_Tree_SalesRegion_ID: " + AD_Tree_SalesRegion_ID
            + ", AD_Tree_Product_ID: " + AD_Tree_Product_ID);
      if (InitialClientSetupData.insertClientInfo(conn, this, AD_Client_ID, AD_Tree_Menu_ID,
          AD_Tree_Org_ID, AD_Tree_BPartner_ID, AD_Tree_Project_ID, AD_Tree_SalesRegion_ID,
          AD_Tree_Product_ID) != 1) {
        String err = "InitialClientSetup - createClient - ClientInfo NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - CLIENT INFO CREATED");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());
      // Add images
      Client newClient = OBDal.getInstance().get(Client.class, AD_Client_ID);
      SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");
      if (sys.getYourCompanyBigImage() != null) {
        Image yourCompanyBigImage = OBProvider.getInstance().get(Image.class);
        yourCompanyBigImage.setClient(newClient);
        yourCompanyBigImage.setBindaryData(sys.getYourCompanyBigImage().getBindaryData());
        yourCompanyBigImage.setName(sys.getYourCompanyBigImage().getName());
        newClient.getClientInformationList().get(0).setYourCompanyBigImage(yourCompanyBigImage);
        OBDal.getInstance().save(yourCompanyBigImage);
      }

      if (sys.getYourCompanyDocumentImage() != null) {
        Image yourCompanyDocumentImage = OBProvider.getInstance().get(Image.class);
        yourCompanyDocumentImage.setClient(newClient);
        yourCompanyDocumentImage.setBindaryData(sys.getYourCompanyDocumentImage().getBindaryData());
        yourCompanyDocumentImage.setName(sys.getYourCompanyBigImage().getName());
        newClient.getClientInformationList().get(0).setYourCompanyDocumentImage(
            yourCompanyDocumentImage);
        OBDal.getInstance().save(yourCompanyDocumentImage);
      }

      if (sys.getYourCompanyMenuImage() != null) {
        Image yourCompanyMenuImage = OBProvider.getInstance().get(Image.class);
        yourCompanyMenuImage.setClient(newClient);
        yourCompanyMenuImage.setBindaryData(sys.getYourCompanyMenuImage().getBindaryData());
        yourCompanyMenuImage.setName(sys.getYourCompanyMenuImage().getName());
        newClient.getClientInformationList().get(0).setYourCompanyMenuImage(yourCompanyMenuImage);
        OBDal.getInstance().save(yourCompanyMenuImage);
      }

      OBDal.getInstance().save(newClient);
      OBDal.getInstance().flush();

      // * Create Roles
      // * - Admin
      // * - User

      name = clientName + " Admin";
      String AD_Role_ID = SequenceIdData.getUUID();
      if (InitialClientSetupData.insertRole(conn, this, AD_Client_ID, AD_Role_ID, name, "0") != 1) {
        String err = "InitialClientSetup - createClient - Admin Role A NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - ROLE CREATED");
      // OrgAccess x,0
      if (InitialClientSetupData.insertRoleOrgAccess(conn, this, AD_Client_ID, "0", AD_Role_ID) != 1)
        log4j.warn("InitialClientSetup - createClient - Admin Role_OrgAccess 0 NOT created");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - ROLE ORG ACCESS CREATED");
      // Info - Admin Role
      m_info.append(Utility.messageBD(this, "AD_Role_ID", vars.getLanguage())).append("=").append(
          name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());

      // Info - Client Role
      m_info.append(Utility.messageBD(this, "AD_Role_ID", vars.getLanguage())).append("=").append(
          name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());

      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();

      // * Create Users
      // * - Client
      // * - Org

      name = userClient;
      if (name == null || name.length() == 0)
        name = clientName + "Client";
      AD_User_ID = SequenceIdData.getUUID();
      AD_User_Name = name;
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - AD_User_Name : " + AD_User_Name);
      if (InitialClientSetupData.insertUser(conn, this, AD_Client_ID, AD_User_ID, name,
          FormatUtilities.sha1Base64(name), vars.getLanguage(), AD_Role_ID) != 1) {
        String err = "InitialClientSetup - createClient - Admin User A NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - USER INSERTED " + name);
      // Info
      m_info.append(Utility.messageBD(this, "AD_User_ID", vars.getLanguage())).append("=").append(
          name).append("/").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());

      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();

      // * Create User-Role

      // ClientUser - Admin & User
      if (InitialClientSetupData.insertUserRoles(conn, this, AD_Client_ID, AD_User_ID, AD_Role_ID) != 1)
        log4j.warn("InitialClientSetup - createClient - UserRole ClientUser+Admin NOT inserted");
      // SuperUser(100) - Admin & User
      if (InitialClientSetupData.insertUserRoles(conn, this, AD_Client_ID, "100", AD_Role_ID) != 1)
        log4j.warn("InitialClientSetup - createClient - UserRole SuperUser+Admin NOT inserted");
      releaseCommitConnection(conn);
    } catch (Exception e) {
      m_info.append(e).append(SALTO_LINEA);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      try {
        conn = this.getTransactionConnection();
      } catch (Exception ignored) {
      }
      return false;
    }
    m_info.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateClientSuccess", vars.getLanguage())).append(SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateClientSuccess", vars.getLanguage())).append(SALTO_LINEA);
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - createClient - m_info last: " + m_info.toString());
    return true;
  }

  private boolean save(Connection conn, VariablesSecureApp vars, String AD_Client_ID,
      String C_Element_ID, AccountingValueData[] data) throws ServletException {
    String strAccountTree = InitialClientSetupData.selectTree(this, AD_Client_ID);
    for (int i = 0; i < data.length; i++) {
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - DATA LENGTH : " + data.length + ", POSICION : "
            + i + ", DEFAULT_ACCT: " + data[i].defaultAccount);
      data[i].cElementValueId = SequenceIdData.getUUID();
      String IsDocControlled = data[i].accountDocument.equals("Yes") ? "Y" : "N";
      String C_ElementValue_ID = data[i].cElementValueId;
      String IsSummary = data[i].accountSummary.equals("Yes") ? "Y" : "N";
      String accountType = "";
      String accountSign = "";
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - AccountType debug");
      if (!data[i].accountType.equals("")) {
        String s = data[i].accountType.toUpperCase().substring(0, 1);
        if (s.equals("A") || s.equals("L") || s.equals("O") || s.equals("E") || s.equals("R")
            || s.equals("M"))
          accountType = s;
        else
          accountType = "E";
        if (log4j.isDebugEnabled())
          log4j.debug("InitialClientSetup - save - Not is account type");
      } else {
        accountType = "E";
        if (log4j.isDebugEnabled())
          log4j.debug("InitialClientSetup - save - Is account type");
      }
      if (!data[i].accountSign.equals("")) {
        String s = data[i].accountSign.toUpperCase().substring(0, 1);
        if (s.equals("D") || s.equals("C"))
          accountSign = s;
        else
          accountSign = "N";
      } else
        accountSign = "N";
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - ACCOUNT VALUE : " + data[i].accountValue
            + " ACCOUNT NAME : " + data[i].accountName + " DEFAULT_ACCOUNT: "
            + data[i].defaultAccount);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - C_ElementValue_ID: " + C_ElementValue_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - C_Element_ID: " + C_Element_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - data[i].accountValue: " + data[i].accountValue);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - data[i].accountName: " + data[i].accountName);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - data[i].accountDescription: "
            + data[i].accountDescription);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - save - accountType: " + accountType);

      if (!data[i].accountValue.equals("")) {
        try {
          if (InitialClientSetupData.insertElementValue(conn, this, C_ElementValue_ID,
              C_Element_ID, AD_Client_ID, "0", data[i].accountValue, data[i].accountName,
              data[i].accountDescription, accountType, accountSign, IsDocControlled, IsSummary,
              data[i].elementLevel) != 1) {
            log4j.warn("InitialClientSetup - save - Natural Account not added");
            data[i].cElementValueId = "";
            return false;
          } else {
            String strParent = InitialClientSetupData.selectParent(conn, this,
                data[i].accountParent, AD_Client_ID);
            if (strParent != null && !strParent.equals(""))
              InitialClientSetupData.updateTreeNode(conn, this, strParent, strAccountTree,
                  C_ElementValue_ID, AD_Client_ID);
          }
        } catch (ServletException e) {
          log4j.warn("InitialClientSetup - save - Natural Account not added");
          data[i].cElementValueId = "";
          return false;
        }
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - save - NATURAL ACCOUNT ADDED");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - save - m_info last: " + m_info.toString());
    return updateOperands(conn, vars, AD_Client_ID, data, C_Element_ID);
  }// save

  private boolean updateOperands(Connection conn, VariablesSecureApp vars, String AD_Client_ID,
      AccountingValueData[] data, String C_Element_ID) throws ServletException {
    boolean OK = true;
    for (int i = 0; i < data.length; i++) {
      String[][] strOperand = operandProcess(data[i].operands);
      String strSeqNo = "10";
      for (int j = 0; strOperand != null && j < strOperand.length; j++) {
        String C_ElementValue_Operand_ID = SequenceIdData.getUUID();
        String strAccount = InitialClientSetupData.selectAccount(conn, this, strOperand[j][0],
            C_Element_ID);
        String strElementValue = InitialClientSetupData.selectAccount(conn, this,
            data[i].accountValue, C_Element_ID);
        if (strAccount != null && !strAccount.equals("")) {
          log4j.info("Operand - Value = " + strOperand[j][0]);
          InitialClientSetupData.insertOperands(conn, this, C_ElementValue_Operand_ID,
              (strOperand[j][1].equals("+") ? "1" : "-1"), strElementValue, strAccount, strSeqNo,
              AD_Client_ID, vars.getUser());
          strSeqNo = nextSeqNo(strSeqNo);
        } else {
          m_infoOperands.append("Operand not inserted: Account = ").append(data[i].accountValue)
              .append(" - Operand = ").append(strOperand[j][0]);
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
    StringTokenizer st = new StringTokenizer(strOperand, "+-", true);
    StringTokenizer stNo = new StringTokenizer(strOperand, "+-", false);
    int no = stNo.countTokens();
    String[][] strResult = new String[no][2];
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
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    String SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  private AccountingValueData[] parseData(FieldProvider[] data) throws ServletException {
    AccountingValueData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      AccountingValueData dataAux = new AccountingValueData();
      dataAux.accountValue = data[i].getField("accountValue");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountValue: " + dataAux.accountValue);
      dataAux.accountName = data[i].getField("accountName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountName: " + dataAux.accountName);
      dataAux.accountDescription = data[i].getField("accountDescription");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountDescription: "
            + dataAux.accountDescription);
      dataAux.accountType = data[i].getField("accountType");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountType: " + dataAux.accountType);
      dataAux.accountSign = data[i].getField("accountSign");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountSign: " + dataAux.accountSign);
      dataAux.accountDocument = data[i].getField("accountDocument");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountDocument: " + dataAux.accountDocument);
      dataAux.accountSummary = data[i].getField("accountSummary");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountSummary: " + dataAux.accountSummary);
      dataAux.defaultAccount = data[i].getField("defaultAccount");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.defaultAccount: " + dataAux.defaultAccount);
      dataAux.accountParent = data[i].getField("accountParent");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.accountParent: " + dataAux.accountParent);
      dataAux.elementLevel = data[i].getField("elementLevel");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.elementLevel: " + dataAux.elementLevel);
      dataAux.balanceSheet = data[i].getField("balanceSheet");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.operands: " + dataAux.operands);
      dataAux.operands = data[i].getField("operands");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.balanceSheet: " + dataAux.balanceSheet);
      dataAux.balanceSheetName = data[i].getField("balanceSheetName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.balanceSheetName: " + dataAux.balanceSheetName);
      dataAux.uS1120BalanceSheet = data[i].getField("uS1120BalanceSheet");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.uS1120BalanceSheet: "
            + dataAux.uS1120BalanceSheet);
      dataAux.uS1120BalanceSheetName = data[i].getField("uS1120BalanceSheetName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.uS1120BalanceSheetName: "
            + dataAux.uS1120BalanceSheetName);
      dataAux.profitAndLoss = data[i].getField("profitAndLoss");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.profitAndLoss: " + dataAux.profitAndLoss);
      dataAux.profitAndLossName = data[i].getField("profitAndLossName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.profitAndLossName: " + dataAux.profitAndLossName);
      dataAux.uS1120IncomeStatement = data[i].getField("uS1120IncomeStatement");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.uS1120IncomeStatement: "
            + dataAux.uS1120IncomeStatement);
      dataAux.uS1120IncomeStatementName = data[i].getField("uS1120IncomeStatementName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.uS1120IncomeStatementName: "
            + dataAux.uS1120IncomeStatementName);
      dataAux.cashFlow = data[i].getField("cashFlow");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.cashFlow: " + dataAux.cashFlow);
      dataAux.cashFlowName = data[i].getField("cashFlowName");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.cashFlowName: " + dataAux.cashFlowName);
      dataAux.cElementValueId = data[i].getField("cElementValueId");
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - dataAux.cElementValueId: " + dataAux.cElementValueId);
      vec.addElement(dataAux);
    }
    result = new AccountingValueData[vec.size()];
    vec.copyInto(result);
    return result;
  }// parseData

  private String getC_ElementValue_ID(AccountingValueData[] data, String key) {
    if (data == null || data.length == 0)
      return "";
    for (int i = 0; i < data.length; i++) {
      if (data[i].defaultAccount.equalsIgnoreCase("INCOMESUMMARY_ACCT") && log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - getC_ElementValue_ID: " + data[i].defaultAccount
            + " - KEY: " + key);
      if (data[i].defaultAccount.equalsIgnoreCase(key) && !data[i].defaultAccount.equals(""))
        return data[i].cElementValueId;
    }
    return "";
  } // getC_ElementValue_ID

  private boolean createAccounting(Connection conn, VariablesSecureApp vars,
      String newC_Currency_ID, String curName, boolean hasProduct, boolean hasBPartner,
      boolean hasProject, boolean hasMCampaign, boolean hasSRegion, FieldProvider[] avData)
      throws ServletException {
    //
    C_Currency_ID = newC_Currency_ID;
    m_hasProject = hasProject;
    m_hasMCampaign = hasMCampaign;
    m_hasSRegion = hasSRegion;

    String name = null;
    String C_Year_ID = null;
    String C_Element_ID = null;
    String C_ElementValue_ID = null;
    String GAAP = null;
    String CostingMethod = null;
    AccountingValueData[] data = null;
    try {

      // Standard variables
      m_info.append(SALTO_LINEA);

      // * Create Calendar

      C_Calendar_ID = SequenceIdData.getUUID();
      name = clientName + " " + Utility.messageBD(this, "C_Calendar_ID", vars.getLanguage());
      if (InitialClientSetupData.insertCalendar(conn, this, AD_Client_ID, C_Calendar_ID, name) != 1) {
        String err = "InitialClientSetup - createAccounting - Calendar NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createAccounting - CALENDAR INSERTED");
      // Info
      m_info.append(Utility.messageBD(this, "C_Calendar_ID", vars.getLanguage())).append("=")
          .append(name).append(SALTO_LINEA);

      // Year
      C_Year_ID = SequenceIdData.getUUID();
      if (InitialClientSetupData.insertYear(conn, this, C_Year_ID, AD_Client_ID, C_Calendar_ID) != 1)
        log4j.warn("InitialClientSetup - createAccounting - Year NOT inserted");
      // @todo Create Periods
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createAccounting - YEAR INSERTED");
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();

      // Create Account Elements
      C_Element_ID = SequenceIdData.getUUID();
      name = clientName + " " + Utility.messageBD(this, "Account_ID", vars.getLanguage());
      if (InitialClientSetupData.insertElement(conn, this, AD_Client_ID, C_Element_ID, name,
          AD_Tree_Account_ID) != 1) {
        String err = "InitialClientSetup - createAccounting - Acct Element NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createAccounting - ELEMENT INSERTED :" + C_Element_ID);
      m_info.append(Utility.messageBD(this, "C_Element_ID", vars.getLanguage())).append("=")
          .append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createAccounting - m_info last: " + m_info.toString());

      // Create Account Values
      data = parseData(avData);
      boolean errMsg = save(conn, vars, AD_Client_ID, C_Element_ID, data);
      if (!errMsg) {
        releaseRollbackConnection(conn);
        String err = "InitialClientSetup - createAccounting - Acct Element Values or operands NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        return false;
      } else
        m_info.append(Utility.messageBD(this, "C_ElementValue_ID", vars.getLanguage())).append(
            " # ").append(SALTO_LINEA);
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createAccounting - m_info last: " + m_info.toString());

      // * Create AccountingSchema
      C_ElementValue_ID = getC_ElementValue_ID(data, "DEFAULT_ACCT");
      C_AcctSchema_ID = SequenceIdData.getUUID();
      //
      GAAP = "US"; // AD_Reference_ID=123
      CostingMethod = "A"; // AD_Reference_ID=122
      name = clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
      //
      if (InitialClientSetupData.insertAcctSchema(conn, this, AD_Client_ID, C_AcctSchema_ID, name,
          GAAP, CostingMethod, C_Currency_ID) != 1) {
        String err = "InitialClientSetup - createAccounting - AcctSchema NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createAccounting - ACCT SCHEMA INSERTED");
      // Info
      m_info.append(Utility.messageBD(this, "C_AcctSchema_ID", vars.getLanguage())).append("=")
          .append(name).append(SALTO_LINEA);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      throw new ServletException(ex2.getMessage());
    } catch (Exception ex3) {
      throw new ServletException(ex3.getMessage());
    }

    // * Create AccountingSchema Elements (Structure)

    FieldProvider[] data1 = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_AcctSchema ElementType", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "InitialClientSetup"), Utility.getContext(this, vars, "#User_Client",
              "InitialClientSetup"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "InitialClientSetup", "");
      data1 = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      for (int i = 0; i < data1.length; i++) {
        String ElementType = data1[i].getField("id");
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
          log4j.debug("InitialClientSetup - createAccounting - C_ElementValue_ID: "
              + C_ElementValue_ID);

        if (!IsMandatory.equals("")) {
          if (InitialClientSetupData.insertAcctSchemaElement(conn, this, AD_Client_ID,
              C_AcctSchema_Element_ID, C_AcctSchema_ID, ElementType, name, SeqNo, IsMandatory,
              IsBalanced) == 1)
            m_info.append(Utility.messageBD(this, "C_AcctSchema_Element_ID", vars.getLanguage()))
                .append("=").append(name).append(SALTO_LINEA);
          else
            m_info.append(Utility.messageBD(this, "C_AcctSchema_Element_ID", vars.getLanguage()))
                .append("=").append(name).append(" NOT inserted").append(SALTO_LINEA);

          // Default value for mandatory elements: OO and AC
          if (ElementType.equals("OO")) {
            if (InitialClientSetupData.updateAcctSchemaElement(conn, this, "0",
                C_AcctSchema_Element_ID) != 1) {
              log4j
                  .warn("InitialClientSetup - createAccounting - Default Org in AcctSchamaElement NOT updated");
              m_info
                  .append(
                      "InitialClientSetup - createAccounting - Default Org in AcctSchamaElement NOT updated")
                  .append(SALTO_LINEA);
            }
          }
          if (ElementType.equals("AC")) {
            if (InitialClientSetupData.updateAcctSchemaElement2(conn, this, C_ElementValue_ID,
                C_Element_ID, C_AcctSchema_Element_ID) != 1) {
              log4j
                  .warn("InitialClientSetup - createAccounting - Default Account in AcctSchamaElement NOT updated");
              m_info
                  .append(
                      "InitialClientSetup - createAccounting - Default Account in AcctSchamaElement NOT updated")
                  .append(SALTO_LINEA);
            }
          }
        }
      }
    } catch (Exception e1) {
      log4j.warn("InitialClientSetup - createAccounting - Elements", e1);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException(e1);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - createAccounting - ACCT SCHEMA ELEMENTS INSERTED");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - createAccounting - m_info last: " + m_info.toString());
    try {
      // Create AcctSchema
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      m_AcctSchema = new AcctSchema(this, C_AcctSchema_ID);
      String strAccount = getAcct(conn, data, "INCOMESUMMARY_ACCT");
      log4j.debug("InitialClientSetup - getC_ElementValue_ID - strAccount: " + strAccount);

      if (InitialClientSetupData.insertAcctSchemaGL(conn, this, AD_Client_ID, C_AcctSchema_ID,
          getAcct(conn, data, "SUSPENSEBALANCING_ACCT"), getAcct(conn, data, "SUSPENSEERROR_ACCT"),
          getAcct(conn, data, "CURRENCYBALANCING_ACCT"),
          getAcct(conn, data, "RETAINEDEARNING_ACCT"), strAccount, getAcct(conn, data,
              "INTERCOMPANYDUETO_ACCT"), getAcct(conn, data, "INTERCOMPANYDUEFROM_ACCT"), getAcct(
              conn, data, "PPVOFFSET_ACCT")) != 1) {
        String err = "InitialClientSetup - createAccounting - GL Accounts NOT inserted";
        log4j.warn(err);
        m_info.append(err);
        return false;
      }

      String C_AcctSchema_Default_ID = SequenceIdData.getUUID();
      if (InitialClientSetupData.insertAcctSchemaDEFAULT(conn, this, AD_Client_ID, C_AcctSchema_ID,
          getAcct(conn, data, "W_INVENTORY_ACCT"), getAcct(conn, data, "W_DIFFERENCES_ACCT"),
          getAcct(conn, data, "W_REVALUATION_ACCT"), getAcct(conn, data, "W_INVACTUALADJUST_ACCT"),
          getAcct(conn, data, "P_REVENUE_ACCT"), getAcct(conn, data, "P_EXPENSE_ACCT"), getAcct(
              conn, data, "P_ASSET_ACCT"), getAcct(conn, data, "P_COGS_ACCT"), getAcct(conn, data,
              "P_PURCHASEPRICEVARIANCE_ACCT"), getAcct(conn, data, "P_INVOICEPRICEVARIANCE_ACCT"),
          getAcct(conn, data, "P_TRADEDISCOUNTREC_ACCT"), getAcct(conn, data,
              "P_TRADEDISCOUNTGRANT_ACCT"), getAcct(conn, data, "C_RECEIVABLE_ACCT"), getAcct(conn,
              data, "C_PREPAYMENT_ACCT"), getAcct(conn, data, "V_LIABILITY_ACCT"), getAcct(conn,
              data, "V_LIABILITY_SERVICES_ACCT"), getAcct(conn, data, "V_PREPAYMENT_ACCT"),
          getAcct(conn, data, "PAYDISCOUNT_EXP_ACCT"), getAcct(conn, data, "PAYDISCOUNT_REV_ACCT"),
          getAcct(conn, data, "WRITEOFF_ACCT"), getAcct(conn, data, "UNREALIZEDGAIN_ACCT"),
          getAcct(conn, data, "UNREALIZEDLOSS_ACCT"), getAcct(conn, data, "REALIZEDGAIN_ACCT"),
          getAcct(conn, data, "REALIZEDLOSS_ACCT"), getAcct(conn, data, "WITHHOLDING_ACCT"),
          getAcct(conn, data, "E_PREPAYMENT_ACCT"), getAcct(conn, data, "E_EXPENSE_ACCT"), getAcct(
              conn, data, "PJ_ASSET_ACCT"), getAcct(conn, data, "PJ_WIP_ACCT"), getAcct(conn, data,
              "T_EXPENSE_ACCT"), getAcct(conn, data, "T_LIABILITY_ACCT"), getAcct(conn, data,
              "T_RECEIVABLES_ACCT"), getAcct(conn, data, "T_DUE_ACCT"), getAcct(conn, data,
              "T_CREDIT_ACCT"), getAcct(conn, data, "B_INTRANSIT_ACCT"), getAcct(conn, data,
              "B_ASSET_ACCT"), getAcct(conn, data, "B_EXPENSE_ACCT"), getAcct(conn, data,
              "B_INTERESTREV_ACCT"), getAcct(conn, data, "B_INTERESTEXP_ACCT"), getAcct(conn, data,
              "B_UNIDENTIFIED_ACCT"), getAcct(conn, data, "B_SETTLEMENTGAIN_ACCT"), getAcct(conn,
              data, "B_SETTLEMENTLOSS_ACCT"), getAcct(conn, data, "B_REVALUATIONGAIN_ACCT"),
          getAcct(conn, data, "B_REVALUATIONLOSS_ACCT"),
          getAcct(conn, data, "B_PAYMENTSELECT_ACCT"),
          getAcct(conn, data, "B_UNALLOCATEDCASH_ACCT"), getAcct(conn, data, "CH_EXPENSE_ACCT"),
          getAcct(conn, data, "CH_REVENUE_ACCT"), getAcct(conn, data, "UNEARNEDREVENUE_ACCT"),
          getAcct(conn, data, "NOTINVOICEDRECEIVABLES_ACCT"), getAcct(conn, data,
              "NOTINVOICEDREVENUE_ACCT"), getAcct(conn, data, "NOTINVOICEDRECEIPTS_ACCT"), getAcct(
              conn, data, "CB_ASSET_ACCT"), getAcct(conn, data, "CB_CASHTRANSFER_ACCT"), getAcct(
              conn, data, "CB_DIFFERENCES_ACCT"), getAcct(conn, data, "CB_EXPENSE_ACCT"), getAcct(
              conn, data, "CB_RECEIPT_ACCT"), C_AcctSchema_Default_ID, getAcct(conn, data,
              "A_DEPRECIATION_ACCT"), getAcct(conn, data, "A_ACCUMDEPRECIATION_ACCT"), getAcct(
              conn, data, "A_DISPOSAL_LOSS"), getAcct(conn, data, "A_DISPOSAL_GAIN")) != 1) {
        String err = "InitialClientSetup - createAccounting - Default Accounts NOT inserted";
        log4j.warn(err);
        m_info.append(err);
        return false;
      }
      InitialOrgSetupData.insertOrgAcctSchema(conn, this, AD_Client_ID, "0", vars.getUser(),
          C_AcctSchema_ID);
      releaseCommitConnection(conn);
    } catch (Exception ex) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException(ex.getMessage());
    }
    m_info.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateAccountingSuccess", vars.getLanguage())).append(SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateAccountingSuccess", vars.getLanguage())).append(SALTO_LINEA);
    return true;
  } // createAccounting

  private boolean createDocumentTypes(VariablesSecureApp vars) throws ServletException {
    // GL Categories
    String GL_Standard = createGLCategory(vars, "Standard", "M", true);
    String GL_None = createGLCategory(vars, "None", "D", false);
    String GL_GL = createGLCategory(vars, "Manual", "M", false);
    String GL_ARI = createGLCategory(vars, "AR Invoice", "D", false);
    String GL_ARR = createGLCategory(vars, "AR Receipt", "D", false);
    String GL_MM = createGLCategory(vars, "Material Management", "D", false);
    String GL_API = createGLCategory(vars, "AP Invoice", "D", false);
    String GL_APP = createGLCategory(vars, "AP Payment", "D", false);
    String GL_STT = createGLCategory(vars, "Settlement", "D", false);
    String GL_CMB = createGLCategory(vars, "Bank Statement", "D", false);
    String GL_CMC = createGLCategory(vars, "Cash", "D", false);
    String GL_MMI = createGLCategory(vars, "Inventory", "D", false);
    String GL_MMM = createGLCategory(vars, "Movement", "D", false);
    String GL_MMP = createGLCategory(vars, "Production", "D", false);
    String GL_MXI = createGLCategory(vars, "MatchInv", "D", false);
    String GL_MXP = createGLCategory(vars, "MatchPO", "D", false);
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - createDocumentTypes - GL CATEGORIES CREATED");

    // Base DocumentTypes
    createDocType(vars, "GL Journal", "Journal", "GLJ", "", "0", "0", "1000", GL_GL, "224");

    String DT_I = createDocType(vars, "AR Invoice", "Invoice", "ARI", "", "0", "0", "100000",
        GL_ARI, "318");
    createDocTypeTemplate(vars, DT_I, "AR Invoice Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Invoice-@our_ref@", "RptC_Invoice.jrxml");

    String DT_II = createDocType(vars, "AR Invoice Indirect", "Invoice Indirect.", "ARI", "", "0",
        "0", "200000", GL_ARI, "318");
    createDocTypeTemplate(vars, DT_II, "AR Invoice Indirect Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Invoice-@our_ref@", "RptC_Invoice.jrxml");

    String arcDoctypeId = createDocType(vars, "AR Credit Memo", "Credit Memo", "ARC", "", "0", "0",
        "300000", GL_ARI, "318");
    createDocTypeTemplate(vars, arcDoctypeId, "AR Credit Memo Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Invoice-@our_ref@", "RptC_Invoice.jrxml");

    createDocType(vars, "AR Receipt", "Receipt", "ARR", "", "0", "0", "400000", GL_ARR, "");
    String DT_S = createDocType(vars, "MM Shipment", "Delivery Note", "MMS", "", "0", "0",
        "500000", GL_MM, "319");
    String DT_SI = createDocType(vars, "MM Shipment Indirect", "Delivery Note", "MMS", "", "0",
        "0", "600000", GL_MM, "319");
    createDocType(vars, "MM Receipt", "Vendor Delivery", "MMR", "", "0", "0", "0", GL_MM, "319");

    String apiDoctypeId = createDocType(vars, "AP Invoice", "Vendor Invoice", "API", "", "0", "0",
        "0", GL_API, "318");
    createDocTypeTemplate(vars, apiDoctypeId, "AP Invoice Report template",
        "@basedesign@/org/openbravo/erpReports", "Invoice-@our_ref@", "RptC_Invoice.jrxml");

    String apcDoctypeId = createDocType(vars, "AP CreditMemo", "Vendor Credit Memo", "APC", "",
        "0", "0", "0", GL_API, "318");
    createDocTypeTemplate(vars, apcDoctypeId, "AP Credit Report template",
        "@basedesign@/org/openbravo/erpReports", "Purchase Invoice-@our_ref@", "RptC_Invoice.jrxml");

    createDocType(vars, "AP Payment", "Vendor Payment", "APP", "", "0", "0", "700000", GL_APP, "");

    String pooDoctypeId = createDocType(vars, "Purchase Order", "Purchase Order", "POO", "", "0",
        "0", "800000", GL_None, "259");
    createDocTypeTemplate(vars, pooDoctypeId, "Purchase Order Report template",
        "@basedesign@/org/openbravo/erpReports", "Purchase Order-@our_ref@", "RptC_OrderPO.jrxml");

    createDocType(vars, "Purchase Requisition", "Purchase Requisition", "POR", "", "0", "0",
        "900000", GL_None, "259");

    createDocType(vars, "Settlement", "Settlement", "STT", "", "0", "0", "10000", GL_STT, "800019");
    createDocType(vars, "Manual Settlement", "Manual Settlement", "STM", "", "0", "0", "10000",
        GL_STT, "800019");
    createDocType(vars, "Bank Statement", "Bank Statement", "CMB", "", "0", "0", "1000000", GL_CMB,
        "392");
    createDocType(vars, "Cash Journal", "Cash Journal", "CMC", "", "0", "0", "1000000", GL_CMC,
        "407");
    createDocType(vars, "Physical Inventory", "Physical Inventory", "MMI", "", "0", "0", "1000000",
        GL_MMI, "321");
    createDocType(vars, "Inventory Move", "Inventory Move", "MMM", "", "0", "0", "1000000", GL_MMM,
        "323");
    createDocType(vars, "Production", "Production", "MMP", "", "0", "0", "1000000", GL_MMP, "325");
    createDocType(vars, "Matched Invoices", "Matched Invoices", "MXI", "", "0", "0", "1000000",
        GL_MXI, "472");
    createDocType(vars, "Matched Purchase Orders", "Matched Purchase Orders", "MXP", "", "0", "0",
        "1000000", GL_MXP, "473");
    createDocType(vars, "Debt Payment Management", "Debt Payment Management", "DPM", "", "0", "0",
        "10000", GL_Standard, "800176");
    createDocType(vars, "Depreciation", "Depreciation", "AMZ", "", "0", "0", "10000", GL_Standard,
        "800060");

    // Order Entry
    String sooDoctypeId2 = createDocType(vars, "Quotation", "Binding offer", "SOO", "OB", "0", "0",
        "10000", GL_None, "259");
    createDocTypeTemplate(vars, sooDoctypeId2, "Quotation Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Order-@our_ref@", "C_OrderJR.jrxml");

    String sooDoctypeId4 = createDocType(vars, "Proposal", "Non binding offer", "SOO", "ON", "0",
        "0", "20000", GL_None, "259");
    createDocTypeTemplate(vars, sooDoctypeId4, "Proposal Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Order-@our_ref@", "C_OrderJR.jrxml");

    String sooDoctypeId1 = createDocType(vars, "Prepay Order", "Prepay Order", "SOO", "PR", DT_S,
        DT_I, "30000", GL_None, "259");
    createDocTypeTemplate(vars, sooDoctypeId1, "Prepay Order Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Order-@our_ref@", "C_OrderJR.jrxml");

    String sooDoctypeId5 = createDocType(vars, "Return Material", "Return Material Authorization",
        "SOO", "RM", DT_S, DT_I, "40000", GL_None, "259");
    createDocTypeTemplate(vars, sooDoctypeId5, "Return Material Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Order-@our_ref@", "C_OrderJR.jrxml");

    String sooDoctypeId3 = createDocType(vars, "Standard Order", "Order Confirmation", "SOO", "SO",
        DT_S, DT_I, "50000", GL_None, "259");
    createDocTypeTemplate(vars, sooDoctypeId3, "Standard order report template",
        "@basedesign@/org/openbravo/erpReports", "StandardOrder-@our_ref@", "C_OrderJR.jrxml");

    String sooDoctypeId6 = createDocType(vars, "Credit Order", "Order Confirmation", "SOO", "WI",
        DT_SI, DT_I, "60000", GL_None, "259"); // RE
    createDocTypeTemplate(vars, sooDoctypeId6, "On Credit Order Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Order-@our_ref@", "C_OrderJR.jrxml");

    String DT_WO = createDocType(vars, "Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S,
        DT_I, "70000", GL_None, "259"); // LS
    createDocTypeTemplate(vars, DT_WO, "Warehouse Order Memo Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Order-@our_ref@", "C_OrderJR.jrxml");

    String DT = createDocType(vars, "POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II,
        "80000", GL_None, "259"); // Bar
    createDocTypeTemplate(vars, DT, "POS Order Report template",
        "@basedesign@/org/openbravo/erpReports", "Sales Order-@our_ref@", "C_OrderJR.jrxml");

    createPreference(vars, "C_DocTypeTarget_ID", DT, "143");
    createPreference(vars, "C_DocTypeTarget_ID", DT_WO, "800004");
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - createDocumentTypes - DOCTYPES & PREFERENCE CREATED");
    m_info.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateDocumentTypesSuccess", vars.getLanguage())).append(
        SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(
        Utility.messageBD(this, "CreateDocumentTypesSuccess", vars.getLanguage())).append(
        SALTO_LINEA);
    return true;
  }

  private String createGLCategory(VariablesSecureApp vars, String Name, String CategoryType,
      boolean isDefault) throws ServletException {
    Connection conn = null;
    String GL_Category_ID = "";
    try {
      conn = this.getTransactionConnection();
      GL_Category_ID = SequenceIdData.getUUID();
      String strisDefault = (isDefault ? "Y" : "N");
      if (InitialClientSetupData.insertCategory(conn, this, GL_Category_ID, AD_Client_ID, Name,
          CategoryType, strisDefault) != 1)
        log4j.warn("InitialClientSetup - createGLCategory - GL Logger NOT created - " + Name);
      m_info.append(Utility.messageBD(this, "GL_Category", vars.getLanguage())).append("=").append(
          Name).append(SALTO_LINEA);
      //
      releaseCommitConnection(conn);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@"
          + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
    return GL_Category_ID;
  }

  private void createPreference(VariablesSecureApp vars, String Attribute, String Value,
      String AD_Window_ID) throws ServletException {
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      String AD_Preference_ID = SequenceIdData.getUUID();
      if (InitialClientSetupData.insertPreference(conn, this, AD_Preference_ID, AD_Client_ID,
          Attribute, Value, AD_Window_ID) != 1)
        log4j
            .warn("InitialClientSetup - createPreference - Preference NOT inserted - " + Attribute);
      releaseCommitConnection(conn);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@"
          + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
  } // createPreference

  private String createDocType(VariablesSecureApp vars, String Name, String PrintName,
      String DocBaseType, String DocSubTypeSO, String C_DocTypeShipment_ID,
      String C_DocTypeInvoice_ID, String StartNo, String GL_Category_ID, String strTableId)
      throws ServletException {
    Connection conn = null;
    String C_DocType_ID = "";
    try {
      conn = this.getTransactionConnection();
      // Get Sequence
      String AD_Sequence_ID = "";
      if (!StartNo.equals("0")) {// manual sequence, if startNo == 0
        AD_Sequence_ID = SequenceIdData.getUUID();
        log4j.debug("inserting sequence ID:" + AD_Sequence_ID + " name: " + Name);
        if (InitialClientSetupData.insertSequence(conn, this, AD_Sequence_ID, AD_Client_ID, Name,
            StartNo) != 1)
          log4j.warn("InitialClientSetup - createDocType - Sequence NOT created - " + Name);
      }

      // Get Document Type
      C_DocType_ID = SequenceIdData.getUUID();
      String IsDocNoControlled = "";
      String IsSOTrx = "";
      if (AD_Sequence_ID.equals(""))
        IsDocNoControlled = "N";
      else
        IsDocNoControlled = "Y";
      if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO")
          || DocBaseType.equals("STT"))
        IsSOTrx = "Y";
      else
        IsSOTrx = "N";
      if (log4j.isDebugEnabled())
        log4j.debug("InitialClientSetup - createDocType - C_DocType_ID: " + C_DocType_ID
            + ", AD_Client_ID: " + AD_Client_ID + ", Name: " + Name + ", PrintName: " + PrintName
            + "DocBaseType: " + DocBaseType + ", DocSubTypeSO: " + DocSubTypeSO
            + ", C_DocTypeShipment_ID: " + C_DocTypeShipment_ID + ", C_DocTypeInvoice_ID: "
            + C_DocTypeInvoice_ID + ", IsDocNoControlled: " + IsDocNoControlled
            + ", AD_Sequence_ID: " + AD_Sequence_ID + ", GL_Category_ID: " + GL_Category_ID
            + ", IsSOTrx: " + IsSOTrx);
      if (InitialClientSetupData.insertDocType(conn, this, C_DocType_ID, AD_Client_ID, Name,
          PrintName, DocBaseType, DocSubTypeSO, C_DocTypeShipment_ID, C_DocTypeInvoice_ID,
          IsDocNoControlled, AD_Sequence_ID, GL_Category_ID, IsSOTrx, strTableId) != 1)
        log4j.warn("InitialClientSetup - createDocType - DocType NOT created - " + Name);
      //
      m_info.append(Utility.messageBD(this, "C_DocType", vars.getLanguage())).append("=").append(
          Name).append(SALTO_LINEA);
      releaseCommitConnection(conn);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@"
          + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
    return C_DocType_ID;
  } // createDocType

  /**
   * Returns the error. "" if there is no error
   * 
   * @param conn
   * @param vars
   * @param strClient
   * @param strModules
   * @param strCurrency
   * @param hasProduct
   * @param hasBPartner
   * @param hasProject
   * @param hasMCampaign
   * @param hasSRegion
   * @param bCreateAccounting
   * @return the error. "" if there is no error
   */

  private String createReferenceData(Connection conn, VariablesSecureApp vars, String strClient,
      String strModules, String strCurrency, boolean hasProduct, boolean hasBPartner,
      boolean hasProject, boolean hasMCampaign, boolean hasSRegion, boolean bCreateAccounting)
      throws ServletException, IOException, SQLException, NoConnectionAvailableException {
    if (strModules != null && !strModules.equals("")) {
      // Remove ( ) characters from the In string as it causes a failure
      if (strModules.charAt(0) == '(')
        strModules = strModules.substring(1, strModules.length());
      if (strModules.charAt(strModules.length() - 1) == ')')
        strModules = strModules.substring(0, strModules.length() - 1);

      // Import Charts of accounts
      InitialClientSetupData[] dataCOA = null;
      if (bCreateAccounting) {
        dataCOA = InitialClientSetupData.selectCOAModules(this, strModules);
        try {
          ModuleUtiltiy.orderModuleByDependency(dataCOA);
        } catch (Exception e) {
          log4j.error("Error ordering modules", e);
        }
        if (dataCOA != null && dataCOA.length != 0) {
          DataImportService myData = DataImportService.getInstance();
          for (int i = 0; i < dataCOA.length; i++) {
            String strPath = vars.getSessionValue("#SOURCEPATH") + "/modules" + dataCOA[i].path;
            FileInputStream in = new FileInputStream(strPath);
            AccountingValueData av = new AccountingValueData(vars, in, true, "C");
            m_info.append(SALTO_LINEA).append(
                Utility.messageBD(this, "StartingAccounting", vars.getLanguage())).append(
                SALTO_LINEA);
            if (!createAccounting(conn, vars, strCurrency, InitialClientSetupData.currency(this,
                strCurrency), hasProduct, hasBPartner, hasProject, hasMCampaign, hasSRegion, av
                .getFieldProvider())) {
              releaseRollbackConnection(conn);
              conn = this.getTransactionConnection();
              m_info.append(SALTO_LINEA).append(
                  Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(
                  SALTO_LINEA);
            } else {
              if (!InitialClientSetupData
                  .existsClientModule(this, strClient, dataCOA[i].adModuleId)) {
                InitialClientSetupData.insertClientModule(this, strClient, vars.getUser(),
                    dataCOA[i].adModuleId, dataCOA[i].version);
              }
              m_info.append(SALTO_LINEA).append(
                  Utility.messageBD(this, "CreateReferenceDataSuccess", vars.getLanguage()))
                  .append(SALTO_LINEA);
              strSummary.append(SALTO_LINEA).append(
                  Utility.messageBD(this, "CreateReferenceDataSuccess", vars.getLanguage()))
                  .append(SALTO_LINEA);
            }
          }
        }
      }

      // Import Reference data
      InitialClientSetupData[] data = InitialClientSetupData.selectRDModules(this, strModules);
      try {
        ModuleUtiltiy.orderModuleByDependency(data);
      } catch (Exception e) {
        log4j.error("Error ordering modules", e);
      }

      if (data != null && data.length != 0) {
        DataImportService myData = DataImportService.getInstance();
        StringBuffer strError = new StringBuffer("");

        for (int j = 0; j < data.length; j++) {
          String strPath = vars.getSessionValue("#SOURCEPATH") + "/modules/" + data[j].javapackage
              + "/referencedata/standard";
          File datasetFile = new File(strPath + "/" + Utility.wikifiedName(data[j].datasetname)
              + ".xml");
          if (!datasetFile.exists()) {
            continue;
          }

          String strXml = Utility.fileToString(datasetFile.getPath());
          ImportResult myResult = myData.importDataFromXML((Client) OBDal.getInstance().get(
              Client.class, strClient), (Organization) OBDal.getInstance().get(Organization.class,
              "0"), strXml, (Module) OBDal.getInstance().get(Module.class, data[j].adModuleId));
          m_info.append(SALTO_LINEA).append("File: ").append(datasetFile.getName()).append(":")
              .append(SALTO_LINEA);
          if (myResult.getLogMessages() != null && !myResult.getLogMessages().equals("")
              && !myResult.getLogMessages().equals("null")) {
            m_info.append(SALTO_LINEA).append("LOG:").append(SALTO_LINEA);
            m_info.append(SALTO_LINEA).append(myResult.getLogMessages()).append(SALTO_LINEA);
          }
          if (myResult.getWarningMessages() != null && !myResult.getWarningMessages().equals("")
              && !myResult.getWarningMessages().equals("null")) {
            m_info.append(SALTO_LINEA).append("WARNINGS:").append(SALTO_LINEA);
            m_info.append(SALTO_LINEA).append(myResult.getWarningMessages()).append(SALTO_LINEA);
          }
          if (myResult.getErrorMessages() != null && !myResult.getErrorMessages().equals("")
              && !myResult.getErrorMessages().equals("null")) {
            m_info.append(SALTO_LINEA).append("ERRORS:").append(SALTO_LINEA);
            if (myResult.getErrorMessages().startsWith("isBatchUpdateException:")) {
              String messageKey = myResult.getErrorMessages().substring(
                  "isBatchUpdateException:".length()).trim();
              m_info.append(SALTO_LINEA).append(
                  Utility.translateError(this, vars, vars.getLanguage(), messageKey).getMessage())
                  .append(SALTO_LINEA);
              strError = strError.append(Utility.translateError(this, vars, vars.getLanguage(),
                  messageKey).getMessage());
            } else {
              m_info.append(SALTO_LINEA).append(myResult.getErrorMessages()).append(SALTO_LINEA);
              strError = strError.append(myResult.getErrorMessages());
            }
          }

          if (!strError.toString().equals(""))
            return strError.toString();
          else {
            if (!InitialClientSetupData.existsClientModule(this, strClient, data[j].adModuleId)) {
              InitialClientSetupData.insertClientModule(this, strClient, vars.getUser(),
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
      } else {
        // wrong modules in case no coa and not rd
        if (dataCOA == null || dataCOA.length == 0) {
          return "WrongModules";
        }
      }
    } else
      return "NoModules";
    return "";
  }

  private String getAcct(Connection conn, AccountingValueData[] data, String key)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - getAcct - " + key);
    String C_ElementValue_ID = getC_ElementValue_ID(data, key);
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - getAcct - C_ElementValue_ID: " + C_ElementValue_ID);
    Account vc = Account.getDefault(m_AcctSchema, true);
    vc.Account_ID = C_ElementValue_ID;
    vc.save(conn, this, AD_Client_ID, "");// BEFORE, HERE IT WAS 0
    String C_ValidCombination_ID = vc.C_ValidCombination_ID;
    if (C_ValidCombination_ID.equals("")) {
      log4j.warn("InitialClientSetup - getAcct - C_ElementValue_ID: " + C_ElementValue_ID);
      log4j.warn("InitialClientSetup - getAcct - no account for " + key);
      C_ValidCombination_ID = "";// HERE IT WAS 0
    }
    if (log4j.isDebugEnabled())
      log4j.debug("InitialClientSetup - getAcct - " + key + "-- valid combination:"
          + C_ValidCombination_ID);
    return C_ValidCombination_ID;
  }

  private void createDocTypeTemplate(VariablesSecureApp vars, String doctypeId, String name,
      String templateLocation, String reportFilename, String templateFilename)
      throws ServletException {
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      String doctypetemplateId = SequenceIdData.getUUID();
      if (InitialClientSetupData.insertDoctypeTemplate2(conn, this, doctypetemplateId,
          AD_Client_ID, doctypeId, name, templateLocation, reportFilename, templateFilename) == 1
          && InitialClientSetupData.insertEmailDefinition(conn, this, AD_Client_ID,
              doctypetemplateId) == 1) {
        m_info.append(Utility.messageBD(this, "Template", vars.getLanguage())).append("=").append(
            name).append(SALTO_LINEA);
      } else {
        log4j.warn("InitialClientSetup - createDocTypeTemplate - DocType Template NOT created - "
            + name);
      }
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@"
          + ex2.getMessage());
    } catch (Exception ex3) {
      throw new ServletException("@CODE=@" + ex3.getMessage());
    } finally {
      try {
        releaseCommitConnection(conn);
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }
    }

  }
}
