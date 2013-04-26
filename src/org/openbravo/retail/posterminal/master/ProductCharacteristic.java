/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.process.ProcessHQLQuery;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;

public class ProductCharacteristic extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
    List<String> hqlQueries = new ArrayList<String>();

    // standard product categories
    hqlQueries
        .add("select product_ch.id as m_product_ch_id, product_ch.product.id as m_product, product_ch.characteristic.id as characteristic_id, "
            + "product_ch.characteristic.name as characteristic, product_ch.characteristicValue.id as ch_value_id, "
            + "product_ch.characteristicValue.name as ch_value, product_ch.characteristic.name as _identifier "
            + "from ProductCharacteristicValue product_ch "
            + "where product_ch.product.id in (select product.id from OBRETCO_Prol_Product assort where obretcoProductlist.id= '"
            + productList.getId()
            + "') "
            + "and $naturalOrgCriteria and $readableClientCriteria and ($incrementalUpdateCriteria)");

    return hqlQueries;
  }
}