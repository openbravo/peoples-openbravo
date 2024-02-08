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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.RequisitionProcessor;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.procurement.Requisition;
import org.openbravo.xmlEngine.XmlDocument;

/*
 * Process the  transaction document Status of the Requisition through the Requisition Processor.
 */
public class ProcessRequisition extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final String M_REQUISITION_POST_ID = "1004400003";
  private static final String M_REQUISITION_TABLE_ID = "800212";
  private static final String REQUISITION_DOCUMENT_ACTION = "135";

  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    try {
      if (vars.commandIn("DEFAULT")) {
        defaultProcessRequisition(response, vars);
      } else if (vars.commandIn("SAVE_BUTTONDocAction1004400003")) {
        processRequisition(response, vars);
      }
    } catch (Exception e) {

    }
  }

  private void defaultProcessRequisition(final HttpServletResponse response,
      final VariablesSecureApp vars) throws ServletException, IOException {
    final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessRequisition|Window_ID",
        IsIDFilter.instance);
    final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessRequisition|Tab_ID",
        IsIDFilter.instance);

    final String strM_Requisition_ID = vars.getGlobalVariable("inpmRequisitionId",
        strWindowId + "|M_Requisition_ID", "", IsIDFilter.instance);

    final String strdocaction = vars.getStringParameter("inpdocaction");
    final String strProcessing = vars.getStringParameter("inpprocessing", "Y");
    final String strOrg = vars.getRequestGlobalVariable("inpadOrgId", "ProcessRequisition|Org_ID",
        IsIDFilter.instance);
    final String strClient = vars.getStringParameter("inpadClientId", IsIDFilter.instance);

    final String strdocstatus = vars.getRequiredStringParameter("inpdocstatus");
    final int accesslevel = 1;

    if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(),
        strTabId))
        || !(Utility.isElementInList(
            Utility.getContext(this, vars, "#User_Client", strWindowId, accesslevel), strClient)
            && Utility.isElementInList(
                Utility.getContext(this, vars, "#User_Org", strWindowId, accesslevel), strOrg))) {
      final OBError myError = Utility.translateError(this, vars, vars.getLanguage(),
          Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
      vars.setMessage(strTabId, myError);
      printPageClosePopUp(response, vars);
    } else {
      printPageDocAction(response, vars, strM_Requisition_ID, strdocaction, strProcessing,
          strdocstatus, M_REQUISITION_TABLE_ID, strWindowId);
    }
  }

  void printPageDocAction(final HttpServletResponse response, final VariablesSecureApp vars,
      final String strM_Requisition_ID, final String strdocaction, final String strProcessing,
      final String strdocstatus, final String stradTableId, final String strWindowId)
      throws IOException {
    log4j.debug("Output: Button process 1004400003");

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    final Requisition requisition = (Requisition) OBDal.getInstance()
        .getProxy(Requisition.ENTITY_NAME, strM_Requisition_ID);

    final XmlDocument xmlDocument = fillXmlDocumentPageDocAction(vars, strM_Requisition_ID,
        strdocaction, strProcessing, strdocstatus, stradTableId, strWindowId, requisition);

    out.println(xmlDocument.print());
    out.close();

  }

  private XmlDocument fillXmlDocumentPageDocAction(final VariablesSecureApp vars,
      final String strM_Requisition_ID, final String strdocaction, final String strProcessing,
      final String strdocstatus, final String stradTableId, final String strWindowId,
      final Requisition requisition) {
    final String[] discard = { "newDiscard" };
    final XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ProcessRequisition", discard)
        .createXmlDocument();

    xmlDocument.setParameter("key", strM_Requisition_ID);
    xmlDocument.setParameter("processing", strProcessing);
    xmlDocument.setParameter("form", "ProcessRequisition.html");
    xmlDocument.setParameter("window", strWindowId);
    xmlDocument.setParameter("css", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("processId", M_REQUISITION_POST_ID);
    xmlDocument.setParameter("cancel", Utility.messageBD(this, "Cancel", vars.getLanguage()));
    xmlDocument.setParameter("ok", Utility.messageBD(this, "OK", vars.getLanguage()));

    final OBError myMessage = vars.getMessage(M_REQUISITION_POST_ID);
    vars.removeMessage(M_REQUISITION_POST_ID);
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    String processDescription = null;
    try {
      OBContext.setAdminMode(true);
      final Process process = (Process) OBDal.getInstance()
          .getProxy(Process.ENTITY_NAME, M_REQUISITION_POST_ID);
      processDescription = process.getDescription();
    } finally {
      OBContext.restorePreviousMode();
    }

    xmlDocument.setParameter("docstatus", strdocstatus);
    xmlDocument.setParameter("adTableId", stradTableId);
    xmlDocument.setParameter("processId", M_REQUISITION_POST_ID);
    xmlDocument.setParameter("processDescription", processDescription);
    xmlDocument.setParameter("docaction", strdocaction);
    final FieldProvider[] dataDocAction = ActionButtonUtility.docAction(this, vars, strdocaction,
        REQUISITION_DOCUMENT_ACTION, strdocstatus, strProcessing, stradTableId);
    xmlDocument.setData("reportdocaction", "liststructure", dataDocAction);
    final StringBuilder dact = new StringBuilder();
    if (dataDocAction != null) {
      dact.append("var arrDocAction = new Array(\n");
      for (int i = 0; i < dataDocAction.length; i++) {
        dact.append("new Array(\"" + dataDocAction[i].getField("id") + "\", \""
            + dataDocAction[i].getField("name") + "\", \""
            + dataDocAction[i].getField("description") + "\")\n");
        if (i < dataDocAction.length - 1) {
          dact.append(",\n");
        }
      }
      dact.append(");");
    } else {
      dact.append("var arrDocAction = null");
    }
    xmlDocument.setParameter("array", dact.toString());
    return xmlDocument;
  }

  private void generateErrorProcessReceipt(final HttpServletResponse response,
      final VariablesSecureApp vars, final String strTabId, Exception ex) throws IOException {
    final OBError myMessage;
    myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    if (!myMessage.isConnectionAvailable()) {
      bdErrorConnection(response);
    } else {
      vars.setMessage(strTabId, myMessage);
    }
  }

  private void processRequisition(final HttpServletResponse response, final VariablesSecureApp vars)
      throws ServletException, IOException {
    final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessRequisition|Window_ID",
        IsIDFilter.instance);
    final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessRequisition|Tab_ID",
        IsIDFilter.instance);
    final String strM_Requisition_ID = vars.getGlobalVariable("inpKey",
        strWindowId + "|M_Requisition_ID", "");
    final String strdocaction = vars.getStringParameter("inpdocaction");
    OBError myMessage = null;
    try {
      RequisitionProcessor requisitionProcessor = new RequisitionProcessor();
      ProcessInstance pinstance = requisitionProcessor.processRequisition(strM_Requisition_ID,
          strdocaction);

      final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this,
          pinstance.getId());
      myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
      log4j.debug(myMessage.getMessage());
      vars.setMessage(strTabId, myMessage);

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals("")) {
        strWindowPath = strDefaultServlet;
      }
      printPageClosePopUp(response, vars, strWindowPath);

    } catch (OBException ex) {
      generateErrorProcessReceipt(response, vars, strTabId, ex);
    }
  }

}
