/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BPartnerFilter extends ProcessHQLQuery {

  public static final String bPartnerFilterPropertyExtension = "OBPOS_BPartnerFilterExtension";

  @Inject
  @Any
  @Qualifier(bPartnerFilterPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Product Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList bpHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions,
        getParams(jsonsent));
    propertiesList.add(bpHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<String>();

    Map<String, Object> params = getParams(jsonsent);
    final Boolean location = (Boolean) params.get("location");
    HQLPropertyList bpHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions, params);

    String hql = "SELECT " + bpHQLProperties.getHqlSelect();
    if (location) {
      Long lastUpdated = jsonsent.has("lastUpdated")
          && !jsonsent.get("lastUpdated").equals("undefined")
          && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
      String operator = lastUpdated == null ? " AND " : " OR ";
      hql = hql
          + "FROM BusinessPartnerLocation bpl left outer join bpl.businessPartner.aDUserList AS ulist "
          + "WHERE $filtersCriteria AND " + "bpl.businessPartner.customer = true AND "
          + "bpl.businessPartner.priceList IS NOT NULL AND "
          + "bpl.$readableSimpleClientCriteria AND " + "bpl.$naturalOrgCriteria AND "
          + "(bpl.$incrementalUpdateCriteria" + operator
          + "bpl.businessPartner.$incrementalUpdateCriteria) ";
    } else {
      hql = hql + "FROM BusinessPartner bp left outer join bp.aDUserList AS ulist "
          + "WHERE $filtersCriteria AND bp.customer = true AND "
          + "bp.priceList IS NOT NULL AND bp.$readableSimpleClientCriteria AND "
          + "bp.$naturalOrgCriteria AND bp.$incrementalUpdateCriteria";
    }

    hqlQueries.add(hql);
    return hqlQueries;
  }

  private Map<String, Object> getParams(JSONObject jsonsent) {
    Boolean location = false;
    Map<String, Object> result = new HashMap<String, Object>();
    try {
      JSONArray remoteFilters = jsonsent.getJSONArray("remoteFilters");
      for (int i = 0; i < remoteFilters.length(); i++) {
        JSONObject filter = remoteFilters.getJSONObject(i);
        if (filter.has("location") && filter.getBoolean("location")) {
          location = true;
          break;
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    result.put("location", location);
    return result;
  }
}
