/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class ProductPrice extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> prices = new ArrayList<String>();
    prices
        .add("select pricingProductPrice.id as id, pricingProductPrice.priceListVersion.id as priceListVersion, pricingProductPrice.product.id as product, "
            + " pricingProductPrice.listPrice as listPrice, pricingProductPrice.standardPrice as standardPrice, pricingProductPrice.priceLimit as priceLimit, "
            + "pricingProductPrice.cost as cost, pricingProductPrice.product.name||' - '||pricingProductPrice.priceListVersion.priceList.name as _identifier"
            + " from PricingProductPrice as pricingProductPrice where "
            + "(pricingProductPrice.$incrementalUpdateCriteria) AND priceListVersion in "
            + "(select plv.id from PricingPriceList as ppl, PricingPriceListVersion as plv "
            + "where ppl.id = '"
            + POSUtils.getPriceListByOrgId(jsonsent.getString("organization")).getId()
            + "' and ppl.id = plv.priceList.id  and "
            + "plv.validFromDate = (select max(pplv.validFromDate) from PricingPriceListVersion as pplv where pplv.priceList.id = ppl.id))");
    return prices;
  }
}
