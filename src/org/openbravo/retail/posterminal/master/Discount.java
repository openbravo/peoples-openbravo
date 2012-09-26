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

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    String priceListId = POSUtils.getPriceListByOrgId(jsonsent.getString("organization")).getId();

    String hql = "from PricingAdjustment p ";
    hql += "where active = true ";
    hql += "and (endingDate is null or endingDate>:today) ";

    hql += "and ((includePriceLists='Y' ";
    hql += "  and not exists (select 1 ";
    hql += "         from PricingAdjustmentPriceList pl";
    hql += "        where active = true";
    hql += "          and pl.priceAdjustment.id = p.id";
    hql += "          and pl.priceList.id ='" + priceListId + "')) ";
    hql += "   or (includePriceLists='N' ";
    hql += "  and  exists (select 1 ";
    hql += "         from PricingAdjustmentPriceList pl";
    hql += "        where active = true";
    hql += "          and pl.priceAdjustment.id = p.id";
    hql += "          and pl.priceList.id ='" + priceListId + "')) ";
    hql += "    ))";

    hql += "order by priority, id";

    JSONObject today = new JSONObject();
    JSONObject value = new JSONObject();
    value.put("type", "DATE");
    Calendar now = Calendar.getInstance();
    now.add(Calendar.DAY_OF_MONTH, -1);
    value.put("value", JsonUtils.createDateFormat().format(new Date(now.getTimeInMillis())));
    today.put("today", value);
    jsonsent.put("parameters", today);

    // TODO: prefilter by organization, price list
    return hql;
  }
}
