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

  
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


// imports for transactions
import java.sql.Connection;

public class CopyFromGLJournal extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      vars.getGlobalVariable("inpwindowId", "CopyFromGLJournal|windowId");
      vars.getGlobalVariable("inpTabId", "CopyFromGLJournal|tabId");
      vars.getGlobalVariable("inpglJournalbatchId", "CopyFromGLJournal|glJournalbatchId");
      printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strWindow = vars.getGlobalVariable("inpwindowId", "CopyFromGLJournal|windowId");
      String strTab = vars.getGlobalVariable("inpTabId", "CopyFromGLJournal|tabId");
      String strDescription = vars.getStringParameter("inpDescription", "");
      String strDocumentNo = vars.getStringParameter("inpDocumentNo", "");
      printPageFrame1(response, vars, strDescription, strDocumentNo, strWindow, strTab);
    } else if (vars.commandIn("FRAME2")) {
      String strWindow = vars.getGlobalVariable("inpwindowId", "CopyFromGLJournal|windowId");
      String strTab = vars.getGlobalVariable("inpTabId", "CopyFromGLJournal|tabId");
      String strDescription = vars.getStringParameter("inpDescription");
      String strDocumentNo = vars.getStringParameter("inpDocumentNo");
      String strKey = vars.getGlobalVariable("inpglJournalbatchId", "CopyFromGLJournal|glJournalbatchId");
      printPageFrame2(response, vars, strDescription, strDocumentNo, strWindow, strTab, strKey);
    } else if (vars.commandIn("FIND")) {
      String strWindow = vars.getGlobalVariable("inpwindowId", "CopyFromGLJournal|windowId");
      String strTab = vars.getGlobalVariable("inpTabId", "CopyFromGLJournal|tabId");
      String strDescription = vars.getStringParameter("inpDescription");
      String strDocumentNo = vars.getStringParameter("inpDocumentNo");
      String strKey = vars.getGlobalVariable("inpglJournalbatchId", "CopyFromGLJournal|glJournalbatchId");
      printPageFrame2(response, vars, strDescription, strDocumentNo, strWindow, strTab, strKey);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTab);
      String strWindowPath="", strTabName="";
      if (tab!=null && tab.length!=0) {
        strTabName = FormatUtilities.replace(tab[0].name);
        if (tab[0].help.equals("Y")) strWindowPath="../utility/WindowTree_FS.html?inpTabId=" + strTab;
        else strWindowPath = "../" + FormatUtilities.replace(tab[0].description) + "/" + strTabName + "_Relation.html";
      } else strWindowPath = strDefaultServlet;
      String strKey = vars.getRequiredStringParameter("inpglJournalbatchId");
      String strGLJournalBatch = vars.getStringParameter("inpClave");
      
      
     
      
       OBError myError = null;
       try {
        myError = processButton(vars, strKey, strGLJournalBatch, strWindow);
      } catch(ServletException ex) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        }
      }
      
      vars.setMessage(strTab, myError);
      
      printPageClosePopUp(response, vars, strWindowPath);
    } else pageErrorPopUp(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: GLJournalBatch seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/CopyFromGLJournal_FS").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strDescription, String strDocumentNo, String strWindow, String strTab) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the GLJournalBatch seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/CopyFromGLJournal_F1").createXmlDocument();
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("window", strWindow);
    xmlDocument.setParameter("tab", strTab);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }


  OBError processButton(VariablesSecureApp vars, String strKey, String strGLJournalBatch, String windowId) throws ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: GLJournal");
    if (strGLJournalBatch.equals("")) return new OBError();;
    Connection conn = null;
    
    OBError myError = null;
    try {
      conn = this.getTransactionConnection();
      CopyFromGLJournalData [] data = CopyFromGLJournalData.select(this, strKey,strGLJournalBatch);
      for(int i=0;data != null && i<data.length;i++){
        String strSequence = SequenceIdData.getSequence(this, "GL_Journal", vars.getClient());
        String strDocumentNo = Utility.getDocumentNo(this, vars, windowId, "GL_Journal", Utility.getContext(this, vars, "C_DocTypeTarget_ID", "132"), Utility.getContext(this, vars, "C_DocType_ID", "132"), false, true);
        if(CopyFromGLJournalData.insertGLJournal(conn,this,strSequence, vars.getClient(), vars.getOrg(), vars.getUser(), data[i].cAcctschemaId, data[i].cDoctypeId, "DR", "CO", data[i].isapproved, data[i].isprinted, data[i].description, data[i].postingtype, data[i].glBudgetId, data[i].glCategoryId, data[i].datedoc, data[i].dateacct, data[i].cPeriodId, data[i].cCurrencyId, data[i].currencyratetype, data[i].currencyrate,strKey, data[i].controlamt, strDocumentNo, "N", "N", "N")==0)log4j.warn("Save: GLJournal record " + i + " not inserted. Sequence = " + strSequence);
        CopyFromGLJournalData [] dataLines = CopyFromGLJournalData.selectLines(this, data[i].glJournalId);
        for(int j=0;dataLines!=null && j<dataLines.length;j++){
          String strLineSequence = SequenceIdData.getSequence(this, "GL_JournalLine", vars.getClient());
          if(CopyFromGLJournalData.insertGLJournalLine(conn, this, strLineSequence, vars.getClient(), vars.getOrg(), vars.getUser(), strSequence, dataLines[j].line, dataLines[j].isgenerated, dataLines[j].description, dataLines[j].amtsourcedr, dataLines[j].amtsourcecr, dataLines[j].cCurrencyId, dataLines[j].currencyratetype, dataLines[j].currencyrate, dataLines[j].amtacctdr, dataLines[j].amtacctcr, dataLines[j].cUomId, dataLines[j].qty, dataLines[j].cValidcombinationId)==0)log4j.warn("Save: GLJournalLine record " + j + " not inserted. Sequence = " + strLineSequence);
        }
      }
      releaseCommitConnection(conn);
      
    } catch (ServletException ex) {
        try {
          releaseRollbackConnection(conn);
        } catch (Exception ignored) {}
        throw ex;
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=ProcessRunError");
    }
    if (myError==null) {
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle("");
      myError.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    }
    return myError;
  }


  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strDescription, String strDocumentNo, String strWindow,String strTab, String strKey)
    throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: Button process copy GLJournalBatch details");
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/CopyFromGLJournal_F2").createXmlDocument();
      CopyFromGLJournalData [] data = CopyFromGLJournalData.selectFrom(this, strDescription, strDocumentNo, vars.getClient(), Utility.getContext(this, vars, "#User_Org", "CopyFromGLJournal"));
      xmlDocument.setData("structure1", data);
      xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("window", strWindow);
      xmlDocument.setParameter("tab", strTab);
      xmlDocument.setParameter("key", strKey);
      {
        OBError myMessage = vars.getMessage(strTab);
        vars.removeMessage(strTab);
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output:: Button process copy GLJournalBatch details");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/CopyFromGLJournal_F3").createXmlDocument();

    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Project set Type";
  } // end of getServletInfo() method
}

