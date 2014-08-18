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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.algorithm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.utility.FIN_CandidateRecord;
import org.openbravo.advpaymentmngt.utility.FIN_MatchingCandidatesAlgorithm_I;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;

public class StandardMatchingCandidatesAlgorithm implements FIN_MatchingCandidatesAlgorithm_I {

  @Override
  public List<FIN_CandidateRecord> getTransactionCandidates(FIN_BankStatementLine line,
      List<FIN_FinaccTransaction> excluded) {
    final BigDecimal amount = line.getCramount().subtract(line.getDramount());
    final List<FIN_CandidateRecord> candidateRecords = new ArrayList<FIN_CandidateRecord>();

    for (final FIN_FinaccTransaction transactionCandidate : MatchTransactionDao
        .getMatchingFinancialTransaction(line.getBankStatement().getAccount().getId(), amount,
            excluded)) {
      candidateRecords.add(new FIN_CandidateRecord(transactionCandidate));
    }

    return candidateRecords;
  }

  @Override
  public List<FIN_CandidateRecord> getPaymentCandidates(FIN_BankStatementLine line,
      List<FIN_Payment> excluded) {
    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    BigDecimal amount = line.getCramount().subtract(line.getDramount());
    boolean isReceipt = amount.signum() > 0;
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as p "); // pending payments //
      whereClause.append(" where not exists ");
      whereClause
          .append(" (select 1 from FIN_Finacc_Transaction as t where t.finPayment.id = p.id and t.processed = true) ");

      if (line.getBusinessPartner() != null) {
        whereClause.append(" and p.");
        whereClause.append(FIN_Payment.PROPERTY_BUSINESSPARTNER);
        whereClause.append(".id = '");
        whereClause.append(line.getBusinessPartner().getId());
        whereClause.append("'");
      }
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_PROCESSED);
      whereClause.append(" = true ");
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_ORGANIZATION);
      whereClause.append(".id in (");
      whereClause.append(parse(OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(line.getOrganization().getId())));
      whereClause.append(")");
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_STATUS);
      whereClause.append(" not in ('RPAP', 'RPAE') ");
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_RECEIPT);
      whereClause.append(" = ");
      whereClause.append(isReceipt);
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_CURRENCY);
      whereClause.append(".id = '");
      whereClause.append(line.getBankStatement().getAccount().getCurrency().getId());
      whereClause.append("'");
      // amount
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_AMOUNT);
      whereClause.append(" = ?");
      parameters.add(amount.abs());
      // dateTo
      whereClause.append(" and p.");
      whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
      whereClause.append(" <= ?");
      parameters.add(line.getTransactionDate());
      // TODO: Add order to show first scheduled payments from invoices and later scheduled payments
      // from not invoiced orders.
      whereClause.append(" order by");
      whereClause.append(" p.");
      whereClause.append(FIN_Payment.PROPERTY_PAYMENTDATE);
      whereClause.append(", p.");
      whereClause.append(FIN_Payment.PROPERTY_DOCUMENTNO);
      final OBQuery<FIN_Payment> obqPayment = OBDal.getInstance().createQuery(FIN_Payment.class,
          whereClause.toString());

      obqPayment.setParameters(parameters);

      // TODO use Scrollable
      final List<FIN_CandidateRecord> candidateRecords = new ArrayList<FIN_CandidateRecord>();
      for (final FIN_Payment paymentCandidate : obqPayment.list()) {
        candidateRecords.add(new FIN_CandidateRecord(paymentCandidate));
      }
      return candidateRecords;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public List<FIN_CandidateRecord> getInvoiceCandidates(FIN_BankStatementLine line,
      List<Invoice> excluded) {
    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    BigDecimal amount = line.getCramount().subtract(line.getDramount());
    boolean isReceipt = amount.signum() > 0;
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd "); // pending scheduled payments //
      whereClause.append(" left outer join psd.invoicePaymentSchedule as psdi");
      whereClause.append(" left outer join psdi.invoice ");
      whereClause.append(" where psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      whereClause.append(" is null");
      whereClause.append(" and psd.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORGANIZATION);
      whereClause.append(".id in (");
      whereClause.append(parse(OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(line.getOrganization().getId())));
      whereClause.append(")");

      // Transaction type filter
      whereClause.append(" and psdi.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(" is not null");

      if (line.getBusinessPartner() != null) {
        whereClause.append(" and psdi.");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        whereClause.append(".");
        whereClause.append(Invoice.PROPERTY_BUSINESSPARTNER);
        whereClause.append(".id = '");
        whereClause.append(line.getBusinessPartner().getId());
        whereClause.append("'");
      }
      whereClause.append(" and psdi.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(".");
      whereClause.append(Invoice.PROPERTY_SALESTRANSACTION);
      whereClause.append(" = ");
      whereClause.append(isReceipt);
      whereClause.append(" and psdi.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(".");
      whereClause.append(Invoice.PROPERTY_CURRENCY);
      whereClause.append(".id = '");
      whereClause.append(line.getBankStatement().getAccount().getCurrency().getId());
      whereClause.append("'");
      // amount
      whereClause.append(" and psdi.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT);
      whereClause.append(" = ?");
      parameters.add(amount.abs());
      // dateTo
      // TODO Review this date. i guess someone can pay a bill prior to due date
      // whereClause.append(" and psd.");
      // whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      // whereClause.append(".");
      // whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      // whereClause.append(" <= ?");
      // parameters.add(line.getTransactionDate());
      // TODO: Add order to show first scheduled payments from invoices and later scheduled payments
      // from not invoiced orders.
      whereClause.append(" order by");
      whereClause.append(" psdi.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_FINPAYMENTPRIORITY);
      whereClause.append(", psdi.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", psdi.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(".");
      whereClause.append(Invoice.PROPERTY_DOCUMENTNO);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      obqPSD.setParameters(parameters);

      // TODO get invoices and use Scrollable
      final List<FIN_CandidateRecord> candidateRecords = new ArrayList<FIN_CandidateRecord>();
      final Set<Invoice> invoices = new HashSet<Invoice>();
      for (final FIN_PaymentScheduleDetail psd : obqPSD.list()) {
        invoices.add(psd.getInvoicePaymentSchedule().getInvoice());
      }
      for (final Invoice invoice : invoices) {
        candidateRecords.add(new FIN_CandidateRecord(invoice));
      }

      return candidateRecords;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public List<FIN_CandidateRecord> getOrderCandidates(FIN_BankStatementLine line,
      List<Order> excluded) {
    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    BigDecimal amount = line.getCramount().subtract(line.getDramount());
    boolean isReceipt = amount.signum() > 0;
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd "); // pending scheduled payments //
      whereClause.append(" left outer join psd.orderPaymentSchedule psdo");
      whereClause.append(" left outer join psdo.order ");
      whereClause.append(" where psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      whereClause.append(" is null");
      whereClause.append(" and psd.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORGANIZATION);
      whereClause.append(".id in (");
      whereClause.append(parse(OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(line.getOrganization().getId())));
      whereClause.append(")");

      // Transaction type filter
      whereClause.append(" and psdo.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      whereClause.append(" is not null");

      if (line.getBusinessPartner() != null) {
        whereClause.append(" and psdo.");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
        whereClause.append(".");
        whereClause.append(Order.PROPERTY_BUSINESSPARTNER);
        whereClause.append(".id = '");
        whereClause.append(line.getBusinessPartner().getId());
        whereClause.append("'");
      }
      whereClause.append(" and psdo.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      whereClause.append(".");
      whereClause.append(Order.PROPERTY_SALESTRANSACTION);
      whereClause.append(" = ");
      whereClause.append(isReceipt);
      whereClause.append(" and psdo.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      whereClause.append(".");
      whereClause.append(Order.PROPERTY_CURRENCY);
      whereClause.append(".id = '");
      whereClause.append(line.getBankStatement().getAccount().getCurrency().getId());
      whereClause.append("'");
      // amount
      whereClause.append(" and psdo.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT);
      whereClause.append(" = ?");
      parameters.add(amount.abs());
      // dateTo
      // TODO Review this date. i guess someone can pay a bill prior to due date
      // whereClause.append(" and psd.");
      // whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_ORDERPAYMENTSCHEDULE);
      // whereClause.append(".");
      // whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      // whereClause.append(" <= ?");
      // parameters.add(line.getTransactionDate());
      // TODO: Add order to show first scheduled payments from invoices and later scheduled payments
      // from not invoiced orders.
      whereClause.append(" order by");
      whereClause.append(" psdo.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_FINPAYMENTPRIORITY);
      whereClause.append(", psdo.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", psdo.");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      whereClause.append(".");
      whereClause.append(Order.PROPERTY_DOCUMENTNO);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      obqPSD.setParameters(parameters);

      // TODO get invoices and use Scrollable
      final List<FIN_CandidateRecord> candidateRecords = new ArrayList<FIN_CandidateRecord>();
      final Set<Order> orders = new HashSet<Order>();
      for (final FIN_PaymentScheduleDetail psd : obqPSD.list()) {
        orders.add(psd.getOrderPaymentSchedule().getOrder());
      }
      for (final Order order : orders) {
        candidateRecords.add(new FIN_CandidateRecord(order));
      }

      return candidateRecords;

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  String parse(Set<String> stringSet) {
    String result = "";
    Iterator<String> i = stringSet.iterator();
    while (i.hasNext()) {
      result += "'";
      result += i.next();
      result += "', ";
    }
    if (result.length() > 0)
      result = result.substring(0, result.length() - 2);
    return result;
  }

}
