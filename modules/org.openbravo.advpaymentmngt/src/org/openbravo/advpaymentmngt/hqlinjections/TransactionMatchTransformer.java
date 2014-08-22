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

package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Date;
import java.util.Map;

import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.db.DalConnectionProvider;

@ComponentProvider.Qualifier("D56CF1065EF14D52ADAD2AAB0CB63EFC")
public class TransactionMatchTransformer extends HqlQueryTransformer {
  final static String RDBMS = new DalConnectionProvider(false).getRDBMS();
  final static String TABLE_ID = "D56CF1065EF14D52ADAD2AAB0CB63EFC";

  @Override
  public String transformHqlQuery(String _hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    final StringBuffer whereClause = getWhereClause(requestParameters, queryNamedParameters);

    String transformedHql = _hqlQuery.replace("@whereclause@", whereClause.toString());
    transformedHql = transformedHql.replace("@selectClause@", " ");
    transformedHql = transformedHql.replace("@joinClause@", " ");
    return transformedHql;
  }

  private StringBuffer getWhereClause(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    final StringBuffer whereClause = new StringBuffer();
    // TODO remove NOT
    whereClause.append("e.reconciliation is null ");
    whereClause.append("and e.account.id = :account  ");
    whereClause.append("and e.transactionDate <= :dateTo  ");

    final String accountId = requestParameters.get("@FIN_Financial_Account.id@");
    // TODO transaction date
    final Date transactionDate = new Date();

    queryNamedParameters.put("account", accountId);
    queryNamedParameters.put("dateTo", transactionDate);

    return whereClause;
  }

}
