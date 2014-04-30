package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Map;

import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;

@ComponentProvider.Qualifier("58AF4D3E594B421A9A7307480736F03E")
public class AddPaymentOrderInvoicesTransformer extends HqlQueryTransformer {

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    // String transformedHql = hqlQuery.replace("from ", ", e.symbol as em_hqlext_symbol from ");
    // return transformedHql;
    return hqlQuery;
  }
}