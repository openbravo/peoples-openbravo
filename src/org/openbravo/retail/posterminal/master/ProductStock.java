/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.Collections;
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
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class ProductStock extends ProcessHQLQuery {
  public static final String productStockPropertyExtension = "OBPOS_ProductStockExtension";;

  @Inject
  @Any
  @Qualifier(productStockPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    return Collections.singletonList(ModelExtensionUtils.getPropertyExtensions(extensions));
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    return Product.createRegularProductValues(jsonsent);

  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return prepareQuery(jsonsent);
  }

  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {

    try {
      OBContext.setAdminMode(false);
      String regularProductHql = Product.createSimplifiedProductHql(jsonsent);
      HQLPropertyList regularProductStockHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);
      return Collections.singletonList("SELECT " + regularProductStockHQLProperties.getHqlSelect() //
          + "FROM MaterialMgmtStorageDetail AS storagedetail " //
          + "JOIN storagedetail.storageBin AS locator " //
          + "WHERE EXISTS (" //
          + "  SELECT 1 " + regularProductHql + " AND pli.product.id = storagedetail.product.id) " //
          + "AND EXISTS ( " //
          + "  SELECT 1 " //
          + "  FROM OrganizationWarehouse ow " //
          + "  WHERE ow.organization.id = :orgId" + "  AND ow.warehouse.id = locator.warehouse.id) " //
          + "GROUP BY storagedetail.product.id, storagedetail.product.searchKey, locator.warehouse.id " //
          + "ORDER BY storagedetail.product.id, storagedetail.product.searchKey, locator.warehouse.id ");
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
