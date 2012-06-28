/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.config.org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Category extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    String terminalSearchKey = (String) jsonsent.get("terminal");
    List<String> lstOrganizations = POSUtils.getStoreList(terminalSearchKey);

    OBRETCOProductList productList = POSUtils
        .getProductListFromRetailOrganizations(lstOrganizations);

    if (productList != null) {
      return "select distinct pli.product.productCategory.id as id, pli.product.productCategory.searchKey as searchKey,pli.product.productCategory.name as name, pli.product.productCategory.name as _identifier, img.bindaryData as img "
          + "from OBRETCO_Prol_Product pli left outer join pli.product.productCategory.image img "
          + "where pli.obretcoProductlist = '" + productList.getId() + "'";
    } else {
      throw new JSONException("Product list not found");
    }
  }
}