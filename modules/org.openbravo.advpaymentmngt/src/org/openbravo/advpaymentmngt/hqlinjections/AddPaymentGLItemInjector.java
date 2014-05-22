package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Map;

import org.openbravo.service.datasource.hql.HQLInjectionQualifier;
import org.openbravo.service.datasource.hql.HqlInjector;

@HQLInjectionQualifier.Qualifier(tableId = "864A35C8FCD548B0AD1D69C89BBA6118", injectionId = "0")
public class AddPaymentGLItemInjector extends HqlInjector {

  @Override
  public String injectHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    final String strPaymentId = requestParameters.get("fin_payment_id");
    queryNamedParameters.put("pid", strPaymentId);
    return "p.id = :pid";
  }
}