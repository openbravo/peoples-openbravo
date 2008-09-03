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
 * All portions are Copyright (C) 2001-2007 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.ToolBar;

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;
import java.sql.Connection;
import java.util.*;
import org.openbravo.erpCommon.utility.SequenceIdData;

import org.openbravo.erpCommon.reference.*;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.ad_combos.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;



public class RemittanceCancel extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
//      printPageFS(response, vars);
//    } else if (vars.commandIn("FRAME1")) {
      String strDesde = vars.getGlobalVariable("inpDesde", "RemittanceCancel.inpDesde","");
      String strHasta = vars.getGlobalVariable("inpHasta", "RemittanceCancel.inpHasta","");
      String strRemittanceId = vars.getGlobalVariable("inpRemittanceId", "RemittanceCancel.inpRemittanceId","");
      if (log4j.isDebugEnabled()) log4j.debug("FRAME1--inpDesde:"+strDesde+" inpHasta:"+strHasta+"inpRemittanceId:"+strRemittanceId);
//      printPageFrame1(response, vars, strDesde, strHasta, strRemittanceId);
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
/*    } else if (vars.commandIn("FRAME2")) {
      String strDesde = vars.getGlobalVariable("inpDesde", "RemittanceCancel.inpDesde","");
      String strHasta = vars.getGlobalVariable("inpHasta", "RemittanceCancel.inpHasta","");
      String strRemittanceId = vars.getGlobalVariable("inpRemittanceId", "RemittanceCancel.inpRemittanceId","");
      if (log4j.isDebugEnabled()) log4j.debug("FRAME2--inpDesde:"+strDesde+" inpHasta:"+strHasta+"inpRemittanceId:"+strRemittanceId);
      printPageFrame2(response, vars, strDesde, strHasta, strRemittanceId);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
*/    } else if (vars.commandIn("FIND")) {
      String strDesde = vars.getStringParameter("inpDesde");
      String strHasta = vars.getStringParameter("inpHasta");
      String strRemittanceId = vars.getStringParameter("inpRemittanceId");
      if (!strDesde.equals("")) vars.setSessionValue("RemittanceCancel.inpDesde",strDesde);
      else vars.removeSessionValue("RemittanceCancel.inpDesde");
      if (!strHasta.equals("")) vars.setSessionValue("RemittanceCancel.inpHasta",strDesde);
      else  vars.removeSessionValue("RemittanceCancel.inpHasta");
      if (!strRemittanceId.equals("")) vars.setSessionValue("RemittanceCancel.inpRemittanceId",strRemittanceId);
      else vars.removeSessionValue("RemittanceCancel.inpRemittanceId");
      if (log4j.isDebugEnabled()) log4j.debug("FIND--inpDesde:"+strDesde+" inpHasta:"+strHasta+"inpRemittanceId:"+strRemittanceId);
//      printPageFrame2(response, vars, strDesde, strHasta, strRemittanceId);
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
    } else if (vars.commandIn("CANCEL")) { 
      processCancel(vars);
      String strDesde = vars.getGlobalVariable("inpDesde", "RemittanceCancel.inpDesde","");
      String strHasta = vars.getGlobalVariable("inpHasta", "RemittanceCancel.inpHasta","");
      String strRemittanceId = vars.getGlobalVariable("inpRemittanceId", "RemittanceCancel.inpRemittanceId","");
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
    } else if (vars.commandIn("RETURN")) {      
      processReturn(vars);
      /* vars.setSessionValue("RemittanceCancel.message", strMessage);
      response.sendRedirect(strDireccion + request.getServletPath());*/
      String strDesde = vars.getGlobalVariable("inpDesde", "RemittanceCancel.inpDesde","");
      String strHasta = vars.getGlobalVariable("inpHasta", "RemittanceCancel.inpHasta","");
      String strRemittanceId = vars.getGlobalVariable("inpRemittanceId", "RemittanceCancel.inpRemittanceId","");
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
    }else pageError(response);
  }

  void processReturn(VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("processReturn");
    String strRemittanceLineId = vars.getInStringParameter("inpcRLId");
    String strDateAcct = vars.getRequiredStringParameter("inpDateAcct");
    
    OBError myMessage = null;
    if (strRemittanceLineId.equals("")) return;
    Connection conn = null;
     
    String strDPMID = "";

    try {
      conn = this.getTransactionConnection();
      if (strRemittanceLineId.startsWith("(")) strRemittanceLineId = strRemittanceLineId.substring(1, strRemittanceLineId.length()-1);
      if (!strRemittanceLineId.equals("")) {
        strRemittanceLineId = Replace.replace(strRemittanceLineId, "'", "");
        StringTokenizer st = new StringTokenizer(strRemittanceLineId, ",", false);

        Integer line = null; 
        while (st.hasMoreTokens()) {
          String strRLId = st.nextToken().trim();
          RemittanceCancelData.setReturned(conn, this, strRLId);
          if (strDPMID.equals("")) {
            strDPMID = SequenceIdData.getSequenceConnection(conn, this, "C_DP_Management", vars.getClient());
            line = new Integer(RemittanceCancelData.getLineDPMConnection(conn, this, strDPMID));
            String strDocumentNo = Utility.getDocumentNoConnection(conn, this, vars.getClient(), "C_DP_Management", true);
            RemittanceCancelData.insertDPManagement(conn, this, strDPMID, vars.getClient(), vars.getOrg(), vars.getUser(), strDocumentNo, strDateAcct, strRLId);
          }
          line += 10;
          String strLID = SequenceIdData.getSequenceConnection(conn, this, "C_DP_ManagementLine", vars.getClient());
          RemittanceCancelData.returnDPOriginal(conn, this, strLID, vars.getClient(), vars.getOrg(), vars.getUser(), strDPMID,strRLId, line.toString());

          /*line += 10;
          strLID = SequenceIdData.getSequence(this, "C_DP_ManagementLine", vars.getClient());
          RemittanceCancelData.returnDPGenerated(conn, this, strLID, vars.getClient(), vars.getOrg(), vars.getUser(), strDPMID,strRLId, line.toString());*/
        }
        if (log4j.isDebugEnabled()) log4j.debug("*********************dpmid: "+strDPMID);
        //Call c_dp_management_post
        if (!strDPMID.equals("")){
          String pinstance = SequenceIdData.getSequenceConnection(conn, this, "AD_PInstance", vars.getClient());
          PInstanceProcessData.insertPInstance(conn, this, pinstance, "800140", strDPMID, "N", vars.getUser(), vars.getClient(), vars.getOrg());
          RemittanceCancelData.process800140(conn, this, pinstance);
         

          PInstanceProcessData[] pinstanceData = PInstanceProcessData.selectConnection(conn, this, pinstance);
          myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        }
      
      } else {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
      }
      releaseCommitConnection(conn);
    } catch (Exception e) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
    }
    if (myMessage==null) {
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    }
    vars.setMessage("RemittanceCancel", myMessage);
  }

  void processCancel(VariablesSecureApp vars) throws IOException, ServletException {
   if (log4j.isDebugEnabled()) log4j.debug("processCancel");
    String strRemittanceLineId = vars.getInStringParameter("inpcRLId");
    String strDateAcct = vars.getRequiredStringParameter("inpDateAcct");
    
    if (strRemittanceLineId.equals("")) return;
    Connection conn = null;
   
    String strSettlementID = "";
    String strDocumentNo = "";
    OBError myMessage = null;

    try {
      conn = this.getTransactionConnection();
      if (strRemittanceLineId.startsWith("(")) strRemittanceLineId = strRemittanceLineId.substring(1, strRemittanceLineId.length()-1);
      if (!strRemittanceLineId.equals("")) {
        strRemittanceLineId = Replace.replace(strRemittanceLineId, "'", "");
        StringTokenizer st = new StringTokenizer(strRemittanceLineId, ",", false);
        while (st.hasMoreTokens()) {
          String strRLId = st.nextToken().trim();
          if (log4j.isDebugEnabled()) log4j.debug("RemittanceLineId"+strRLId);
          if (strSettlementID.equals(""))
          {
            String strOrg = RemittanceCancelData.selectOrg(this, strRLId);
            strSettlementID = SequenceIdData.getSequence(this, "C_Settlement", vars.getClient());
            strDocumentNo = Utility.getDocumentNo(this, vars.getClient(), "C_Settlement", true);
            RemittanceCancelData.insertSettlement(conn, this, strSettlementID, vars.getClient(), strOrg, vars.getUser(), strDocumentNo, strDateAcct, strRLId);
            if (log4j.isDebugEnabled()) log4j.debug("Inserted Settlement "+strSettlementID);
          }
          RemittanceCancelData.cancel(conn, this, strSettlementID, strRLId);
          if (log4j.isDebugEnabled()) log4j.debug("Canceled "+strSettlementID);
        }
        //releaseCommitConnection(conn);
        //Call c_settlement_poost
        if (!strSettlementID.equals("")){
          String pinstance = SequenceIdData.getSequence(this, "AD_PInstance", vars.getClient());
          //PInstanceProcessData.insertPInstance(this, pinstance, "800025", strSettlementID, "N", vars.getUser(), vars.getClient(), vars.getOrg());
          if (log4j.isDebugEnabled()) log4j.debug("call c_settlement_post pinstnce "+pinstance);
          RemittanceCancelData.process800025(conn, this, strSettlementID);
          /*PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
          if (log4j.isDebugEnabled()) log4j.debug("got data from instance "+pinstance);*/

          /*if (pinstanceData!=null && pinstanceData.length>0) {
            if (!pinstanceData[0].errormsg.equals("")) {
              String message = pinstanceData[0].errormsg;
              if (message.startsWith("@") && message.endsWith("@")) {
                message = message.substring(1, message.length()-1);
                if (message.indexOf("@")==-1) messageResult = Utility.messageBD(this, message, vars.getLanguage());
                else messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), "@" + message + "@");
              } else {
                messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), message);
              }
            } else if (!pinstanceData[0].pMsg.equals("")) {
              String message = pinstanceData[0].pMsg;
              messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), message);
            } else if (pinstanceData[0].result.equals("1")) {
              messageResult = Utility.messageBD(this, "Success", vars.getLanguage());              
            } else {
              messageResult = Utility.messageBD(this, "Error", vars.getLanguage());
            }

            if (pinstanceData[0].result.equals("1")) 
            { 
              releaseCommitConnection(conn);
              if (log4j.isDebugEnabled()) log4j.debug("commit cancel ");
            } else{ 
              releaseRollbackConnection(conn); 
              if (log4j.isDebugEnabled()) log4j.debug("rollback cancel ");
            }
          } else releaseRollbackConnection(conn);
          messageResult = Replace.replace(messageResult, "'", "\\'");*/
        }
      }
      releaseCommitConnection(conn);
    } catch (Exception e) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.debug("Rollback in transaction");
    }
    if (myMessage==null) {
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage())+" Doc No. "+strDocumentNo);
    }
    vars.setMessage("RemittanceCancel", myMessage);
  }

