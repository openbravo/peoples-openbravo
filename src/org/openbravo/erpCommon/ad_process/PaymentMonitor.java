package org.openbravo.erpCommon.ad_process;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.DebtPayment;
import org.openbravo.service.db.DalConnectionProvider;

public class PaymentMonitor {
  static Logger log4j = Logger.getLogger(PaymentMonitor.class);

  /**
   * 
   * @param invoice
   */
  public static void updateInvoice(Invoice invoice) {
    List<DebtPayment> payments = invoice.getFinancialMgmtDebtPaymentList();
    BigDecimal paidAmount = BigDecimal.ZERO;
    BigDecimal overDueAmount = BigDecimal.ZERO;
    for (DebtPayment payment : payments) {
      if (payment.isPaymentComplete())
        paidAmount = paidAmount.add(getConvertedAmt(payment.getAmount(), payment.getCurrency()
            .getId(), invoice.getCurrency().getId(), invoice.getAccountingDate(), invoice
            .getClient().getId(), invoice.getOrganization().getId()));
      else {
        paidAmount = paidAmount.add(calculatePaidAmount(payment, invoice.getCurrency().getId(),
            invoice.getAccountingDate(), new BigDecimal("1")));
        overDueAmount = overDueAmount.add(calculateOverdueAmount(payment, invoice.getCurrency()
            .getId(), invoice.getAccountingDate(), new BigDecimal("1")));
      }
    }
    if (paidAmount.setScale(invoice.getCurrency().getStandardPrecision().intValue(),
        BigDecimal.ROUND_HALF_UP).compareTo(invoice.getGrandTotalAmount()) == 0) {
      invoice.setDaysTillDue(new Long(0));
      invoice.setDueAmount(BigDecimal.ZERO);
      invoice.setPaymentComplete(true);
    } else {
      invoice.setDaysTillDue(getDaysTillDue(invoice));
      invoice.setPaymentComplete(false);
    }
    invoice.setTotalpaid(paidAmount.setScale(invoice.getCurrency().getStandardPrecision()
        .intValue(), BigDecimal.ROUND_HALF_UP));
    invoice.setDueAmount(overDueAmount.setScale(invoice.getCurrency().getStandardPrecision()
        .intValue(), BigDecimal.ROUND_HALF_UP));
    invoice.setOutstandingAmount(invoice.getGrandTotalAmount().subtract(invoice.getTotalpaid()));
    invoice.setLastCalculatedOnDate(new Date(System.currentTimeMillis()));
    OBDal.getInstance().save(invoice);
    OBDal.getInstance().flush();
    return;
  }

  /**
   * 
   * @param invoice
   * @return
   */
  public static Long getDaysTillDue(Invoice invoice) {
    Long daysToDue = new Long(0);
    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as dp ");
    whereClause.append(" where dp.invoice.id ='").append(invoice.getId()).append("'");
    whereClause.append(" order by dp.dueDate");

    final OBQuery<DebtPayment> obqParameters = OBDal.getInstance().createQuery(DebtPayment.class,
        whereClause.toString());
    final List<DebtPayment> payments = obqParameters.list();
    ArrayList<Long> allDaysToDue = new ArrayList<Long>();
    for (DebtPayment payment : payments) {
      if (!payment.isPaymentComplete()) {
        Long paymentDue = getPaymentDaysToDue(payment);
        if (paymentDue != null)
          allDaysToDue.add(paymentDue);
      }
    }
    if (allDaysToDue == null || allDaysToDue.size() == 0)
      return daysToDue;
    allDaysToDue = sort(allDaysToDue);
    daysToDue = allDaysToDue.get(0);
    return daysToDue;
  }

