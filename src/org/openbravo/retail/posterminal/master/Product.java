/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Product extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);

    final PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId);

    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    Calendar now = Calendar.getInstance();

    List<String> products = new ArrayList<String>();

    // regular products
    products
        .add("select pli.product.id as id, pli.product.searchKey as searchkey, pli.product.name as _identifier, pli.product.taxCategory.id as taxCategory, "
            + "pli.product.productCategory.id as productCategory, pli.product.obposScale as obposScale, pli.product.uOM.id as uOM, pli.product.uOM.symbol as uOMsymbol, pli.product.uPCEAN as uPCEAN, img.bindaryData as img "
            + ", pli.product.description as description "
            + ", pli.product.obposGroupedproduct as groupProduct "
            + ", pli.product.stocked as stocked "
            + ", pli.product.obposShowstock as showstock "
            + ", pli.bestseller as bestseller "
            + ", 'false' as ispack, "
            + "ppp.listPrice as listPrice, ppp.standardPrice as standardPrice, ppp.priceLimit as priceLimit, "
            + "ppp.cost as cost "
            + "FROM OBRETCO_Prol_Product as pli left outer join pli.product.image img, "
            + "PricingProductPrice ppp, "
            + "PricingPriceListVersion pplv "
            + "WHERE (pli.obretcoProductlist = '"
            + productList.getId()
            + "') "
            + "AND ("
            + "pplv.id='"
            + priceListVersion.getId()
            + "'"
            + ") AND ("
            + "ppp.priceListVersion.id = pplv.id"
            + ") AND ("
            + "pli.product.id = ppp.product.id"
            + ") AND ("
            + "pli.product.active = true"
            + ") AND "
            + "((pli.$incrementalUpdateCriteria) or (pli.product.$incrementalUpdateCriteria) or (ppp.$incrementalUpdateCriteria) ) order by pli.product.name");

    // discounts which type is defined as category
    products
        .add("select p.id as id, p.name as searchkey, p.name as _identifier, p.discountType.id as productCategory, p.obdiscPrice as listPrice, p.obdiscPrice as standardPrice, p.obdiscUpc as uPCEAN, img.bindaryData as img, 'true' as ispack, 'false' as stocked"//
            + "  from PricingAdjustment as p left outer join p.obdiscImage img" //
            + " where p.discountType.obposIsCategory = true "//
            + "   and p.discountType.active = true " //
            + "   and p.active = true"//
            + "   and p.$readableClientCriteria"//
            + "   and (p.endingDate is null or p.endingDate >= '"
            + format.format(now.getTime())
            + "')" //
            + "   and p.startingDate <= '"
            + format.format(now.getTime())
            + "'"
            + "   and (p.$incrementalUpdateCriteria) "//
        );

    return products;

  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
