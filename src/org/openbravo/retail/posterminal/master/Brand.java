/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
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
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Brand extends ProcessHQLQuery {
  public static final String brandPropertyExtension = "OBPOS_BrandExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(brandPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(final JSONObject jsonsent) {
    final HQLPropertyList brandHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final List<HQLPropertyList> propertiesList = new ArrayList<>();
    propertiesList.add(brandHQLProperties);
    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    final String posId = jsonsent.getString("pos");
    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    final String productListId = POSUtils.getProductListByPosterminalId(posId).getId();
    final List<String> orgIds = POSUtils.getOrgListCrossStore(posId, isCrossStoreSearch);
    final Set<String> productListIds = POSUtils.getProductListCrossStore(posId, isCrossStoreSearch);

    final Map<String, Object> paramValues = new HashMap<>();
    paramValues.put("orgIds", orgIds);
    paramValues.put("productListId", productListId);
    paramValues.put("productListIds", productListIds);
    return paramValues;
  }

  @Override
  protected List<String> getQuery(final JSONObject jsonsent) throws JSONException {
    final HQLPropertyList brandHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final List<String> hqlQueries = new ArrayList<>();
    hqlQueries.add(getBrandHqlString(brandHQLProperties));
    return hqlQueries;
  }

  private String getBrandHqlString(final HQLPropertyList brandHQLProperties) {
    return " select" //
        + brandHQLProperties.getHqlSelect() //
        + " from Brand brand" //
        + " where $filtersCriteria" //
        + " and $hqlCriteria" //
        + " and $incrementalUpdateCriteria" //
        + " and brand.active = true" //
        + " and exists (" //
        + "   select 1" //
        + "   from Product p" //
        + "   join p.oBRETCOProlProductList assort" //
        + "   where p.brand.id = brand.id" //
        + "   and assort.obretcoProductlist.id in :productListIds" //
        + " )" //
        + " and exists (" //
        + "   select 1" //
        + "   from Organization o" //
        + "   where o.id in :orgIds" //
        + "   and ad_org_isinnaturaltree(brand.organization.id, o.id, brand.client.id) = 'Y'" //
        + " )" //
        + " order by brand.name, brand.id";
  }

  private static boolean isCrossStoreSearch(final JSONObject jsonsent) {
    boolean crossStoreSearch = false;
    try {
      crossStoreSearch = jsonsent.has("remoteParams")
          && jsonsent.getJSONObject("remoteParams").optBoolean("crossStoreSearch");
    } catch (JSONException e) {
      log.error("Error while getting crossStoreSearch " + e.getMessage(), e);
    }
    return crossStoreSearch;
  }

}