  /**
   * 
   * @param payment
   * @return
   */
  public static Long getPaymentDaysToDue(DebtPayment payment) {
    if (payment.isPaymentComplete())
      return null;
    if (payment.getSettlementCancelled() == null)
      return getDaysToDue(payment.getDueDate());
    final OBCriteria<DebtPayment> obc = OBDal.getInstance().createCriteria(DebtPayment.class);
    obc.add(Expression.eq("settlementGenerate", payment.getSettlementCancelled()));
    obc.addOrderBy("dueDate", true);
    final List<DebtPayment> payments = obc.list();
    ArrayList<Long> allDaysToDue = new ArrayList<Long>();
    for (DebtPayment generatedPayment : payments) {
      Long generatedPaymentOverDue = getPaymentDaysToDue(generatedPayment);
      if (generatedPaymentOverDue != null)
        allDaysToDue.add(generatedPaymentOverDue);
    }
    if (allDaysToDue == null || allDaysToDue.size() == 0)
      return null;
    allDaysToDue = sort(allDaysToDue);
    Long daysToDue = new Long(0);
    daysToDue = allDaysToDue.get(0);
    return daysToDue;
  }

  /**
   * 
   * @param date
   * @return
   */
  public static Long getDaysToDue(Date date) {
    Date now = new Date(System.currentTimeMillis());
    DateFormat df1 = DateFormat.getDateInstance(DateFormat.SHORT);
    String strToday = df1.format(now);
    Date today = null;
    try {
      today = df1.parse(strToday);
    } catch (ParseException e) {
      log4j.error(e);
    }
    return (date.getTime() - today.getTime()) / 86400000;
  }

  /**
   * 
   * @param al
   * @return
   */
  public static ArrayList<Long> sort(ArrayList<Long> al) {
    Collections.sort(al);
    return al;
  }

