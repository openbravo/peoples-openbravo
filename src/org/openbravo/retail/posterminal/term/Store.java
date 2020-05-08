/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

public class Store extends QueryTerminalProperty {

  @Override
  protected List<String> getQuery(final JSONObject jsonsent) throws JSONException {
    final StringBuilder hql = new StringBuilder();
    hql.append(" select org.id as id,");
    hql.append("   org.name as name,");
    hql.append("   orgInfo.locationAddress.country.id as country,");
    hql.append("   orgInfo.locationAddress.region.id as region");
    hql.append(" from Organization org");
    hql.append("   left join org.organizationInformationList orgInfo");
    hql.append(" where org.$readableSimpleClientCriteria");
    hql.append(" and org.$activeCriteria");
    hql.append(" and org.oBRETCORetailOrgType = 'S'");
    hql.append(" and org.oBRETCOCrossStoreOrganization is not null");
    hql.append(" and org.oBRETCOCrossStoreOrganization.id = :crossStoreId");
    hql.append(" and org.id <> :orgId");
    hql.append(" order by org.name");

    return Arrays.asList(hql.toString());
  }

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    final OBPOSApplications pOSTerminal = POSUtils.getTerminalById(jsonsent.getString("pos"));
    final Organization org = pOSTerminal.getOrganization();
    final String crossStoreId = org.getOBRETCOCrossStoreOrganization() != null
        ? org.getOBRETCOCrossStoreOrganization().getId()
        : "";

    Map<String, Object> paramValues = new HashMap<>();
    paramValues.put("crossStoreId", crossStoreId);
    paramValues.put("orgId", org.getId());

    return paramValues;
  }

  @Override
  public String getProperty() {
    return "store";
  }

  @Override
  protected boolean isAdminMode() {
    return false;
  }

  @Override
  public boolean returnList() {
    return false;
  }

}
