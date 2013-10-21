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
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Category extends ProcessHQLQuery {
  public static final String productCategoryPropertyExtension = "OBPOS_ProductCategoryExtension";

  @Inject
  @Any
  @Qualifier(productCategoryPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();

    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);

    final Date terminalDate = OBMOBCUtils.calculateServerDate(jsonsent.getJSONObject("parameters")
        .getString("terminalTime"),
        jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset").getLong("value"));

    final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
        terminalDate);
    if (productList == null) {
      throw new JSONException("Product list not found");
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    Calendar now = Calendar.getInstance();

    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularProductsCategoriesHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries.add("select"
        + regularProductsCategoriesHQLProperties.getHqlSelect() //
        + "from ProductCategory as pCat left outer join pCat.image as img  " + " where exists("
        + "from OBRETCO_Prol_Product pli, " + "PricingProductPrice ppp, "
        + "PricingPriceListVersion pplv "
        + "WHERE pCat=pli.product.productCategory and (pli.obretcoProductlist = '"
        + productList.getId() + "') " + "AND (pplv.id='" + priceListVersion.getId() + "') AND ("
        + "ppp.priceListVersion.id = pplv.id" + ") AND (" + "pli.product.id = ppp.product.id"
        + ") AND (" + "pli.product.active = true" + ") AND "
        + "(ppp.$incrementalUpdateCriteria) AND (pplv.$incrementalUpdateCriteria))"
        + "AND pCat.active = true order by pCat.name");

    // Discounts marked as category
    hqlQueries
        .add("select pt.id as id, pt.commercialName as searchKey, pt.commercialName as name, img.bindaryData as img, pt.commercialName as _identifier"
            + " from PromotionType as pt left outer join pt.obposImage img " //
            + "where pt.obposIsCategory = true "//
            + "  and pt.active = true "//
            + "  and pt.$readableClientCriteria" //
            + "  and (pt.$incrementalUpdateCriteria)"//
            + "  and exists (select 1"//
            + "                from PricingAdjustment p " //
            + "               where p.discountType.active = true " //
            + "                 and p.active = true"//
            + "                 and p.discountType = pt"//
            + "                 and (p.endingDate is null or p.endingDate >= TO_DATE('"
            + format.format(now.getTime())
            + "','yyyy/MM/dd'))" //
            + "                 and p.startingDate <= TO_DATE('"
            + format.format(now.getTime())
            + "', 'yyyy/MM/dd'))");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}