  /**
   * 
   * @param payment
   * @param strCurrencyTo
   * @param conversionDate
   * @param multiplier
   * @return
   */
  public static BigDecimal calculatePaidAmount(DebtPayment payment, String strCurrencyTo,
      Date conversionDate, BigDecimal multiplier) {
    BigDecimal paidAmount = BigDecimal.ZERO;
    if (payment.getSettlementCancelled() == null)
      return paidAmount;
    else if (payment.getSettlementCancelled().getProcessed().equals("Y")) {
      if (payment.isPaymentComplete())
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
            .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
            .getOrganization().getId());

      BigDecimal cancelledNotPaidAmount = BigDecimal.ZERO;
      BigDecimal cancelledNotPaidWriteOffAmount = BigDecimal.ZERO;
      List<DebtPayment> cancelledPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentSettlementCancelledList();
      ArrayList<DebtPayment> cancelledNotPaidPayments = new ArrayList<DebtPayment>();
      for (DebtPayment cancelledPayment : cancelledPayments) {
        if (!cancelledPayment.isPaymentComplete()) {
          cancelledNotPaidPayments.add(cancelledPayment);
          cancelledNotPaidAmount = cancelledNotPaidAmount.add(getConvertedAmt(cancelledPayment
              .getAmount(), cancelledPayment.getCurrency().getId(), strCurrencyTo, cancelledPayment
              .getSettlementCancelled().getAccountingDate(), cancelledPayment.getClient().getId(),
              cancelledPayment.getOrganization().getId()));
          cancelledNotPaidWriteOffAmount = cancelledNotPaidWriteOffAmount.add(getConvertedAmt(
              cancelledPayment.getWriteoffAmount(), cancelledPayment.getCurrency().getId(),
              strCurrencyTo, cancelledPayment.getSettlementCancelled().getAccountingDate(),
              cancelledPayment.getClient().getId(), cancelledPayment.getOrganization().getId()));
        }
      }
      List<DebtPayment> generatedPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentCSettlementGenerateIDList();
      // Add the write-off amount
      paidAmount = paidAmount.add(getConvertedAmt(payment.getWriteoffAmount(), payment
          .getCurrency().getId(), strCurrencyTo, conversionDate, payment.getClient().getId(),
          payment.getOrganization().getId()));
      if (generatedPayments == null || generatedPayments.size() == 0)
        return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
            .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
            .getOrganization().getId());
      for (DebtPayment generatedPayment : generatedPayments) {
        BigDecimal generatedPaymentPaidAmount = BigDecimal.ZERO;
        generatedPaymentPaidAmount = calculatePaidAmount(generatedPayment, strCurrencyTo,
            generatedPayment.getSettlementGenerate().getAccountingDate(), payment.getAmount()
                .divide(cancelledNotPaidAmount, 1000, BigDecimal.ROUND_HALF_UP));
        paidAmount = paidAmount.add(generatedPaymentPaidAmount);
      }
    }
    return paidAmount;
  }

  /**
   * 
   * @param payment
   * @param strCurrencyTo
   * @param conversionDate
   * @param multiplier
   * @return
   */
  public static BigDecimal calculateOverdueAmount(DebtPayment payment, String strCurrencyTo,
      Date conversionDate, BigDecimal multiplier) {
    BigDecimal overdueAmount = BigDecimal.ZERO;

    if (payment.getSettlementCancelled() == null
        && payment.getDueDate().compareTo(new Date(System.currentTimeMillis())) < 0)
      return getConvertedAmt(payment.getAmount().multiply(multiplier), payment.getCurrency()
          .getId(), strCurrencyTo, conversionDate, payment.getClient().getId(), payment
          .getOrganization().getId());
    else if (payment.getSettlementCancelled() != null
        && payment.getSettlementCancelled().getProcessed().equals("Y")) {
      if (payment.isPaymentComplete())
        return BigDecimal.ZERO;
      BigDecimal cancelledNotPaidAmount = BigDecimal.ZERO;
      BigDecimal cancelledNotPaidWriteOffAmount = BigDecimal.ZERO;
      List<DebtPayment> cancelledPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentSettlementCancelledList();
      ArrayList<DebtPayment> cancelledNotPaidPayments = new ArrayList<DebtPayment>();
      for (DebtPayment cancelledPayment : cancelledPayments) {
        if (!cancelledPayment.isPaymentComplete()) {
          cancelledNotPaidPayments.add(cancelledPayment);
          cancelledNotPaidAmount = cancelledNotPaidAmount.add(getConvertedAmt(cancelledPayment
              .getAmount(), cancelledPayment.getCurrency().getId(), strCurrencyTo, cancelledPayment
              .getSettlementCancelled().getAccountingDate(), cancelledPayment.getClient().getId(),
              cancelledPayment.getOrganization().getId()));
          cancelledNotPaidWriteOffAmount = cancelledNotPaidWriteOffAmount.add(getConvertedAmt(
              cancelledPayment.getWriteoffAmount(), cancelledPayment.getCurrency().getId(),
              strCurrencyTo, cancelledPayment.getSettlementCancelled().getAccountingDate(),
              cancelledPayment.getClient().getId(), cancelledPayment.getOrganization().getId()));
        }
      }
      List<DebtPayment> generatedPayments = payment.getSettlementCancelled()
          .getFinancialMgmtDebtPaymentCSettlementGenerateIDList();
      if (generatedPayments == null || generatedPayments.size() == 0)
        return BigDecimal.ZERO;
      for (DebtPayment generatedPayment : generatedPayments) {
        BigDecimal generatedPaymentOverdueAmount = BigDecimal.ZERO;
        if (!generatedPayment.isPaymentComplete())
          generatedPaymentOverdueAmount = calculateOverdueAmount(generatedPayment, strCurrencyTo,
              generatedPayment.getSettlementGenerate().getAccountingDate(), payment.getAmount()
                  .divide(cancelledNotPaidAmount, 1000, BigDecimal.ROUND_HALF_UP));
        overdueAmount = overdueAmount.add(generatedPaymentOverdueAmount);
      }
    }
    return overdueAmount;
  }

  /**
   * 
   * @param Amt
   * @param CurFrom_ID
   * @param CurTo_ID
   * @param ConvDate
   * @param client
   * @param org
   * @return
   */
  public static BigDecimal getConvertedAmt(BigDecimal Amt, String CurFrom_ID, String CurTo_ID,
      Date ConvDate, String client, String org) {
    if (CurFrom_ID == null || CurTo_ID == null || CurFrom_ID.equals(CurTo_ID))
      return Amt;
    DalConnectionProvider conn = new DalConnectionProvider();
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
        "dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    String strConvertedAmount = AcctServer.getConvertedAmt(Amt.toString(), CurFrom_ID, CurTo_ID,
        dateFormater.format(ConvDate).toString(), "S", client, org, conn);
    return new BigDecimal(strConvertedAmount);
  }
}
