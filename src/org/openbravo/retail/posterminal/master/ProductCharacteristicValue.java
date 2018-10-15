/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
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

import org.codehaus.jettison.json.JSONArray;
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

public class ProductCharacteristicValue extends ProcessHQLQuery {
  public static final String productCharacteristicValuePropertyExtension = "OBPOS_ProductCharacteristicValueExtension";

  @Inject
  @Any
  @Qualifier(productCharacteristicValuePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Product Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    Map<String, Object> args = new HashMap<String, Object>();
    HQLPropertyList productcharacteristicsHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions, args);
    propertiesList.add(productcharacteristicsHQLProperties);

    return propertiesList;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final OBRETCOProductList productList = POSUtils.getProductListByPosterminalId(jsonsent
          .getString("pos"));

      final Date terminalDate = OBMOBCUtils
          .calculateServerDate(
              jsonsent.getJSONObject("parameters").getString("terminalTime"),
              jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset")
                  .getLong("value"));

      final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
          terminalDate);
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("productListId", productList.getId());
      paramValues.put("priceListVersionId", priceListVersion.getId());

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
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();
    String hqlQuery = "";

    Long lastUpdated = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? jsonsent.getLong("lastUpdated") : null;

    HQLPropertyList regularProductsCharacteristicHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQuery = "select "
        + regularProductsCharacteristicHQLProperties.getHqlSelect()
        + "from ProductCharacteristicValue pcv "
        + "inner join pcv.characteristic characteristic "
        + "inner join pcv.characteristicValue characteristicValue "
        + "inner join pcv.product product "
        + "inner join product.oBRETCOProlProductList opp "
        + "inner join product.pricingProductPriceList ppp "
        + "where opp.obretcoProductlist.id= :productListId "
        + "and ppp.priceListVersion.id= :priceListVersionId "
        + "and characteristic.obposUseonwebpos = true "
        + "and pcv.$filtersCriteria AND pcv.$hqlCriteria "
        + "and pcv.$naturalOrgCriteria and pcv.$readableSimpleClientCriteria "
        + ((lastUpdated != null) ? "and (opp.$incrementalUpdateCriteria OR ppp.$incrementalUpdateCriteria OR "
            + "pcv.$incrementalUpdateCriteria OR characteristic.$incrementalUpdateCriteria OR "
            + "characteristicValue.$incrementalUpdateCriteria) "
            : "and (opp.$incrementalUpdateCriteria AND ppp.$incrementalUpdateCriteria AND "
                + "pcv.$incrementalUpdateCriteria AND characteristic.$incrementalUpdateCriteria AND "
                + "characteristicValue.$incrementalUpdateCriteria) ")
        + "and characteristic.active = 'Y' ";

    // Optional filtering by a list of m_product_id
    if (jsonsent.getJSONObject("parameters").has("filterProductList")
        && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("undefined")
        && !jsonsent.getJSONObject("parameters").get("filterProductList").equals("null")) {
      hqlQuery += "and pcv.product.id in (:filterProductList) ";
    }

    hqlQuery += "order by pcv.id";

    hqlQueries.add(hqlQuery);
    return hqlQueries;
  }
}
