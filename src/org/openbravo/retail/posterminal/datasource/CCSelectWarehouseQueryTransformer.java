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

@ComponentProvider.Qualifier("28BE992AF4EF44728E1C7E72377B3785")
public class CCSelectWarehouseQueryTransformer extends HqlQueryTransformer {

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
    String organizationId = vars.getStringParameter("@Organization.id@");
    String transformedHql = hqlQuery
        .replaceAll(
            "where e.client",
            "where not exists (select 1 from OBPOS_OrgWarehouseExtra as oww where oww.organization.id = 'replace_org_id' and oww.warehouse.id = e.id and oww.warehouseType <> 'cross_channel')"
                + " and not exists (select 1 from OrganizationWarehouse as oww where oww.organization.id = 'replace_org_id' and oww.warehouse.id = e.id)"
                + " and e.client");
    transformedHql = transformedHql.replaceAll("replace_org_id", organizationId);
    transformedHql = transformedHql.replaceAll("join_0.name", "e.organization.name");
    transformedHql = transformedHql.replaceAll("join_0", "e.locationAddress");
    transformedHql = transformedHql.replaceAll("join_1", "e.locationAddress.region");
    transformedHql = transformedHql.replaceAll("join_2", "e.locationAddress.country");
    transformedHql = transformedHql.replaceAll("AND e.organization in \\(.*?\\)", "");
    return transformedHql;
  }
}
