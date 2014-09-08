/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class DiscountFilterCharacteristic extends Discount {

  @Override
  protected List<String> prepareQuery(JSONObject jsonsent) throws JSONException {
    String hql = "select c.id || cvl.characteristicValue.id as id, c.active, c.creationDate, c.createdBy,  ";
    hql += " c.updated, c.updatedBy, c.client.id as client, c.organization.id as organization, c.characteristic.id as characteristic, cvl.characteristicValue.id as chValue, c.offer.id as offer  ";

    hql += " from PricingAdjustmentCharacteristic c  ";
    hql += "   left join c.characteristic.productCharacteristicValueList cvl ";
    hql += " where c.active = true ";
    hql += "   and m_isparent_ch_value(cvl.characteristicValue.id, c.chValue.id, c.characteristic.id) != -1 ";
    hql += "   and exists (select 1 " + getPromotionsHQL(jsonsent);
    hql += "                and c.offer = p)";

    return Arrays.asList(new String[] { hql });
  }
}
