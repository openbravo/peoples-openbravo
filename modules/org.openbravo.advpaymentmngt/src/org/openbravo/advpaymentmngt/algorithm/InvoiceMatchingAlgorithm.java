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

package org.openbravo.advpaymentmngt.algorithm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_MatchedTransaction;
import org.openbravo.advpaymentmngt.utility.FIN_MatchingAlgorithm;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.DalConnectionProvider;

public class InvoiceMatchingAlgorithm implements FIN_MatchingAlgorithm {

  public FIN_MatchedTransaction match(FIN_BankStatementLine line,
      List<FIN_FinaccTransaction> excluded) throws ServletException {
    List<FIN_FinaccTransaction> transactions = MatchTransactionDao.getMatchingFinancialTransaction(
        line.getBankStatement().getAccount().getId(), line.getReferenceNo(), (line.getCramount()
            .subtract(line.getDramount())), line.getBpartnername(), excluded);
    if (!transactions.isEmpty())
      return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.STRONG);
    transactions = MatchTransactionDao.getMatchingFinancialTransaction(line.getBankStatement()
        .getAccount().getId(), line.getCramount().subtract(line.getDramount()), excluded);
    if (!transactions.isEmpty())
      return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.WEAK);
    // If there is no match against existing transactions then try with existing payments
    FIN_FinaccTransaction transactionCreatedFromPayment = findPayment(line);
    if (transactionCreatedFromPayment != null)
      transactions.add(transactionCreatedFromPayment);
    if (!transactions.isEmpty() && transactions.get(0) != null)
      return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.STRONG);
    // If there is no match against existing transactions then try with existing invoices
    FIN_FinaccTransaction transactionCreatedFromInvoice = findInvoice(line);
    if (transactionCreatedFromInvoice != null)
      transactions.add(transactionCreatedFromInvoice);
    if (!transactions.isEmpty() && transactions.get(0) != null)
      return new FIN_MatchedTransaction(transactions.get(0), FIN_MatchedTransaction.STRONG);

    return new FIN_MatchedTransaction(null, FIN_MatchedTransaction.NOMATCH);
  }

  FIN_FinaccTransaction findInvoice(FIN_BankStatementLine line) {
    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    BigDecimal amount = line.getCramount().subtract(line.getDramount());
    boolean isReceipt = amount.signum() > 0;
    // FIXME: added to access the FIN_PaymentSchedule and FIN_PaymentScheduleDetail tables to be
    // removed when new security implementation is done
    OBContext.setAdminMode();
    try {

      whereClause.append(" as psd "); // pending scheduled payments //
      whereClause.append(" left outer join psd.invoicePaymentSchedule ");
      whereClause.append(" left outer join psd.invoicePaymentSchedule.invoice ");
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
      whereClause.append(" and psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(" is not null");
      if (line.getBusinessPartner() != null) {
        whereClause.append(" and psd.");
        whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
        whereClause.append(".");
        whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
        whereClause.append(".");
        whereClause.append(Invoice.PROPERTY_BUSINESSPARTNER);
        whereClause.append(".id = '");
        whereClause.append(line.getBusinessPartner().getId());
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
      whereClause.append(line.getBankStatement().getAccount().getCurrency().getId());
      whereClause.append("'");
      // amount
      whereClause.append(" and psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
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
      whereClause.append(" psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_DUEDATE);
      whereClause.append(", psd.");
      whereClause.append(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE);
      whereClause.append(".");
      whereClause.append(FIN_PaymentSchedule.PROPERTY_INVOICE);
      whereClause.append(".");
      whereClause.append(Invoice.PROPERTY_DOCUMENTNO);
      final OBQuery<FIN_PaymentScheduleDetail> obqPSD = OBDal.getInstance().createQuery(
          FIN_PaymentScheduleDetail.class, whereClause.toString());

      obqPSD.setParameters(parameters);
      List<FIN_PaymentScheduleDetail> paymentScheduleDetails = obqPSD.list();
      if (!paymentScheduleDetails.isEmpty()) {
        AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
        DocumentType docType = FIN_Utility.getDocumentType(paymentScheduleDetails.get(0)
            .getOrganization(), isReceipt ? "ARR" : "APP");
        // get DocumentNo
        HashMap<String, BigDecimal> hm = new HashMap<String, BigDecimal>();
        hm.put(paymentScheduleDetails.get(0).getId(), paymentScheduleDetails.get(0).getAmount());
        String strPaymentDocumentNo = FIN_Utility.getDocumentNo(docType,
            docType.getTable() != null ? docType.getTable().getDBTableName() : "");
        FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt, docType,
            strPaymentDocumentNo, paymentScheduleDetails.get(0).getInvoicePaymentSchedule()
                .getInvoice().getBusinessPartner(), paymentScheduleDetails.get(0)
                .getInvoicePaymentSchedule().getFinPaymentmethod(), line.getBankStatement()
                .getAccount(), paymentScheduleDetails.get(0).getAmount().toString(), line
                .getTransactionDate(), paymentScheduleDetails.get(0).getOrganization(), line
                .getReferenceNo(), paymentScheduleDetails.subList(0, 1), hm, false, false);
        try {
          ConnectionProvider conn = new DalConnectionProvider();
          FIN_AddPayment.processPayment(new VariablesSecureApp(OBContext.getOBContext().getUser()
              .getId(), OBContext.getOBContext().getCurrentClient().getId(), OBContext
              .getOBContext().getCurrentOrganization().getId(), OBContext.getOBContext().getRole()
              .getId()), conn, "P", payment);
        } catch (Exception e) {
          return null;
        }
        FIN_FinaccTransaction transaction = dao.getFinancialTransaction(payment);
        TransactionsDao.process(transaction);
        return transaction;
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  FIN_FinaccTransaction findPayment(FIN_BankStatementLine line) {
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
      List<FIN_Payment> payments = obqPayment.list();
      if (!payments.isEmpty()) {
        AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
        FIN_FinaccTransaction transaction = dao.getFinancialTransaction(payments.get(0));
        TransactionsDao.process(transaction);
        return transaction;
      }
      return null;
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
