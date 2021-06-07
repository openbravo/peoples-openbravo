/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;

public class DeliveryModes extends QueryTerminalProperty {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    final StringBuilder hqlDeliveryModes = new StringBuilder();
    hqlDeliveryModes.append("SELECT list.searchKey AS id, ");
    hqlDeliveryModes.append("COALESCE((SELECT trl.name ");
    hqlDeliveryModes.append("FROM list.aDListTrlList AS trl ");
    hqlDeliveryModes.append("WHERE trl.language = '");
    hqlDeliveryModes.append(OBContext.getOBContext().getLanguage().getLanguage());
    hqlDeliveryModes.append("'), list.name) AS name ");
    hqlDeliveryModes.append("FROM ADList AS list ");
    hqlDeliveryModes.append("WHERE list.reference.id = '41D44C94D0AC41DEBA9A2BEB0EAF059C' ");
    hqlDeliveryModes.append("AND list.$readableSimpleCriteria ");
    hqlDeliveryModes.append("AND list.$activeCriteria ");
    hqlDeliveryModes.append("ORDER BY list.sequenceNumber");

    return Arrays.asList(hqlDeliveryModes.toString());
  }

  @Override
  public String getProperty() {
    return "deliveryModes";
  }

  @Override
  public boolean returnList() {
    return false;
  }

}
