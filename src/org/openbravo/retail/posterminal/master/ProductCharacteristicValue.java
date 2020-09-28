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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;

@MasterDataModel("ProductCharacteristicValue")
public class ProductCharacteristicValue extends MasterDataProcessHQLQuery {
  public static final String productCharacteristicValuePropertyExtension = "OBPOS_ProductCharacteristicValueExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(productCharacteristicValuePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(final JSONObject jsonsent) {
    final Map<String, Object> args = new HashMap<>();
    final HQLPropertyList productcharacteristicsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);

    final List<HQLPropertyList> propertiesList = new ArrayList<>();
    propertiesList.add(productcharacteristicsHQLProperties);
    return propertiesList;
  }

  private List<String> getUsedInWebPOSCharacteristics() {
    String hqlQuery = "select id from Characteristic c where c.obposUseonwebpos = true and isactive = 'Y'";
    final Session session = OBDal.getInstance().getSession();
    final Query<String> qry = session.createQuery(hqlQuery, String.class);
    return qry.list();
  }

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final String posId = jsonsent.getString("pos");
      final Date terminalDate = getTerminalDate(jsonsent);
      final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final OBRETCOProductList productList = POSUtils.getProductListByPosterminalId(posId);
      final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
          terminalDate);

      final Map<String, Object> paramValues = new HashMap<>();
      if (isCrossStoreSearch) {
        paramValues.put("productId", jsonsent.getJSONObject("remoteParams").getString("productId"));
      }
      // Optional filtering by a list of m_product_id
      if (jsonsent.getJSONObject("parameters").has("filterProductList")
          && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("undefined")
          && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("null")) {
        final JSONArray filterProductList = jsonsent.getJSONObject("parameters")
            .getJSONArray("filterProductList");
        paramValues.put("filterProductList", filterProductList);
      }
      paramValues.put("productListId", productList.getId());
      paramValues.put("priceListVersionId", priceListVersion.getId());
      List<String> characteristicsIds = getUsedInWebPOSCharacteristics();
      paramValues.put("characteristicIds", characteristicsIds.size() > 0 ? characteristicsIds : "");
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    final Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    // Optional filtering by a list of m_product_id
    final boolean filterProductList = jsonsent.getJSONObject("parameters").has("filterProductList")
        && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("undefined")
        && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("null");
    final HQLPropertyList regularProductsCharacteristicHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final List<String> hqlQueries = new ArrayList<>();
    hqlQueries
        .add(getProductCharacteristicValueHqlString(regularProductsCharacteristicHQLProperties,
            filterProductList, lastUpdated, isCrossStoreSearch));
    return hqlQueries;
  }

  private String getProductCharacteristicValueHqlString(
      final HQLPropertyList regularProductsCharacteristicHQLProperties,
      final boolean filterProductList, final Long lastUpdated, final boolean isCrossStoreSearch) {
    final boolean filterWithProductEntities = getPreference(
        "OBPOS_FilterProductByEntitiesOnRefresh");
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsCharacteristicHQLProperties.getHqlSelect());
    query.append(" from ProductCharacteristicValue pcv");
    query.append(" join pcv.characteristic characteristic");
    query.append(" join pcv.characteristicValue characteristicValue");
    query.append(" join pcv.product product");
    query.append(" join product.oBRETCOProlProductList opp");
    query.append(" join product.pricingProductPriceList ppp");
    query.append(" where pcv.$readableSimpleClientCriteria");
    query.append(" and characteristic.id in (:characteristicIds) ");
    if (isCrossStoreSearch) {
      query.append(" and pcv.product.id = :productId");
    } else {
      query.append(" and pcv.$filtersCriteria");
      query.append(" and pcv.$hqlCriteria");
      query.append(" and pcv.$naturalOrgCriteria");
      query.append(" and opp.obretcoProductlist.id = :productListId");
      query.append(" and ppp.priceListVersion.id = :priceListVersionId");
      if (filterProductList) {
        query.append(" and pcv.product.id in :filterProductList");
      }
      if (lastUpdated == null) {
        query.append(" and (opp.$incrementalUpdateCriteria");
        query.append(" and ppp.$incrementalUpdateCriteria");
        query.append(" and pcv.$incrementalUpdateCriteria");
        query.append(" and characteristic.$incrementalUpdateCriteria");
        query.append(" and characteristicValue.$incrementalUpdateCriteria)");
      } else if (filterWithProductEntities) {
        query.append(" and (opp.$incrementalUpdateCriteria");
        query.append(" or product.$incrementalUpdateCriteria)");
      } else {
        query.append(" and product.$incrementalUpdateCriteria");
      }
    }
    query.append(" and pcv.$paginationByIdCriteria ");
    query.append(" order by pcv.id");
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

  private Date getTerminalDate(final JSONObject jsonsent) throws JSONException {
    return OBMOBCUtils.calculateServerDate(
        jsonsent.getJSONObject("parameters").getString("terminalTime"),
        jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));
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
