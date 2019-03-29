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
import java.util.HashSet;
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
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.config.OBRETCOProductList;
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
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Product Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    Map<String, Object> args = new HashMap<String, Object>();
    HQLPropertyList characteristicsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    propertiesList.add(characteristicsHQLProperties);

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {

    final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final boolean crossStoreSearch = POSUtils.isCrossStoreSearch(jsonsent);
    final String posId = jsonsent.getString("pos");

    final OBRETCOProductList productList = POSUtils
        .getProductListByPosterminalId(jsonsent.getString("pos"));
    Set<String> productListIds = new HashSet<>();
    productListIds.add(productList.getId());

    final Map<String, Object> paramValues = new HashMap<>();
    if (crossStoreSearch) {
      Set<String> crossStoreNaturalTree = OBContext.getOBContext()
          .getOrganizationStructureProvider(OBContext.getOBContext().getCurrentClient().getId())
          .getNaturalTree(orgId);
      crossStoreNaturalTree.addAll(POSUtils.getOrgListCrossStore(posId));

      productListIds = POSUtils.getProductListCrossStore(posId);

      paramValues.put("crossStoreNaturalTree", crossStoreNaturalTree);
    }

    paramValues.put("productListIds", productListIds);
    return paramValues;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularProductsChValueHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

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

    StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsChValueHQLProperties.getHqlSelect());
    query.append(" from Characteristic ch ");
    query.append(" where $filtersCriteria");
    query.append(" and $hqlCriteria");
    query.append(" and ch.obposFilteronwebpos = true");
    if (!isRemote && !POSUtils.isCrossStoreSearch(jsonsent)) {
      query.append(" and exists (");
      query.append("   select 1");
      query.append("   from  ProductCharacteristicValue pcv,");
      query.append("   OBRETCO_Prol_Product assort");
      query.append("   where pcv.characteristic.id = ch.id");
      query.append("   and pcv.product.id = assort.product.id ");
      query.append("   and assort.obretcoProductlist.id in :productListIds");
      query.append("  )");
    }
    if (POSUtils.isCrossStoreSearch(jsonsent)) {
      query.append(" and ch.organization.id in :crossStoreNaturalTree");
    } else {
      query.append(" and ch.$naturalOrgCriteria");
    }
    query.append(" and ch.$readableSimpleClientCriteria");
    query.append(" and (ch.$incrementalUpdateCriteria)");
    query.append(" order by ch.name, ch.id");

    hqlQueries.add(query.toString());
    return hqlQueries;
  }
}
