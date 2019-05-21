/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Characteristic extends ProcessHQLQuery {
  public static final String characteristicPropertyExtension = "OBPOS_CharacteristicExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(characteristicPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(final JSONObject jsonsent) {
    // Get Product Properties
    final Map<String, Object> args = new HashMap<>();
    final HQLPropertyList characteristicsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);

    final List<HQLPropertyList> propertiesList = new ArrayList<>();
    propertiesList.add(characteristicsHQLProperties);
    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    final String posId = jsonsent.getString("pos");
    final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
    final String productListId = POSUtils.getProductListByPosterminalId(posId).getId();

    final Map<String, Object> paramValues = new HashMap<>();
    paramValues.put("crossStoreOrgIds", crossStoreOrgIds);
    paramValues.put("productListId", productListId);
    return paramValues;
  }

  @Override
  protected List<String> getQuery(final JSONObject jsonsent) throws JSONException {
    final List<String> hqlQueries = new ArrayList<>();
    final HQLPropertyList characteristicHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    hqlQueries
        .add(getCharacteristicHqlString(characteristicHQLProperties, isRemote, isCrossStoreSearch));
    return hqlQueries;
  }

  private String getCharacteristicHqlString(final HQLPropertyList characteristicHQLProperties,
      final boolean isRemote, final boolean isCrossStoreSearch) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(characteristicHQLProperties.getHqlSelect());
    query.append(" from Characteristic ch ");
    query.append(" where $filtersCriteria");
    query.append(" and $hqlCriteria");
    query.append(" and ch.$readableSimpleClientCriteria");
    query.append(" and ch.$incrementalUpdateCriteria");
    query.append(" and ch.obposFilteronwebpos = true");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
    query.append("   and ad_org_isinnaturaltree(ch.organization.id, o.id, o.client.id) = 'Y'");
    query.append(" )");
    if (!isRemote && !isCrossStoreSearch) {
      query.append(" and exists (");
      query.append("   select 1");
      query.append("   from ProductCharacteristicValue pcv");
      query.append("   , OBRETCO_Prol_Product assort");
      query.append("   where pcv.product.id = assort.product.id");
      query.append("   and pcv.characteristic.id = ch.id");
      query.append("   and assort.obretcoProductlist.id = :productListId");
      query.append(" )");
    }
    query.append(" order by ch.name, ch.id");
    return query.toString();
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
