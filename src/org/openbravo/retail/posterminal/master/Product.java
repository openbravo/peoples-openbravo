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
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Product extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    final OBPOSApplications pos = POSUtils.getTerminalById(jsonsent.getString("terminal"));
    final List<String> lstOrganizations = POSUtils.getStoreListByTerminalId(pos.getId());
    final OBRETCOProductList productList = POSUtils
        .getProductListFromRetailOrganizations(lstOrganizations);

    if (productList != null) {
      return "select pli.product.id as id, pli.product.name as _identifier, pli.product.taxCategory.id as taxCategory, pli.product.productCategory.id as productCategory, pli.product.obposScale as obposScale, pli.product.uOM.id as uOM, pli.product.uPCEAN as uPCEAN, img.bindaryData as img "
          + "from OBRETCO_Prol_Product pli left outer join pli.product.image img "
          + "where pli.obretcoProductlist = '" + productList.getId() + "'";
    } else {
      throw new JSONException("Product list not found");
    }
  }
}
