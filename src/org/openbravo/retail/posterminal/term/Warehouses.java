/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
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
import org.openbravo.dal.core.OBContext;

public class Warehouses extends QueryTerminalProperty {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("orgId", OBContext.getOBContext().getCurrentOrganization().getId());

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Arrays
        .asList(new String[] { "select ow.warehouse.id as warehouseid, ow.warehouse.name as warehousename, ow.priority as priority "
            + "from OrganizationWarehouse as ow where ow.warehouse.active = true and ow.$readableSimpleCriteria and ow.$activeCriteria and ow.organization.id = :orgId order by priority asc" });
  }

  @Override
  public String getProperty() {
    return "warehouses";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}
