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
package org.openbravo.erpCommon.ad_process;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_combos.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;


// imports for transactions
import java.sql.Connection;

public class ChangeOrderOrg extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strBPartner = vars.getGlobalVariable("inpcBpartnerId", "ChangeOrderOrg.cBpartnerId", "");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "ChangeOrderOrg.adOrgId", "");
      vars.getGlobalVariable("inpadShipperpathId", "ChangeOrderOrg.adShipperpathId", "");
      String strForcedOrg = vars.getGlobalVariable("inpadForcedOrgId", "ChangeOrderOrg.adForcedOrgId", "");
      String strPayment = vars.getGlobalVariable("inppaymentrule", "ChangeOrderOrg.paymentrule", "");
      /*String strNewOrg = vars.getGlobalVariable("inpadOrgIdNew", "ChangeOrderOrg.adOrgIdNew", "");
      String strTax = vars.getGlobalVariable("inpcTaxId", "ChangeOrderOrg.cTaxId", "");*/
      printPage(response, vars, strBPartner, strOrg, strForcedOrg, "", strPayment, "", "");
    } else if (vars.commandIn("FIND")) {
      String strBPartner = vars.getRequestGlobalVariable("inpcBpartnerId", "ChangeOrderOrg.cBpartnerId");
      String strOrg = vars.getRequestGlobalVariable("inpadOrgId", "ChangeOrderOrg.adOrgId");
      String strShipperpath = vars.getRequestGlobalVariable("inpadShipperpathId", "ChangeOrderOrg.adShipperpathId");
      String strForcedOrg = vars.getRequestGlobalVariable("inpadForcedOrgId", "ChangeOrderOrg.adForcedOrgId");
      String strPayment = vars.getRequestGlobalVariable("inppaymentrule", "ChangeOrderOrg.paymentrule");
      String strNewOrg = vars.getStringParameter("inpadOrgIdNew");
      String strTax = vars.getStringParameter("inpcTaxId");
      printPage(response, vars, strBPartner, strOrg, strForcedOrg, strShipperpath, strPayment, strNewOrg, strTax);
    } else if (vars.commandIn("SAVE")) {
      String strNewOrg = vars.getRequiredStringParameter("inpadOrgIdNew");
      String strTax = vars.getStringParameter("inpcTaxId");
      String strOrder = vars.getRequiredInParameter("inpcOrderId");
      // new message system
      OBError myMessage = processSave(vars, strOrder, strNewOrg, strTax);      
      //if (!strMessage.equals("")) vars.setSessionValue("ChangeOrderOrg.message", strMessage);
      vars.setMessage("ChangeOrderOrg", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else pageErrorPopUp(response);
  }

  OBError processSave(VariablesSecureApp vars, String strOrder, String strNewOrg, String strTax) {
	  OBError myMessage = null;
	  
	  myMessage = new OBError();	  
	  myMessage.setTitle("");
	  
    if (log4j.isDebugEnabled()) log4j.debug("Save: ChangeOrderOrg ");
    if (strOrder.equals("")){
    	myMessage.setType("Error");      
        myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
        return myMessage;
    	//return "";
    }
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      if (strOrder.startsWith("(")) strOrder = strOrder.substring(1, strOrder.length()-1);
      if (!strOrder.equals("")) {
        strOrder = Replace.replace(strOrder, "'", "");
        StringTokenizer st = new StringTokenizer(strOrder, ",", false);
        while (st.hasMoreTokens()) {
          String strOrderId = st.nextToken();
          String StrDocBaseType = ChangeOrderOrgData.selectDocbasetype(this, strOrderId);
          String StrDocSubTypeSo = ChangeOrderOrgData.selectDocsubtypeso(this, strOrderId);
          String StrDocTypeID = ChangeOrderOrgData.selectDoctypeneworg(this, StrDocBaseType, StrDocSubTypeSo, strNewOrg);
          String StrExistsShipment = ChangeOrderOrgData.selectExistsShipment(this, strOrderId);
          if (ChangeOrderOrgData.checkRestrictions(conn, this, strOrderId)) {
            releaseRollbackConnection(conn);
            log4j.warn("The order is incorrect");
            myMessage.setType("Error");      
            myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
            return myMessage;
            //return Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
          }
          boolean shouldDelete = ChangeOrderOrgData.shouldDeleteCashLine(conn, this, strOrderId);
          ChangeOrderOrgData.resetOrder(conn, this, strOrderId);
          ChangeOrderOrgData.updateDocAction(conn, this, vars.getUser(), strOrderId);
          String pinstance = SequenceIdData.getSequence(this, "AD_PInstance", vars.getClient());
          ChangeOrderOrgData.insertPInstance(conn, this, pinstance, "104", strOrderId, vars.getUser(), vars.getClient(), vars.getOrg());
          ChangeOrderOrgData.processOrder(conn, this, pinstance);
          ChangeOrderOrgData.updateLines(conn, this, strTax, vars.getUser(), strNewOrg, strOrderId);
          ChangeOrderOrgData.update(conn, this, vars.getUser(), strNewOrg, StrDocTypeID, StrDocTypeID, strOrderId);
            if (!StrExistsShipment.equals("0")) {
                int i=0;
                ChangeOrderOrgData[] shipmentsData = ChangeOrderOrgData.selectShipment(this, strOrderId);
                for (i=0;i<shipmentsData.length;i++) {
                ChangeOrderOrgData.updateProcessed(conn, this, vars.getUser(), shipmentsData[i].mInoutId);
                String StrDocTypeIDShipment = ChangeOrderOrgData.cDocttypeshipment(this, StrDocTypeID);
                String DocumentnoShipment = Utility.getDocumentNo(this, vars, "169", "M_Inout", StrDocTypeIDShipment, StrDocTypeIDShipment, false, true);
                ChangeOrderOrgData.updateShipment(conn, this, vars.getUser(), DocumentnoShipment, StrDocTypeIDShipment, strNewOrg, shipmentsData[i].mInoutId);
                ChangeOrderOrgData.updateShipmentlines(conn, this, vars.getUser(), strNewOrg, shipmentsData[i].mInoutId);
                ChangeOrderOrgData.updateMtransaction(conn, this, strNewOrg, vars.getUser(), shipmentsData[i].mInoutId);
                }
            }
            /*Pending change org for the invoices related*/
          pinstance = SequenceIdData.getSequence(this, "AD_PInstance", vars.getClient());
          ChangeOrderOrgData.insertPInstance(conn, this, pinstance, "104", strOrderId, vars.getUser(), vars.getClient(), vars.getOrg());
          ChangeOrderOrgData.processOrder(conn, this, pinstance);
          if (shouldDelete) ChangeOrderOrgData.deleteCashLine(conn, this, strOrderId);
        }
      }
      releaseCommitConnection(conn);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      
      myMessage.setType("Error");      
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
      //return Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
    }
    //new message system    
    myMessage.setType("Success");    
    myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    return myMessage;
    //return Utility.messageBD(this, "Success", vars.getLanguage());
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner, String strOrg, String strForcedOrg, String strShipperpath, String strPayment, String strNewOrg, String strTax) throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: process ChangeOrderOrg");

      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/ChangeOrderOrg").createXmlDocument();

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ChangeOrderOrg", false, "", "", "",false, "ad_process",  strReplaceWith, false,  true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        KeyMap key = new KeyMap(this, vars, "ChangeOrderOrg.html");
        xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      try {
        WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_process.ChangeOrderOrg");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ChangeOrderOrg.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ChangeOrderOrg.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ChangeOrderOrg");
        vars.removeMessage("ChangeOrderOrg");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");

      xmlDocument.setParameter("bpartner", strBPartner);
      xmlDocument.setParameter("organization", strOrg);
      xmlDocument.setParameter("forcedOrg", strForcedOrg);
      xmlDocument.setParameter("paymentRule", strPayment);
      xmlDocument.setParameter("organizationNew", strNewOrg);
      xmlDocument.setParameter("shipperpath", strShipperpath);
      xmlDocument.setParameter("tax", strTax);
      xmlDocument.setParameter("bpartnerDes", ChangeOrderOrgData.selectBPartner(this, strBPartner));
      //new message system
      //String strMessage = vars.getSessionValue("ChangeOrderOrg.message");
      //if (!strMessage.equals("")) {
      //  vars.removeSessionValue("ChangeOrderOrg.message");
      //  strMessage = "alert('" + Replace.replace(strMessage, "'", "\'") + "');";
      //}
      //xmlDocument.setParameter("body", strMessage);

      xmlDocument.setData("reportAD_Org_ID", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));
      xmlDocument.setData("reportAD_Org_IDNew", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));

        try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Tax_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ChangeOrderOrg"), Utility.getContext(this, vars, "#User_Client", "ChangeOrderOrg"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "ChangeOrderOrg", strTax);
          xmlDocument.setData("reportC_TAX_ID", "liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }


        try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ChangeOrderOrg"), Utility.getContext(this, vars, "#User_Client", "ChangeOrderOrg"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "ChangeOrderOrg", strForcedOrg);
          xmlDocument.setData("reportAD_Forced_Org_ID", "liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }


        try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ChangeOrderOrg"), Utility.getContext(this, vars, "#User_Client","ChangeOrderOrg"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "ChangeOrderOrg", strShipperpath);
          xmlDocument.setData("reportM_Warehouse_ID","liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }

        try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "All_Payment Rule", "", Utility.getContext(this, vars, "#User_Org", "ChangeOrderOrg"), Utility.getContext(this, vars, "#User_Client","ChangeOrderOrg"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "ChangeOrderOrg", "");
          xmlDocument.setData("reportPaymentRule","liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }


      if (vars.commandIn("DEFAULT") && strBPartner.equals("") && strOrg.equals("")) {
        xmlDocument.setData("structure1", new ChangeOrderOrgData[0]);
      } else {
        xmlDocument.setData("structure1", ChangeOrderOrgData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "ChangeOrderOrg"), Utility.getContext(this, vars, "#User_Org", "ChangeOrderOrg"), strBPartner, strOrg, strForcedOrg, strPayment, strShipperpath));
      }
      
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  public String getServletInfo() {
    return "Servlet ChangeOrderOrg";
  } // end of getServletInfo() method
}