/*  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: CancelRemittance seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel_FS").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel_F3").createXmlDocument();
    

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strDesde, String strHasta, String strRemittanceId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the CancelRemittance seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel_F1").createXmlDocument();
    
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RemittanceCancel_F1", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    // New interface paramenters
      try {
        WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_process.CashBankOperations");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CashBankOperations.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CashBankOperations.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("CashBankOperations");
        vars.removeMessage("CashBankOperations");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      
     ////----

    if (!strRemittanceId.equals("")) xmlDocument.setParameter("remittanceId",strRemittanceId);
    
    xmlDocument.setData("reportRemittance_ID","liststructure", RemittanceComboData.selectNoCanceled(this, vars.getLanguage(), vars.getUserClient(), vars.getUserOrg()));
    if (!strDesde.equals("")) xmlDocument.setParameter("desde",strDesde);
    if (!strHasta.equals("")) xmlDocument.setParameter("hasta",strHasta);    

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strDesde, String strHasta, String strRemittanceId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the RemittanceCancel seeker");
    XmlDocument xmlDocument;

   RemittanceCancelData data[]=RemittanceCancelData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "RemittanceCancel"), 
                                                                                     Utility.getContext(this, vars, "#User_Org", "RemittanceCancel"),
                                                                                     strRemittanceId,strDesde,strHasta);  
    
   
      
    if (data.length!=0) {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel_F2").createXmlDocument();
      xmlDocument.setData("structure1", data);
    } else {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel_F2",discard).createXmlDocument();
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    String strMessage = vars.getSessionValue("RemittanceCancel.message");
    vars.removeSessionValue("RemittanceCancel.message");
    if (!strMessage.equals(""))
    {
        xmlDocument.setParameter("message", "alert('"+strMessage+"');");
    }
    
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }*/


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDesde, String strHasta, String strRemittanceId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the CancelRemittance seeker");
    XmlDocument xmlDocument;
    RemittanceCancelData data[]=RemittanceCancelData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "RemittanceCancel"), 
                                                                                      Utility.getContext(this, vars, "#User_Org", "RemittanceCancel"),
                                                                                      strRemittanceId,strDesde,strHasta);

    if (data.length!=0) {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel").createXmlDocument();
      xmlDocument.setData("structure1", data);
    } else {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel",discard).createXmlDocument();
    }
//    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel_F1").createXmlDocument();
    
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RemittanceCancel", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateAcctdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateAcctsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    // New interface paramenters
      try {
        WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.RemittanceCancel");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "RemittanceCancel.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "RemittanceCancel.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("RemittanceCancel");
        vars.removeMessage("RemittanceCancel");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      
     ////----

    if (!strRemittanceId.equals("")) xmlDocument.setParameter("remittanceId",strRemittanceId);
    
    xmlDocument.setData("reportRemittance_ID","liststructure", RemittanceComboData.selectNoCanceled(this, vars.getLanguage(), vars.getUserClient(), vars.getUserOrg()));
    if (!strDesde.equals("")) xmlDocument.setParameter("desde",strDesde);
    if (!strHasta.equals("")) xmlDocument.setParameter("hasta",strHasta);    

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the MaterialTransactions seeker";
  } // end of getServletInfo() method
}
