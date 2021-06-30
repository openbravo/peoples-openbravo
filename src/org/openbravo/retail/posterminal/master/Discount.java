/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
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
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.service.json.JsonUtils;

@MasterDataModel("Discount")
public class Discount extends MasterDataProcessHQLQuery {

  public static final Logger log = LogManager.getLogger();
  public static final String discountPropertyExtension = "OBPOS_DiscountExtension";

  @Inject
  @Any
  @Qualifier(discountPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      Map<String, Object> paramValues = new HashMap<>();

      // Optional filtering by a list of m_offer_id
      // Used for this class and whom inherit this class
      if (jsonsent.has("parameters")
          && jsonsent.getJSONObject("parameters").has("filterPromotionList")
          && !jsonsent.getJSONObject("parameters").get("filterPromotionList").equals("undefined")
          && !jsonsent.getJSONObject("parameters").get("filterPromotionList").equals("null")) {
        JSONArray filterPromotionList = jsonsent.getJSONObject("parameters")
            .getJSONArray("filterPromotionList");
        paramValues.put("filterPromotionList", filterPromotionList);
      }

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected String getPromotionsHQL(JSONObject jsonsent) throws JSONException {
    return getPromotionsHQL(jsonsent, false);
  }

  protected String getPromotionsHQL(JSONObject jsonsent, boolean addIncrementalUpdateFilter)
      throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();

    final OBRETCOProductList productList = POSUtils
        .getProductListByPosterminalId(jsonsent.getString("pos"));
    PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    String priceListId = priceList.getId();

    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    Long lastUpdated = null;

    if (jsonsent != null) {
      lastUpdated = jsonsent.has("lastUpdated") && !jsonsent.get("lastUpdated").equals("undefined")
          && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    }

    String hql = "from PricingAdjustment p ";
    hql += "where p.client.id = '" + OBContext.getOBContext().getCurrentClient().getId() + "' ";
    if (addIncrementalUpdateFilter) {
      hql += "AND ((p.$incrementalUpdateCriteria)) ";
    }

    boolean multiPrices = false;
    try {
      multiPrices = "Y".equals(Preferences.getPreferenceValue("OBPOS_EnableMultiPriceList", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e1) {
      log.error("Error getting Preference: " + e1.getMessage(), e1);
    }

    // Optional filtering by a list of m_offer_id
    // Used for this class and whom inherit this class
    if (jsonsent.has("parameters")
        && jsonsent.getJSONObject("parameters").has("filterPromotionList")
        && !jsonsent.getJSONObject("parameters").get("filterPromotionList").equals("undefined")
        && !jsonsent.getJSONObject("parameters").get("filterPromotionList").equals("null")) {
      hql += "AND p.id IN (:filterPromotionList) ";
    }

    if (!multiPrices && (lastUpdated == null || addIncrementalUpdateFilter == false)) {
      // price list
      hql += "and ((includePriceLists='Y' ";
      hql += "  and not exists (select 1 ";
      hql += "         from PricingAdjustmentPriceList pl";
      hql += "        where active = true";
      hql += "          and pl.priceAdjustment = p";
      hql += "          and pl.priceList.id ='" + priceListId + "')) ";
      hql += "   or (includePriceLists='N' ";
      hql += "  and  exists (select 1 ";
      hql += "         from PricingAdjustmentPriceList pl";
      hql += "        where active = true";
      hql += "          and pl.priceAdjustment = p";
      hql += "          and pl.priceList.id ='" + priceListId + "')) ";
      hql += "    ) ";
    }
    // assortment products
    hql += "and ((p.includedProducts = 'Y' ";
    hql += (addIncrementalUpdateFilter ? "and (p.$incrementalUpdateCriteria))" : ")");
    hql += " or (p.includedProducts = 'N' and (";
    hql += (addIncrementalUpdateFilter ? "(p.$incrementalUpdateCriteria) or " : "");
    hql += "   exists (select 1 ";
    hql += "      from PricingAdjustmentProduct pap, OBRETCO_Prol_Product ppl ";
    hql += "      where pap.priceAdjustment = p ";
    if (lastUpdated == null) {
      hql += "    and pap.active = true ";
    }
    hql += "      and pap.product.id = ppl.product.id ";
    hql += "      and ppl.active = true and pap.product.active = true ";
    if (addIncrementalUpdateFilter) {
      hql += "      and ((ppl.$incrementalUpdateCriteria) ";
      hql += "      or (pap.product.$incrementalUpdateCriteria)) ";
    }
    hql += "      and ppl.obretcoProductlist.id ='" + productList.getId() + "'))) ";
    hql += "  ) ";

    // organization
    hql += " and p.$naturalOrgCriteria";
    if (lastUpdated == null || addIncrementalUpdateFilter == false) {
      hql += " and ((p.includedOrganizations='Y' ";
      hql += "  and not exists (select 1 ";
      hql += "         from PricingAdjustmentOrganization o";
      hql += "        where active = true";
      hql += "          and o.priceAdjustment = p";
      hql += "          and o.organization.id ='" + orgId + "')) ";
      hql += "   or (p.includedOrganizations='N' ";
      hql += "  and  exists (select 1 ";
      hql += "         from PricingAdjustmentOrganization o";
      hql += "        where active = true";
      hql += "          and o.priceAdjustment = p";
      hql += "          and o.organization.id ='" + orgId + "')) ";
      hql += "    ) ";
    }

    // Rules with currency can be only applied if the price list has the same currency
    try {
      // Hack: currency is defined in discounts module, check if it is present not to fail the query
      // other case
      PriceAdjustment.class.getField("PROPERTY_OBDISCCCURRENCY");
      hql += " and (oBDISCCCurrency is null or ";
      hql += "     oBDISCCCurrency.id = '" + priceList.getCurrency().getId() + "')";
    } catch (Exception e) {
      // ignore, the module column is not present: don't include it in the query
    }

    return hql;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    JSONObject today = new JSONObject();
    JSONObject value = new JSONObject();

    // Optional filtering by a list of m_offer_id
    // Used for this class and whom inherit this class
    if (jsonsent.has("parameters")
        && jsonsent.getJSONObject("parameters").has("filterPromotionList")
        && !jsonsent.getJSONObject("parameters").get("filterPromotionList").equals("undefined")
        && !jsonsent.getJSONObject("parameters").get("filterPromotionList").equals("null")) {
      final JSONArray filterPromotionList = jsonsent.getJSONObject("parameters")
          .getJSONArray("filterPromotionList");
      today.put("filterPromotionList", filterPromotionList);
    }

    value.put("type", "DATE");
    Calendar now = Calendar.getInstance();
    now.add(Calendar.DAY_OF_MONTH, -1);
    value.put("value", JsonUtils.createDateFormat().format(new Date(now.getTimeInMillis())));
    today.put("today", value);
    jsonsent.put("parameters", today);
    Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    // if it is a total refresh we need to ensure that all(AND) entities are active. In a
    // incremental refresh, we need to retrieve it if some (OR) ot the entities have changed
    jsonsent.put("operator", lastUpdated == null ? " AND " : " OR ");

    return prepareQuery(jsonsent);
  }

  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    Long lastUpdated = null;
    if (jsonsent != null) {
      lastUpdated = jsonsent.has("lastUpdated") && !jsonsent.get("lastUpdated").equals("undefined")
          && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    }
    boolean isMultiPriceList = false;
    try {
      isMultiPriceList = "Y".equals(Preferences.getPreferenceValue("OBPOS_EnableMultiPriceList",
          true, OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e1) {
      log.error("Error getting Preference: " + e1.getMessage(), e1);
    }

    Map<String, Object> args = new HashMap<>();
    args.put("isIncrementalRefresh", lastUpdated != null);
    args.put("isMultiPriceList", isMultiPriceList);
    args.put("priceListId",
        POSUtils.getPriceListByOrgId(OBContext.getOBContext().getCurrentOrganization().getId())
            .getId());

    HQLPropertyList discountHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions,
        args);
    return Arrays.asList("select" + discountHQLProperties.getHqlSelect()
        + getPromotionsHQL(jsonsent, true) + "order by priority, id");
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(PriceAdjustment.class);
  }
}
