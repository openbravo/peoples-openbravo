/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class SalesRepresentative extends ProcessHQLQuery {
  public static final String salesRepresentativePropertyExtension = "OBPOS_SalesRepresentativeExtension";

  @Inject
  @Any
  @Qualifier(salesRepresentativePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Sales Representative Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    Map<String, Object> args = new HashMap<String, Object>();
    HQLPropertyList characteristicsHQLProperties = ModelExtensionUtils.getPropertyExtensions(
        extensions, args);
    propertiesList.add(characteristicsHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    List<String> hqlQueries = new ArrayList<String>();
    HQLPropertyList regularSalesRepresentativeHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    String operator = lastUpdated == null ? " AND " : " OR ";

    hqlQueries
        .add("select"
            + regularSalesRepresentativeHQLProperties.getHqlSelect() //
            + "from ADUser user "
            + "where $filtersCriteria AND"
            + " exists (select 1 from BusinessPartner bp where user.businessPartner = bp AND bp.isSalesRepresentative = true AND (bp.$naturalOrgCriteria)) "
            + "AND ((user.$incrementalUpdateCriteria) "
            + operator
            + " (user.businessPartner.$incrementalUpdateCriteria)) AND (user.$naturalOrgCriteria) AND (user.$readableSimpleClientCriteria) order by user.name asc, user.id");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}