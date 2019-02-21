/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
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
    hql.append(" select id as id,");
    hql.append(" name as name");
    hql.append(" from Organization organization");
    hql.append(" where $readableSimpleClientCriteria");
    hql.append(" and $activeCriteria");
    hql.append(" and oBRETCORetailOrgType = 'S'");
    hql.append(" and oBPOSCrossStoreOrganization is not null");
    hql.append(" and oBPOSCrossStoreOrganization.id = :corssStoreId");
    hql.append(" and id <> :orgId");
    hql.append(" order by name");

    return Arrays.asList(hql.toString());
  }

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    final OBPOSApplications pOSTerminal = POSUtils.getTerminalById(jsonsent.getString("pos"));
    final Organization org = pOSTerminal.getOrganization();
    final String crossStoreId = org.getOBPOSCrossStoreOrganization() != null
        ? org.getOBPOSCrossStoreOrganization().getId()
        : "";

    Map<String, Object> paramValues = new HashMap<>();
    paramValues.put("corssStoreId", crossStoreId);
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
