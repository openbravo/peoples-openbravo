/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;

@MasterDataModel("Product")
public class Product extends MasterDataProcessHQLQuery {
  public static final String productPropertyExtension = "OBPOS_ProductExtension";
  public static final String productDiscPropertyExtension = "OBPOS_ProductDiscExtension";
  private static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(productPropertyExtension)
  private Instance<ModelExtension> extensions;
  @Inject
  @Any
  @Qualifier(productDiscPropertyExtension)
  private Instance<ModelExtension> extensionsDisc;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Product Properties
    List<HQLPropertyList> propertiesList = new ArrayList<>();

    final boolean remoteSearch = isRemoteSearch(jsonsent);
    final String posPrecision = getPosPrecision();
    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    final boolean isMultiPriceListSearch = isMultiPriceListSearch(jsonsent);
    final boolean isRemote = remoteSearch || isCrossStoreSearch;
    final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");

    Map<String, Object> args = new HashMap<>();
    args.put("posPrecision", posPrecision);
    args.put("multiPriceList", isRemote && isMultipricelist && isMultiPriceListSearch);
    args.put("terminalId", getTerminalId(jsonsent));
    args.put("isCrossStoreSearch", isCrossStoreSearch);
    args.put("crossStore", false);
    args.put("isRemoteSearch", remoteSearch);
    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    HQLPropertyList regularProductsDiscHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsDisc, args);

    if (isCrossStoreSearch) {
      args.put("crossStore", true);
      HQLPropertyList crossStoreRegularProductsHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions, args);
      propertiesList.add(regularProductsHQLProperties);
      propertiesList.add(crossStoreRegularProductsHQLProperties);
      propertiesList.add(regularProductsDiscHQLProperties);
      propertiesList.add(regularProductsHQLProperties);
    } else {
      propertiesList.add(regularProductsHQLProperties);
      propertiesList.add(regularProductsDiscHQLProperties);
      propertiesList.add(regularProductsHQLProperties);
    }

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      final Date terminalDate = getTerminalDate(jsonsent);
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
      final boolean isMultiPriceListSearch = isMultiPriceListSearch(jsonsent);
      final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");
      final boolean isRemote = isRemoteSearch(jsonsent) || isCrossStoreSearch;

      final Calendar now = Calendar.getInstance();
      final String posId = getTerminalId(jsonsent);
      final List<String> orgIds = POSUtils.getOrgListCrossStore(posId, isCrossStoreSearch);
      final String productListId = POSUtils.getProductListByPosterminalId(posId).getId();
      final String priceListVersionId = POSUtils.getPriceListVersionByOrgId(orgId, terminalDate)
          .getId();

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("productListId", productListId);
      paramValues.put("priceListVersionId", priceListVersionId);
      paramValues.put("orgId", orgId);
      paramValues.put("orgIds", orgIds);
      paramValues.put("startingDate", now.getTime());
      paramValues.put("endingDate", now.getTime());
      paramValues.put("terminalDate", terminalDate);
      if (isRemote && isMultipricelist && isMultiPriceListSearch) {
        paramValues.put("multipriceListVersionId",
            POSUtils.getPriceListVersionForPriceList(
                jsonsent.getJSONObject("remoteParams").getString("currentPriceList"), terminalDate)
                .getId());
      }
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static Map<String, Object> createRegularProductValues(JSONObject jsonsent)
      throws JSONException {
    try {
      OBContext.setAdminMode(true);
      final Date terminalDate = getTerminalDate(jsonsent);
      final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final String posId = getTerminalId(jsonsent);
      final List<String> orgIds = POSUtils.getOrgListCrossStore(posId, isCrossStoreSearch);
      final String productListId = POSUtils.getProductListByPosterminalId(posId).getId();
      final String priceListVersionId = POSUtils.getPriceListVersionByOrgId(orgId, terminalDate)
          .getId();

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("productListId", productListId);
      paramValues.put("priceListVersionId", priceListVersionId);
      paramValues.put("orgId", orgId);
      paramValues.put("orgIds", orgIds);
      paramValues.put("terminalDate", terminalDate);
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return prepareQuery(jsonsent);
  }

  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    final OBRETCOProductList productList = POSUtils
        .getProductListByPosterminalId(getTerminalId(jsonsent));
    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    List<String> products = new ArrayList<>();
    final String posPrecision = getPosPrecision();
    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    final boolean isMultiPriceListSearch = isMultiPriceListSearch(jsonsent);
    final boolean isRemote = isRemoteSearch(jsonsent) || isCrossStoreSearch;
    final boolean useGetForProductImages = getPreference("OBPOS_retail.productImages");
    final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");
    final boolean executeGenericProductQry = getPreference("OBPOS_ExecuteGenericProductQuery");
    final boolean allowNoPriceInMainPriceList = getPreference(
        "OBPOS_allowProductsNoPriceInMainPricelist");

    Map<String, Object> args = new HashMap<>();
    args.put("posPrecision", posPrecision);
    args.put("multiPriceList", isRemote && isMultipricelist && isMultiPriceListSearch);
    args.put("terminalId", getTerminalId(jsonsent));
    args.put("isCrossStoreSearch", isCrossStoreSearch);

    args.put("crossStore", false);
    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    HQLPropertyList regularProductsDiscHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsDisc, args);
    args.put("crossStore", true);
    HQLPropertyList crossStoreRegularProductsHQLProperties = isCrossStoreSearch
        ? ModelExtensionUtils.getPropertyExtensions(extensions, args)
        : null;

    // Regular products
    final String regularProductHqlString = getRegularProductHqlString(jsonsent, isRemote,
        useGetForProductImages, isMultipricelist, allowNoPriceInMainPriceList,
        regularProductsHQLProperties);
    products.add(regularProductHqlString);
    if (isCrossStoreSearch) {
      final String crossStoreRegularProductHqlString = getCrossStoreRegularProductHqlString(
          jsonsent, useGetForProductImages, isMultipricelist, allowNoPriceInMainPriceList,
          crossStoreRegularProductsHQLProperties);
      products.add(crossStoreRegularProductHqlString);
    }

    // Packs
    final String packHqlString = getPackProductHqlString(isRemote, useGetForProductImages,
        regularProductsDiscHQLProperties);
    products.add(packHqlString);

    // Generic products
    if (executeGenericProductQry && !isRemote) {
      // BROWSE tab is hidden, we do not need to send generic products
      final String genericProductsHqlString = getGenericProductHqlString(useGetForProductImages,
          regularProductsHQLProperties);
      products.add(genericProductsHqlString);
    }

    return products;
  }

  private String getRegularProductHqlString(final JSONObject jsonsent, final boolean isRemote,
      final boolean useGetForProductImages, final boolean isMultipricelist,
      final boolean allowNoPriceInMainPriceList, final HQLPropertyList regularProductsHQLProperties)
      throws JSONException {
    final Long lastUpdated = getLastUpdated(jsonsent);
    final boolean filterWithProductEntities = getPreference(
        "OBPOS_FilterProductByEntitiesOnRefresh");
    final boolean filterWithProductPrice = getPreference("OBPOS_FilterProductByPriceOnRefresh");
    if (filterWithProductEntities && filterWithProductPrice) {
      throw new JSONException("Preference \"WebPOS Product filter by entities\" "
          + "and \"WebPOS Product filter by Price\" should not be set at same time");
    }

    String hql = "select" + regularProductsHQLProperties.getHqlSelect();
    hql += getRegularProductHql(isRemote, isMultipricelist, jsonsent, useGetForProductImages,
        allowNoPriceInMainPriceList);
    if (lastUpdated != null) {
      hql += "AND (";
      if (filterWithProductPrice) {
        hql += "(ppp.$incrementalUpdateCriteria)";
      } else {
        hql += "(product.$incrementalUpdateCriteria)";
        if (filterWithProductEntities) {
          hql += " OR (pli.$incrementalUpdateCriteria) OR (product.uOM.$incrementalUpdateCriteria) OR (ppp.$incrementalUpdateCriteria)";
        }
      }
      hql += ") ";
    } else {
      hql += "AND ((product.$incrementalUpdateCriteria)";
      if (filterWithProductEntities) {
        hql += " AND (pli.$incrementalUpdateCriteria) AND (product.uOM.$incrementalUpdateCriteria)";
        if (!allowNoPriceInMainPriceList) {
          hql += " AND (ppp.$incrementalUpdateCriteria)";
        }
      }
      hql += ") ";
    }
    if (isRemote) {
      hql += "order by product.name asc, product.id";
    } else {
      hql += "and product.$paginationByIdCriteria order by product.id";
    }

    return hql;
  }

  protected String getRegularProductHql(boolean isRemote, boolean isMultipricelist,
      JSONObject jsonsent, boolean useGetForProductImages) {
    return createRegularProductHql(isRemote, isMultipricelist, jsonsent, useGetForProductImages,
        false);
  }

  protected String getRegularProductHql(boolean isRemote, boolean isMultipricelist,
      JSONObject jsonsent, boolean useGetForProductImages, boolean allowNoPriceInMainPriceList) {
    return createRegularProductHql(isRemote, isMultipricelist, jsonsent, useGetForProductImages,
        allowNoPriceInMainPriceList);
  }

  public static String createRegularProductHql(boolean isRemote, boolean isMultipricelist,
      JSONObject jsonsent, boolean useGetForProductImages, boolean allowNoPriceInMainPriceList) {

    String hql = "FROM OBRETCO_Prol_Product as pli ";
    hql += "inner join pli.product as product ";
    if (!useGetForProductImages) {
      hql += "left outer join product.image img ";
    }
    hql += "left join product.attributeSet as attrset ";

    if (isMultipricelist && allowNoPriceInMainPriceList
        && (!isRemote || isMultiPriceListSearch(jsonsent))) {
      hql += "left outer join product.pricingProductPriceList ppp with (ppp.priceListVersion.id = :priceListVersionId) ";
    } else {
      hql += "inner join product.pricingProductPriceList ppp ";
    }

    if (isRemote && isMultipricelist && isMultiPriceListSearch(jsonsent)) {
      hql += ", PricingProductPrice pp WHERE pp.product=pli.product and pp.priceListVersion.id= :multipriceListVersionId ";
    } else {
      hql += " WHERE 1=1";
    }

    hql += " AND $filtersCriteria AND $hqlCriteria ";

    if (isMultipricelist && allowNoPriceInMainPriceList
        && (!isRemote || isMultiPriceListSearch(jsonsent))) {
      hql += "AND (pli.obretcoProductlist.id = :productListId) ";
    } else {
      hql += "AND (pli.obretcoProductlist.id = :productListId) ";
      hql += "AND (ppp.priceListVersion.id = :priceListVersionId) ";
    }

    return hql;
  }

  private String getGenericProductHqlString(final boolean useGetForProductImages,
      HQLPropertyList regularProductsHQLProperties) {
    String genericProductsHqlString = "select " //
        + regularProductsHQLProperties.getHqlSelect() + " from Product product ";
    if (!useGetForProductImages) {
      genericProductsHqlString += "left outer join product.image img ";
    }
    genericProductsHqlString += "left join product.attributeSet as attrset ";
    genericProductsHqlString += "left join product.oBRETCOProlProductList as pli left outer join product.pricingProductPriceList ppp "
        + " where $filtersCriteria AND ppp.priceListVersion.id = :priceListVersionId AND product.isGeneric = 'Y' AND (product.$incrementalUpdateCriteria) and exists (select 1 from Product product2 left join product2.oBRETCOProlProductList as pli2, "
        + " PricingProductPrice ppp2 where product.id = product2.genericProduct.id and product2 = ppp2.product and ppp2.priceListVersion.id = :priceListVersionId "
        + " and pli2.obretcoProductlist.id = :productListId)" //
        + " and product.$paginationByIdCriteria order by product.id";
    return genericProductsHqlString;
  }

  private String getPackProductHqlString(final boolean isRemote,
      final boolean useGetForProductImages,
      final HQLPropertyList regularProductsDiscHQLProperties) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(regularProductsDiscHQLProperties.getHqlSelect());
    query.append(" from PricingAdjustment p");
    query.append(" join p.discountType pt");
    if (!useGetForProductImages) {
      query.append(" left join p.obdiscImage img");
    }
    query.append(" where $filtersCriteria");
    query.append(" and pt.obposIsCategory = true");
    query.append(" and pt.active = true");
    query.append(" and p.$incrementalUpdateCriteria");
    query.append(" and" + getPackProductWhereClause());
    if (isRemote) {
      query.append(" order by p.name asc, p.id");
    } else {
      query.append(" and p.$paginationByIdCriteria order by p.id");
    }

    return query.toString();
  }

  static String getPackProductWhereClause() {
    final StringBuilder query = new StringBuilder();
    query.append(" p.$readableSimpleClientCriteria");
    query.append(" and p.$naturalOrgCriteria");
    query.append(" and p.startingDate <= :startingDate");
    query.append(" and (p.endingDate is null");
    query.append(" or p.endingDate >= :endingDate)");
    // Pack is included in the assortment of current store
    query.append(" and ((p.includedProducts = 'N'");
    query.append(" and not exists (");
    query.append("   select 1 ");
    query.append("   from PricingAdjustmentProduct pap");
    query.append("   where pap.priceAdjustment.id = p.id");
    query.append("   and pap.active = true");
    query.append("   and not exists (");
    query.append("     select 1");
    query.append("     from OBRETCO_Prol_Product pli");
    query.append("     where pli.product.id = pap.product.id");
    query.append("     and pli.obretcoProductlist.id = :productListId");
    query.append("     and pli.active = true");
    query.append("   )");
    query.append(" ))");
    // Pack is not excluded in the assortment of current store
    query.append(" or (p.includedProducts = 'Y'");
    query.append(" and not exists (");
    query.append("   select 1 ");
    query.append("   from PricingAdjustmentProduct pap");
    query.append("   where pap.priceAdjustment.id = p.id");
    query.append("   and pap.active = true");
    query.append("   and exists (");
    query.append("     select 1");
    query.append("     from OBRETCO_Prol_Product pli");
    query.append("     where pli.product.id = pap.product.id");
    query.append("     and pli.obretcoProductlist.id = :productListId");
    query.append("     and pli.active = true");
    query.append("   )");
    query.append(" )))");
    // Pack is included in current store
    query.append(" and ((p.includedOrganizations='N'");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from PricingAdjustmentOrganization pao");
    query.append("   where pao.priceAdjustment.id = p.id");
    query.append("   and pao.organization.id = :orgId");
    query.append("   and pao.active = true");
    query.append(" ))");
    // Pack is not excluded in current store
    query.append(" or (p.includedOrganizations='Y'");
    query.append(" and not exists (");
    query.append("   select 1");
    query.append("   from PricingAdjustmentOrganization pao");
    query.append("   where pao.priceAdjustment.id = p.id");
    query.append("   and pao.organization.id = :orgId");
    query.append("   and pao.active = true");
    query.append(" )))");
    return query.toString();
  }

  private String getCrossStoreRegularProductHqlString(final JSONObject jsonsent,
      final boolean useGetForProductImages, final boolean isMultipricelist,
      final boolean allowNoPriceInMainPriceList,
      final HQLPropertyList crossStoreRegularProductsHQLProperties) {
    final boolean showProductsWithCurrentPrice = isMultipricelist
        && isMultiPriceListSearch(jsonsent);
    final boolean showOnlyProductsWithPrice = !showProductsWithCurrentPrice
        || !allowNoPriceInMainPriceList;

    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(crossStoreRegularProductsHQLProperties.getHqlSelect());
    query.append(" from Product product");
    query.append(" left join product.attributeSet as attrset");
    query.append(" left join product.oBRETCOProlProductList pli");
    query.append(" with pli.obretcoProductlist.id = :productListId");
    if (!useGetForProductImages) {
      query.append(" left join product.image img");
    }
    query.append(" where $filtersCriteria");
    query.append(" and $hqlCriteria");
    // Product exists in the assortment and price list of a cross store
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :orgIds");
    query.append("   and exists (");
    query.append("     select 1");
    query.append("     from OBRETCO_Prol_Product pli");
    query.append("     where pli.product.id = product.id");
    query.append("     and pli.obretcoProductlist.id = o.obretcoProductlist.id");
    query.append("   )");
    if (showProductsWithCurrentPrice) {
      query.append("   and exists (");
      query.append("     select 1");
      query.append("     from PricingProductPrice ppp");
      query.append("     where ppp.product.id = product.id");
      query.append("     and ppp.priceListVersion.id = :multipriceListVersionId");
      query.append("   )");
    }
    if (showOnlyProductsWithPrice) {
      query.append("   and exists (");
      query.append("     select 1");
      query.append("     from PricingProductPrice ppp");
      query.append("     join ppp.priceListVersion plv");
      query.append("     where ppp.product.id = product.id");
      query.append("     and plv.priceList.id = o.obretcoPricelist.id");
      query.append("     and plv.validFromDate <= :terminalDate");
      query.append("   )");
    }
    query.append(" )");
    // Product doesn't exist in the assortment or price list of current store
    query.append(" and (not exists (");
    query.append("   select 1");
    query.append("   from OBRETCO_Prol_Product pli");
    query.append("   where pli.product.id = product.id");
    query.append("   and pli.obretcoProductlist.id = :productListId");
    query.append(" )");
    if (showProductsWithCurrentPrice) {
      query.append(" or not exists (");
      query.append("   select 1");
      query.append("   from PricingProductPrice ppp");
      query.append("   where ppp.product.id = product.id");
      query.append("   and ppp.priceListVersion.id = :multipriceListVersionId");
      query.append(" )");
    }
    if (showOnlyProductsWithPrice) {
      query.append(" or not exists (");
      query.append("   select 1");
      query.append("   from PricingProductPrice ppp");
      query.append("   where ppp.product.id = product.id");
      query.append("   and ppp.priceListVersion.id = :priceListVersionId");
      query.append(" )");
    }
    query.append(" )");
    query.append(" and product.$incrementalUpdateCriteria");
    query.append(" order by product.name asc, product.id");

    return query.toString();
  }

  private String getPosPrecision() {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    String posPrecision = "";
    try {
      OBContext.setAdminMode(false);
      posPrecision = (priceList.getCurrency().getObposPosprecision() == null
          ? priceList.getCurrency().getPricePrecision()
          : priceList.getCurrency().getObposPosprecision()).toString();
    } catch (Exception e) {
      log.error("Error getting currency by id: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return posPrecision;
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

  private static String getTerminalId(final JSONObject jsonsent) {
    String terminalId = null;
    try {
      terminalId = jsonsent.getString("pos");
    } catch (JSONException e) {
      log.error("Error while getting pos " + e.getMessage(), e);
    }
    return terminalId;
  }

  private static Date getTerminalDate(JSONObject jsonsent) throws JSONException {
    return OBMOBCUtils.calculateServerDate(
        jsonsent.getJSONObject("parameters").getString("terminalTime"),
        jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));
  }

  private Long getLastUpdated(final JSONObject jsonsent) throws JSONException {
    return jsonsent.has("lastUpdated") && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
  }

  private static boolean isMultiPriceListSearch(final JSONObject jsonsent) {
    boolean multiPriceListSearch = false;
    try {
      multiPriceListSearch = jsonsent.has("remoteParams") && StringUtils
          .isNotEmpty(jsonsent.getJSONObject("remoteParams").optString("currentPriceList"));
    } catch (JSONException e) {
      log.error("Error while getting currentPriceList " + e.getMessage(), e);
    }
    return multiPriceListSearch;
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

  private boolean isRemoteSearch(final JSONObject jsonsent) {
    boolean remoteSearch = false;
    try {
      remoteSearch = (jsonsent.has("parameters")
          && jsonsent.getJSONObject("parameters").optBoolean("forceRemote"))
          || "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true,
              OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
              OBContext.getOBContext().getRole(), null));
      ;
    } catch (JSONException e) {
      log.error("Error while getting forceRemote property " + e.getMessage(), e);
    } catch (PropertyException e) {
      throw new OBException(e.getMessage());
    }
    return remoteSearch;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean mustHaveRemoteFilters() {
    return true;
  }

  @Override
  protected String messageWhenNoFilters() {
    return "OBPOS_ProductSearchTooBroad";
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);
  }
}
