/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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
import org.openbravo.mobile.core.process.ProcessHQLQuery;

public class ProductCharacteristic extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();

    // standard product categories
    hqlQueries
        .add("select product_ch.id as m_product_ch_id, product_ch.product.id as m_product, product_ch.characteristic.id as characteristic_id, "
            + "product_ch.characteristic.name as characteristic, product_ch.characteristicValue.id as ch_value_id, "
            + "product_ch.characteristicValue.name as ch_value, product_ch.characteristic.name as _identifier "
            + "from ProductCharacteristicValue product_ch ");
    // + "where "
    // +
    // " exists (select 1 from BusinessPartner bp where user.businessPartner = bp AND bp.isSalesRepresentative = true) "
    // +
    // "and (user.$incrementalUpdateCriteria) AND (user.$incrementalUpdateCriteria) AND $readableCriteria order by user.name asc");

    return hqlQueries;
  }
}