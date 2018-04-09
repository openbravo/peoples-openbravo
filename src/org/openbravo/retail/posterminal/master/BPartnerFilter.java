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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQueryValidated;

public class BPartnerFilter extends ProcessHQLQueryValidated {

  public static final String bPartnerFilterPropertyExtension = "OBPOS_BPartnerFilterExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(bPartnerFilterPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Product Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList bpHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions,
        jsonsent);
    propertiesList.add(bpHQLProperties);

    return propertiesList;
  }

  @Override
  protected String getFilterEntity() {
    return "BPartnerFilter";
  }

  @Override
  protected List<String> getQueryValidated(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<String>();

    Map<String, Object> params = getParams(jsonsent);
    Boolean location = (Boolean) params.get("location");
    HQLPropertyList bpHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions,
        jsonsent);

    String hql = "SELECT " + bpHQLProperties.getHqlSelect();
    if (location) {
      hql = hql
          + "FROM BusinessPartnerLocation bpl left outer join bpl.businessPartner AS bp join bp.aDUserList AS ulist "
          + getWhereClause(location, jsonsent);
    } else {
      hql = hql + "FROM BusinessPartner bp left outer join bp.aDUserList AS ulist "
          + getWhereClause(location, jsonsent);
    }
    hql = hql + "$orderByCriteria";

    hqlQueries.add(hql);
    return hqlQueries;
  }

  protected String getWhereClause(boolean isLocation, JSONObject jsonsent) {
    if (isLocation) {
      return "WHERE $filtersCriteria AND bp.customer = true AND "
          + "bp.priceList IS NOT NULL AND bpl.$readableSimpleClientCriteria AND "
          + "bpl.$naturalOrgCriteria AND bp.active = true AND bpl.active = true ";
    } else {
      return "WHERE $filtersCriteria AND bp.customer = true AND "
          + "bp.priceList IS NOT NULL AND bp.$readableSimpleClientCriteria AND "
          + "bp.$naturalOrgCriteria AND bp.active = true ";
    }
  }

  public static Map<String, Object> getParams(JSONObject jsonsent) {
    Boolean location = false;
    String pref = "N";
    try {
      pref = Preferences.getPreferenceValue("OBPOS_FilterAlwaysBPByAddress", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null);
    } catch (PropertyException e1) {
      log.error("Error getting preference OBPOS_FilterAlwaysBPByAddress " + e1.getMessage(), e1);
    }
    if ("N".equals(pref)) {
      try {
        if (jsonsent.has("orderByClause")) {
          String orderByClause = jsonsent.getString("orderByClause");
          if (orderByClause.contains("bpl.")) {
            location = true;
          }
        }
        if (!location) {
          JSONArray remoteFilters = jsonsent.getJSONArray("remoteFilters");
          for (int i = 0; i < remoteFilters.length(); i++) {
            JSONObject filter = remoteFilters.getJSONObject(i);
            if (filter.has("location") && filter.getBoolean("location")) {
              location = true;
              break;
            }
          }
        }
      } catch (JSONException e) {
        log.error("Error getting parameteres: " + e.getMessage(), e);
      }
    } else {
      location = true;
    }
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("location", location);
    return result;
  }
}
