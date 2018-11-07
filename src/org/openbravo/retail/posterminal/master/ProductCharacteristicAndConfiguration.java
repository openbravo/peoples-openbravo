/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

public class ProductCharacteristicAndConfiguration extends Product {
  public static final String productChAndConfExtension = "OBPOS_productChAndConfExtension";

  @Inject
  @Any
  @Qualifier(productChAndConfExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList regularProductStockHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    propertiesList.add(regularProductStockHQLProperties);

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      // TODO: get optional filter

      Map<String, Object> paramValues = new HashMap<String, Object>();

      // Optional filtering by a list of m_product_id
      if (jsonsent.getJSONObject("parameters").has("filterProductList")
          && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("undefined")
          && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("null")) {
        JSONArray filterProductList = jsonsent.getJSONObject("parameters").getJSONArray(
            "filterProductList");
        paramValues.put("filterProductList", filterProductList);
      }

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();

    try {
      OBContext.setAdminMode(false);

      HQLPropertyList regularproductChAndConfHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);
      String hql = "SELECT " + regularproductChAndConfHQLProperties.getHqlSelect() //
          + " FROM ProductCharacteristic AS pc, ProductCharacteristicConf AS pcc " //
          + " WHERE pc.id = pcc.characteristicOfProduct.id ";

      // TODO: get optional filter
      if (jsonsent.getJSONObject("parameters").has("filterProductList")
          && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("undefined")
          && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("null")) {
        hql += "AND pc.product.id in (:filterProductList) ";
      }

      hql += "ORDER BY pc.id, pcc.id ";

      hqlQueries.add(hql);

      return hqlQueries;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean mustHaveRemoteFilters() {
    return true;
  }

}
