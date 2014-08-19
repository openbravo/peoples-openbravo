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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
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
    OBContext.setAdminMode(true);
    ScrollableResults scrollLines = null;
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

      final List<FIN_CandidateRecord> candidateRecords = new ArrayList<FIN_CandidateRecord>();
      scrollLines = obqPayment.scroll(ScrollMode.FORWARD_ONLY);
      int j = 1;
      while (scrollLines.next()) {
        final FIN_Payment paymentCandidate = (FIN_Payment) scrollLines.get(0);
        candidateRecords.add(new FIN_CandidateRecord(paymentCandidate));
        if (j % 100 == 0) {
          OBDal.getInstance().getSession().clear();
        }
        j++;
      }
      return candidateRecords;
    } finally {
      if (scrollLines != null) {
        scrollLines.close();
      }
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public List<FIN_CandidateRecord> getInvoiceCandidates(FIN_BankStatementLine line,
      List<Invoice> excluded) {
    final StringBuilder hql = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    BigDecimal amount = line.getCramount().subtract(line.getDramount());
    boolean isReceipt = amount.signum() > 0;
    OBContext.setAdminMode(true);
    ScrollableResults scrollLines = null;
    try {
      hql.append(" select distinct(i.id) ");

      // hql.append(" psdi.");
      // hql.append(FIN_PaymentSchedule.PROPERTY_FINPAYMENTPRIORITY);
      // hql.append(", psdi.");
      // hql.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      // hql.append(", psdi.");
      // hql.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      // hql.append(".");
      // hql.append(Invoice.PROPERTY_DOCUMENTNO);

      hql.append(" from FIN_Payment_ScheduleDetail as psd "); // pending scheduled payments //
      hql.append(" inner join psd.invoicePaymentSchedule as psdi");
      hql.append(" inner join psdi.invoice as i ");
      hql.append(" where psd.");
      hql.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      hql.append(" is null");
      hql.append(" and psd.");
      hql.append(FIN_PaymentSchedule.PROPERTY_ORGANIZATION);
      hql.append(".id in (");
      hql.append(parse(OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(line.getOrganization().getId())));
      hql.append(")");

      // Transaction type filter
      hql.append(" and psdi.");
      hql.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      hql.append(" is not null");

      if (line.getBusinessPartner() != null) {
        hql.append(" and psdi.");
        hql.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        hql.append(".");
        hql.append(Invoice.PROPERTY_BUSINESSPARTNER);
        hql.append(".id = '");
        hql.append(line.getBusinessPartner().getId());
        hql.append("'");
      }
      hql.append(" and psdi.");
      hql.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      hql.append(".");
      hql.append(Invoice.PROPERTY_SALESTRANSACTION);
      hql.append(" = ");
      hql.append(isReceipt);
      hql.append(" and psdi.");
      hql.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      hql.append(".");
      hql.append(Invoice.PROPERTY_CURRENCY);
      hql.append(".id = '");
      hql.append(line.getBankStatement().getAccount().getCurrency().getId());
      hql.append("'");
      // amount
      hql.append(" and psdi.");
      hql.append(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT);
      hql.append(" = ?");
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
      // hql.append(" order by");
      // hql.append(" psdi.");
      // hql.append(FIN_PaymentSchedule.PROPERTY_FINPAYMENTPRIORITY);
      // hql.append(", psdi.");
      // hql.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      // hql.append(", psdi.");
      // hql.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      // hql.append(".");
      // hql.append(Invoice.PROPERTY_DOCUMENTNO);

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hql.toString());
      for (int i = 0; i < parameters.size(); i++) {
        query.setParameter(i, parameters.get(i));
      }

      final List<FIN_CandidateRecord> candidateRecords = new ArrayList<FIN_CandidateRecord>();
      scrollLines = query.scroll(ScrollMode.FORWARD_ONLY);
      int j = 1;
      while (scrollLines.next()) {
        final String invoiceId = scrollLines.getString(0);
        candidateRecords.add(new FIN_CandidateRecord(OBDal.getInstance().get(Invoice.class,
            invoiceId)));
        if (j % 100 == 0) {
          session.clear();
        }
        j++;
      }

      return candidateRecords;
    } finally {
      if (scrollLines != null) {
        scrollLines.close();
      }
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public List<FIN_CandidateRecord> getOrderCandidates(FIN_BankStatementLine line,
      List<Order> excluded) {
    final StringBuilder hql = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    BigDecimal amount = line.getCramount().subtract(line.getDramount());
    boolean isReceipt = amount.signum() > 0;
    OBContext.setAdminMode(true);
    ScrollableResults scrollLines = null;
    try {
      hql.append(" select distinct(o.id) ");
      hql.append(" from FIN_Payment_ScheduleDetail as psd "); // pending scheduled payments //
      hql.append(" inner join psd.orderPaymentSchedule psdo");
      hql.append(" inner join psdo.order as o ");
      hql.append(" where psd.");
      hql.append(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS);
      hql.append(" is null");
      hql.append(" and psd.");
      hql.append(FIN_PaymentSchedule.PROPERTY_ORGANIZATION);
      hql.append(".id in (");
      hql.append(parse(OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(line.getOrganization().getId())));
      hql.append(")");

      // Transaction type filter
      hql.append(" and psdo.");
      hql.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      hql.append(" is not null");

      if (line.getBusinessPartner() != null) {
        hql.append(" and psdo.");
        hql.append(FIN_PaymentSchedule.PROPERTY_ORDER);
        hql.append(".");
        hql.append(Order.PROPERTY_BUSINESSPARTNER);
        hql.append(".id = '");
        hql.append(line.getBusinessPartner().getId());
        hql.append("'");
      }
      hql.append(" and psdo.");
      hql.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      hql.append(".");
      hql.append(Order.PROPERTY_SALESTRANSACTION);
      hql.append(" = ");
      hql.append(isReceipt);
      hql.append(" and psdo.");
      hql.append(FIN_PaymentSchedule.PROPERTY_ORDER);
      hql.append(".");
      hql.append(Order.PROPERTY_CURRENCY);
      hql.append(".id = '");
      hql.append(line.getBankStatement().getAccount().getCurrency().getId());
      hql.append("'");
      // amount
      hql.append(" and psdo.");
      hql.append(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT);
      hql.append(" = ?");
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

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hql.toString());
      for (int i = 0; i < parameters.size(); i++) {
        query.setParameter(i, parameters.get(i));
      }

      final List<FIN_CandidateRecord> candidateRecords = new ArrayList<FIN_CandidateRecord>();
      scrollLines = query.scroll(ScrollMode.FORWARD_ONLY);
      int j = 1;
      while (scrollLines.next()) {
        final String orderId = scrollLines.getString(0);
        candidateRecords
            .add(new FIN_CandidateRecord(OBDal.getInstance().get(Order.class, orderId)));
        if (j % 100 == 0) {
          session.clear();
        }
        j++;
      }

      return candidateRecords;

    } finally {
      if (scrollLines != null) {
        scrollLines.close();
      }
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
