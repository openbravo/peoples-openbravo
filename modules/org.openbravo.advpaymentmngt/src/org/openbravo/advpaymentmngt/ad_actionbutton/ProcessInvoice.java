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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonUtility;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.service.db.CallProcess;
import org.openbravo.xmlEngine.XmlDocument;

public class ProcessInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    if (vars.commandIn("DEFAULT")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessInvoice|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessInvoice|Tab_ID",
          IsIDFilter.instance);

      final String strC_Invoice_ID = vars.getGlobalVariable("inpcInvoiceId", strWindowId
          + "|C_Invoice_ID", "", IsIDFilter.instance);

      final String strdocaction = vars.getStringParameter("inpdocaction");
      final String strProcessing = vars.getStringParameter("inpprocessing", "Y");
      final String strOrg = vars.getRequestGlobalVariable("inpadOrgId", "ProcessInvoice|Org_ID",
          IsIDFilter.instance);
      final String strClient = vars.getStringParameter("inpadClientId", IsIDFilter.instance);

      final String strdocstatus = vars.getRequiredStringParameter("inpdocstatus");
      final String stradTableId = "318";
      final int accesslevel = 1;

      if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(),
          strTabId))
          || !(Utility.isElementInList(Utility.getContext(this, vars, "#User_Client", strWindowId,
              accesslevel), strClient) && Utility.isElementInList(Utility.getContext(this, vars,
              "#User_Org", strWindowId, accesslevel), strOrg))) {
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(), Utility.messageBD(
            this, "NoWriteAccess", vars.getLanguage()));
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars);
      } else {
        printPageDocAction(response, vars, strC_Invoice_ID, strdocaction, strProcessing,
            strdocstatus, stradTableId, strWindowId);
      }
    } else if (vars.commandIn("SAVE_BUTTONDocAction111")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessInvoice|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessInvoice|Tab_ID",
          IsIDFilter.instance);
      final String strC_Invoice_ID = vars.getGlobalVariable("inpKey",
          strWindowId + "|C_Invoice_ID", "");
      final String strdocaction = vars.getStringParameter("inpdocaction");
      OBError myMessage = null;
      try {

        Invoice invoice = dao.getObject(Invoice.class, strC_Invoice_ID);
        invoice.setDocumentAction(strdocaction);
        OBDal.getInstance().save(invoice);
        OBDal.getInstance().flush();

        OBContext.setAdminMode();
        Process process = null;
        try {
          process = dao.getObject(Process.class, "111");
        } finally {
          OBContext.restorePreviousMode();
        }

        final ProcessInstance pinstance = CallProcess.getInstance().call(process, strC_Invoice_ID,
            null);

        // invoice = dao.getObject(Invoice.class, strC_Invoice_ID);
        OBDal.getInstance().getSession().refresh(invoice);
        invoice.setAPRMProcessinvoice(invoice.getDocumentAction());
        OBDal.getInstance().save(invoice);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
        final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance
            .getId());
        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        log4j.debug(myMessage.getMessage());
        vars.setMessage(strTabId, myMessage);
        // on error close popup
        if (pinstance.getResult() == 0L || !"CO".equals(strdocaction)) {
          String strWindowPath = Utility.getTabURL(this, strTabId, "R");
          if (strWindowPath.equals(""))
            strWindowPath = strDefaultServlet;
          printPageClosePopUp(response, vars, strWindowPath);
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myMessage.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage(strTabId, myMessage);
      }
      List<FIN_Payment> payments = dao.getPendingExecutionPayments(strC_Invoice_ID);
      if (payments != null && payments.size() > 0) {
        vars.setSessionValue("ExecutePayments|Window_ID", strWindowId);
        vars.setSessionValue("ExecutePayments|Tab_ID", strTabId);
        vars.setSessionValue("ExecutePayments|Org_ID", vars
            .getSessionValue("ProcessInvoice|Org_ID"));
        vars.setSessionValue("ExecutePayments|payments", FIN_Utility.getInStrList(payments));
        if (myMessage != null)
          vars.setMessage("ExecutePayments|message", myMessage);
        response.sendRedirect(strDireccion
            + "/org.openbravo.advpaymentmngt.ad_actionbutton/ExecutePayments.html");
      } else {
        String strWindowPath = Utility.getTabURL(this, strTabId, "R");
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        printPageClosePopUp(response, vars, strWindowPath);
      }

      vars.removeSessionValue("ProcessInvoice|Window_ID");
      vars.removeSessionValue("ProcessInvoice|Tab_ID");
      vars.removeSessionValue("ProcessInvoice|Org_ID");

    }
  }

  void printPageDocAction(HttpServletResponse response, VariablesSecureApp vars,
      String strC_Invoice_ID, String strdocaction, String strProcessing, String strdocstatus,
      String stradTableId, String strWindowId) throws IOException, ServletException {
    log4j.debug("Output: Button process 111");
    String[] discard = { "newDiscard" };
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/DocAction", discard).createXmlDocument();
    xmlDocument.setParameter("key", strC_Invoice_ID);
    xmlDocument.setParameter("processing", strProcessing);
    xmlDocument.setParameter("form", "ProcessInvoice.html");
    xmlDocument.setParameter("window", strWindowId);
    xmlDocument.setParameter("css", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("processId", "111");
    xmlDocument.setParameter("cancel", Utility.messageBD(this, "Cancel", vars.getLanguage()));
    xmlDocument.setParameter("ok", Utility.messageBD(this, "OK", vars.getLanguage()));

    OBError myMessage = vars.getMessage("111");
    vars.removeMessage("111");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("docstatus", strdocstatus);
    xmlDocument.setParameter("adTableId", stradTableId);
    xmlDocument.setParameter("processId", "111");
    xmlDocument.setParameter("processDescription", "Process Invoice");
    xmlDocument.setParameter("docaction", (strdocaction.equals("--") ? "CL" : strdocaction));
    FieldProvider[] dataDocAction = ActionButtonUtility.docAction(this, vars, strdocaction, "135",
        strdocstatus, strProcessing, stradTableId);
    xmlDocument.setData("reportdocaction", "liststructure", dataDocAction);
    StringBuffer dact = new StringBuffer();
    if (dataDocAction != null) {
      dact.append("var arrDocAction = new Array(\n");
      for (int i = 0; i < dataDocAction.length; i++) {
        dact.append("new Array(\"" + dataDocAction[i].getField("id") + "\", \""
            + dataDocAction[i].getField("name") + "\", \""
            + dataDocAction[i].getField("description") + "\")\n");
        if (i < dataDocAction.length - 1)
          dact.append(",\n");
      }
      dact.append(");");
    } else
      dact.append("var arrDocAction = null");
    xmlDocument.setParameter("array", dact.toString());

    out.println(xmlDocument.print());
    out.close();

  }

  public String getServletInfo() {
    return "Servlet to Process Invoice";
  }
}
