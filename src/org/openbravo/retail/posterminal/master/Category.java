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
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Category extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(jsonsent
        .getString("organization"));

    final PriceList priceList = POSUtils.getPriceListByOrgId(jsonsent.getString("organization"));
    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    List<String> hqlQueries = new ArrayList<String>();

    // standard product categories
    hqlQueries
        .add("select pCat.id as id, pCat.searchKey as searchKey,pCat.name as name, pCat.name as _identifier, img.bindaryData as img  from ProductCategory as pCat left outer join pCat.image as img  "
            + " where exists("
            + "from OBRETCO_Prol_Product pli, "
            + "PricingProductPrice ppp, "
            + "PricingPriceListVersion pplv "
            + "WHERE pCat=pli.product.productCategory and (pli.obretcoProductlist = '"
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
            + "pli.product.id = ppp.product.id"
            + ") AND "
            + "(ppp.$incrementalUpdateCriteria) AND (pplv.$incrementalUpdateCriteria)) order by pCat.name");

    // Discounts marked as category
    hqlQueries
        .add("select pt.id as id, pt.commercialName as searchKey, pt.commercialName as name, pt.obposImage.bindaryData as img, pt.commercialName as _identifier"// TODO:
            + " from PromotionType pt " //
            + "where obposIsCategory = true "//
            + "  and active = true "//
            + "  and pt.$readableClientCriteria" //
            + "  and ($incrementalUpdateCriteria)"//
            + " and exists (select 1"//
            + "  from PricingAdjustment p " //
            + " where  p.discountType.active = true " //
            + "   and p.active = true"//
            + " and p.discountType = pt)"

        );

    return hqlQueries;
  }
}