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
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
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

  @Inject
  @Any
  @Qualifier(productPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);

    final Date terminalDate = OBMOBCUtils.calculateServerDate(jsonsent.getJSONObject("parameters")
        .getString("terminalTime"),
        jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));

    final PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
        terminalDate);

    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    Calendar now = Calendar.getInstance();

    List<String> products = new ArrayList<String>();

    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    // regular products
    products
        .add("select"
            + regularProductsHQLProperties.getHqlSelect()
            + "FROM OBRETCO_Prol_Product as pli left outer join pli.product.image img inner join pli.product as product, "
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
        .add("select p.id as id, p.name as searchkey, p.name as _identifier, p.discountType.id as productCategory, p.obdiscPrice as listPrice, p.obdiscPrice as standardPrice, p.obdiscUpc as uPCEAN, img.bindaryData as img, '[[null]]' as generic_product_id, 'false' as showchdesc, 'true' as ispack, 'false' as isGeneric , 'false' as stocked"//
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

    // generic products
    products
        .add("select "
            + regularProductsHQLProperties.getHqlSelect()
            + "from Product product left outer join product.image img left join product.oBRETCOProlProductList as pli, PricingProductPrice ppp "
            + "where product = ppp.product and ppp.priceListVersion.id = '"
            + priceListVersion.getId()
            + "' and product.id in (select pli2.product.genericProduct.id as genericProduct "
            + "FROM OBRETCO_Prol_Product as pli2 left outer join pli2.product.image img, "
            + "PricingProductPrice ppp2, "
            + "PricingPriceListVersion pplv "
            + "WHERE (pli2.obretcoProductlist = '"
            + productList.getId()
            + "') "
            + "AND ("
            + "pplv.id='"
            + priceListVersion.getId()
            + "'"
            + ") AND ("
            + "ppp.priceListVersion.id = pplv.id"
            + ") AND ("
            + "pli2.product.id = ppp2.product.id"
            + ") AND ("
            + "pli2.product.active = true"
            + ") AND "
            + "((pli2.$incrementalUpdateCriteria) or (pli2.product.$incrementalUpdateCriteria) or (ppp.$incrementalUpdateCriteria) ) order by pli2.product.name)");

    return products;

  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
