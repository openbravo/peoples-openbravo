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
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class RemittanceCancel extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDesde = vars.getGlobalVariable("inpDesde", "RemittanceCancel.inpDesde", "");
      String strHasta = vars.getGlobalVariable("inpHasta", "RemittanceCancel.inpHasta", "");
      String strRemittanceId = vars.getGlobalVariable("inpRemittanceId",
          "RemittanceCancel.inpRemittanceId", "");
      if (log4j.isDebugEnabled())
        log4j.debug("FRAME1--inpDesde:" + strDesde + " inpHasta:" + strHasta + "inpRemittanceId:"
            + strRemittanceId);
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
    } else if (vars.commandIn("FIND")) {
      String strDesde = vars.getStringParameter("inpDesde");
      String strHasta = vars.getStringParameter("inpHasta");
      String strRemittanceId = vars.getStringParameter("inpRemittanceId");
      if (!strDesde.equals(""))
        vars.setSessionValue("RemittanceCancel.inpDesde", strDesde);
      else
        vars.removeSessionValue("RemittanceCancel.inpDesde");
      if (!strHasta.equals(""))
        vars.setSessionValue("RemittanceCancel.inpHasta", strDesde);
      else
        vars.removeSessionValue("RemittanceCancel.inpHasta");
      if (!strRemittanceId.equals(""))
        vars.setSessionValue("RemittanceCancel.inpRemittanceId", strRemittanceId);
      else
        vars.removeSessionValue("RemittanceCancel.inpRemittanceId");
      if (log4j.isDebugEnabled())
        log4j.debug("FIND--inpDesde:" + strDesde + " inpHasta:" + strHasta + "inpRemittanceId:"
            + strRemittanceId);
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
    } else if (vars.commandIn("CANCEL")) {
      String strRemittanceLineId = vars.getInStringParameter("inpcRLId", IsIDFilter.instance);
      String strDateAcct = vars.getRequiredStringParameter("inpDateAcct");
      OBError myMessage = processCancel(vars, strRemittanceLineId, strDateAcct);
      vars.setMessage("RemittanceCancel", myMessage);
      String strDesde = vars.getGlobalVariable("inpDesde", "RemittanceCancel.inpDesde", "");
      String strHasta = vars.getGlobalVariable("inpHasta", "RemittanceCancel.inpHasta", "");
      String strRemittanceId = vars.getGlobalVariable("inpRemittanceId",
          "RemittanceCancel.inpRemittanceId", "");
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
    } else if (vars.commandIn("RETURN")) {
      String strRemittanceLineId = vars.getInStringParameter("inpcRLId", IsIDFilter.instance);
      String strDateAcct = vars.getRequiredStringParameter("inpDateAcct");
      OBError myMessage = processReturn(vars, strRemittanceLineId, strDateAcct);
      vars.setMessage("RemittanceCancel", myMessage);
      String strDesde = vars.getGlobalVariable("inpDesde", "RemittanceCancel.inpDesde", "");
      String strHasta = vars.getGlobalVariable("inpHasta", "RemittanceCancel.inpHasta", "");
      String strRemittanceId = vars.getGlobalVariable("inpRemittanceId",
          "RemittanceCancel.inpRemittanceId", "");
      printPage(response, vars, strDesde, strHasta, strRemittanceId);
    } else
      pageError(response);
  }

  private OBError processReturn(VariablesSecureApp vars, String strRemittanceLineId,
      String strDateAcct) {
    if (log4j.isDebugEnabled())
      log4j.debug("processReturn");

    OBError myMessage = null;
    if (strRemittanceLineId.equals("")) {
      return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    Connection conn = null;

    String strDPMID = "";

    try {
      conn = this.getTransactionConnection();
      if (strRemittanceLineId.startsWith("("))
        strRemittanceLineId = strRemittanceLineId.substring(1, strRemittanceLineId.length() - 1);
      strRemittanceLineId = Replace.replace(strRemittanceLineId, "'", "");
      StringTokenizer st = new StringTokenizer(strRemittanceLineId, ",", false);

      Integer line = null;
      while (st.hasMoreTokens()) {
        String strRLId = st.nextToken().trim();
        RemittanceCancelData.setReturned(conn, this, strRLId);
        if (strDPMID.equals("")) {
          strDPMID = SequenceIdData.getUUID();
          line = new Integer(RemittanceCancelData.getLineDPMConnection(conn, this, strDPMID));
          String strDocumentNo = Utility.getDocumentNoConnection(conn, this, vars.getClient(),
              "C_DP_Management", true);
          try {
            String strOrg = RemittanceCancelData.selectOrg(this, strRLId);
            RemittanceCancelData.insertDPManagement(conn, this, strDPMID, vars.getClient(), strOrg,
                vars.getUser(), strDocumentNo, strDateAcct, strRLId);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
        line += 10;
        String strLID = SequenceIdData.getUUID();
        RemittanceCancelData.returnDPOriginal(conn, this, strLID, vars.getClient(), vars.getOrg(),
            vars.getUser(), strDPMID, strRLId, line.toString());

      }
      if (log4j.isDebugEnabled())
        log4j.debug("*********************dpmid: " + strDPMID);
      // Call c_dp_management_post
      if (!strDPMID.equals("")) {
        String pinstance = SequenceIdData.getUUID();
        try {
          PInstanceProcessData.insertPInstance(conn, this, pinstance, "800140", strDPMID, "N", vars
              .getUser(), vars.getClient(), vars.getOrg());
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }
        RemittanceCancelData.process800140(conn, this, pinstance);

        PInstanceProcessData[] pinstanceData = PInstanceProcessData.selectConnection(conn, this,
            pinstance);
        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
      }

      releaseCommitConnection(conn);
      if (myMessage == null) {
        myMessage = new OBError();
        myMessage.setType("Success");
        myMessage.setTitle("");
        myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
      }
    } catch (Exception e) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
    }
    return myMessage;
  }

  private OBError processCancel(VariablesSecureApp vars, String strRemittanceLineId,
      String strDateAcct) {
    if (log4j.isDebugEnabled())
      log4j.debug("processCancel");

    OBError myMessage = null;
    if (strRemittanceLineId.equals("")) {
      return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    Connection conn = null;

    String strSettlementID = "";
    String strDocumentNo = "";

    try {
      conn = this.getTransactionConnection();
      if (strRemittanceLineId.startsWith("("))
        strRemittanceLineId = strRemittanceLineId.substring(1, strRemittanceLineId.length() - 1);
      if (!strRemittanceLineId.equals("")) {
        strRemittanceLineId = Replace.replace(strRemittanceLineId, "'", "");
        StringTokenizer st = new StringTokenizer(strRemittanceLineId, ",", false);
        while (st.hasMoreTokens()) {
          String strRLId = st.nextToken().trim();
          if (log4j.isDebugEnabled())
            log4j.debug("RemittanceLineId" + strRLId);
          if (strSettlementID.equals("")) {
            String strOrg = RemittanceCancelData.selectOrg(this, strRLId);
            strSettlementID = SequenceIdData.getUUID();
            strDocumentNo = Utility.getDocumentNo(this, vars.getClient(), "C_Settlement", true);
            try {
              RemittanceCancelData.insertSettlement(conn, this, strSettlementID, vars.getClient(),
                  strOrg, vars.getUser(), strDocumentNo, strDateAcct, strRLId);
            } catch (ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              releaseRollbackConnection(conn);
              return myMessage;
            }
            if (log4j.isDebugEnabled())
              log4j.debug("Inserted Settlement " + strSettlementID);
          }
          RemittanceCancelData.cancel(conn, this, strSettlementID, strRLId);
          if (log4j.isDebugEnabled())
            log4j.debug("Canceled " + strSettlementID);
        }
        // Call c_settlement_poost
        if (!strSettlementID.equals("")) {
          String pinstance = SequenceIdData.getUUID();
          if (log4j.isDebugEnabled())
            log4j.debug("call c_settlement_post pinstnce " + pinstance);
          RemittanceCancelData.process800025(conn, this, strSettlementID);

        }
      }

      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()) + " Doc No. "
          + strDocumentNo);
    } catch (Exception e) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.debug("Rollback in transaction");
    }
    return myMessage;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDesde,
      String strHasta, String strRemittanceId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the CancelRemittance seeker");
    XmlDocument xmlDocument;
    RemittanceCancelData data[] = RemittanceCancelData.select(this, vars.getLanguage(), Utility
        .getContext(this, vars, "#User_Client", "RemittanceCancel"), Utility.getContext(this, vars,
        "#User_Org", "RemittanceCancel"), strRemittanceId, strDesde, strHasta);

    if (data.length != 0) {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel")
          .createXmlDocument();
      xmlDocument.setData("structure1", data);
    } else {
      String[] discard = { "sectionDetail" };
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/RemittanceCancel",
          discard).createXmlDocument();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RemittanceCancel", false, "", "", "",
        false, "ad_forms", strReplaceWith, false, true, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
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
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.RemittanceCancel");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "RemittanceCancel.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "RemittanceCancel.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("RemittanceCancel");
      vars.removeMessage("RemittanceCancel");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    // //----

    if (!strRemittanceId.equals(""))
      xmlDocument.setParameter("remittanceId", strRemittanceId);

    xmlDocument.setData("reportRemittance_ID", "liststructure", RemittanceComboData
        .selectNoCanceled(this, vars.getLanguage(), vars.getUserClient(), vars.getUserOrg()));
    if (!strDesde.equals(""))
      xmlDocument.setParameter("desde", strDesde);
    if (!strHasta.equals(""))
      xmlDocument.setParameter("hasta", strHasta);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the MaterialTransactions seeker";
  } // end of getServletInfo() method
}
