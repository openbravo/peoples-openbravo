/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

public class Warehouses extends QueryTerminalProperty {
  public static final String warehousesPropertyExtension = "OBPOS_WarehousesExtension";

  @Inject
  @Any
  @Qualifier(warehousesPropertyExtension)
  private Instance<ModelExtension> extensions;

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
    HQLPropertyList warehousesHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);
    return Arrays
        .asList(new String[] { "select "
            + warehousesHQLProperties.getHqlSelect()
            + " from OrganizationWarehouse as ow where ow.warehouse.active = true and ow.$readableSimpleCriteria and ow.$activeCriteria and ow.organization.id = :orgId order by priority asc" });
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
