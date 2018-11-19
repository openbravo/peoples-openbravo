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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ProductStock extends Product {
  public static final String productStockPropertyExtension = "OBPOS_ProductStockExtension";;

  @Inject
  @Any
  @Qualifier(productStockPropertyExtension)
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
      final Date terminalDate = OBMOBCUtils
          .calculateServerDate(
              jsonsent.getJSONObject("parameters").getString("terminalTime"),
              jsonsent.getJSONObject("parameters").getJSONObject("terminalTimeOffset")
                  .getLong("value"));
      String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
      final OBRETCOProductList productList = POSUtils.getProductListByPosterminalId(jsonsent
          .getString("pos"));
      final PriceListVersion priceListVersion = POSUtils.getPriceListVersionByOrgId(orgId,
          terminalDate);

      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("productListId", productList.getId());
      paramValues.put("orgId", orgId);
      paramValues.put("priceListVersionId", priceListVersion.getId());

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    return prepareQuery(jsonsent);

  }

  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {

    List<String> products = new ArrayList<String>();

    try {
      OBContext.setAdminMode(false);
      String regularProductHql = getRegularProductHql(false, false, jsonsent, false, false);
      HQLPropertyList regularProductStockHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);
      String hql = "SELECT "
          + regularProductStockHQLProperties.getHqlSelect() //
          + "FROM MaterialMgmtStorageDetail AS storagedetail " //
          + "JOIN storagedetail.storageBin AS locator " //
          + "WHERE EXISTS (" //
          + "  SELECT 1 " + regularProductHql
          + " AND pli.product.id = storagedetail.product.id) " //
          + "AND EXISTS ( " //
          + "  SELECT 1 " //
          + "  FROM OrganizationWarehouse ow " //
          + "  WHERE ow.organization.id = :orgId"
          + "  AND ow.warehouse.id = locator.warehouse.id) " //
          + "GROUP BY storagedetail.product.id, storagedetail.product.searchKey, locator.warehouse.id " //
          + "ORDER BY storagedetail.product.id, storagedetail.product.searchKey, locator.warehouse.id ";

      products.add(hql);

      return products;
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
