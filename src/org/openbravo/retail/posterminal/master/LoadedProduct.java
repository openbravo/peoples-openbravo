/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class LoadedProduct extends ProcessHQLQuery {
  public static final String loadedProductPropertyExtension = "OBPOS_LoadedProductExtension";
  public static final Logger log = Logger.getLogger(Product.class);

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

    HQLPropertyList regularProductsHQLProperties = ModelExtensionUtils.getPropertyExtensions(
        extensions, args);
    regularProductsHQLProperties.addAll(ProductProperties.getMainProductHQLProperties(args));
    String hql = "select" + regularProductsHQLProperties.getHqlSelect()
        + "FROM Product product where product.id=:productId";
    products.add(hql);
    return products;

  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    Map<String, Object> paramValues = new HashMap<String, Object>();
    paramValues.put("productId", jsonsent.getJSONObject("parameters").getJSONObject("productId")
        .getString("value"));
    return paramValues;
  }
}
