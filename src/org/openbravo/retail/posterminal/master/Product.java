/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
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

import org.apache.log4j.Logger;
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
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Product extends ProcessHQLQuery {
  public static final String productPropertyExtension = "OBPOS_ProductExtension";
  public static final String productDiscPropertyExtension = "OBPOS_ProductDiscExtension";
  private static final Logger log = Logger.getLogger(Product.class);

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
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    String posPrecision = "";
    try {
      OBContext.setAdminMode(false);
      posPrecision = (priceList.getCurrency().getObposPosprecision() == null ? priceList
          .getCurrency().getPricePrecision() : priceList.getCurrency().getObposPosprecision())
          .toString();
    } catch (Exception e) {
      log.error("Error getting currency by id: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      posPrecision = (priceList.getCurrency().getObposPosprecision() == null ? priceList
          .getCurrency().getPricePrecision() : priceList.getCurrency().getObposPosprecision())
          .toString();
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    boolean isMultipricelist = false;
    try {
      OBContext.setAdminMode(false);
      isMultipricelist = "Y".equals(Preferences.getPreferenceValue("OBPOS_EnableMultiPriceList",
          true, OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference EnableMultiPriceList " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("posPrecision", posPrecision);
    try {
      if (isRemote && isMultipricelist && jsonsent.has("remoteParams")
          && jsonsent.getJSONObject("remoteParams").getString("currentPriceList") != null) {
        args.put("multiPriceList", true);
      } else {
        args.put("multiPriceList", false);
      }
    } catch (JSONException e) {
      log.error("Error while getting multiPriceList", e);
    }

    try {
      args.put("terminalId", jsonsent.getString("pos"));
    } catch (JSONException e) {
      log.error("Error while getting terminalId " + e.getMessage(), e);
    }

    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils.getPropertyExtensions(
        extensions, args);
    HQLPropertyList regularProductsDiscHQLProperties = ModelExtensionUtils.getPropertyExtensions(
        extensionsDisc, args);

    propertiesList.add(regularProductsHQLProperties);
    propertiesList.add(regularProductsDiscHQLProperties);
    propertiesList.add(regularProductsHQLProperties);

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      final Date terminalDate = OBMOBCUtils
          .calculateServerDate(
              jsonsent.getJSONObject("parameters").getString("terminalTime"),
              jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset")
                  .getLong("value"));

      String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      boolean isMultipricelist = false;
      try {
        OBContext.setAdminMode(false);
        isMultipricelist = "Y".equals(Preferences.getPreferenceValue("OBPOS_EnableMultiPriceList",
            true, OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                .getOBContext().getRole(), null));
      } catch (PropertyException e) {
        log.error("Error getting preference EnableMultiPriceList " + e.getMessage(), e);
      } finally {
        OBContext.restorePreviousMode();
      }
      boolean isRemote = false;
      try {
        OBContext.setAdminMode(false);
        isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true,
            OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                .getOBContext().getRole(), null));
      } catch (PropertyException e) {
        log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
      } finally {
        OBContext.restorePreviousMode();
      }
      final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
      final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
          terminalDate);
      Calendar now = Calendar.getInstance();
      Map<String, Object> paramValues = new HashMap<String, Object>();
      if (isRemote && isMultipricelist && jsonsent.has("remoteParams")) {
        paramValues.put(
            "multipriceListVersionId",
            POSUtils.getPriceListVersionForPriceList(
                jsonsent.getJSONObject("remoteParams").getString("currentPriceList"), terminalDate)
                .getId());
      }
      paramValues.put("productListId", productList.getId());
      paramValues.put("priceListVersionId", priceListVersion.getId());
      paramValues.put("endingDate", now.getTime());
      paramValues.put("startingDate", now.getTime());
      paramValues.put("orgId", orgId);
      boolean isForceRemote = jsonsent.getJSONObject("parameters").has("forceRemote")
          && jsonsent.getJSONObject("parameters").getBoolean("forceRemote");
      if (!isRemote && !isForceRemote) {
        paramValues.put("priceListVersionId", priceListVersion.getId());
        paramValues.put("productListId", productList.getId());
      }

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);

    final PriceList priceList = POSUtils.getPriceListByOrgId(orgId);

    if (productList == null) {
      throw new JSONException("Product list not found");
    }
    List<String> products = new ArrayList<String>();
    String posPrecision = "";
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      posPrecision = (priceList.getCurrency().getObposPosprecision() == null ? priceList
          .getCurrency().getPricePrecision() : priceList.getCurrency().getObposPosprecision())
          .toString();
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    boolean useGetForProductImages = false;
    try {
      useGetForProductImages = "Y".equals(Preferences.getPreferenceValue(
          "OBPOS_retail.productImages", true, OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      useGetForProductImages = false;
    }
    boolean isMultipricelist = false;
    try {
      OBContext.setAdminMode(false);
      isMultipricelist = "Y".equals(Preferences.getPreferenceValue("OBPOS_EnableMultiPriceList",
          true, OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference EnableMultiPriceList " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    boolean executeGenericProductQry = false;
    try {
      OBContext.setAdminMode(false);
      executeGenericProductQry = "Y".equals(Preferences.getPreferenceValue(
          "OBPOS_ExecuteGenericProductQuery", true, OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_ShowGenericProduct " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("posPrecision", posPrecision);
    try {
      if (isRemote && isMultipricelist && jsonsent.has("remoteParams")
          && jsonsent.getJSONObject("remoteParams").getString("currentPriceList") != null) {
        args.put("multiPriceList", true);
      } else {
        args.put("multiPriceList", false);
      }
    } catch (JSONException e) {
      log.error("Error while getting multiPriceList", e);
    }

    try {
      args.put("terminalId", jsonsent.getString("pos"));
    } catch (JSONException e) {
      log.error("Error while getting terminalId " + e.getMessage(), e);
    }

    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils.getPropertyExtensions(
        extensions, args);
    HQLPropertyList regularProductsDiscHQLProperties = ModelExtensionUtils.getPropertyExtensions(
        extensionsDisc, args);

    Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;

    // regular products
    String hql = "select" + regularProductsHQLProperties.getHqlSelect()
        + "FROM OBRETCO_Prol_Product as pli ";
    if (!useGetForProductImages) {
      hql += "left outer join pli.product.image img ";
    }
    hql += "inner join pli.product as product, PricingProductPrice ppp ";
    if (isRemote && isMultipricelist && jsonsent.has("remoteParams")) {
      hql += ", PricingProductPrice pp WHERE pp.product=pli.product and pp.priceListVersion.id= :multipriceListVersionId ";
    } else {
      hql += " WHERE 1=1";
    }

    hql += " AND $filtersCriteria AND $hqlCriteria AND (pli.obretcoProductlist.id = :productListId) "
        + "AND (ppp.priceListVersion.id= :priceListVersionId ) "
        + " AND ("
        + "pli.product.id = ppp.product.id" + ") ";

    if (lastUpdated != null) {
      hql += "AND ((pli.product.$incrementalUpdateCriteria) OR (pli.$incrementalUpdateCriteria) OR (ppp.$incrementalUpdateCriteria) OR (product.uOM.$incrementalUpdateCriteria))";
    } else {
      hql += "AND ((pli.product.$incrementalUpdateCriteria) AND (pli.$incrementalUpdateCriteria)) ";
    }

    if (isRemote) {
      hql += "order by pli.product.name asc, pli.product.id";
    }
    products.add(hql);
    // Packs, combos...
    String packAndCombosHqlString = "select " //
        + regularProductsDiscHQLProperties.getHqlSelect() + " from PricingAdjustment as p ";
    if (!useGetForProductImages) {
      packAndCombosHqlString += "left outer join p.obdiscImage img ";
    }
    packAndCombosHqlString += "where $filtersCriteria AND p.discountType.obposIsCategory = true "//
        + "   and p.discountType.active = true " //
        + "   and p.$readableSimpleClientCriteria"//
        + "   and (p.endingDate is null or p.endingDate >= :endingDate)" //
        + "   and p.startingDate <= :startingDate "
        + "   and (p.$incrementalUpdateCriteria) "//
        // assortment products
        + "and ((p.includedProducts = 'N' and not exists (select 1 "
        + "      from PricingAdjustmentProduct pap where pap.active = true and "
        + "      pap.priceAdjustment = p and pap.product.sale = true "
        + "      and pap.product not in (select ppl.product.id from OBRETCO_Prol_Product ppl "
        + "      where ppl.obretcoProductlist.id = :productListId and ppl.active = true))) "
        + " or (p.includedProducts = 'Y' and not exists (select 1 "
        + "      from PricingAdjustmentProduct pap, OBRETCO_Prol_Product ppl "
        + "      where pap.active = true and pap.priceAdjustment = p "
        + "      and pap.product.id = ppl.product.id "
        + "      and ppl.obretcoProductlist.id = :productListId))) "
        // organization
        + "and p.$naturalOrgCriteria and ((p.includedOrganizations='Y' "
        + "  and not exists (select 1 "
        + "         from PricingAdjustmentOrganization o"
        + "        where active = true"
        + "          and o.priceAdjustment = p"
        + "          and o.organization.id = :orgId )) "
        + "   or (p.includedOrganizations='N' "
        + "  and  exists (select 1 "
        + "         from PricingAdjustmentOrganization o"
        + "        where active = true"
        + "          and o.priceAdjustment = p"
        + "          and o.organization.id = :orgId )) )";
    if (isRemote) {
      packAndCombosHqlString += " order by p.name asc, p.id";
    }

    products.add(packAndCombosHqlString);
    // generic products
    boolean isForceRemote = jsonsent.getJSONObject("parameters").has("forceRemote")
        && jsonsent.getJSONObject("parameters").getBoolean("forceRemote");
    if (executeGenericProductQry && !isRemote && !isForceRemote) {
      // BROWSE tab is hidden, we do not need to send generic products
      String genericProductsHqlString = "select " //
          + regularProductsHQLProperties.getHqlSelect() + " from Product product ";
      if (!useGetForProductImages) {
        genericProductsHqlString += "left outer join product.image img ";
      }
      genericProductsHqlString += "left join product.oBRETCOProlProductList as pli left outer join product.pricingProductPriceList ppp "
          + " where $filtersCriteria AND ppp.priceListVersion.id = :priceListVersionId AND product.isGeneric = 'Y' AND (product.$incrementalUpdateCriteria) and exists (select 1 from Product product2 left join product2.oBRETCOProlProductList as pli2, "
          + " PricingProductPrice ppp2 where product.id = product2.genericProduct.id and product2 = ppp2.product and ppp2.priceListVersion.id = :priceListVersionId "
          + " and pli2.obretcoProductlist.id = :productListId)" //
          + " order by product.id";
      products.add(genericProductsHqlString);
    }
    return products;
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
