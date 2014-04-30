package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Map;

import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.service.datasource.hql.HQLInjectionQualifier;
import org.openbravo.service.datasource.hql.HqlInjector;

@HQLInjectionQualifier.Qualifier(tableId = "59ED9B23854A4B048CBBAE38436B99C2", injectionId = "0")
public class AddPaymentCreditToUseInjector extends HqlInjector {

  @Override
  public String injectHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    final String strPaymentId = requestParameters.get("@FIN_Payment.id@");
    final String strInvoiceId = requestParameters.get("@Invoice.id@");
    Invoice invoice = null;
    BusinessPartner businessPartner = null;
    if (strPaymentId != null) {
      final FIN_Payment finPayment = OBDal.getInstance().get(FIN_Payment.class, strPaymentId);
      businessPartner = finPayment.getBusinessPartner();
    }
    if (strInvoiceId != null) {
      invoice = OBDal.getInstance().get(Invoice.class, strInvoiceId);
      businessPartner = invoice.getBusinessPartner();
    }
    queryNamedParameters.put("bp", businessPartner.getId());
    queryNamedParameters.put("issotrx", invoice.isSalesTransaction());
    return "f.businessPartner.id = :bp and f.receipt = :issotrx";
  }
}