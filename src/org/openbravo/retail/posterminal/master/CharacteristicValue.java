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

/*
 * This class fills the m_ch_value table in WebSQL even if it is called productChValue.
 */
public class CharacteristicValue extends ProcessHQLQuery {
  public static final String characteristicValuePropertyExtension = "OBPOS_CharacteristicValueExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(characteristicValuePropertyExtension)
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
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    final List<String> hqlQueries = new ArrayList<>();
    final HQLPropertyList characteristicValueHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    final Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
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

    hqlQueries.add(getCharacteristicValueHqlString(characteristicValueHQLProperties, isRemote,
        isCrossStoreSearch, lastUpdated));
    return hqlQueries;
  }

  private String getCharacteristicValueHqlString(
      final HQLPropertyList characteristicValueHQLProperties, final boolean isRemote,
      final boolean isCrossStoreSearch, final Long lastUpdated) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(characteristicValueHQLProperties.getHqlSelect());
    query.append(" from CharacteristicValue cv");
    query.append(" join cv.characteristic ch");
    if (isRemote || isCrossStoreSearch) {
      query.append(" where cv.$incrementalUpdateCriteria");
    } else {
      query.append(" , ADTreeNode node");
      query.append(" where ch.tree = node.tree");
      query.append(" and cv.id = node.node");
      query.append(" and ((cv.summaryLevel = false");
      query.append(" and exists (");
      query.append("   select 1");
      query.append("   from  ProductCharacteristicValue pcv");
      query.append("   , OBRETCO_Prol_Product assort");
      query.append("   where pcv.product.id = assort.product.id");
      query.append("   and pcv.characteristicValue.id = cv.id");
      query.append("   and assort.obretcoProductlist.id = :productListId");
      query.append(" ))");
      query.append(" or cv.summaryLevel = true)");
      query.append(" and (cv.$incrementalUpdateCriteria");
      query
          .append(" " + (lastUpdated == null ? "and" : "or") + " node.$incrementalUpdateCriteria)");
    }
    query.append(" and $filtersCriteria");
    query.append(" and $hqlCriteria");
    query.append(" and cv.$readableSimpleClientCriteria");
    query.append(" and ch.obposUseonwebpos = true");
    query.append(" and cv.active = true");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
    query.append("   and ad_org_isinnaturaltree(cv.organization.id, o.id, o.client.id) = 'Y'");
    query.append(" )");
    query.append(" order by cv.name, cv.id");
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
