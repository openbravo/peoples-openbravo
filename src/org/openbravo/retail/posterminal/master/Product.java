/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
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
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Product extends ProcessHQLQuery {
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

    final String posPrecision = getPosPrecision();
    final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
    final boolean isMultiPriceListSearch = isMultiPriceListSearch(jsonsent);
    final boolean isRemote = getPreference("OBPOS_remote.product") || isCrossStoreSearch;
    final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");

    Map<String, Object> args = new HashMap<>();
    args.put("posPrecision", posPrecision);
    args.put("multiPriceList", isRemote && isMultipricelist && isMultiPriceListSearch);
    args.put("terminalId", getTerminalId(jsonsent));
    args.put("crossStore", false);
    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    HQLPropertyList regularProductsDiscHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsDisc, args);

    if (isCrossStoreSearch) {
      args.put("crossStore", true);
      HQLPropertyList crossStoreRegularProductsHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions, args);
      HQLPropertyList crossStoreRegularProductsDiscHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensionsDisc, args);
      propertiesList.add(regularProductsHQLProperties);
      propertiesList.add(crossStoreRegularProductsHQLProperties);
      propertiesList.add(regularProductsDiscHQLProperties);
      propertiesList.add(crossStoreRegularProductsDiscHQLProperties);
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
      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));

      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final boolean isCrossStoreSearch = isCrossStoreSearch(jsonsent);
      final boolean isMultiPriceListSearch = isMultiPriceListSearch(jsonsent);
      final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");
      final boolean isRemote = getPreference("OBPOS_remote.product") || isCrossStoreSearch;

      final Calendar now = Calendar.getInstance();
      final String posId = getTerminalId(jsonsent);
      final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
      crossStoreOrgIds.remove(orgId);
      Set<String> crossStoreNaturalTree = OBContext.getOBContext()
          .getOrganizationStructureProvider(OBContext.getOBContext().getCurrentClient().getId())
          .getNaturalTree(orgId);
      crossStoreNaturalTree.addAll(POSUtils.getOrgListCrossStore(posId));
      final String assortmentId = POSUtils.getProductListByPosterminalId(posId).getId();
      final String priceListVersionId = POSUtils.getPriceListVersionByOrgId(orgId, terminalDate)
          .getId();

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("assortmentId", assortmentId);
      paramValues.put("priceListVersionId", priceListVersionId);
      paramValues.put("orgId", orgId);
      paramValues.put("crossStoreOrgIds", crossStoreOrgIds);
      paramValues.put("crossStoreNaturalTree", crossStoreNaturalTree);
      paramValues.put("endingDate", now.getTime());
      paramValues.put("startingDate", now.getTime());
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

      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));
      final String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final String posId = getTerminalId(jsonsent);
      final List<String> crossStoreOrgIds = POSUtils.getOrgListCrossStore(posId);
      crossStoreOrgIds.remove(orgId);
      final String assortmentId = POSUtils.getProductListByPosterminalId(posId).getId();
      final String priceListVersionId = POSUtils.getPriceListVersionByOrgId(orgId, terminalDate)
          .getId();

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("assortmentId", assortmentId);
      paramValues.put("priceListVersionId", priceListVersionId);
      paramValues.put("orgId", orgId);
      paramValues.put("crossStoreOrgIds", crossStoreOrgIds);
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
    final boolean isRemote = getPreference("OBPOS_remote.product") || isCrossStoreSearch;
    final boolean useGetForProductImages = getPreference("OBPOS_retail.productImages");
    final boolean isMultipricelist = getPreference("OBPOS_EnableMultiPriceList");
    final boolean executeGenericProductQry = getPreference("OBPOS_ExecuteGenericProductQuery");
    final boolean allowNoPriceInMainPriceList = getPreference(
        "OBPOS_allowProductsNoPriceInMainPricelist");

    Map<String, Object> args = new HashMap<>();
    args.put("posPrecision", posPrecision);
    args.put("multiPriceList", isRemote && isMultipricelist && isMultiPriceListSearch);
    args.put("terminalId", getTerminalId(jsonsent));

    args.put("crossStore", false);
    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    HQLPropertyList regularProductsDiscHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsDisc, args);
    args.put("crossStore", true);
    HQLPropertyList crossStoreRegularProductsHQLProperties = isCrossStoreSearch
        ? ModelExtensionUtils.getPropertyExtensions(extensions, args)
        : null;
    HQLPropertyList crossStoreRegularProductsDiscHQLProperties = isCrossStoreSearch
        ? ModelExtensionUtils.getPropertyExtensions(extensionsDisc, args)
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

    // Packs, combos...
    final String packAndCombosHqlString = getPackAndCombosHqlString(isRemote,
        useGetForProductImages, regularProductsDiscHQLProperties);
    products.add(packAndCombosHqlString);
    if (isCrossStoreSearch) {
      final String crossStorePackAndCombosHqlString = getCrossStorePackAndCombosHqlString(
          useGetForProductImages, crossStoreRegularProductsDiscHQLProperties);
      products.add(crossStorePackAndCombosHqlString);
    }

    // Generic products
    boolean isForceRemote = jsonsent.getJSONObject("parameters").has("forceRemote")
        && jsonsent.getJSONObject("parameters").getBoolean("forceRemote");
    if (executeGenericProductQry && !isRemote && !isForceRemote) {
      // BROWSE tab is hidden, we do not need to send generic products
      final String genericProductsHqlString = getGenericProductsHqlString(useGetForProductImages,
          regularProductsHQLProperties);
      products.add(genericProductsHqlString);
    }

    return products;
  }

  private String getRegularProductHqlString(final JSONObject jsonsent, final boolean isRemote,
      final boolean useGetForProductImages, final boolean isMultipricelist,
      final boolean allowNoPriceInMainPriceList, final HQLPropertyList regularProductsHQLProperties)
      throws JSONException {
    Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;

    String hql = "select" + regularProductsHQLProperties.getHqlSelect();
    hql += getRegularProductHql(isRemote, isMultipricelist, jsonsent, useGetForProductImages,
        allowNoPriceInMainPriceList);
    if (lastUpdated != null) {
      hql += "AND ((product.$incrementalUpdateCriteria) OR (pli.$incrementalUpdateCriteria) OR (ppp.$incrementalUpdateCriteria) OR (product.uOM.$incrementalUpdateCriteria))";
    } else {
      hql += "AND ((product.$incrementalUpdateCriteria) AND (pli.$incrementalUpdateCriteria)) ";
    }
    if (isRemote) {
      hql += "order by product.name asc, product.id";
    } else {
      hql += "order by product.id";
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
    if (!useGetForProductImages) {
      hql += "left outer join pli.product.image img ";
    }
    hql += "inner join pli.product as product ";

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
      hql += "AND (pli.obretcoProductlist.id = :assortmentId) ";
    } else {
      hql += "AND (pli.obretcoProductlist.id = :assortmentId) ";
      hql += "AND (ppp.priceListVersion.id = :priceListVersionId) ";
    }

    return hql;
  }

  private String getGenericProductsHqlString(final boolean useGetForProductImages,
      HQLPropertyList regularProductsHQLProperties) {
    String genericProductsHqlString = "select " //
        + regularProductsHQLProperties.getHqlSelect() + " from Product product ";
    if (!useGetForProductImages) {
      genericProductsHqlString += "left outer join product.image img ";
    }
    genericProductsHqlString += "left join product.oBRETCOProlProductList as pli left outer join product.pricingProductPriceList ppp "
        + " where $filtersCriteria AND ppp.priceListVersion.id = :priceListVersionId AND product.isGeneric = 'Y' AND (product.$incrementalUpdateCriteria) and exists (select 1 from Product product2 left join product2.oBRETCOProlProductList as pli2, "
        + " PricingProductPrice ppp2 where product.id = product2.genericProduct.id and product2 = ppp2.product and ppp2.priceListVersion.id = :priceListVersionId "
        + " and pli2.obretcoProductlist.id = :assortmentId)" //
        + " order by product.id";
    return genericProductsHqlString;
  }

  private String getPackAndCombosHqlString(final boolean isRemote,
      final boolean useGetForProductImages,
      final HQLPropertyList regularProductsDiscHQLProperties) {
    String packAndCombosHqlString = "select " //
        + regularProductsDiscHQLProperties.getHqlSelect() + " from PricingAdjustment as p ";
    if (!useGetForProductImages) {
      packAndCombosHqlString += "left outer join p.obdiscImage img ";
    }
    packAndCombosHqlString += "where $filtersCriteria AND p.discountType.obposIsCategory = true "//
        + "   and p.discountType.active = true " //
        + "   and p.$readableSimpleClientCriteria"//
        + "   and (p.endingDate is null or p.endingDate >= :endingDate)" //
        + "   and p.startingDate <= :startingDate " + "   and (p.$incrementalUpdateCriteria) "//
        // assortment products
        + "and ((p.includedProducts = 'N' and not exists (select 1 "
        + "      from PricingAdjustmentProduct pap where pap.active = true and "
        + "      pap.priceAdjustment = p and pap.product.sale = true "
        + "      and pap.product not in (select ppl.product.id from OBRETCO_Prol_Product ppl "
        + "      where ppl.obretcoProductlist.id = :assortmentId and ppl.active = true))) "
        + " or (p.includedProducts = 'Y' and not exists (select 1 "
        + "      from PricingAdjustmentProduct pap, OBRETCO_Prol_Product ppl "
        + "      where pap.active = true and pap.priceAdjustment = p "
        + "      and pap.product.id = ppl.product.id "
        + "      and ppl.obretcoProductlist.id = :assortmentId))) "
        // organization
        + "and p.$naturalOrgCriteria and ((p.includedOrganizations='Y' "
        + "  and not exists (select 1 " + "         from PricingAdjustmentOrganization o"
        + "        where active = true" + "          and o.priceAdjustment = p"
        + "          and o.organization.id = :orgId )) " + "   or (p.includedOrganizations='N' "
        + "  and  exists (select 1 " + "         from PricingAdjustmentOrganization o"
        + "        where active = true" + "          and o.priceAdjustment = p"
        + "          and o.organization.id = :orgId )) )";
    if (isRemote) {
      packAndCombosHqlString += " order by p.name asc, p.id";
    } else {
      packAndCombosHqlString += " order by p.id";
    }
    return packAndCombosHqlString;
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
    if (!useGetForProductImages) {
      query.append(" left outer join product.image img");
    }
    query.append(" where $filtersCriteria");
    query.append(" and $hqlCriteria");
    // Product exists in the assortment and price list of a cross store
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
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
    // Product doesn't exist in the assortment and price list of current store
    query.append(" and (not exists (");
    query.append("   select 1");
    query.append("   from OBRETCO_Prol_Product pli");
    query.append("   where pli.product.id = product.id");
    query.append("   and pli.obretcoProductlist.id = :assortmentId");
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

  private String getCrossStorePackAndCombosHqlString(final boolean useGetForProductImages,
      final HQLPropertyList crossStoreRegularProductsDiscHQLProperties) {
    final StringBuilder query = new StringBuilder();
    query.append(" select");
    query.append(crossStoreRegularProductsDiscHQLProperties.getHqlSelect());
    query.append(" from PricingAdjustment p");
    query.append(" join p.discountType pt");
    if (!useGetForProductImages) {
      query.append(" left outer join p.obdiscImage img");
    }
    query.append(" where $filtersCriteria");
    query.append(" and p.$readableSimpleClientCriteria");
    query.append(" and p.$incrementalUpdateCriteria");
    query.append(" and pt.obposIsCategory = true");
    query.append(" and pt.active = true");
    query.append(" and p.startingDate <= :startingDate");
    query.append(" and (p.endingDate is null");
    query.append(" or p.endingDate >= :endingDate)");
    // Pack is included in the assortment of a cross store
    query.append(" and ((p.includedProducts = 'N'");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
    query.append("   and not exists (");
    query.append("     select 1 ");
    query.append("     from PricingAdjustmentProduct pap");
    query.append("     join pap.product papp");
    query.append("     where pap.active = true");
    query.append("     and pap.priceAdjustment.id = p.id");
    query.append("     and papp.sale = true");
    query.append("     and not exists (");
    query.append("       select 1");
    query.append("       from OBRETCO_Prol_Product pli");
    query.append("       where pli.product.id = papp.id");
    query.append("       and pli.obretcoProductlist.id = o.obretcoProductlist.id");
    query.append("       and pli.active = true");
    query.append("     )");
    query.append("   )");
    query.append(" ))");
    // Pack is not excluded in the assortment of a cross store
    query.append(" or (p.includedProducts = 'Y'");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
    query.append("   and not exists (");
    query.append("     select 1 ");
    query.append("     from PricingAdjustmentProduct pap");
    query.append("     join pap.product papp");
    query.append("     where pap.active = true");
    query.append("     and pap.priceAdjustment.id = p.id");
    query.append("     and papp.sale = true");
    query.append("     and exists (");
    query.append("       select 1");
    query.append("       from OBRETCO_Prol_Product pli");
    query.append("       where pli.product.id = papp.id");
    query.append("       and pli.obretcoProductlist.id = o.obretcoProductlist.id");
    query.append("       and pli.active = true");
    query.append("     )");
    query.append("   )");
    query.append(" )))");
    query.append(" and p.organization.id in :crossStoreNaturalTree");
    // Pack is included in a cross store
    query.append(" and ((p.includedOrganizations='N'");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
    query.append("   and exists (");
    query.append("     select 1");
    query.append("     from PricingAdjustmentOrganization pao");
    query.append("     where pao.organization.id = o.id");
    query.append("     and pao.active = true");
    query.append("   )");
    query.append(" ))");
    // Pack is not excluded in a cross store
    query.append(" or (p.includedOrganizations='Y'");
    query.append(" and exists (");
    query.append("   select 1");
    query.append("   from Organization o");
    query.append("   where o.id in :crossStoreOrgIds");
    query.append("   and not exists (");
    query.append("     select 1");
    query.append("     from PricingAdjustmentOrganization pao");
    query.append("     where pao.organization.id = o.id");
    query.append("     and pao.active = true");
    query.append("   )");
    query.append(" )))");
    query.append(" order by p.name asc, p.id");

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

}
