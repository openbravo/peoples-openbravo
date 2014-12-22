package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Map;

import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;

@ComponentProvider.Qualifier("59ED9B23854A4B048CBBAE38436B99C2")
public class CreditToUseTransformer extends HqlQueryTransformer {

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    
    String transformedHQL = hqlQuery.replace("@selectClause@", " ");
    transformedHQL = transformedHQL.replace("@joinClause@", " ");
    transformedHQL = transformedHQL.replace("@whereClause@", 
        getWhereClause(requestParameters, queryNamedParameters));
    
    return transformedHQL;
  }

  private CharSequence getWhereClause(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    return " ";
  }

}
