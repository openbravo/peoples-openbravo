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
 * Contributions are Copyright (C) 2001-2006 Openbravo S.L.
 ******************************************************************************
*/
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.utils.FormatUtilities;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;

import java.util.Vector;
import javax.servlet.http.*;

import org.openbravo.erpCommon.ad_combos.*;
import org.openbravo.exception.*;

import org.openbravo.data.*;

// imports for transactions
import java.sql.*;

public class InitialClientSetup extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  static final String SALTO_LINEA = "<BR>\n";
  String C_Currency_ID = "";
  String clientName="";
  String AD_User_ID="";
  String AD_User_Name="";
  String AD_User_U_Name="";
  String AD_User_U_ID="";
  String AD_Client_ID="";
  String AD_Org_ID="";
  String C_AcctSchema_ID="";
  String client = "1000000";
  String strError = "";
  String C_Calendar_ID = null;
  StringBuffer strSummary = new StringBuffer();
  AcctSchema m_AcctSchema;
  private String     C_Cycle_ID;
  boolean m_hasProject ;
  boolean m_hasMCampaign ;
  boolean m_hasSRegion ;
  boolean isOK = true;
  String AD_Tree_Org_ID="", AD_Tree_BPartner_ID="", AD_Tree_Project_ID="",
  AD_Tree_SalesRegion_ID="", AD_Tree_Product_ID="", AD_Tree_Account_ID="";
  static StringBuffer    m_info=  new StringBuffer();
  private final String    CompiereSys = "N";           //  Should NOT be changed


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("ACEPTAR")) {
      AccountingValueData av = new AccountingValueData(vars, "inpArchivo", true, "C");
      m_info.delete(0,m_info.length());
      String strResultado = procesarFichero(av.getFieldProvider(), request, response, vars);
      log4j.debug("InitialClientSetup - after procesarFichero");
      printPageResultado(response, vars, strResultado);
    } else if (vars.commandIn("CANCELAR")) {
    } else pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException{
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/InitialClientSetup").createXmlDocument();
    
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialClientSetup", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.InitialClientSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialClientSetup.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialClientSetup.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      vars.removeMessage("InitialClientSetup");
      OBError myMessage = vars.getMessage("InitialClientSetup");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    
    xmlDocument.setParameter("region", arrayDobleEntrada("arrRegion", RegionComboData.selectTotal(this)));
    xmlDocument.setData("reportMoneda","liststructure", MonedaComboData.select(this));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Country_ID", "156", "", Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "", "");
      xmlDocument.setData("reportPais", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }    

	response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
  }

  private void printPageResultado(HttpServletResponse response, VariablesSecureApp vars, String strResultado) throws IOException, ServletException{
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Resultado").createXmlDocument();
    
    xmlDocument.setParameter("resultado",strResultado);
   
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "InitialClientSetup", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.InitialClientSetup");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "InitialClientSetup.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "InitialClientSetup.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    OBError myMessage = new OBError();
    myMessage.setTitle("");
    if(log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - before setMessage");
    if(strError!=null && !strError.equals("")) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), strError);
    }
    if(log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - isOK: " + isOK);
    if(isOK) myMessage.setType("Success");
    else myMessage.setType("Error");
    if(log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - Message Type: " + myMessage.getType());
    vars.setMessage("InitialClientSetup", myMessage);
    if(log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - after setMessage");
    if (myMessage!=null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String procesarFichero(FieldProvider[] avData, HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars) throws IOException {

    Connection conn = null;
    isOK = true;
    strSummary = new StringBuffer();
    strSummary.append(Utility.messageBD(this, "ReportSummary", vars.getLanguage())).append(SALTO_LINEA);
    String strCliente = vars.getStringParameter("inpCliente");
    String strOrganizacion = vars.getStringParameter("inpOrganizacion");
    String strClienteUsuario = vars.getStringParameter("inpClienteUsuario");
    String strOrganizacionUsuario = vars.getStringParameter("inpOrganizacionUsuario");
    String strMoneda = vars.getStringParameter("inpMoneda");
    String strPais = vars.getStringParameter("inpPais");
    String strCiudad = vars.getStringParameter("inpCiudad");
    String strRegion = vars.getStringParameter("inpRegion");
    boolean bProducto = isTrue(vars.getStringParameter("inpProducto"));
    boolean bTercero = isTrue(vars.getStringParameter("inpTercero"));
    boolean bProyecto = isTrue(vars.getStringParameter("inpProyecto"));
    boolean bCampana = isTrue(vars.getStringParameter("inpCampana"));
    boolean bZonaVentas = isTrue(vars.getStringParameter("inpZonaVentas"));
    boolean bIsSystemInstalation = isTrue(vars.getStringParameter("inpSystem"));
    if (bIsSystemInstalation) client = vars.getClient();
    try {
      conn = this.getTransactionConnection();
      if (InitialClientSetupData.updateUser2(conn, this,strClienteUsuario)!=0){
        m_info.append("Duplicate UserClient").append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return m_info.toString();
      } else if (InitialClientSetupData.updateUser2(conn, this,strOrganizacionUsuario)!=0){
        m_info.append("Duplicate UserOrg").append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return m_info.toString();
      }
      releaseCommitConnection(conn);
    } catch (Exception err){
      log4j.warn(err);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
    }
    try {
      m_info.append(SALTO_LINEA).append("*****************************************************").append(SALTO_LINEA);
      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "StartingClient", vars.getLanguage())).append(SALTO_LINEA);
      if (!createClient(vars, strCliente, strOrganizacion, strClienteUsuario, strOrganizacionUsuario)){
        releaseRollbackConnection(conn);
        m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
        strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
        isOK = false;
        return m_info.toString();
      }
    } catch (Exception err){
      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
      strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateClientFailed", vars.getLanguage())).append(SALTO_LINEA);
      strError = err.toString();
      strError = strError.substring( strError.lastIndexOf("@ORA-") ,strError.length());
      isOK = false;
      log4j.warn(err);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
    }
    m_info.append(SALTO_LINEA).append("*****************************************************").append(SALTO_LINEA);
    if(avData.length == 0) {    	
	    m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "SkippingAccounting", vars.getLanguage())).append(SALTO_LINEA);	    
    }
    else {
	    try {	      
	      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "StartingAccounting", vars.getLanguage())).append(SALTO_LINEA);
	      if (!createAccounting(vars, strMoneda, InitialClientSetupData.moneda(this, strMoneda), bProducto, bTercero, bProyecto, bCampana, bZonaVentas, avData)){
	        releaseRollbackConnection(conn);
	        m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(SALTO_LINEA);
	        strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(SALTO_LINEA);
	        isOK = false;
	        return m_info.toString();
	      }
	    } catch (Exception err){
	      log4j.warn(err);
	      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(SALTO_LINEA);
	      strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateAccountingFailed", vars.getLanguage())).append(SALTO_LINEA);
	      strError = err.toString();
	      strError = strError.substring( strError.lastIndexOf("@ORA-") ,strError.length());
	      log4j.debug("InitialClientSetup - after strError: " + strError);
	      isOK = false;
	      try {
	        releaseRollbackConnection(conn);
	      } catch (Exception ignored) {}
	    }
    }
    try {
      m_info.append(SALTO_LINEA).append("*****************************************************").append(SALTO_LINEA);
      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "StartingDocumentTypes", vars.getLanguage())).append(SALTO_LINEA);
      if (!createDocumentTypes(vars)){
        releaseRollbackConnection(conn);
        m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(SALTO_LINEA);
        strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(SALTO_LINEA);
        isOK = false;
        return m_info.toString();
      }
    } catch (Exception err){
      log4j.warn(err);
      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(SALTO_LINEA);
      strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateDocumentTypesFailed", vars.getLanguage())).append(SALTO_LINEA);
      strError = err.toString();
      strError = strError.substring( strError.lastIndexOf("@ORA-") ,strError.length());
      log4j.debug("InitialClientSetup - after strError: " + strError);
      isOK = false;
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
    }
    try {
      m_info.append(SALTO_LINEA).append("*****************************************************").append(SALTO_LINEA);
      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "StartingMasterData", vars.getLanguage())).append(SALTO_LINEA);
      if (!createEntities(vars, strPais, strCiudad, strRegion)){
        releaseRollbackConnection(conn);
        m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateMasterDataFailed", vars.getLanguage())).append(SALTO_LINEA);
        strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateMasterDataFailed", vars.getLanguage())).append(SALTO_LINEA);
        isOK = false;
        return m_info.toString();
      }
    } catch (Exception err){
      log4j.warn(err);
      m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateMasterDataFailed", vars.getLanguage())).append(SALTO_LINEA);
      strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateMasterDataFailed", vars.getLanguage())).append(SALTO_LINEA);
      strError = err.toString();
      strError = strError.substring( strError.lastIndexOf("@ORA-") ,strError.length());
      isOK = false;
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
    }
    log4j.debug("InitialClientSetup - after createEntities");
    if(isOK)strError = Utility.messageBD(this, "Success", vars.getLanguage());
    log4j.debug("InitialClientSetup - after strError");
    strSummary.append(m_info.toString());
    m_info = strSummary;
    return m_info.toString();
  } 

  public boolean isTrue(String s){
    if (s==null || s.equals("")) return false;
    else return true;
  }


  public boolean createClient(VariablesSecureApp vars, String m_ClientName, String orgName, String userClient, String userOrg) throws ServletException{

    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient");
    clientName = m_ClientName;
    if (clientName.equals(""))
    clientName = "newClient";

    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      //  info header
      m_info.append(SALTO_LINEA);
      //  Standard columns
      String name = null;

  //     *  Create Client

      vars.setSessionValue("#CompiereSys", CompiereSys);
      AD_Client_ID = SequenceIdData.getSequence(this, "AD_Client", client);
      vars.setSessionValue("AD_Client_ID", AD_Client_ID);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - CLIENT_ID: " + AD_Client_ID);

      if (InitialClientSetupData.insertCliente(conn ,this, AD_Client_ID, clientName) != 1) {
        String err = "InitialClientSetup - createClient - Client NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      //  Info - Client
      m_info.append(Utility.messageBD(this, "AD_Client_ID", vars.getLanguage())).append("=").append(clientName).append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - M_INFO: " + m_info.toString());


      //     *  Create Trees
      //  Get TreeTypes & Name
      FieldProvider [] data = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "AD_TreeType Type", "", Utility.getContext(this, vars, "#User_Org", "InitialClientSetup"), Utility.getContext(this, vars, "#User_Client", "InitialClientSetup"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "InitialClientSetup", "");
        data = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }


      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - LIST COUNT: " + data.length);

      //  Tree IDs


      try {
        int i =0;
        while (i<data.length) {
          String value = data[i].getField("id");
          String AD_Tree_ID = "0";
          if (value.equals("OO")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
            AD_Tree_Org_ID = AD_Tree_ID;
          }else if (value.equals("BP")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
            AD_Tree_BPartner_ID = AD_Tree_ID;
          }else if (value.equals("PJ")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
            AD_Tree_Project_ID = AD_Tree_ID;
          }else if (value.equals("SR")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
            AD_Tree_SalesRegion_ID = AD_Tree_ID;
          }else if (value.equals("PR")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
            AD_Tree_Product_ID = AD_Tree_ID;
          }else if (value.endsWith("EV")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
            AD_Tree_Account_ID = AD_Tree_ID;
          }else if (value.endsWith("TR")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
           // Not added to clientinfo
          }else if (value.endsWith("AR")) {
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
           // Not added to clientinfo
          }else if (!value.equals("MM")){ //  No Menu
            AD_Tree_ID = SequenceIdData.getSequence(this, "AD_Tree", client);
          }
          //
          if (!AD_Tree_ID.equals("0")){
            name = clientName + " " + data[i].getField("name");
            if (InitialClientSetupData.insertTree(conn, this,AD_Client_ID, AD_Tree_ID, name, value) == 1) {
              m_info.append(Utility.messageBD(this, "AD_Client_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
            } else log4j.warn("InitialClientSetup - createClient - Tree NOT created: " + name);
          }
          if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - VALUE " + i + ": "+ value + " ,AD_Tree_ID: " + AD_Tree_ID);
          i++;
        }
      } catch (ServletException e1) {
        log4j.warn("InitialClientSetup - createClient - Trees");
      }
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      //  Get Primary Tree
      String AD_Tree_Menu_ID = "10";  //  hardcoded
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - AD_Client_ID: " + AD_Client_ID + ", AD_Tree_Menu_ID: " + AD_Tree_Menu_ID + ", AD_Tree_Org_ID: " + AD_Tree_Org_ID + ", AD_Tree_BPartner_ID: " + AD_Tree_BPartner_ID + ", AD_Tree_Project_ID: " + AD_Tree_Project_ID + ", AD_Tree_SalesRegion_ID: " + AD_Tree_SalesRegion_ID + ", AD_Tree_Product_ID: " + AD_Tree_Product_ID);
      if (InitialClientSetupData.insertClientInfo(conn ,this, AD_Client_ID, AD_Tree_Menu_ID, AD_Tree_Org_ID, AD_Tree_BPartner_ID, AD_Tree_Project_ID, AD_Tree_SalesRegion_ID, AD_Tree_Product_ID) != 1) {
        String err = "InitialClientSetup - createClient - ClientInfo NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - CLIENT INFO CREATED");


//     *  Create Org

      AD_Org_ID = SequenceIdData.getSequence(this, "AD_Org", client);
      name = orgName;
      if (name == null || name.length() == 0)
        name = "newOrg";
      if (InitialClientSetupData.insertOrg(conn ,this, AD_Client_ID, AD_Org_ID, name) != 1) {
        String err = "InitialClientSetup - createClient - Org NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      //  Info
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();
      m_info.append(Utility.messageBD(this, "AD_Org_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());


//     *  Create Roles
//     *  - Admin
//     *  - User

    
      name = clientName + " Admin";
      String AD_Role_ID = SequenceIdData.getSequence(this, "AD_Role", client);
      if (InitialClientSetupData.insertRole(conn ,this, AD_Client_ID, AD_Role_ID, name, "0,"+AD_Org_ID) != 1) {
        String err = "InitialClientSetup - createClient - Admin Role A NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - ROLE CREATED");
      //  OrgAccess x,0
      if (InitialClientSetupData.insertRoleOrgAccess(conn ,this, AD_Client_ID,"0" , AD_Role_ID) != 1)
        log4j.warn("InitialClientSetup - createClient - Admin Role_OrgAccess 0 NOT created");
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - ROLE ORG ACCESS CREATED");
      //  OrgAccess x,y
      if (InitialClientSetupData.insertRoleOrgAccess(conn ,this, AD_Client_ID,AD_Org_ID, AD_Role_ID) != 1)
        log4j.warn("InitialClientSetup - createClient - Admin Role_OrgAccess NOT created");
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - SECOND ROLE ORG ACCESS CREATED");
      //  Info - Admin Role
      m_info.append(Utility.messageBD(this, "AD_Role_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());

      //
      name = clientName + " User";
      String AD_Role_ID_U = SequenceIdData.getSequence(this, "AD_Role", client);
      if (InitialClientSetupData.insertRole2(conn ,this, AD_Client_ID, AD_Role_ID_U, name,AD_Org_ID) != 1){
        String err = "InitialClientSetup - createClient - User Role A NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      else if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - ROLE2 CREATED");
      //  OrgAccess x,y
      if (InitialClientSetupData.insertRoleOrgAccess(conn, this, AD_Client_ID,AD_Org_ID , AD_Role_ID_U) != 1)
        log4j.warn("InitialClientSetup - createClient - User Role_OrgAccess NOT created");
      else if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - ROLE2 ORG ACCESS CREATED");
      //  Info - Client Role
      m_info.append(Utility.messageBD(this, "AD_Role_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());

      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();

//     *  Create Users
//     *  - Client
//     *  - Org

      name = userClient;
      if (name == null || name.length() == 0)
        name = clientName + "Client";
      AD_User_ID = SequenceIdData.getSequence(this, "AD_User", client);
      AD_User_Name = name;
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - AD_User_Name : " + AD_User_Name);
      if (InitialClientSetupData.insertUser(conn ,this, AD_Client_ID,AD_User_ID , name, FormatUtilities.sha1Base64(name)) != 1) {
        String err = "InitialClientSetup - createClient - Admin User A NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - USER INSERTED " + name);
      //  Info
      m_info.append(Utility.messageBD(this, "AD_User_ID", vars.getLanguage())).append("=").append(name).append("/").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());


      name = userOrg;
      if (name == null || name.length() == 0 || userClient == userOrg)
        name = clientName + "Org";
      AD_User_U_ID = SequenceIdData.getSequence(this, "AD_User", client);
      AD_User_U_Name = name;
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - AD_User_U_Name : " + AD_User_U_Name);
      if (InitialClientSetupData.insertUser(conn ,this, AD_Client_ID,AD_User_U_ID , name, FormatUtilities.sha1Base64(name)) != 1) {
        String err = "InitialClientSetup - createClient - Org User A NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - USER INSERTED " + name);
      //  Info
      m_info.append(Utility.messageBD(this, "AD_User_ID", vars.getLanguage())).append("=").append(name).append("/").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - m_info: " + m_info.toString());
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();


//     *  Create User-Role

      //  ClientUser          - Admin & User
      if (InitialClientSetupData.insertUserRoles(conn ,this, AD_Client_ID,AD_User_ID , AD_Role_ID) != 1)
        log4j.warn("InitialClientSetup - createClient - UserRole ClientUser+Admin NOT inserted");
      if (InitialClientSetupData.insertUserRoles(conn ,this, AD_Client_ID,AD_User_ID , AD_Role_ID_U) != 1)
        log4j.warn("InitialClientSetup - createClient - UserRole ClientUser+User NOT inserted");
      //  OrgUser             - User
      if (InitialClientSetupData.insertUserRoles(conn ,this, AD_Client_ID,AD_User_U_ID , AD_Role_ID_U) != 1)
        log4j.warn("InitialClientSetup - createClient - UserRole OrgUser+Org NOT inserted");
      //  SuperUser(100)      - Admin & User
      if (InitialClientSetupData.insertUserRoles(conn ,this, AD_Client_ID,"100" , AD_Role_ID) != 1)
        log4j.warn("InitialClientSetup - createClient - UserRole SuperUser+Admin NOT inserted");
      if (InitialClientSetupData.insertUserRoles(conn ,this, AD_Client_ID,"100" , AD_Role_ID_U) != 1)
        log4j.warn("InitialClientSetup - createClient - UserRole SuperUser+User NOT inserted");
      releaseCommitConnection(conn);
    } catch (Exception e) {
      m_info.append(e).append(SALTO_LINEA);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      try {
        conn = this.getTransactionConnection();
      } catch (Exception ignored) {}
      return false;
    }
    m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateClientSuccess", vars.getLanguage())).append(SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateClientSuccess", vars.getLanguage())).append(SALTO_LINEA);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createClient - m_info last: " + m_info.toString());
    return true;
  } 



  public boolean save (Connection conn, VariablesSecureApp vars, String AD_Client_ID, String AD_Org_ID, String C_Element_ID, AccountingValueData[] data) throws ServletException {
    boolean OK=true;
    String strAccountTree = InitialClientSetupData.selectTree(this, AD_Client_ID);
    for (int i=0;i<data.length;i++) {
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - DATA LENGTH : " + data.length + ", POSICION : " + i + ", DEFAULT_ACCT: " + data[i].defaultAccount);
      data[i].cElementValueId = SequenceIdData.getSequence(this, "C_ElementValue", client);
      String IsDocControlled = data[i].accountDocument.equals("Yes") ? "Y" : "N";
      String C_ElementValue_ID = data[i].cElementValueId;
      String IsSummary =data[i].accountSummary.equals("Yes") ? "Y" : "N";
      String accountType="";
      String accountSign="";
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - AccountType debug");
      if (!data[i].accountType.equals("")){
        String s = data[i].accountType.toUpperCase().substring(0, 1);
        if(s.equals("A") || s.equals("L") || s.equals("O") || s.equals("E") || s.equals("R") || s.equals("M")) accountType = s;
        else accountType = "E";
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - Not is account type");
      } else {
        accountType = "E";
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - Is account type");
      }
      if (!data[i].accountSign.equals("")){
        String s = data[i].accountSign.toUpperCase().substring(0, 1);
        if(s.equals("D") || s.equals("C")) accountSign = s;
        else accountSign = "N";
      } else accountSign = "N";
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - ACCOUNT VALUE : " + data[i].accountValue + " ACCOUNT NAME : " + data[i].accountName + " DEFAULT_ACCOUNT: " + data[i].defaultAccount);
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - C_ElementValue_ID: " + C_ElementValue_ID );
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - C_Element_ID: " + C_Element_ID);
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - data[i].accountValue: " + data[i].accountValue);
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - data[i].accountName: " + data[i].accountName);
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - data[i].accountDescription: " + data[i].accountDescription);
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - accountType: " + accountType);

      if (!data[i].accountValue.equals("")) {
        try {
          if (InitialClientSetupData.insertElementValue(conn ,this, C_ElementValue_ID, C_Element_ID, AD_Client_ID, AD_Org_ID, data[i].accountValue, data[i].accountName, data[i].accountDescription, accountType, accountSign, IsDocControlled, IsSummary, data[i].elementLevel) != 1) {
            log4j.warn("InitialClientSetup - save - Natural Account not added");
            data[i].cElementValueId = "";
            return false;
            } else {
              String strParent = InitialClientSetupData.selectParent(conn ,this, data[i].accountParent, AD_Client_ID);
              if(strParent!=null && !strParent.equals(""))InitialClientSetupData.updateTreeNode(conn ,this, strParent, strAccountTree, C_ElementValue_ID, AD_Client_ID);
            }
        } catch (ServletException e) {
          log4j.warn("InitialClientSetup - save - Natural Account not added");
          data[i].cElementValueId = "";
          return false;
        }
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - NATURAL ACCOUNT ADDED");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - save - m_info last: " + m_info.toString());
    return OK;
  }//  save

  public AccountingValueData[] parseData (FieldProvider[] data) throws ServletException {
    AccountingValueData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    for (int i=0;i<data.length;i++) {
      AccountingValueData dataAux = new AccountingValueData();
      dataAux.accountValue = data[i].getField("accountValue");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountValue: " + dataAux.accountValue);
      dataAux.accountName = data[i].getField("accountName");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountName: " + dataAux.accountName);
      dataAux.accountDescription = data[i].getField("accountDescription");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountDescription: " + dataAux.accountDescription);
      dataAux.accountType = data[i].getField("accountType");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountType: " + dataAux.accountType);
      dataAux.accountSign = data[i].getField("accountSign");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountSign: " + dataAux.accountSign);
      dataAux.accountDocument = data[i].getField("accountDocument");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountDocument: " + dataAux.accountDocument);
      dataAux.accountSummary = data[i].getField("accountSummary");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountSummary: " + dataAux.accountSummary);
      dataAux.defaultAccount = data[i].getField("defaultAccount");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.defaultAccount: " + dataAux.defaultAccount);
      dataAux.accountParent = data[i].getField("accountParent");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.accountParent: " + dataAux.accountParent);
      dataAux.elementLevel = data[i].getField("elementLevel");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.elementLevel: " + dataAux.elementLevel);
      dataAux.balanceSheet = data[i].getField("balanceSheet");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.balanceSheet: " + dataAux.balanceSheet);
      dataAux.balanceSheetName = data[i].getField("balanceSheetName");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.balanceSheetName: " + dataAux.balanceSheetName);
      dataAux.uS1120BalanceSheet = data[i].getField("uS1120BalanceSheet");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.uS1120BalanceSheet: " + dataAux.uS1120BalanceSheet);
      dataAux.uS1120BalanceSheetName = data[i].getField("uS1120BalanceSheetName");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.uS1120BalanceSheetName: " + dataAux.uS1120BalanceSheetName);
      dataAux.profitAndLoss = data[i].getField("profitAndLoss");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.profitAndLoss: " + dataAux.profitAndLoss);
      dataAux.profitAndLossName = data[i].getField("profitAndLossName");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.profitAndLossName: " + dataAux.profitAndLossName);
      dataAux.uS1120IncomeStatement = data[i].getField("uS1120IncomeStatement");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.uS1120IncomeStatement: " + dataAux.uS1120IncomeStatement);
      dataAux.uS1120IncomeStatementName = data[i].getField("uS1120IncomeStatementName");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.uS1120IncomeStatementName: " + dataAux.uS1120IncomeStatementName);
      dataAux.cashFlow = data[i].getField("cashFlow");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.cashFlow: " + dataAux.cashFlow);
      dataAux.cashFlowName = data[i].getField("cashFlowName");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.cashFlowName: " + dataAux.cashFlowName);
      dataAux.cElementValueId = data[i].getField("cElementValueId");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - dataAux.cElementValueId: " + dataAux.cElementValueId);
      vec.addElement(dataAux);
    }
    result = new AccountingValueData[vec.size()];
    vec.copyInto(result);
    return result;
  }//  parseData

  public String getC_ElementValue_ID (AccountingValueData[] data, String key) {
    if (data==null || data.length==0) return "";
    for (int i=0;i<data.length;i++) if (data[i].defaultAccount.equalsIgnoreCase(key) && !data[i].defaultAccount.equals("")) return data[i].cElementValueId;
    return "";
  }   //  getC_ElementValue_ID

  public boolean createAccounting(VariablesSecureApp vars, String newC_Currency_ID, String curName,boolean hasProduct, boolean hasBPartner, boolean hasProject,
    boolean hasMCampaign, boolean hasSRegion,FieldProvider[] avData) throws ServletException{
    //
    C_Currency_ID = newC_Currency_ID;
    m_hasProject = hasProject;
    m_hasMCampaign = hasMCampaign;
    m_hasSRegion = hasSRegion;

    Connection conn = null;
    String name = null;
    String C_Year_ID = null;
    String C_Element_ID = null;
    String C_ElementValue_ID = null;
    String GAAP = null;
    String CostingMethod = null;
    AccountingValueData[] data = null;
    try {
      conn = this.getTransactionConnection();

      //  Standard variables
      m_info.append(SALTO_LINEA);
  
  
  //     *  Create Calendar
  
      C_Calendar_ID = SequenceIdData.getSequence(this, "C_Calendar", client);
      name = clientName + " " + Utility.messageBD(this, "C_Calendar_ID", vars.getLanguage());
      if (InitialClientSetupData.insertCalendar(conn ,this, AD_Client_ID,C_Calendar_ID , name) != 1) {
        String err = "InitialClientSetup - createAccounting - Calendar NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - CALENDAR INSERTED");
      //  Info
      m_info.append(Utility.messageBD(this, "C_Calendar_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
  
      //  Year
      C_Year_ID = SequenceIdData.getSequence(this, "C_Year", client);
      if (InitialClientSetupData.insertYear(conn ,this, C_Year_ID, AD_Client_ID,C_Calendar_ID) != 1)
        log4j.warn("InitialClientSetup - createAccounting - Year NOT inserted");
      // @todo Create Periods 
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - YEAR INSERTED");
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();

      //  Create Account Elements
      C_Element_ID = SequenceIdData.getSequence(this, "C_Element", client);
      name = clientName + " " + Utility.messageBD(this, "Account_ID", vars.getLanguage());
      if (InitialClientSetupData.insertElement(conn ,this, AD_Client_ID, C_Element_ID,name, AD_Tree_Account_ID) != 1) {
        String err = "InitialClientSetup - createAccounting - Acct Element NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - ELEMENT INSERTED :" + C_Element_ID);
      m_info.append(Utility.messageBD(this, "C_Element_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - m_info last: " + m_info.toString());
  
      //  Create Account Values
      data = parseData(avData);
      boolean errMsg = save(conn, vars, AD_Client_ID, AD_Org_ID, C_Element_ID, data);
      if (!errMsg) {
        releaseRollbackConnection(conn);
        String err = "InitialClientSetup - createAccounting - Acct Element Values NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        return false;
      }
      else m_info.append(Utility.messageBD(this, "C_ElementValue_ID", vars.getLanguage())).append(" # ").append(SALTO_LINEA);
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - m_info last: " + m_info.toString());
  
  //     *  Create AccountingSchema
      C_ElementValue_ID = getC_ElementValue_ID(data, "DEFAULT_ACCT");
      C_AcctSchema_ID = SequenceIdData.getSequence(this, "C_AcctSchema", client);
      //
      GAAP = "US";             //  AD_Reference_ID=123
      CostingMethod = "A";     //  AD_Reference_ID=122
      name = clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
      //
      if (InitialClientSetupData.insertAcctSchema(conn ,this, AD_Client_ID, C_AcctSchema_ID,name, GAAP, CostingMethod, C_Currency_ID) != 1){
        String err = "InitialClientSetup - createAccounting - AcctSchema NOT inserted";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
        }
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - ACCT SCHEMA INSERTED");
      //  Info
      m_info.append(Utility.messageBD(this, "C_AcctSchema_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      throw new ServletException(ex2.getMessage());
    } catch (Exception ex3) {
      throw new ServletException(ex3.getMessage());
    }


//     *  Create AccountingSchema Elements (Structure)

    FieldProvider [] data1 = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_AcctSchema ElementType", "", Utility.getContext(this, vars, "#User_Org", "InitialClientSetup"), Utility.getContext(this, vars, "#User_Client", "InitialClientSetup"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "InitialClientSetup", "");
      data1 = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      for (int i=0; i< data1.length;i++) {
        String ElementType = data1[i].getField("id");
        name = data1[i].getField("name");
        //
        String IsMandatory = "";
        String IsBalanced = "N";
        String SeqNo = "";
        String  C_AcctSchema_Element_ID = "";

        if (ElementType.equals("OO")){
          C_AcctSchema_Element_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Element", client);
          IsMandatory = "Y";
          IsBalanced = "Y";
          SeqNo = "10";
        } else if (ElementType.equals("AC")){
          C_AcctSchema_Element_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Element", client);
          IsMandatory = "Y";
          SeqNo = "20";
        } else if (ElementType.equals("PR") && hasProduct){
          C_AcctSchema_Element_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Element", client);
          IsMandatory = "N";
          SeqNo = "30";
        } else if (ElementType.equals("BP") && hasBPartner){
          C_AcctSchema_Element_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Element", client);
          IsMandatory = "N";
          SeqNo = "40";
        } else if (ElementType.equals("PJ") && hasProject){
          C_AcctSchema_Element_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Element", client);
          IsMandatory = "N";
          SeqNo = "50";
        } else if (ElementType.equals("MC") && hasMCampaign){
          C_AcctSchema_Element_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Element", client);
          IsMandatory = "N";
          SeqNo = "60";
        } else if (ElementType.equals("SR") && hasSRegion){
          C_AcctSchema_Element_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Element", client);
          IsMandatory = "N";
          SeqNo = "70";
        }
        //  Not OT, LF, LT, U1, U2, AY
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - C_ElementValue_ID: " + C_ElementValue_ID);

        if (!IsMandatory.equals("")){
          if (InitialClientSetupData.insertAcctSchemaElement(conn, this, AD_Client_ID, C_AcctSchema_Element_ID,C_AcctSchema_ID, ElementType, name, SeqNo, IsMandatory, IsBalanced) == 1)
            m_info.append(Utility.messageBD(this, "C_AcctSchema_Element_ID", vars.getLanguage())).append("=").append(name).append(SALTO_LINEA);
          else m_info.append(Utility.messageBD(this, "C_AcctSchema_Element_ID", vars.getLanguage())).append("=").append(name).append(" NOT inserted").append(SALTO_LINEA);

          // Default value for mandatory elements: OO and AC 
          if (ElementType.equals("OO")){
            if (InitialClientSetupData.updateAcctSchemaElement(conn, this, AD_Org_ID, C_AcctSchema_Element_ID) != 1){
              log4j.warn("InitialClientSetup - createAccounting - Default Org in AcctSchamaElement NOT updated");
              m_info.append("InitialClientSetup - createAccounting - Default Org in AcctSchamaElement NOT updated").append(SALTO_LINEA);
            }
          }
          if (ElementType.equals("AC")){
            if (InitialClientSetupData.updateAcctSchemaElement2(conn, this, C_ElementValue_ID, C_Element_ID, C_AcctSchema_Element_ID) != 1){
              log4j.warn("InitialClientSetup - createAccounting - Default Account in AcctSchamaElement NOT updated");
              m_info.append("InitialClientSetup - createAccounting - Default Account in AcctSchamaElement NOT updated").append(SALTO_LINEA);
            }
          }
        }
      }
    } catch (Exception e1) {
     log4j.warn("InitialClientSetup - createAccounting - Elements", e1);
     try {
       releaseRollbackConnection(conn);
     } catch (Exception ignored) {}
     throw new ServletException(e1);
    }
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - ACCT SCHEMA ELEMENTS INSERTED");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createAccounting - m_info last: " + m_info.toString());
    try {
      //  Create AcctSchema
      releaseCommitConnection(conn);
      conn = this.getTransactionConnection();

      m_AcctSchema = new AcctSchema(this, C_AcctSchema_ID);
      if(InitialClientSetupData.insertAcctSchemaGL(conn, this, AD_Client_ID, C_AcctSchema_ID, getAcct(conn, data, 
"SUSPENSEBALANCING_ACCT"), getAcct(conn, data, "SUSPENSEERROR_ACCT"), getAcct(conn, data, "CURRENCYBALANCING_ACCT"), getAcct(conn, data, 
"RETAINEDEARNING_ACCT"), getAcct(conn, data, "INCOMESUMMARY_ACCT"), getAcct(conn, data, "INTERCOMPANYDUETO_ACCT"), getAcct(conn, data, 
"INTERCOMPANYDUEFROM_ACCT"), getAcct(conn, data, "PPVOFFSET_ACCT")) != 1) {
          String err = "InitialClientSetup - createAccounting - GL Accounts NOT inserted";
          log4j.warn(err);
          m_info.append(err);
          return false;
      }

      String C_AcctSchema_Default_ID = SequenceIdData.getSequence(this, "C_AcctSchema_Default", AD_Client_ID);
      if(InitialClientSetupData.insertAcctSchemaDEFAULT(conn, this, AD_Client_ID, C_AcctSchema_ID, getAcct(conn, data, 
"W_INVENTORY_ACCT"), getAcct(conn, data, "W_DIFFERENCES_ACCT"), getAcct(conn, data, "W_REVALUATION_ACCT"), getAcct(conn, data, 
"W_INVACTUALADJUST_ACCT"), getAcct(conn, data, "P_REVENUE_ACCT"), getAcct(conn, data, "P_EXPENSE_ACCT"), getAcct(conn, data, 
"P_ASSET_ACCT"), getAcct(conn, data, "P_COGS_ACCT"), getAcct(conn, data, "P_PURCHASEPRICEVARIANCE_ACCT"), getAcct(conn, data, 
"P_INVOICEPRICEVARIANCE_ACCT"), getAcct(conn, data, "P_TRADEDISCOUNTREC_ACCT"), getAcct(conn, data, "P_TRADEDISCOUNTGRANT_ACCT"), 
getAcct(conn, data, "C_RECEIVABLE_ACCT"), getAcct(conn, data, "C_PREPAYMENT_ACCT"), getAcct(conn, data, "V_LIABILITY_ACCT"), 
getAcct(conn, data, "V_LIABILITY_SERVICES_ACCT"), getAcct(conn, data, "V_PREPAYMENT_ACCT"), getAcct(conn, data, "PAYDISCOUNT_EXP_ACCT"), 
getAcct(conn, data, "PAYDISCOUNT_REV_ACCT"), getAcct(conn, data, "WRITEOFF_ACCT"), getAcct(conn, data, "UNREALIZEDGAIN_ACCT"), 
getAcct(conn, data, "UNREALIZEDLOSS_ACCT"), getAcct(conn, data, "REALIZEDGAIN_ACCT"), getAcct(conn, data, "REALIZEDLOSS_ACCT"), 
getAcct(conn, data, "WITHHOLDING_ACCT"), getAcct(conn, data, "E_PREPAYMENT_ACCT"), getAcct(conn, data, "E_EXPENSE_ACCT"), getAcct(conn, 
data, "PJ_ASSET_ACCT"), getAcct(conn, data, "PJ_WIP_ACCT"), getAcct(conn, data, "T_EXPENSE_ACCT"), getAcct(conn, data, 
"T_LIABILITY_ACCT"), getAcct(conn, data, "T_RECEIVABLES_ACCT"), getAcct(conn, data, "T_DUE_ACCT"), getAcct(conn, data, "T_CREDIT_ACCT"), 
getAcct(conn, data, "B_INTRANSIT_ACCT"), getAcct(conn, data, "B_ASSET_ACCT"), getAcct(conn, data, "B_EXPENSE_ACCT"), getAcct(conn, data, 
"B_INTERESTREV_ACCT"), getAcct(conn, data, "B_INTERESTEXP_ACCT"), getAcct(conn, data, "B_UNIDENTIFIED_ACCT"), getAcct(conn, data, 
"B_SETTLEMENTGAIN_ACCT"), getAcct(conn, data, "B_SETTLEMENTLOSS_ACCT"), getAcct(conn, data, "B_REVALUATIONGAIN_ACCT"), getAcct(conn, 
data, "B_REVALUATIONLOSS_ACCT"), getAcct(conn, data, "B_PAYMENTSELECT_ACCT"), getAcct(conn, data, "B_UNALLOCATEDCASH_ACCT"), 
getAcct(conn, data, "CH_EXPENSE_ACCT"), getAcct(conn, data, "CH_REVENUE_ACCT"), getAcct(conn, data, "UNEARNEDREVENUE_ACCT"), 
getAcct(conn, data, "NOTINVOICEDRECEIVABLES_ACCT"), getAcct(conn, data, "NOTINVOICEDREVENUE_ACCT"), getAcct(conn, data, 
"NOTINVOICEDRECEIPTS_ACCT"), getAcct(conn, data, "CB_ASSET_ACCT"), getAcct(conn, data, "CB_CASHTRANSFER_ACCT"), getAcct(conn, data, 
"CB_DIFFERENCES_ACCT"), getAcct(conn, data, "CB_EXPENSE_ACCT"), getAcct(conn, data, "CB_RECEIPT_ACCT"),C_AcctSchema_Default_ID,
getAcct(conn, data, "A_DEPRECIATION_ACCT"),getAcct(conn, data, "A_ACCUMDEPRECIATION_ACCT"),getAcct(conn, data, "A_DISPOSAL_LOSS"),getAcct(conn, data, "A_DISPOSAL_GAIN"))  != 1) {
        String err = "InitialClientSetup - createAccounting - Default Accounts NOT inserted";
        log4j.warn(err);
        m_info.append(err);
        return false;
      }
      releaseCommitConnection(conn);
    } catch (Exception ex) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException(ex.getMessage());
    }
    m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateAccountingSuccess", vars.getLanguage())).append(SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateAccountingSuccess", vars.getLanguage())).append(SALTO_LINEA);
    return true;
  }   //  createAccounting  

  public boolean createDocumentTypes(VariablesSecureApp vars) throws ServletException{
    Connection conn = null;
    //  Standard variables
    m_info.append(SALTO_LINEA);
    try {
      conn = this.getTransactionConnection();
    } catch (Exception ignored) {}
    //  GL Categories
    String GL_Standard = createGLCategory(vars,"Standard", "M", true);
    String GL_None = createGLCategory(vars,"None", "D", false);
    String GL_GL = createGLCategory(vars,"Manual", "M", false);
    String GL_ARI = createGLCategory(vars,"AR Invoice", "D", false);
    String GL_ARR = createGLCategory(vars,"AR Receipt", "D", false);
    String GL_MM = createGLCategory(vars,"Material Management", "D", false);
    String GL_API = createGLCategory(vars,"AP Invoice", "D", false);
    String GL_APP = createGLCategory(vars,"AP Payment", "D", false);
    String GL_STT = createGLCategory(vars,"Settlement", "D", false);
    String GL_CMB = createGLCategory(vars,"Bank Statement", "D", false);
    String GL_CMC = createGLCategory(vars,"Cash", "D", false);
    String GL_MMI = createGLCategory(vars,"Inventory", "D", false);
    String GL_MMM = createGLCategory(vars,"Movement", "D", false);
    String GL_MMP = createGLCategory(vars,"Production", "D", false);
    String GL_MXI = createGLCategory(vars,"MatchInv", "D", false);
    String GL_MXP = createGLCategory(vars,"MatchPO", "D", false);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createDocumentTypes - GL CATEGORIES CREATED");

    //  Base DocumentTypes
    createDocType(vars,"GL Journal", "Journal", "GLJ", "", "0", "0", "1000", GL_GL, "224");
    String DT_I = createDocType(vars,"AR Invoice", "Invoice", "ARI", "", "0", "0", "100000", GL_ARI, "318");
    String DT_II = createDocType(vars,"AR Invoice Indirect", "Invoice Indirect.", "ARI", "", "0", "0", "200000", GL_ARI, "318");
    createDocType(vars,"AR Credit Memo", "Credit Memo", "ARC", "", "0", "0", "300000", GL_ARI, "318");
    createDocType(vars,"AR Receipt", "Receipt", "ARR", "", "0", "0", "400000", GL_ARR, "");
    String DT_S  = createDocType(vars,"MM Shipment", "Delivery Note", "MMS", "", "0", "0", "500000", GL_MM, "319");
    String DT_SI = createDocType(vars,"MM Shipment Indirect", "Delivery Note", "MMS", "", "0", "0", "600000", GL_MM, "319");
    createDocType(vars,"MM Receipt", "Vendor Delivery", "MMR", "", "0", "0", "0", GL_MM, "319");
    createDocType(vars,"AP Invoice", "Vendor Invoice", "API", "", "0", "0", "0", GL_API, "318");
    createDocType(vars,"AP CreditMemo", "Vendor Credit Memo", "APC", "", "0", "0", "0", GL_API, "318");
    createDocType(vars,"AP Payment", "Vendor Payment", "APP", "", "0", "0", "700000", GL_APP, "");
    createDocType(vars,"Purchase Order", "Purchase Order", "POO", "", "0", "0", "800000", GL_None, "259");
    createDocType(vars,"Purchase Requisition", "Purchase Requisition", "POR", "", "0", "0", "900000", GL_None, "259");


    createDocType(vars,"Settlement", "Settlement", "STT", "", "0", "0", "10000", GL_STT, "800019");
    createDocType(vars,"Manual Settlement", "Manual Settlement", "STM", "", "0", "0", "10000", GL_STT, "800019");
    createDocType(vars,"Bank Statement", "Bank Statement", "CMB", "", "0", "0", "1000000", GL_CMB, "392");
    createDocType(vars,"Cash Journal", "Cash Journal", "CMC", "", "0", "0", "1000000", GL_CMC, "407");
    createDocType(vars,"Physical Inventory", "Physical Inventory", "MMI", "", "0", "0", "1000000", GL_MMI, "321");
    createDocType(vars,"Inventory Move", "Inventory Move", "MMM", "", "0", "0", "1000000", GL_MMM, "323");
    createDocType(vars,"Production", "Production", "MMP", "", "0", "0", "1000000", GL_MMP, "325");
    createDocType(vars,"Matched Invoices", "Matched Invoices", "MXI", "", "0", "0", "1000000", GL_MXI, "472");
    createDocType(vars,"Matched Purchase Orders", "Matched Purchase Orders", "MXP", "", "0", "0", "1000000", GL_MXP, "473");
    createDocType(vars,"Debt Payement Management", "Debt Payement Management", "DPM", "", "0", "0", "10000", GL_Standard, "800176");
    createDocType(vars,"Depreciation", "Depreciation", "AMZ", "", "0", "0", "10000", GL_Standard, "800060");

    //  Order Entry
    createDocType(vars,"Quotation", "Binding offer", "SOO", "OB", "0", "0", "10000", GL_None, "259");
    createDocType(vars,"Proposal", "Non binding offer", "SOO", "ON", "0", "0", "20000", GL_None, "259");
    createDocType(vars,"Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, "30000", GL_None, "259");
    createDocType(vars,"Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, "40000", GL_None, "259");
    createDocType(vars,"Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, "50000", GL_None, "259");
    createDocType(vars,"Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, "60000", GL_None, "259");   //  RE
    String DT_WO = createDocType(vars,"Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, "70000", GL_None, "259");    //  LS
    String DT = createDocType(vars,"POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, "80000", GL_None, "259");    // Bar
    createPreference(vars,"C_DocTypeTarget_ID", DT, "143");
    createPreference(vars,"C_DocTypeTarget_ID", DT_WO, "800004");
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createDocumentTypes - DOCTYPES & PREFERENCE CREATED");

    try {
      //  Update ClientInfo
      if (InitialClientSetupData.updateClientInfo(conn ,this,C_AcctSchema_ID, C_Calendar_ID, AD_Client_ID) != 1){
        String err = "InitialClientSetup - createDocumentTypes - ClientInfo not updated";
        log4j.warn(err);
        m_info.append(err).append(SALTO_LINEA);
        releaseRollbackConnection(conn);
        return false;
      }
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createDocumentTypes - CLIENT INFO UPDATED");
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createDocumentTypes - m_info last: " + m_info.toString());
      //
      releaseCommitConnection(conn);
    } catch (SQLException ex2) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
    m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateDocumentTypesSuccess", vars.getLanguage())).append(SALTO_LINEA);
    strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateDocumentTypesSuccess", vars.getLanguage())).append(SALTO_LINEA);
    return true;
}

  private String createGLCategory (VariablesSecureApp vars, String Name, String CategoryType, boolean isDefault)throws ServletException{
    Connection conn = null;
    String GL_Category_ID = "";
    try {
      conn = this.getTransactionConnection();
      GL_Category_ID = SequenceIdData.getSequence(this, "GL_Category", client);
      String strisDefault = (isDefault ? "Y" : "N");
      if (InitialClientSetupData.insertCategory(conn ,this,GL_Category_ID, AD_Client_ID, Name, CategoryType, strisDefault) != 1)
      log4j.warn("InitialClientSetup - createGLCategory - GL Logger NOT created - " + Name);
      m_info.append(Utility.messageBD(this, "GL_Category", vars.getLanguage())).append("=").append(Name).append(SALTO_LINEA);
      //
      releaseCommitConnection(conn);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
    return GL_Category_ID;
  }

  private void createPreference (VariablesSecureApp vars, String Attribute, String Value, String AD_Window_ID)throws ServletException{
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      String AD_Preference_ID = SequenceIdData.getSequence(this, "AD_Preference", client);
      if (InitialClientSetupData.insertPreference(conn ,this,AD_Preference_ID, AD_Client_ID, Attribute, Value, AD_Window_ID) != 1)
        log4j.warn("InitialClientSetup - createPreference - Preference NOT inserted - " + Attribute);
      releaseCommitConnection(conn);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
  }   //  createPreference

  private String createDocType (VariablesSecureApp vars, String Name, String PrintName,String DocBaseType, String DocSubTypeSO,
    String C_DocTypeShipment_ID, String C_DocTypeInvoice_ID,String StartNo, String GL_Category_ID, String strTableId)throws ServletException{
    Connection conn = null;
    String C_DocType_ID = "";
    try {
      conn = this.getTransactionConnection();
      //  Get Sequence
      String AD_Sequence_ID = "";
      if (!StartNo.equals("0")){//  manual sequenec, if startNo == 0
        AD_Sequence_ID = SequenceIdData.getSequence(this, "AD_Sequence", client);
        log4j.debug("inserting sequence ID:"+AD_Sequence_ID+" name: "+Name);
        if (InitialClientSetupData.insertSequence(conn ,this,AD_Sequence_ID, AD_Client_ID, Name, StartNo) != 1)
        log4j.warn("InitialClientSetup - createDocType - Sequence NOT created - " + Name);
      }
  
      //  Get Document Type
      C_DocType_ID = SequenceIdData.getSequence(this, "C_DocType", client);
      String IsDocNoControlled = "";
      String IsSOTrx = "";
      if (AD_Sequence_ID.equals(""))
        IsDocNoControlled = "N";
      else
        IsDocNoControlled = "Y";
      String IsTransferred = "";
      if (DocBaseType.equals("SOO"))
        IsTransferred = "N";
      else
        IsTransferred = "Y";
      if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
        IsSOTrx = "Y";
      else
        IsSOTrx = "N";
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createDocType - C_DocType_ID: " + C_DocType_ID + ", AD_Client_ID: " + AD_Client_ID + ", Name: " + Name + ", PrintName: " + PrintName + "DocBaseType: " + DocBaseType + ", DocSubTypeSO: " + DocSubTypeSO + ", C_DocTypeShipment_ID: " + C_DocTypeShipment_ID + ", C_DocTypeInvoice_ID: " + C_DocTypeInvoice_ID + ", IsDocNoControlled: " + IsDocNoControlled + ", AD_Sequence_ID: " + AD_Sequence_ID + ", GL_Category_ID: " + GL_Category_ID + ", IsTransferred: " + IsTransferred + ", IsSOTrx: " + IsSOTrx);
      if (InitialClientSetupData.insertDocType(conn ,this,C_DocType_ID, AD_Client_ID, Name, PrintName,DocBaseType,
      DocSubTypeSO, C_DocTypeShipment_ID, C_DocTypeInvoice_ID, IsDocNoControlled, AD_Sequence_ID, GL_Category_ID,
      IsTransferred, IsSOTrx, strTableId) != 1)
      log4j.warn("InitialClientSetup - createDocType - DocType NOT created - " + Name);
      //
      m_info.append(Utility.messageBD(this, "C_DocType", vars.getLanguage())).append("=").append(Name).append(SALTO_LINEA);
      releaseCommitConnection(conn);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
    return C_DocType_ID;
  }   //  createDocType



public boolean createEntities (VariablesSecureApp vars,String C_Country_ID, String City, String C_Region_ID) throws ServletException{
  m_info.append(SALTO_LINEA).append("----").append(SALTO_LINEA);
  Connection conn = null;
  try {
    conn = this.getTransactionConnection();
    //
    String defaultName = Utility.messageBD(this, "Standard", vars.getLanguage());
  
    //    Create Marketing Channel/Campaign
    String C_Channel_ID = SequenceIdData.getSequence(this, "C_Channel", client);
    if (InitialClientSetupData.insertChannel(conn ,this,C_Channel_ID, defaultName, AD_Client_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - Channel NOT inserted");
    String C_Campaign_ID = SequenceIdData.getSequence(this, "C_Campaign", client);
    if (InitialClientSetupData.insertCampaign(conn ,this,C_Campaign_ID,C_Channel_ID, AD_Client_ID, defaultName) == 1)
      m_info.append(Utility.messageBD(this, "C_Campaign_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - Campaign NOT inserted");
    if (m_hasMCampaign) {
      //  Default
      if (InitialClientSetupData.updateAcctSchemaElementMC(conn ,this,C_Campaign_ID,C_AcctSchema_ID) != 1)
        log4j.warn("InitialClientSetup - createEntities - AcctSchema ELement Campaign NOT updated");
    }
    releaseCommitConnection(conn);
    conn = this.getTransactionConnection();
  
    //    Create Sales Region
    String C_SalesRegion_ID = SequenceIdData.getSequence(this, "C_SalesRegion", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - C_SalesRegion_ID: " + C_SalesRegion_ID + ", AD_Client_ID: " + AD_Client_ID + ", defaultName: " + defaultName);
    if (InitialClientSetupData.insertSalesRegion(conn ,this,C_SalesRegion_ID,AD_Client_ID,defaultName) == 1)
      m_info.append(Utility.messageBD(this, "C_SalesRegion_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - SalesRegion NOT inserted");
    if (m_hasSRegion) {
      //  Default
      if (InitialClientSetupData.updateAcctSchemaElementSR(conn ,this,C_SalesRegion_ID,C_AcctSchema_ID) != 1)
        log4j.warn("InitialClientSetup - createEntities - AcctSchema ELement SalesRegion NOT updated");
    }
    releaseCommitConnection(conn);
    conn = this.getTransactionConnection();
  
    /**
     *  Business Partner
     */
    //  Create BP Group
    String C_BP_Group_ID = SequenceIdData.getSequence(this, "C_BP_Group", client);
    if (InitialClientSetupData.insertBPGroup(conn ,this,C_BP_Group_ID,AD_Client_ID,defaultName) == 1)
      m_info.append(Utility.messageBD(this, "C_BP_Group_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - BP Group NOT inserted");
  
    //    Create BPartner
    String C_BPartner_ID = SequenceIdData.getSequence(this, "C_BPartner", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - C_BPartner_ID: " + C_BPartner_ID + ", AD_Client_ID: " + AD_Client_ID + ", defaultName: " + defaultName + ", C_BP_Group_ID: " + C_BP_Group_ID);
    if (InitialClientSetupData.insertBPartner(conn ,this,C_BPartner_ID,AD_Client_ID,defaultName, C_BP_Group_ID) == 1)
      m_info.append(Utility.messageBD(this, "C_BPartner_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - BPartner NOT inserted");
    //  Default
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - C_BPartner_ID: " + C_BPartner_ID + ", C_AcctSchema_ID: " + C_AcctSchema_ID);
    if (InitialClientSetupData.updateAcctSchemaElementBP(conn ,this,C_BPartner_ID,C_AcctSchema_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - AcctSchema Element BPartner NOT updated");
      if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - C_BPartner_ID: " + C_BPartner_ID + ", C_AcctSchema_ID: " + C_AcctSchema_ID);
  
    //**************************************************************************************************************
    //**************************************************************************************************************
    //createPreference(vars,"C_BPartner_ID", C_BPartner_ID, "143");
    releaseCommitConnection(conn);
    conn = this.getTransactionConnection();
  
    /**
     *  Asset
     */
    //  Create Asset Group
    String A_Asset_Group_ID = SequenceIdData.getSequence(this, "A_Asset_Group", client);
    if (InitialClientSetupData.insertAssetGroup(conn ,this,A_Asset_Group_ID,AD_Client_ID,defaultName) == 1)
      m_info.append(Utility.messageBD(this, "A_Asset_Group", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - Asset Group NOT inserted");
  
  
    /**
     *  Product
     */
    //  Create Product Category
    String M_Product_Category_ID = SequenceIdData.getSequence(this, "M_Product_Category", client);
    if (InitialClientSetupData.insertProductCategory(conn ,this,M_Product_Category_ID,AD_Client_ID, defaultName) == 1)
      m_info.append(Utility.messageBD(this, "M_Product_Category_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - Product Logger NOT inserted");
  
    //  UOM (EA)
    String C_UOM_ID = "100";
  
    //  TaxCategory
    String append="";
    String C_TaxCategory_ID = SequenceIdData.getSequence(this, "C_TaxCategory", client);
    if (C_Country_ID.equals("100"))    // US
      append="Sales Tax";
    else
      append=defaultName;
    if (InitialClientSetupData.insertTaxCategory(conn ,this,C_TaxCategory_ID,AD_Client_ID, append) != 1)
      log4j.warn("InitialClientSetup - createEntities - TaxCategory NOT inserted");
  
    //  Tax - Zero Rate
    String C_Tax_ID = SequenceIdData.getSequence(this, "C_Tax", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - C_Tax_ID: " + C_Tax_ID + ", AD_Client_ID: " + AD_Client_ID + ", defaultName: " + defaultName + ", C_TaxCategory_ID: " + C_TaxCategory_ID + ", C_Country_ID: " + C_Country_ID);
    if (InitialClientSetupData.insertTax(conn ,this,C_Tax_ID,AD_Client_ID,defaultName,C_TaxCategory_ID, C_Country_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - Tax NOT inserted");
  
    //    Create Product
    String M_Product_ID = SequenceIdData.getSequence(this, "M_Product", client);
    if (InitialClientSetupData.insertProduct(conn ,this,M_Product_ID,AD_Client_ID,defaultName,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) == 1)
      m_info.append(Utility.messageBD(this, "M_Product_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - Product NOT inserted");
    //  Default
    if (InitialClientSetupData.updateAcctSchemaElementPR(conn ,this,M_Product_ID,C_AcctSchema_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - AcctSchema Element Product NOT updated");
    releaseCommitConnection(conn);
    conn = this.getTransactionConnection();
  
    /**
     *  Warehouse
     */
    //  Location (Company)
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_Location: ");
    String C_Location_ID = SequenceIdData.getSequence(this, "C_Location", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - THE SEQUENCE -C_Location_ID: " + C_Location_ID);
    InitialClientSetupData.insertLocation(conn ,this,C_Location_ID,AD_Client_ID,City ,C_Country_ID, C_Region_ID);
    if (InitialClientSetupData.updateOrgInfo(conn ,this,C_Location_ID,AD_Org_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - Location NOT inserted");
  
    //*******************************************************************************************
    //*******************************************************************************************
    createPreference(vars,"C_Country_ID", C_Country_ID, "");
  
    //  Default Warehouse
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -M_Warehouse: ");
    String M_Warehouse_ID = SequenceIdData.getSequence(this, "M_Warehouse", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - THE SEQUENCE -M_Warehouse_ID: " + M_Warehouse_ID);
    if (InitialClientSetupData.insertWarehouse(conn ,this,M_Warehouse_ID,AD_Client_ID,AD_Org_ID, defaultName, C_Location_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - Warehouse NOT inserted");
  
    //   Locator
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -M_Locator: ");
    String M_Locator_ID = SequenceIdData.getSequence(this, "M_Locator", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -M_Locator_ID: " + M_Locator_ID);
    if (InitialClientSetupData.insertLocator(conn ,this,M_Locator_ID,AD_Client_ID, defaultName, M_Warehouse_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - Locator NOT inserted");
  
    //  Update ClientInfo
    if (InitialClientSetupData.updateClientInfo2(conn ,this,C_BPartner_ID,M_Product_ID, AD_Client_ID) != 1)
    {
      String err = "InitialClientSetup - createEntities - ClientInfo not updated";
      log4j.warn(err);
      m_info.append(err).append(SALTO_LINEA);
      return false;
    }
    releaseCommitConnection(conn);
    conn = this.getTransactionConnection();
  
    /**
     *  Other
     */
    //  PriceList
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -M_PriceList: ");
    String M_PriceList_ID = SequenceIdData.getSequence(this, "M_PriceList", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -M_PriceList_ID: " + M_PriceList_ID);
    if (InitialClientSetupData.insertPriceList(conn ,this,M_PriceList_ID, AD_Client_ID,defaultName, C_Currency_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - PriceList NOT inserted");
    //  DiscountSchema
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -M_DiscountSchema: ");
    String M_DiscountSchema_ID = SequenceIdData.getSequence(this, "M_DiscountSchema", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -M_DiscountSchema_ID: " + M_DiscountSchema_ID);
    if (InitialClientSetupData.insertDiscountSchema(conn ,this,M_DiscountSchema_ID, AD_Client_ID,defaultName) != 1)
      log4j.warn("InitialClientSetup - createEntities - DiscountSchema NOT inserted");
    //  PriceList Version
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -M_PriceList_Version: ");
    String M_PriceList_Version_ID = SequenceIdData.getSequence(this, "M_PriceList_Version", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -M_PriceList_Version_ID: " + M_PriceList_Version_ID);
    if (InitialClientSetupData.insertPriceListVersion(conn ,this,M_PriceList_Version_ID, AD_Client_ID,M_PriceList_ID, M_DiscountSchema_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - PriceList_Version NOT inserted");
    //  ProductPrice
    if (InitialClientSetupData.insertProductPrice(conn ,this,M_PriceList_Version_ID, AD_Client_ID,M_Product_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - ProductPrice NOT inserted");
  
    //  Location for Standard BP
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_Location: ");
    C_Location_ID = SequenceIdData.getSequence(this, "C_Location", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_Location_ID: " + C_Location_ID);
    InitialClientSetupData.insertLocation(conn ,this,C_Location_ID, AD_Client_ID,City, C_Country_ID, C_Region_ID);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_Location_ID: " + C_Location_ID);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_BPartner_Location: ");
    String C_BPartner_Location_ID = SequenceIdData.getSequence(this, "C_BPartner_Location", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_BPartner_Location_ID: " + C_BPartner_Location_ID);
    if (InitialClientSetupData.insertBPartnerLocation(conn ,this,C_BPartner_Location_ID, AD_Client_ID,City, C_BPartner_ID, C_Location_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - BP_Location NOT inserted");
  
    //    Create Sales Rep for User
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_BPartner: ");
    C_BPartner_ID = SequenceIdData.getSequence(this, "C_BPartner", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_BPartner_ID: " + C_BPartner_ID);
    if (InitialClientSetupData.insertBPartner2(conn ,this,C_BPartner_ID, AD_Client_ID,AD_User_U_Name, C_BP_Group_ID) == 1)
      m_info.append(Utility.messageBD(this, "IsSalesRep", vars.getLanguage())).append("=").append(AD_User_U_Name).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - SalesRep (User) NOT inserted");
    //  Update User
    if (InitialClientSetupData.updateUser(conn ,this, C_BPartner_ID,AD_User_U_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - User of SalesRep (User) NOT updated");
  
    //    Create Sales Rep for Admin
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_BPartner: ");
    C_BPartner_ID = SequenceIdData.getSequence(this, "C_BPartner", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_BPartner_ID: " + C_BPartner_ID);
    if (InitialClientSetupData.insertBPartner2(conn ,this,C_BPartner_ID, AD_Client_ID,AD_User_Name, C_BP_Group_ID) == 1)
      m_info.append(Utility.messageBD(this, "IsSalesRep", vars.getLanguage())).append("=").append(AD_User_Name).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - SalesRep (Admin) NOT inserted");
    //  Update User
    if (InitialClientSetupData.updateUser(conn ,this, C_BPartner_ID,AD_User_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - User of SalesRep (Admin) NOT updated");
  
    //  Payment Term
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_PaymentTerm: ");
    String C_PaymentTerm_ID = SequenceIdData.getSequence(this, "C_PaymentTerm", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_PaymentTerm_ID: " + C_PaymentTerm_ID);
    if (InitialClientSetupData.insertPaymentTerm(conn ,this, C_PaymentTerm_ID,AD_Client_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - PaymentTerm NOT inserted");
  
    //  Project Cycle
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_Cycle: ");
    C_Cycle_ID = SequenceIdData.getSequence(this, "C_Cycle", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_Cycle_ID: " + C_Cycle_ID);
    if (InitialClientSetupData.insertCycle(conn ,this, C_Cycle_ID,AD_Client_ID, defaultName, C_Currency_ID) != 1)
      log4j.warn("InitialClientSetup - createEntities - Cycle NOT inserted");
  
    releaseCommitConnection(conn);
    conn = this.getTransactionConnection();
  
    /**
     *  Organization level data   ===========================================
     */
  
    //    Create Default Project
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_Project: ");
    String C_Project_ID = SequenceIdData.getSequence(this, "C_Project", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_Project_ID: " + C_Project_ID + ", AD_Client_ID: " + AD_Client_ID + ", AD_Org_ID: " + AD_Org_ID + ", defaultName: " + defaultName + ", C_Currency_ID: " + C_Currency_ID);
    if (InitialClientSetupData.insertProject(conn ,this, C_Project_ID,AD_Client_ID,AD_Org_ID, defaultName, C_Currency_ID) == 1)
      m_info.append(Utility.messageBD(this, "C_Project_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - Project NOT inserted");
    //  Default Project
    if (m_hasProject) {
      if (InitialClientSetupData.updateAcctSchemaElement3(conn ,this, C_Project_ID,C_AcctSchema_ID) != 1)
        log4j.warn("InitialClientSetup - createEntities - AcctSchema ELement Project NOT updated");
    }
  
    //  CashBook
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities - SEQUENCE GENERATION -C_CashBook: ");
    String C_CashBook_ID = SequenceIdData.getSequence(this, "C_CashBook", client);
    if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - createEntities -  THE SEQUENCE -C_CashBook_ID: " + C_CashBook_ID);
    if (InitialClientSetupData.insertCashBook(conn ,this, C_CashBook_ID,AD_Client_ID,AD_Org_ID,defaultName,C_Currency_ID) == 1)
      m_info.append(Utility.messageBD(this, "C_CashBook_ID", vars.getLanguage())).append("=").append(defaultName).append(SALTO_LINEA);
    else
      log4j.warn("InitialClientSetup - createEntities - CashBook NOT inserted");
  
  
    //  Create Other Defaults
  /*  try {
      InitialClientSetupData.setup(this, AD_Client_ID,AD_Org_ID);
    }
    catch (Exception e) {
      log4j.warn("InitialClientSetup.CreateEntities - Call AD_Setup", e);
    }*/
    releaseCommitConnection(conn);
  } catch (NoConnectionAvailableException ex) {
    throw new ServletException("@CODE=NoConnectionAvailable");
  } catch (SQLException ex2) {
    try {
      releaseRollbackConnection(conn);
    } catch (Exception ignored) {}
    throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
  } catch (Exception ex3) {
    try {
      releaseRollbackConnection(conn);
    } catch (Exception ignored) {}
    throw new ServletException("@CODE=@" + ex3.getMessage());
  }
  m_info.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateMasterDataSuccess", vars.getLanguage())).append(SALTO_LINEA).append(SALTO_LINEA).append(SALTO_LINEA).append(SALTO_LINEA);
  strSummary.append(SALTO_LINEA).append(Utility.messageBD(this, "CreateMasterDataSuccess", vars.getLanguage())).append(SALTO_LINEA);
  return true;
}

    private String getAcct(Connection conn, AccountingValueData [] data, String key) throws ServletException {
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - getAcct - " + key);
        String C_ElementValue_ID = getC_ElementValue_ID(data, key);
         if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - getAcct - C_ElementValue_ID: " + C_ElementValue_ID);
        Account vc = Account.getDefault(m_AcctSchema, true);
        vc.Account_ID=C_ElementValue_ID;
        vc.save(conn, this, AD_Client_ID, "");// BEFORE, HERE IT WAS 0
        String C_ValidCombination_ID = vc.C_ValidCombination_ID;
        if(C_ValidCombination_ID.equals("")) {
          log4j.warn("InitialClientSetup - getAcct - C_ElementValue_ID: " + C_ElementValue_ID);
          log4j.warn("InitialClientSetup - getAcct - no account for " + key);
          C_ValidCombination_ID = "";// HERE IT WAS 0
        }
        if (log4j.isDebugEnabled()) log4j.debug("InitialClientSetup - getAcct - "+ key + "-- valid combination:" + C_ValidCombination_ID);
        return C_ValidCombination_ID;
    }

 }
