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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonUtility;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetailV;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
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
          || !(Utility.isElementInList(
              Utility.getContext(this, vars, "#User_Client", strWindowId, accesslevel), strClient) && Utility
              .isElementInList(
                  Utility.getContext(this, vars, "#User_Org", strWindowId, accesslevel), strOrg))) {
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(),
            Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
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

        OBContext.setAdminMode(true);
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
        final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this,
            pinstance.getId());
        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        log4j.debug(myMessage.getMessage());
        vars.setMessage(strTabId, myMessage);
        OBContext.setAdminMode();
        try {
          // on error close popup
          if (pinstance.getResult() == 0L || !"CO".equals(strdocaction)) {
            String strWindowPath = Utility.getTabURL(strTabId, "R", true);
            if (strWindowPath.equals(""))
              strWindowPath = strDefaultServlet;
            printPageClosePopUp(response, vars, strWindowPath);
          }
        } finally {
          OBContext.restorePreviousMode();
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myMessage.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage(strTabId, myMessage);
      }

      /*
       * Cancel credit (if any) for the invoice's bp
       */
      if ("CO".equals(strdocaction)) {
        try {
          OBContext.setAdminMode(true);
          final Invoice invoice = OBDal.getInstance().get(Invoice.class, strC_Invoice_ID);
          final String invoiceDocCategory = invoice.getDocumentType().getDocumentCategory();
          if ("API".equals(invoiceDocCategory) || "ARI".equals(invoiceDocCategory)) {
            final FIN_Payment creditPayment = dao.getCreditPayment(invoice);
            // If the invoice grand total is ZERO or already has payments (due to
            // payment method automation) do not cancel credit
            if (creditPayment != null
                && BigDecimal.ZERO.compareTo(invoice.getGrandTotalAmount()) != 0
                && !isInvoiceWithPayments(invoice)) {
              log4j.info("Detected credit payment: " + creditPayment.getIdentifier()
                  + ", that matches the invoice: " + invoice.getIdentifier());
              // Set Used Credit = Invoice's Grand Total Amount
              creditPayment.setUsedCredit(invoice.getGrandTotalAmount());
              final StringBuffer description = new StringBuffer();
              if (creditPayment.getDescription() != null
                  && !creditPayment.getDescription().equals(""))
                description.append(creditPayment.getDescription()).append("\n");
              description.append(String.format(
                  Utility.messageBD(this, "APRM_CreditUsedinInvoice", vars.getLanguage()),
                  invoice.getDocumentNo()));
              creditPayment.setDescription(description.toString());

              final List<FIN_PaymentScheduleDetail> paymentScheduleDetails = new ArrayList<FIN_PaymentScheduleDetail>();
              final HashMap<String, BigDecimal> paymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
              for (final FIN_PaymentSchedule paymentSchedule : invoice.getFINPaymentScheduleList()) {
                for (final FIN_PaymentScheduleDetail paymentScheduleDetail : paymentSchedule
                    .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
                  paymentScheduleDetails.add(paymentScheduleDetail);
                  paymentScheduleDetailsAmounts.put(paymentScheduleDetail.getId(),
                      paymentScheduleDetail.getAmount());
                }
              }

              // Create new Payment
              final boolean isSalesTransaction = invoice.isSalesTransaction();
              final DocumentType docType = FIN_Utility.getDocumentType(invoice.getOrganization(),
                  isSalesTransaction ? "ARR" : "APP");
              final String strPaymentDocumentNo = FIN_Utility.getDocumentNo(docType,
                  docType.getTable() != null ? docType.getTable().getDBTableName() : "");
              final FIN_FinancialAccount bpFinAccount = isSalesTransaction ? invoice
                  .getBusinessPartner().getAccount() : invoice.getBusinessPartner()
                  .getPOFinancialAccount();
              final FIN_Payment newPayment = FIN_AddPayment.savePayment(null, isSalesTransaction,
                  docType, strPaymentDocumentNo, invoice.getBusinessPartner(),
                  invoice.getPaymentMethod(), bpFinAccount, "0", creditPayment.getPaymentDate(),
                  invoice.getOrganization(), invoice.getDocumentNo(), paymentScheduleDetails,
                  paymentScheduleDetailsAmounts, false, false);
              newPayment.setAmount(BigDecimal.ZERO);
              newPayment.setGeneratedCredit(BigDecimal.ZERO);
              newPayment.setUsedCredit(invoice.getGrandTotalAmount());

              // Process the new payment if invoice's payment method is inside BP's financial
              // account
              boolean process = false;
              for (final FinAccPaymentMethod bpFinAccPaymentMethod : bpFinAccount
                  .getFinancialMgmtFinAccPaymentMethodList()) {
                if (bpFinAccPaymentMethod.getPaymentMethod().equals(invoice.getPaymentMethod())) {
                  process = true;
                  break;
                }
              }
              if (process)
                FIN_AddPayment.processPayment(vars, this, "P", newPayment);

              // Update Invoice's description
              final StringBuffer invDesc = new StringBuffer();
              if (invoice.getDescription() != null) {
                invDesc.append(invoice.getDescription());
                invDesc.append("\n");
              }
              invDesc.append(String.format(
                  Utility.messageBD(this, "APRM_InvoiceDescUsedCredit", vars.getLanguage()),
                  creditPayment.getIdentifier()));
              invoice.setDescription(invDesc.toString());
            }
          }
        } catch (final Exception e) {
          log4j.error("Exception while canceling the credit in the invoice: " + strC_Invoice_ID);
          e.printStackTrace();
        } finally {
          OBContext.restorePreviousMode();
        }
      }

      List<FIN_Payment> payments = null;
      try {
        OBContext.setAdminMode(true);
        payments = dao.getPendingExecutionPayments(strC_Invoice_ID);
      } finally {
        OBContext.restorePreviousMode();
      }

      if (payments != null && payments.size() > 0) {
        vars.setSessionValue("ExecutePayments|Window_ID", strWindowId);
        vars.setSessionValue("ExecutePayments|Tab_ID", strTabId);
        vars.setSessionValue("ExecutePayments|Org_ID",
            vars.getSessionValue("ProcessInvoice|Org_ID"));
        vars.setSessionValue("ExecutePayments|payments", FIN_Utility.getInStrList(payments));
        if (myMessage != null)
          vars.setMessage("ExecutePayments|message", myMessage);
        response.sendRedirect(strDireccion
            + "/org.openbravo.advpaymentmngt.ad_actionbutton/ExecutePayments.html");
      } else {
        String strWindowPath = Utility.getTabURL(strTabId, "R", true);
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

  private boolean isInvoiceWithPayments(Invoice invoice) {
    for (FIN_PaymentSchedule ps : OBDao.getFilteredCriteria(FIN_PaymentSchedule.class,
        Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, invoice)).list()) {
      for (FIN_PaymentDetailV pdv : OBDao.getFilteredCriteria(FIN_PaymentDetailV.class,
          Restrictions.eq(FIN_PaymentDetailV.PROPERTY_INVOICEPAYMENTPLAN, ps)).list()) {
        if (pdv.getPayment() != null && !"RPVOID".equals(pdv.getPayment().getStatus())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getServletInfo() {
    return "Servlet to Process Invoice";
  }
}
