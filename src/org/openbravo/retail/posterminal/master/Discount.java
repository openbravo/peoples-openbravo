/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Calendar;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.service.json.JsonUtils;

public class Discount extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  protected String getPromotionsHQL(JSONObject jsonsent) throws JSONException {
    String orgId = jsonsent.getString("organization");
    String priceListId = POSUtils.getPriceListByOrgId(orgId).getId();

    String hql = "from PricingAdjustment p ";
    hql += "where active = true ";
    hql += "and client.id = '" + jsonsent.getString("client") + "' ";
    hql += "and (endingDate is null or endingDate>:today) ";

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

    // organization
    hql += "and ((includedOrganizations='Y' ";
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

    return hql;
  }

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    JSONObject today = new JSONObject();
    JSONObject value = new JSONObject();
    value.put("type", "DATE");
    Calendar now = Calendar.getInstance();
    now.add(Calendar.DAY_OF_MONTH, -1);
    value.put("value", JsonUtils.createDateFormat().format(new Date(now.getTimeInMillis())));
    today.put("today", value);
    jsonsent.put("parameters", today);

    return prepareQuery(jsonsent);
  }

  protected String prepareQuery(JSONObject jsonsent) throws JSONException {
    String hql = getPromotionsHQL(jsonsent);
    hql += "order by priority, id";
    return hql;
  }
}
