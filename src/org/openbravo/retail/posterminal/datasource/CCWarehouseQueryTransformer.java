/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.datasource;

import java.util.Map;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;

@ComponentProvider.Qualifier("BD2096E0DEEB431C8E704143DA4DA6CF")
public class CCWarehouseQueryTransformer extends HqlQueryTransformer {

  /**
   * Returns the transformed hql query
   * 
   * @param hqlQuery
   *          original hql query
   * @param requestParameters
   *          the parameters of the request
   * @param queryNamedParameters
   *          the named parameters of the hql query that will be used to fetch the table data. If
   *          the transformed hql query uses named parameters that did not exist in the original hql
   *          query, the named parameters must be added to this map
   * @return the transformed hql query
   */
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    String[] hqlStringArray = hqlQuery.split("ORDER BY");
    if (hqlStringArray.length > 1) {
      hqlQuery = hqlStringArray[0].concat(" and e.organization.id = '"
          + vars.getStringParameter("@Organization.id@") + "'")
          + " ORDER BY " + hqlStringArray[1];
    } else {
      hqlQuery = hqlQuery.concat(" and e.organization.id = '"
          + vars.getStringParameter("@Organization.id@") + "'");
    }
    return hqlQuery.replace("@Organization.id@", "'" + vars.getStringParameter("@Organization.id@")
        + "'");
  }
}
