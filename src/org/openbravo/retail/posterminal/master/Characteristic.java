/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.POSUtils;

@MasterDataModel("Characteristic")
public class Characteristic extends MasterDataProcessHQLQuery {
  public static final String characteristicPropertyExtension = "OBPOS_CharacteristicExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(characteristicPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(final JSONObject jsonsent) {
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
    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    final List<String> orgIds = POSUtils.getOrgListCrossStore(posId, isCrossStoreSearch);
    final Set<String> productListIds = POSUtils.getProductListCrossStore(posId, isCrossStoreSearch);

    final Map<String, Object> paramValues = new HashMap<>();
    paramValues.put("orgIds", orgIds);
    paramValues.put("productListIds", productListIds);
    return paramValues;
  }

  @Override
  protected List<String> getQuery(final JSONObject jsonsent) throws JSONException {
    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    boolean isRemote = getPreference("OBPOS_remote.product") || isCrossStoreSearch;
    final HQLPropertyList characteristicHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final List<String> hqlQueries = new ArrayList<>();
    hqlQueries
        .add(getCharacteristicHqlString(characteristicHQLProperties, isRemote, isCrossStoreSearch));
    return hqlQueries;
  }

  private String getCharacteristicHqlString(final HQLPropertyList characteristicHQLProperties,
      final boolean isRemote, final boolean isCrossStoreSearch) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(characteristicHQLProperties.getHqlSelect());
    query.append(" from Characteristic ch");
    query.append(" where $filtersCriteria");
    query.append(" and $hqlCriteria");
    query.append(" and ch.$readableSimpleClientCriteria");
    query.append(" and ch.$incrementalUpdateCriteria");
    query.append(" and ch.obposFilteronwebpos = true");
    if (isCrossStoreSearch) {
      query.append(" and exists (");
      query.append("   select 1");
      query.append("   from Organization o");
      query.append("   where o.id in :orgIds");
      query.append("   and ad_org_isinnaturaltree(ch.organization.id, o.id, ch.client.id) = 'Y'");
      query.append(" )");
    } else {
      query.append(" and ch.$naturalOrgCriteria ");
    }
    if (!isRemote) {
      query.append(" and exists (");
      query.append("   select 1");
      query.append("   from ProductCharacteristicValue pcv");
      query.append("   , OBRETCO_Prol_Product assort");
      query.append("   where pcv.product.id = assort.product.id");
      query.append("   and pcv.characteristic.id = ch.id");
      query.append("   and assort.obretcoProductlist.id = :productListIds");
      query.append(" )");
    }
    if (isRemote) {
      query.append(" order by ch.name, ch.id");
    } else {
      query.append(" order by ch.id");
    }
    return query.toString();
  }

  private boolean getPreference(final String preference) {
    OBContext.setAdminMode(false);
    boolean value;
    try {
      value = StringUtils.equals(Preferences.getPreferenceValue(preference, true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null), "Y");
    } catch (PropertyException e) {
      value = false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return value;
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

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);
  }

}
