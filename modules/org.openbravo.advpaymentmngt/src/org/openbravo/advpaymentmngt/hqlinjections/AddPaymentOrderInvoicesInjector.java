package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Map;

import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.service.datasource.hql.HQLInjectionQualifier;
import org.openbravo.service.datasource.hql.HqlInjector;

@HQLInjectionQualifier.Qualifier(tableId = "58AF4D3E594B421A9A7307480736F03E", injectionId = "0")
public class AddPaymentOrderInvoicesInjector extends HqlInjector {

  @Override
  public String injectHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    final String strPaymentId = requestParameters.get("@FIN_Payment.id@");
    final String strInvoiceId = requestParameters.get("@Invoice.id@");
    if (strPaymentId != null) {
      final FIN_Payment finPayment = OBDal.getInstance().get(FIN_Payment.class, strPaymentId);
    }
    if (strInvoiceId != null) {
      final Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceId);
    }
    queryNamedParameters.put("inv", strInvoiceId);
    return " psd.invoicePaymentSchedule.invoice.id = :inv ";
  }
}