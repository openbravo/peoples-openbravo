package org.openbravo.retail.posterminal;

import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;

public abstract class OrderLoaderPaymentHook implements OrderLoaderHook {

  protected FIN_PaymentSchedule paymentSchedule;
  protected FIN_PaymentSchedule paymentScheduleInvoice;

  public FIN_PaymentSchedule getPaymentSchedule() {
    return paymentSchedule;
  }

  public void setPaymentSchedule(FIN_PaymentSchedule paymentSchedule) {
    this.paymentSchedule = paymentSchedule;
  }

  public FIN_PaymentSchedule getPaymentScheduleInvoice() {
    return paymentScheduleInvoice;
  }

  public void setPaymentScheduleInvoice(FIN_PaymentSchedule paymentScheduleInvoice) {
    this.paymentScheduleInvoice = paymentScheduleInvoice;
  }

}
