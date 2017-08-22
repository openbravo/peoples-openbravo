/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.service.json.JsonUtils;

public class Discount extends ProcessHQLQuery {

  public static final Logger log = Logger.getLogger(Discount.class);

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  protected String getPromotionsHQL(JSONObject jsonsent) throws JSONException {
    return getPromotionsHQL(jsonsent, false);
  }

  protected String getPromotionsHQL(JSONObject jsonsent, boolean addIncrementalUpdateFilter)
      throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();

    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
    PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    String priceListId = priceList.getId();

    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    Long lastUpdated;

    if (jsonsent != null) {
      lastUpdated = jsonsent.has("lastUpdated") && !jsonsent.get("lastUpdated").equals("undefined")
          && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;
    } else {
      lastUpdated = null;
    }

    String hql = "from PricingAdjustment p ";
    hql += "where client.id = '" + OBContext.getOBContext().getCurrentClient().getId() + "' ";
    boolean multiPrices = false;
    try {
      multiPrices = "Y".equals(Preferences.getPreferenceValue("OBPOS_EnableMultiPriceList", true,
          OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null));
    } catch (PropertyException e1) {
      log.error("Error getting Preference: " + e1.getMessage(), e1);
    }

    if (lastUpdated != null) {
      hql += ""; // Incremental Refresh
    } else {
      hql += "AND ((p.$incrementalUpdateCriteria)) "; // Full Refresh
    }

    if (!multiPrices) {
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
    hql += "      where pap.active = true and pap.priceAdjustment = p ";
    hql += "      and pap.product.id = ppl.product.id ";
    hql += "      and ppl.active = true and pap.product.active = true ";
    hql += "      and ((ppl.$incrementalUpdateCriteria) ";
    hql += "      or (pap.product.$incrementalUpdateCriteria)) ";
    hql += "      and ppl.obretcoProductlist.id ='" + productList.getId() + "'))) ";
    hql += "  ) ";

    // organization
    hql += " and p.$naturalOrgCriteria";
    hql += " and ((includedOrganizations='Y' ";
    hql += "  and not exists (select 1 ";
    hql += "         from PricingAdjustmentOrganization o";
    hql += "        where active = true";
    hql += "          and o.priceAdjustment = p";
    hql += "          and o.organization.id ='" + orgId + "')) ";
    hql += "   or (includedOrganizations='N' ";
    hql += "  and  exists (select 1 ";
    hql += "         from PricingAdjustmentOrganization o";
    hql += "        where active = true";
    hql += "          and o.priceAdjustment = p";
    hql += "          and o.organization.id ='" + orgId + "')) ";
    hql += "    ) ";

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
    String hql = getPromotionsHQL(jsonsent, true);
    hql += "order by priority, id";

    return Arrays.asList(new String[] { hql });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
