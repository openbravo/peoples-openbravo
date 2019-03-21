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
    List<HQLPropertyList> propertiesList = new ArrayList<>();
    Map<String, Object> args = new HashMap<>();
    HQLPropertyList characteristicsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    propertiesList.add(characteristicsHQLProperties);

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final String posId = jsonsent.getString("pos");
      final boolean crossStoreSearch = jsonsent.has("remoteParams")
          && jsonsent.getJSONObject("remoteParams").optBoolean("crossStoreSearch");
      final Set<String> productListIds = POSUtils.getProductListCrossStore(posId, crossStoreSearch);

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("productListIds", productListIds);

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<>();

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

    String assortmentFilter = "";
    if (!isRemote) {
      assortmentFilter = "exists (select 1 from  ProductCharacteristicValue pcv, OBRETCO_Prol_Product assort "
          + " where pcv.characteristic.id=ch.id " + " and pcv.product.id= assort.product.id "
          + " and assort.obretcoProductlist.id in :productListIds) and";
    }
    hqlQueries.add("select" + regularProductsChValueHQLProperties.getHqlSelect()
        + "from Characteristic ch " + "where  $filtersCriteria AND $hqlCriteria and "
        + "ch.obposFilteronwebpos=true AND " + assortmentFilter
        + " ch.$naturalOrgCriteria and ch.$readableSimpleClientCriteria and (ch.$incrementalUpdateCriteria) "
        + " order by ch.name, ch.id");

    return hqlQueries;
  }
}
