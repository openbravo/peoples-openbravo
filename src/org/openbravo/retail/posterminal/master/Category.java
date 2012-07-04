/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Category extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {

    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(jsonsent
        .getString("organization"));

    final PriceList priceList = POSUtils.getPriceListByOrgId(jsonsent.getString("organization"));

    if (productList != null) {
      return "select distinct pli.product.productCategory.id as id, pli.product.productCategory.searchKey as searchKey,pli.product.productCategory.name as name, pli.product.productCategory.name as _identifier, img.bindaryData as img "
          + "from OBRETCO_Prol_Product pli left outer join pli.product.productCategory.image img, "
          + "PricingProductPrice ppp, "
          + "PricingPriceListVersion pplv "
          + "WHERE (pli.obretcoProductlist = '"
          + productList.getId()
          + "') "
          + "AND ("
          + "pplv.priceList.id = '"
          + priceList.getId()
          + "' AND "
          + "pplv.validFromDate = (select max(a.validFromDate) "
          + "  FROM PricingPriceListVersion a "
          + "  WHERE a.priceList.id = '"
          + priceList.getId()
          + "')"
          + ") AND ("
          + "ppp.priceListVersion.id = pplv.id"
          + ") AND ("
          + "pli.product.id = ppp.product.id" + ")";
    } else {
      throw new JSONException("Product list not found");
    }
  }
}