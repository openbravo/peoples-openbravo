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

package org.openbravo.advpaymentmngt.dao;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRMPendingPaymentFromInvoice;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.advpaymentmngt.utility.Value;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcessParameter;
import org.openbravo.model.financialmgmt.payment.PaymentPriority;
import org.openbravo.model.financialmgmt.payment.PaymentRun;
import org.openbravo.model.financialmgmt.payment.PaymentRunParameter;
import org.openbravo.model.financialmgmt.payment.PaymentRunPayment;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;

public class AdvPaymentMngtDao {

  public AdvPaymentMngtDao() {
  }

  public <T extends BaseOBObject> T getObject(Class<T> t, String strId) {
    return OBDal.getInstance().get(t, strId);
  }

  public List<FIN_PaymentScheduleDetail> getInvoicePendingScheduledPaymentDetails(Invoice invoice) {
    final StringBuilder whereClause = new StringBuilder();

    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd ");
      whereClause.append(" where psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      whereClause.append(" is null");
      whereClause.append("   and psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(".id = '");
      whereClause.append(invoice.getId());
      whereClause.append("'");
      whereClause.append(" order by psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      return obqPSD.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<FIN_PaymentScheduleDetail> getFilteredScheduledPaymentDetails(
      Organization organization, BusinessPartner businessPartner, Currency currency,
      Date dueDateFrom, Date dueDateTo, String strTransactionType, FIN_PaymentMethod paymentMethod,
      List<FIN_PaymentScheduleDetail> selectedScheduledPaymentDetails, boolean isReceipt) {

    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();

    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd "); // pending scheduled payments //
      whereClause.append(" left outer join psd.orderPaymentSchedule ");
      whereClause.append(" left outer join psd.orderPaymentSchedule.order ");
      whereClause.append(" left outer join psd.orderPaymentSchedule.fINPaymentPriority ");
      whereClause.append(" left outer join psd.invoicePaymentSchedule ");
      whereClause.append(" left outer join psd.invoicePaymentSchedule.invoice ");
      whereClause.append(" left outer join psd.invoicePaymentSchedule.fINPaymentPriority ");
      whereClause.append(" where psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      whereClause.append(" is null");
      whereClause.append(" and psd.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORGANIZATION);
      whereClause.append(".id = '");
      whereClause.append(organization.getId());
      whereClause.append("'");

      // remove selected payments
      if (selectedScheduledPaymentDetails != null && selectedScheduledPaymentDetails.size() > 0) {
        String strSelectedPaymentDetails = FIN_Utility
            .getInStrList(selectedScheduledPaymentDetails);
        whereClause.append(" and psd not in (");
        whereClause.append(strSelectedPaymentDetails);
        whereClause.append(")");
      }

      // block schedule payments in other payment proposal
      final OBCriteria<FIN_PaymentPropDetail> obc = OBDal.getInstance().createCriteria(
          FIN_PaymentPropDetail.class);
      obc.add(Expression.isNotNull(FIN_PaymentPropDetail.PROPERTY_FINPAYMENTSCHEDULEDETAIL));
      if (obc.list() != null && obc.list().size() > 0) {
        List<FIN_PaymentScheduleDetail> aux = new ArrayList<FIN_PaymentScheduleDetail>();
        for (FIN_PaymentPropDetail ppd : obc.list()) {
          aux.add(ppd.getFINPaymentScheduledetail());
        }
        whereClause.append(" and psd.id not in (" + FIN_Utility.getInStrList(aux) + ")");
      }

      // Transaction type filter
      whereClause.append(" and (");
      if (strTransactionType.equals("I") || strTransactionType.equals("B")) {
        whereClause.append(" (psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        whereClause.append(" is not null");
        if (businessPartner != null) {
          whereClause.append(" and psd.");
          whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
          whereClause.append(".");
          whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
          whereClause.append(".");
          whereClause.append(Invoice.PROPERTY_BUSINESSPARTNER);
          whereClause.append(".id = '");
          whereClause.append(businessPartner.getId());
          whereClause.append("'");
        }
        if (paymentMethod != null) {
          whereClause.append(" and psd.");
          whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
          whereClause.append(".");
          whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
          whereClause.append(".");
          whereClause.append(Invoice.PROPERTY_PAYMENTMETHOD);
          whereClause.append(".id = '");
          whereClause.append(paymentMethod.getId());
          whereClause.append("'");
        }
        whereClause.append(" and psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        whereClause.append(".");
        whereClause.append(Invoice.PROPERTY_SALESTRANSACTION);
        whereClause.append(" = ");
        whereClause.append(isReceipt);
        whereClause.append(" and psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        whereClause.append(".");
        whereClause.append(Invoice.PROPERTY_CURRENCY);
        whereClause.append(".id = '");
        whereClause.append(currency.getId());
        whereClause.append("')");

      }
      if (strTransactionType.equals("B"))
        whereClause.append(" or ");
      if (strTransactionType.equals("O") || strTransactionType.equals("B")) {
        whereClause.append(" (psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
        whereClause.append(" is not null");
        if (businessPartner != null) {
          whereClause.append(" and psd.");
          whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
          whereClause.append(".");
          whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
          whereClause.append(".");
          whereClause.append(Order.PROPERTY_BUSINESSPARTNER);
          whereClause.append(".id = '");
          whereClause.append(businessPartner.getId());
          whereClause.append("'");
        }
        if (paymentMethod != null) {
          whereClause.append(" and psd.");
          whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
          whereClause.append(".");
          whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
          whereClause.append(".");
          whereClause.append(Order.PROPERTY_PAYMENTMETHOD);
          whereClause.append(".id = '");
          whereClause.append(paymentMethod.getId());
          whereClause.append("'");
        }
        whereClause.append(" and psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
        whereClause.append(".");
        whereClause.append(Order.PROPERTY_SALESTRANSACTION);
        whereClause.append(" = ");
        whereClause.append(isReceipt);
        whereClause.append(" and psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
        whereClause.append(".");
        whereClause.append(Order.PROPERTY_CURRENCY);
        whereClause.append(".id = '");
        whereClause.append(currency.getId());
        whereClause.append("')");
      }
      whereClause.append(")");
      // dateFrom
      if (dueDateFrom != null) {
        whereClause.append(" and COALESCE(psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(", psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(") >= ?");
        parameters.add(dueDateFrom);
      }
      // dateTo
      if (dueDateTo != null) {
        whereClause.append(" and COALESCE(psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(", psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
        whereClause.append(") < ?");
        parameters.add(dueDateTo);
      }
      // TODO: Add order to show first scheduled payments from invoices and later scheduled payments
      // from not invoiced orders.
      whereClause.append(" order by");
      whereClause.append(" COALESCE(psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_FINPAYMENTPRIORITY);
      whereClause.append(".");
      whereClause.append(PaymentPriority.PROPERTY_PRIORITY);
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_FINPAYMENTPRIORITY);
      whereClause.append(".");
      whereClause.append(PaymentPriority.PROPERTY_PRIORITY);
      whereClause.append(")");
      whereClause.append(", ");
      whereClause.append(" COALESCE(psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(")");
      whereClause.append(", COALESCE(psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(".");
      whereClause.append(Invoice.PROPERTY_DOCUMENTNO);
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      whereClause.append(".");
      whereClause.append(Order.PROPERTY_DOCUMENTNO);
      whereClause.append(")");
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      obqPSD.setParameters(parameters);
      return obqPSD.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FIN_Payment getNewPayment(boolean isReceipt, Organization organization,
      DocumentType docType, String strPaymentDocumentNo, BusinessPartner businessPartner,
      FIN_PaymentMethod paymentMethod, FIN_FinancialAccount finAccount, String strPaymentAmount,
      Date paymentDate, String referenceNo) {
    final FIN_Payment newPayment = OBProvider.getInstance().get(FIN_Payment.class);
    newPayment.setReceipt(isReceipt);
    newPayment.setDocumentType(docType);
    newPayment.setDocumentNo(strPaymentDocumentNo);
    newPayment.setOrganization(organization);
    newPayment.setClient(businessPartner.getClient());
    newPayment.setStatus("RPAP");
    newPayment.setBusinessPartner(businessPartner);
    newPayment.setPaymentMethod(paymentMethod);
    newPayment.setAccount(finAccount);
    newPayment.setAmount(new BigDecimal(strPaymentAmount));
    newPayment.setPaymentDate(paymentDate);
    newPayment.setCurrency(finAccount.getCurrency());
    newPayment.setReferenceNo(referenceNo);

    OBDal.getInstance().save(newPayment);
    OBDal.getInstance().flush();

    return newPayment;
  }

  public FIN_PaymentDetail getNewPaymentDetail(FIN_Payment payment,
      FIN_PaymentScheduleDetail paymentScheduleDetail, BigDecimal paymentDetailAmount,
      BigDecimal writeoffAmount, boolean isRefund, GLItem glitem) {
    final FIN_PaymentDetail newPaymentDetail = OBProvider.getInstance()
        .get(FIN_PaymentDetail.class);
    List<FIN_PaymentDetail> paymentDetails = payment.getFINPaymentDetailList();
    newPaymentDetail.setFinPayment(payment);
    newPaymentDetail.setOrganization(payment.getOrganization());
    newPaymentDetail.setClient(payment.getClient());
    newPaymentDetail.setAmount(paymentDetailAmount);
    newPaymentDetail.setWriteoffAmount(writeoffAmount);
    newPaymentDetail.setRefund(isRefund);
    newPaymentDetail.setGLItem(glitem);
    newPaymentDetail.setPrepayment(glitem == null
        && paymentScheduleDetail.getInvoicePaymentSchedule() == null);

    paymentDetails.add(newPaymentDetail);
    payment.setFINPaymentDetailList(paymentDetails);
    payment.setWriteoffAmount(payment.getWriteoffAmount().add(writeoffAmount));

    List<FIN_PaymentScheduleDetail> paymentScheduleDetails = newPaymentDetail
        .getFINPaymentScheduleDetailList();
    paymentScheduleDetail.setPaymentDetails(newPaymentDetail);
    paymentScheduleDetails.add(paymentScheduleDetail);
    newPaymentDetail.setFINPaymentScheduleDetailList(paymentScheduleDetails);

    OBDal.getInstance().save(payment);
    OBDal.getInstance().save(newPaymentDetail);
    OBDal.getInstance().save(paymentScheduleDetail);
    OBDal.getInstance().flush();

    return newPaymentDetail;
  }

  public FIN_PaymentScheduleDetail getNewPaymentScheduleDetail(Organization organization,
      BigDecimal amount) {
    final FIN_PaymentScheduleDetail newPaymentScheduleDetail = OBProvider.getInstance().get(
        FIN_PaymentScheduleDetail.class);
    newPaymentScheduleDetail.setOrganization(organization);
    // As '0' is not a valid organization for transactions we can assume that organization client is
    // transaction client
    newPaymentScheduleDetail.setClient(organization.getClient());
    newPaymentScheduleDetail.setAmount(amount);

    OBDal.getInstance().save(newPaymentScheduleDetail);
    OBDal.getInstance().flush();

    return newPaymentScheduleDetail;
  }

  public FIN_PaymentPropDetail getNewPaymentProposalDetail(Organization organization,
      FIN_PaymentProposal paymentProposal, FIN_PaymentScheduleDetail paymentScheduleDetail,
      BigDecimal amount, BigDecimal writeoffamount, GLItem glitem) {
    final FIN_PaymentPropDetail newPaymentProposalDetail = OBProvider.getInstance().get(
        FIN_PaymentPropDetail.class);
    newPaymentProposalDetail.setOrganization(organization);
    newPaymentProposalDetail.setAmount(amount);
    if (writeoffamount != null)
      newPaymentProposalDetail.setWriteoffAmount(writeoffamount);
    if (glitem != null)
      newPaymentProposalDetail.setGLItem(glitem);
    newPaymentProposalDetail.setFINPaymentScheduledetail(paymentScheduleDetail);

    List<FIN_PaymentPropDetail> paymentProposalDetails = paymentProposal
        .getFINPaymentPropDetailList();
    paymentProposalDetails.add(newPaymentProposalDetail);
    paymentProposal.setFINPaymentPropDetailList(paymentProposalDetails);
    newPaymentProposalDetail.setFinPaymentProposal(paymentProposal);

    OBDal.getInstance().save(newPaymentProposalDetail);
    OBDal.getInstance().save(paymentProposal);
    OBDal.getInstance().flush();

    return newPaymentProposalDetail;
  }

  public FIN_FinaccTransaction getFinancialTransaction(FIN_Payment payment) {
    FIN_FinaccTransaction transaction = FIN_Utility.getOneInstance(FIN_FinaccTransaction.class,
        new Value(FIN_FinaccTransaction.PROPERTY_FINPAYMENT, payment));
    if (transaction == null) {
      transaction = getNewFinancialTransaction(payment.getOrganization(), payment.getCurrency(),
          payment.getAccount(), TransactionsDao.getTransactionMaxLineNo(payment.getAccount()) + 10,
          payment, payment.getDescription(), payment.getPaymentDate(), null, "RPPC", payment
              .isReceipt() ? payment.getAmount() : BigDecimal.ZERO, !payment.isReceipt() ? payment
              .getAmount() : BigDecimal.ZERO, payment.getProject(), payment.getSalesCampaign(),
          payment.getActivity(), payment.isReceipt() ? "BPD" : "BPW", payment.getPaymentDate());
    }
    return transaction;
  }

  public FIN_FinaccTransaction getNewFinancialTransaction(Organization organization,
      Currency currency, FIN_FinancialAccount account, Long line, FIN_Payment payment,
      String description, Date accountingDate, GLItem glItem, String status,
      BigDecimal depositAmount, BigDecimal paymentAmount, Project project, Campaign campaing,
      ABCActivity activity, String transactionType, Date statementDate) {
    FIN_FinaccTransaction finTrans = OBProvider.getInstance().get(FIN_FinaccTransaction.class);
    finTrans.setActive(true);
    finTrans.setOrganization(organization);
    finTrans.setCurrency(currency);
    finTrans.setAccount(account);
    finTrans.setLineNo(line);
    finTrans.setFinPayment(payment);
    String truncateDescription = (description.length() > 255) ? description.substring(0, 252)
        .concat("...").toString() : description.toString();
    finTrans.setDescription(truncateDescription);
    finTrans.setDateAcct(accountingDate);
    finTrans.setGLItem(glItem);
    finTrans.setStatus(status);
    finTrans.setDepositAmount(depositAmount);
    finTrans.setPaymentAmount(paymentAmount);
    finTrans.setProject(project);
    finTrans.setSalesCampaign(campaing);
    finTrans.setActivity(activity);
    finTrans.setTransactionType(transactionType);
    finTrans.setTransactionDate(statementDate);

    OBDal.getInstance().save(finTrans);
    OBDal.getInstance().flush();

    return finTrans;
  }

  public FIN_Reconciliation getNewReconciliation(Organization org, FIN_FinancialAccount account,
      String documentNo, DocumentType docType, Date dateTo, Date statementDate,
      BigDecimal startingBalance, BigDecimal endingBalance, String docStatus) {
    FIN_Reconciliation finRecon = OBProvider.getInstance().get(FIN_Reconciliation.class);
    finRecon.setOrganization(org);
    finRecon.setAccount(account);
    finRecon.setDocumentNo(documentNo);
    finRecon.setDocumentType(docType);
    finRecon.setEndingDate(dateTo);
    finRecon.setTransactionDate(statementDate);
    finRecon.setDocumentStatus(docStatus);
    finRecon.setStartingbalance(startingBalance);
    finRecon.setEndingBalance(endingBalance);

    OBDal.getInstance().save(finRecon);
    OBDal.getInstance().flush();

    return finRecon;

  }

  public PaymentRun getNewPaymentRun(String sourceType, PaymentExecutionProcess executionProcess,
      Organization organization) {
    PaymentRun paymentRun = OBProvider.getInstance().get(PaymentRun.class);
    paymentRun.setStatus("P");
    paymentRun.setOrganization(organization);
    paymentRun.setPaymentExecutionProcess(executionProcess);
    paymentRun.setSourceOfTheExecution(sourceType);

    OBDal.getInstance().save(paymentRun);
    OBDal.getInstance().flush();
    return paymentRun;
  }

  public PaymentRunPayment getNewPaymentRunPayment(PaymentRun paymentRun, FIN_Payment payment) {
    PaymentRunPayment paymentRunPayment = OBProvider.getInstance().get(PaymentRunPayment.class);
    paymentRunPayment.setPaymentRun(paymentRun);
    paymentRunPayment.setPayment(payment);
    paymentRunPayment.setOrganization(paymentRun.getOrganization());
    paymentRunPayment.setResult("P");

    List<PaymentRunPayment> paymentRunPayments = paymentRun.getFinancialMgmtPaymentRunPaymentList();
    paymentRunPayments.add(paymentRunPayment);
    paymentRun.setFinancialMgmtPaymentRunPaymentList(paymentRunPayments);

    OBDal.getInstance().save(paymentRunPayment);
    OBDal.getInstance().save(paymentRun);
    OBDal.getInstance().flush();
    return paymentRunPayment;
  }

  public PaymentRunParameter getNewPaymentRunParameter(PaymentRun paymentRun,
      PaymentExecutionProcessParameter parameter, String value) {
    PaymentRunParameter paymentRunParameter = OBProvider.getInstance().get(
        PaymentRunParameter.class);
    paymentRunParameter.setPaymentRun(paymentRun);
    paymentRunParameter.setOrganization(paymentRun.getOrganization());
    paymentRunParameter.setPaymentExecutionProcessParameter(parameter);
    if ("CHECK".equals(parameter.getInputType()))
      paymentRunParameter.setValueOfTheCheck("Y".equals(value));
    else if ("TEXT".equals(parameter.getInputType()))
      paymentRunParameter.setValueOfTheTextParameter(value);

    List<PaymentRunParameter> paymentRunParameters = paymentRun
        .getFinancialMgmtPaymentRunParameterList();
    paymentRunParameters.add(paymentRunParameter);
    paymentRun.setFinancialMgmtPaymentRunParameterList(paymentRunParameters);

    OBDal.getInstance().save(paymentRunParameter);
    OBDal.getInstance().save(paymentRun);
    OBDal.getInstance().flush();
    return paymentRunParameter;

  }

  public void duplicateScheduleDetail(FIN_PaymentScheduleDetail paymentScheduleDetail,
      BigDecimal writeoffAmount) {
    final FIN_PaymentScheduleDetail newPaymentScheduleDetail = (FIN_PaymentScheduleDetail) DalUtil
        .copy(paymentScheduleDetail);
    newPaymentScheduleDetail.setAmount(writeoffAmount);
    OBDal.getInstance().save(newPaymentScheduleDetail);
    OBDal.getInstance().flush();
  }

  public List<FIN_PaymentPropDetail> getOrderedPaymentProposalDetails(
      FIN_PaymentProposal paymentProposal) {

    final StringBuilder whereClause = new StringBuilder();

    OBContext.setAdminMode();
    try {

      whereClause.append(" as ppd ");
      whereClause.append(" left outer join ppd.fINPaymentScheduledetail ");
      whereClause.append(" left outer join ppd.fINPaymentScheduledetail.invoicePaymentSchedule ");
      whereClause
          .append(" left outer join ppd.fINPaymentScheduledetail.invoicePaymentSchedule.invoice ");
      whereClause.append(" left outer join ppd.fINPaymentScheduledetail.orderPaymentSchedule ");
      whereClause
          .append(" left outer join ppd.fINPaymentScheduledetail.orderPaymentSchedule.order ");
      whereClause.append(" where ppd.finPaymentProposal.id='");
      whereClause.append(paymentProposal.getId());
      whereClause.append("' ");
      whereClause
          .append(" order by COALESCE (ppd.fINPaymentScheduledetail.invoicePaymentSchedule.invoice.businessPartner,ppd.fINPaymentScheduledetail.orderPaymentSchedule.order.businessPartner)");
      // whereClause.append(" left outer join ppd. )
      // whereClause.append(" where psd." + FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS
      // + " is null");
      // whereClause.append("   and psd." + FIN_PaymentScheduleDetail.PROPERTY_FINPAYMENTPROPDETAIL
      // + "." + FIN_PaymentPropDetail.PROPERTY_FINPAYMENTPROPOSAL + ".id='"
      // + paymentProposal.getId() + "'");

      final OBQuery<FIN_PaymentPropDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentPropDetail.class, whereClause.toString());

      return obqPSD.list();

    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public FieldProvider[] getReconciliationDetailReport(VariablesSecureApp vars, String strDate,
      String strReconID) {
    final StringBuilder hsqlScript = new StringBuilder();

    OBContext.setAdminMode();
    try {
      hsqlScript.append(" as recon ");
      hsqlScript.append(" where recon.id='");
      hsqlScript.append(strReconID);
      hsqlScript.append("'");
      final OBQuery<FIN_Reconciliation> obqRecon = OBDal.getInstance().createQuery(
          FIN_Reconciliation.class, hsqlScript.toString());

      List<FIN_Reconciliation> obqRecList = obqRecon.list();
      FIN_Reconciliation[] FIN_Reconcile = new FIN_Reconciliation[0];
      FIN_Reconcile = obqRecList.toArray(FIN_Reconcile);

      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(obqRecList);
      for (int i = 0; i < data.length; i++) {
        FieldProviderFactory.setField(data[i], "FIN_RECONCILIATION_ID", FIN_Reconcile[i].getId());
        FieldProviderFactory.setField(data[i], "ENDDATE", Utility.formatDate(FIN_Reconcile[i]
            .getEndingDate(), vars.getJavaDateFormat()));
        FieldProviderFactory.setField(data[i], "BPARTNER", "Account Balance in Openbravo");
        FieldProviderFactory.setField(data[i], "REFERENCE", "");
        FieldProviderFactory.setField(data[i], "STARTINGBALANCE", FIN_Reconcile[i]
            .getStartingbalance().toString());
      }
      return data;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FieldProvider[] getReconciliationSummaryReport(VariablesSecureApp vars, String strDate,
      String strReconID) {
    final StringBuilder hsqlScript = new StringBuilder();

    OBContext.setAdminMode();
    try {
      hsqlScript.append(" as recon ");
      hsqlScript.append(" where recon.id='");
      hsqlScript.append(strReconID);
      hsqlScript.append("'");
      final OBQuery<FIN_Reconciliation> obqRecon = OBDal.getInstance().createQuery(
          FIN_Reconciliation.class, hsqlScript.toString());
      List<FIN_Reconciliation> obqRecList = obqRecon.list();
      FIN_Reconciliation[] FIN_Reconcile = new FIN_Reconciliation[0];
      FIN_Reconcile = obqRecList.toArray(FIN_Reconcile);

      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(obqRecList);
      for (int i = 0; i < data.length; i++) {
        FieldProviderFactory.setField(data[i], "FIN_RECONCILIATION_ID", FIN_Reconcile[i].getId());
        FieldProviderFactory.setField(data[i], "ENDDATE", Utility.formatDate(FIN_Reconcile[i]
            .getEndingDate(), vars.getJavaDateFormat()));
        FieldProviderFactory.setField(data[i], "ENDINGBALANCE", FIN_Reconcile[i].getEndingBalance()
            .toString());
        FieldProviderFactory.setField(data[i], "STARTINGBALANCE", FIN_Reconcile[i]
            .getStartingbalance().toString());
      }
      return data;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public int getTrxGridRowCount(FIN_FinancialAccount financialAccount, Boolean hideReconciledTrx,
      int maxrowspergridpage, int offset) {
    final StringBuilder whereClause = new StringBuilder();

    OBContext.setAdminMode();
    try {
      whereClause.append(" as fatrx ");
      whereClause.append(" left outer join fatrx.").append(
          FIN_FinaccTransaction.PROPERTY_RECONCILIATION).append(" as reconciliation");
      whereClause.append(" where fatrx.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id='");
      whereClause.append(financialAccount.getId());
      whereClause.append("'");
      if (hideReconciledTrx) {
        whereClause.append(" and (fatrx.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION)
            .append(" is null ");
        whereClause.append(" or reconciliation.").append(FIN_Reconciliation.PROPERTY_PROCESSED)
            .append(" = 'N') ");
      }
      final OBQuery<FIN_FinaccTransaction> obqFATrx = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString());
      obqFATrx.setFirstResult(offset);
      obqFATrx.setMaxResult(maxrowspergridpage);
      return obqFATrx.list().size();

    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public List<FIN_FinaccTransaction> getTrxGridRows(FIN_FinancialAccount financialAccount,
      Boolean hideReconciledTrx, int pageSize, int offset, String orderBy) {
    final StringBuilder whereClause = new StringBuilder();

    OBContext.setAdminMode();
    try {

      whereClause.append(" as fatrx ");
      whereClause.append(" left outer join fatrx.").append(
          FIN_FinaccTransaction.PROPERTY_RECONCILIATION).append(" as reconciliation");
      whereClause.append(" where fatrx.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id='");
      whereClause.append(financialAccount.getId());
      whereClause.append("'");
      if (hideReconciledTrx) {
        whereClause.append(" and (fatrx.").append(FIN_FinaccTransaction.PROPERTY_RECONCILIATION)
            .append(" is null ");
        whereClause.append(" or reconciliation.").append(FIN_Reconciliation.PROPERTY_PROCESSED)
            .append(" = 'N') ");
      }
      whereClause.append(" order by ");
      whereClause.append(orderBy);
      if (!"".equals(orderBy))
        whereClause.append(", ");
      whereClause.append(" fatrx.").append(FIN_FinaccTransaction.PROPERTY_LINENO);
      final OBQuery<FIN_FinaccTransaction> obqFATrx = OBDal.getInstance().createQuery(
          FIN_FinaccTransaction.class, whereClause.toString());
      obqFATrx.setFirstResult(offset);
      obqFATrx.setMaxResult(pageSize);
      return obqFATrx.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public FieldProvider[] getPaymentsNotDeposited(FIN_FinancialAccount account, Date fromDate,
      Date toDate, boolean isReceipt) {

    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
        "dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    OBContext.setAdminMode();
    try {
      final StringBuilder whereClause = new StringBuilder();
      final List<Object> parameters = new ArrayList<Object>();

      whereClause.append(" as p ");
      whereClause.append(" where p.");
      whereClause.append(FIN_Payment.PROPERTY_ID);
      whereClause.append(" not in ");
      whereClause.append(" ( select coalesce(ft.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_FINPAYMENT);
      whereClause.append(".");
      whereClause.append(FIN_Payment.PROPERTY_ID);
      whereClause.append(", '-1') ");
      whereClause.append(" from ");
      whereClause.append(FIN_FinaccTransaction.TABLE_NAME);
      whereClause.append(" as ft");
      whereClause.append(" where ft.");
      whereClause.append(FIN_FinaccTransaction.PROPERTY_ACCOUNT);
      whereClause.append(".id = ?) ");
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_ACCOUNT);
      whereClause.append(".id = ? ");
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_STATUS);
      whereClause.append(" IN ('RPR', 'PPM')");
      parameters.add(account.getId());
      parameters.add(account.getId());
      // IsReceipt
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_RECEIPT);
      whereClause.append(" = ");
      whereClause.append(isReceipt);

      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_AMOUNT);
      whereClause.append(" != ");
      whereClause.append(BigDecimal.ZERO);

      // From Date
      if (fromDate != null) {
        whereClause.append(" and p.");
        whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        whereClause.append(" >= ? ");
        parameters.add(fromDate);
      }
      // To Date
      if (toDate != null) {
        whereClause.append(" AND p.");
        whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
        whereClause.append(" < ?");
        parameters.add(toDate);
      }
      // Order by date and payment no
      whereClause.append(" ORDER BY p.");
      whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
      whereClause.append(", p.");
      whereClause.append(FIN_Payment.PROPERTY_DOCUMENTNO);

      final OBQuery<FIN_Payment> obqP = OBDal.getInstance().createQuery(FIN_Payment.class,
          whereClause.toString(), parameters);

      List<FIN_Payment> paymentOBList = obqP.list();

      FIN_Payment[] FIN_Payments = new FIN_Payment[0];
      FIN_Payments = paymentOBList.toArray(FIN_Payments);
      FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(paymentOBList);

      for (int i = 0; i < data.length; i++) {
        BigDecimal debitAmt = BigDecimal.ZERO;
        BigDecimal creditAmt = BigDecimal.ZERO;

        if (FIN_Payments[i].isReceipt()) {
          if (FIN_Payments[i].getAmount().compareTo(BigDecimal.ZERO) == -1)
            creditAmt = FIN_Payments[i].getAmount().abs();
          else
            debitAmt = FIN_Payments[i].getAmount();
        } else {
          if (FIN_Payments[i].getAmount().compareTo(BigDecimal.ZERO) == -1)
            debitAmt = FIN_Payments[i].getAmount().abs();
          else
            creditAmt = FIN_Payments[i].getAmount();
        }

        FieldProviderFactory.setField(data[i], "paymentId", FIN_Payments[i].getId());
        FieldProviderFactory.setField(data[i], "paymentInfo", FIN_Payments[i].getDocumentNo()
            + " - " + FIN_Payments[i].getBusinessPartner().getName() + " - "
            + FIN_Payments[i].getCurrency().getISOCode());
        FieldProviderFactory.setField(data[i], "paymentDescription", FIN_Payments[i]
            .getDescription());
        FieldProviderFactory.setField(data[i], "paymentDate", dateFormater.format(
            FIN_Payments[i].getPaymentDate()).toString());
        FieldProviderFactory.setField(data[i], "debitAmount", debitAmt.toString());
        FieldProviderFactory.setField(data[i], "creditAmount", creditAmt.toString());
        FieldProviderFactory.setField(data[i], "rownum", "" + i);
      }

      return data;

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getPaymentProposalDetailAmount(FIN_PaymentScheduleDetail finPaymentScheduleDetail,
      FIN_PaymentProposal paymentProposal) {
    String amount = "";
    for (FIN_PaymentPropDetail propDetail : paymentProposal.getFINPaymentPropDetailList())
      if (propDetail.getFINPaymentScheduledetail() == finPaymentScheduleDetail)
        amount = propDetail.getAmount().toString();

    return amount;
  }

  public List<FIN_PaymentMethod> getFilteredPaymentMethods(String strFinancialAccountId,
      String strOrgId, boolean excludePaymentMethodWithoutAccount) {
    final OBCriteria<FIN_PaymentMethod> obc = OBDal.getInstance().createCriteria(
        FIN_PaymentMethod.class);
    obc.add(Expression.in("organization.id", OBContext.getOBContext()
        .getOrganizationStructureProvider().getNaturalTree(strOrgId)));
    obc.setFilterOnReadableOrganization(false);

    List<String> payMethods = new ArrayList<String>();
    if (strFinancialAccountId != null && !strFinancialAccountId.isEmpty()) {
      for (FinAccPaymentMethod finAccPayMethod : getObject(FIN_FinancialAccount.class,
          strFinancialAccountId).getFinancialMgmtFinAccPaymentMethodList()) {
        payMethods.add(finAccPayMethod.getPaymentMethod().getId());
      }
      if (payMethods.isEmpty()) {
        return (new ArrayList<FIN_PaymentMethod>());
      }
      obc.add(Expression.in("id", payMethods));
    } else {
      if (excludePaymentMethodWithoutAccount) {

        final OBCriteria<FinAccPaymentMethod> obcExc = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        obcExc.createAlias(FinAccPaymentMethod.PROPERTY_ACCOUNT, "acc");
        obcExc.add(Restrictions.in("acc.organization.id", OBContext.getOBContext()
            .getOrganizationStructureProvider().getNaturalTree(strOrgId)));
        obcExc.setFilterOnReadableOrganization(false);
        for (FinAccPaymentMethod fapm : obcExc.list()) {
          payMethods.add(fapm.getPaymentMethod().getId());
        }
        if (payMethods.isEmpty()) {
          return (new ArrayList<FIN_PaymentMethod>());
        }
        obc.add(Expression.in("id", payMethods));
      }
    }

    return obc.list();
  }

  public List<FIN_FinancialAccount> getFilteredFinancialAccounts(String strPaymentMethodId,
      String strOrgId, String strCurrencyId) {
    final OBCriteria<FIN_FinancialAccount> obc = OBDal.getInstance().createCriteria(
        FIN_FinancialAccount.class);
    obc.add(Expression.in("organization.id", OBContext.getOBContext()
        .getOrganizationStructureProvider().getNaturalTree(strOrgId)));
    obc.setFilterOnReadableOrganization(false);

    if (strCurrencyId != null && !strCurrencyId.isEmpty()) {
      obc.add(Expression.eq(FIN_FinancialAccount.PROPERTY_CURRENCY, OBDal.getInstance().get(
          Currency.class, strCurrencyId)));
    }

    List<String> finAccs = new ArrayList<String>();
    if (strPaymentMethodId != null && !strPaymentMethodId.isEmpty()) {
      for (FinAccPaymentMethod finAccPayMethod : getObject(FIN_PaymentMethod.class,
          strPaymentMethodId).getFinancialMgmtFinAccPaymentMethodList()) {
        finAccs.add(finAccPayMethod.getAccount().getId());
      }
      if (finAccs.isEmpty()) {
        return (new ArrayList<FIN_FinancialAccount>());
      }
      obc.add(Expression.in("id", finAccs));
    }
    return obc.list();
  }

  public FinAccPaymentMethod getFinancialAccountPaymentMethod(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod) {
    final OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance().createCriteria(
        FinAccPaymentMethod.class);
    obc.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, account));
    obc.add(Expression.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
    obc.add(Expression.eq(FinAccPaymentMethod.PROPERTY_ACTIVE, true));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    return obc.list().get(0);
  }

  public boolean isAutomatedExecutionPayment(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, boolean isReceipt) {
    FinAccPaymentMethod finAccPaymentMethod = getFinancialAccountPaymentMethod(account,
        paymentMethod);
    return "A".equals(isReceipt ? finAccPaymentMethod.getPayinExecutionType() : finAccPaymentMethod
        .getPayoutExecutionType());
  }

  public boolean hasNotDeferredExecutionProcess(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, Boolean isReceipt) {
    FinAccPaymentMethod finAccPaymentMethod = getFinancialAccountPaymentMethod(account,
        paymentMethod);
    return (isReceipt ? (finAccPaymentMethod.getPayinExecutionProcess() != null && !finAccPaymentMethod
        .isPayinDeferred())
        : (finAccPaymentMethod.getPayoutExecutionProcess() != null && !finAccPaymentMethod
            .isPayoutDeferred()));
  }

  public PaymentExecutionProcess getExecutionProcess(FIN_Payment payment) {
    return getExecutionProcess(payment.getAccount(), payment.getPaymentMethod(), payment
        .isReceipt());
  }

  public PaymentExecutionProcess getExecutionProcess(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, Boolean receipt) {
    FinAccPaymentMethod finAccPaymentMethod = getFinancialAccountPaymentMethod(account,
        paymentMethod);
    return receipt ? finAccPaymentMethod.getPayinExecutionProcess() : finAccPaymentMethod
        .getPayoutExecutionProcess();
  }

  public boolean isAutomaticExecutionProcess(PaymentExecutionProcess executionProcess) {
    List<PaymentExecutionProcessParameter> parameters = executionProcess
        .getFinancialMgmtPaymentExecutionProcessParameterList();
    for (PaymentExecutionProcessParameter parameter : parameters) {
      if ("CONSTANT".equals(parameter.getParameterType())
          && parameter.getDefaultTextValue() != null && parameter.getDefaultTextValue() != "")
        return false;
      else if ("IN".equals(parameter.getParameterType())) {
        if ("CHECK".equals(parameter.getInputType())
            && (parameter.getDefaultValueForFlag() == null || "".equals(parameter
                .getDefaultValueForFlag())))
          return false;
        else if ("TEXT".equals(parameter.getInputType())
            && (parameter.getDefaultTextValue() == null || "".equals(parameter
                .getDefaultTextValue())))
          return false;
      }
    }
    return true;
  }

  public List<PaymentExecutionProcessParameter> getInPaymentExecutionParameters(
      PaymentExecutionProcess executionProcess) {
    OBCriteria<PaymentExecutionProcessParameter> obc = OBDal.getInstance().createCriteria(
        PaymentExecutionProcessParameter.class);
    obc.add(Expression.eq(PaymentExecutionProcessParameter.PROPERTY_PAYMENTEXECUTIONPROCESS,
        executionProcess));
    obc.add(Expression.eq(PaymentExecutionProcessParameter.PROPERTY_PARAMETERTYPE, "IN"));
    return obc.list();
  }

  public List<FIN_Payment> getPaymentProposalPayments(FIN_PaymentProposal paymentProposal) {
    List<FIN_Payment> paymentsInProposal = new ArrayList<FIN_Payment>();
    for (FIN_PaymentPropDetail proposalDetail : paymentProposal.getFINPaymentPropDetailList())
      if ("RPAE".equals(proposalDetail.getFINPaymentScheduledetail().getPaymentDetails()
          .getFinPayment().getStatus()))
        paymentsInProposal.add(proposalDetail.getFINPaymentScheduledetail().getPaymentDetails()
            .getFinPayment());

    return paymentsInProposal;
  }

  /**
   * This method returns list of Payments that are in Awaiting Execution status and filtered by the
   * following parameters.
   * 
   * @param organizationId
   *          Organization
   * @param paymentMethodId
   *          Payment Method used for the payment.
   * @param financialAccountId
   *          Financial Account used for the payment.
   * @param dateFrom
   *          Optional. Filters payments made after the specified date.
   * @param dateTo
   *          Optional. Filters payments made before the specified date.
   * @param offset
   *          Starting register number.
   * @param pageSize
   *          Limited the max number of results.
   * @param strOrderByProperty
   *          Property used for ordering the results.
   * @param strAscDesc
   *          if true order by asc, if false order by desc
   * @param isReceipt
   *          if true sales, if false purchase
   * @return Filtered Payment list.
   */
  public List<FIN_Payment> getPayExecRowCount(String organizationId, String paymentMethodId,
      String financialAccountId, Date dateFrom, Date dateTo, int offset, int pageSize,
      String strOrderByProperty, String strAscDesc, boolean isReceipt) {

    List<FIN_Payment> emptyList = new ArrayList<FIN_Payment>();
    if (organizationId == null || organizationId.isEmpty()) {
      return emptyList;
    }

    OBContext.setAdminMode();
    try {

      FIN_PaymentMethod obPayMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          paymentMethodId);
      FIN_FinancialAccount obFinAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
          financialAccountId);

      OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
      obcPayment.add(Expression.in("organization.id", OBContext.getOBContext()
          .getOrganizationStructureProvider().getParentTree(organizationId, true)));
      obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_STATUS, "RPAE"));
      obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_PAYMENTMETHOD, obPayMethod));
      obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_ACCOUNT, obFinAccount));
      obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
      if (dateFrom != null)
        obcPayment.add(Expression.ge(FIN_Payment.PROPERTY_PAYMENTDATE, dateFrom));
      if (dateTo != null)
        obcPayment.add(Expression.lt(FIN_Payment.PROPERTY_PAYMENTDATE, dateTo));

      boolean ascDesc = true;
      if (strAscDesc != null && !strAscDesc.isEmpty())
        ascDesc = "asc".equalsIgnoreCase(strAscDesc);
      if (strOrderByProperty != null && !strOrderByProperty.isEmpty())
        obcPayment.addOrderBy(strOrderByProperty, ascDesc);
      obcPayment.setFirstResult(offset);
      obcPayment.setMaxResults(pageSize);

      return obcPayment.list();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<FIN_Payment> getPendingExecutionPayments(String strInvoiceId) {
    List<FIN_Payment> payments = new ArrayList<FIN_Payment>();
    List<FIN_PaymentSchedule> paySchedList = new AdvPaymentMngtDao().getObject(Invoice.class,
        strInvoiceId).getFINPaymentScheduleList();
    OBCriteria<FIN_PaymentScheduleDetail> psdCriteria = OBDal.getInstance().createCriteria(
        FIN_PaymentScheduleDetail.class);
    if (!paySchedList.isEmpty())
      psdCriteria.add(Expression.in(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
          paySchedList));
    for (FIN_PaymentScheduleDetail psd : psdCriteria.list()) {
      if (psd.getPaymentDetails() != null) {
        FIN_Payment payment = psd.getPaymentDetails().getFinPayment();
        if ("RPAE".equals(payment.getStatus())
            && hasNotDeferredExecutionProcess(payment.getAccount(), payment.getPaymentMethod(),
                payment.isReceipt()))
          payments.add(payment);
      }
    }

    return payments;
  }

  public void setPaymentExecuting(FIN_Payment payment, boolean executing) {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Expression.eq(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENT, payment));
    List<APRMPendingPaymentFromInvoice> pendingPayments = ppfiCriteria.list();
    if (pendingPayments != null && pendingPayments.size() > 0) {
      APRMPendingPaymentFromInvoice pendingPayment = pendingPayments.get(0);
      pendingPayment.setProcessNow(executing);
      OBDal.getInstance().flush();
      OBDal.getInstance().save(pendingPayment);
    }
  }

  public boolean isPaymentBeingExecuted(FIN_Payment payment) {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Expression.eq(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENT, payment));
    List<APRMPendingPaymentFromInvoice> pendingPayments = ppfiCriteria.list();
    if (pendingPayments != null && pendingPayments.size() > 0) {
      return pendingPayments.get(0).isProcessNow();
    } else
      return false;

  }

  public void removeFromExecutionPending(FIN_Payment payment) {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Expression.eq(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENT, payment));
    List<APRMPendingPaymentFromInvoice> pendingPayments = ppfiCriteria.list();
    OBDal.getInstance().remove(pendingPayments.get(0));
    OBDal.getInstance().flush();
  }

  public List<APRMPendingPaymentFromInvoice> getPendingPayments() {
    OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance().createCriteria(
        APRMPendingPaymentFromInvoice.class);
    ppfiCriteria.add(Expression.eq(APRMPendingPaymentFromInvoice.PROPERTY_PROCESSNOW, false));
    ppfiCriteria.addOrderBy(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENTEXECUTIONPROCESS, false);
    ppfiCriteria.addOrderBy(APRMPendingPaymentFromInvoice.PROPERTY_ORGANIZATION, false);
    return ppfiCriteria.list();
  }

  public BigDecimal getCustomerCredit(BusinessPartner bp, boolean isReceipt) {
    BigDecimal creditAmount = BigDecimal.ZERO;
    for (FIN_Payment payment : getCustomerPaymentsWithCredit(bp, isReceipt))
      creditAmount = creditAmount.add(payment.getGeneratedCredit()).subtract(
          payment.getUsedCredit());
    return creditAmount;
  }

  public List<FIN_Payment> getCustomerPaymentsWithCredit(BusinessPartner bp, boolean isReceipt) {
    OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
    obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, bp));
    obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
    obcPayment.add(Expression.ne(FIN_Payment.PROPERTY_GENERATEDCREDIT, BigDecimal.ZERO));
    obcPayment.add(Expression.ne(FIN_Payment.PROPERTY_STATUS, "RPAP"));
    obcPayment.add(Expression.neProperty(FIN_Payment.PROPERTY_GENERATEDCREDIT,
        FIN_Payment.PROPERTY_USEDCREDIT));
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, true);
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_DOCUMENTNO, true);
    return obcPayment.list();
  }

  public List<FIN_Payment> getCustomerPaymentsWithUsedCredit(BusinessPartner bp, Boolean isReceipt) {
    OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
    obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, bp));
    obcPayment.add(Expression.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
    obcPayment.add(Expression.ne(FIN_Payment.PROPERTY_GENERATEDCREDIT, BigDecimal.ZERO));
    obcPayment.add(Expression.ne(FIN_Payment.PROPERTY_USEDCREDIT, BigDecimal.ZERO));
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, false);
    obcPayment.addOrderBy(FIN_Payment.PROPERTY_DOCUMENTNO, false);
    return obcPayment.list();
  }
}
