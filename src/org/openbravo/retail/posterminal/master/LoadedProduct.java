/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class LoadedProduct extends ProcessHQLQuery {
  public static final String loadedProductPropertyExtension = "OBPOS_LoadedProductExtension";
  public static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  @Qualifier(loadedProductPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> products = new ArrayList<String>();
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("multiPriceList", false);
    try {
      args.put("terminalId", jsonsent.getString("pos"));
    } catch (JSONException e) {
      log.error("Error while getting terminalId " + e.getMessage(), e);
    }

    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    regularProductsHQLProperties.addAll(ProductProperties.getMainProductHQLProperties(args));
    regularProductsHQLProperties.add(
        new HQLProperty("coalesce(ppp.standardPrice, ollist.standardPrice)", "standardPrice", 10));
    regularProductsHQLProperties
        .add(new HQLProperty("coalesce(ppp.listPrice, ollist.listPrice)", "listPrice", 10));
    final String hql = "select" + regularProductsHQLProperties.getHqlSelect()
        + "FROM Product product left outer join product.uOM uom "
        + "left outer join product.pricingProductPriceList ppp with ppp.priceListVersion.id=:priceListVersionId "
        + "left outer join product.orderLineList ollist with ollist.id=:salesOrderLineId "
        + "WHERE product.id=:productId ";
    products.add(hql);
    return products;

  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    Map<String, Object> paramValues = new HashMap<String, Object>();
    final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(
        OBContext.getOBContext().getCurrentOrganization().getId(), new Date());
    paramValues.put("productId", jsonsent.getString("productId"));
    paramValues.put("orgId", OBContext.getOBContext().getCurrentOrganization().getId());
    paramValues.put("priceListVersionId", priceListVersion.getId());
    paramValues.put("salesOrderLineId",
        jsonsent.has("salesOrderLineId") ? jsonsent.getString("salesOrderLineId") : "");
    return paramValues;
  }
}
