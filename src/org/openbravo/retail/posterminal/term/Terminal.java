/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.service.json.JsonConstants;

public class Terminal extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    final org.openbravo.model.pricing.pricelist.PriceList pricesList = POSUtils
        .getPriceListByTerminal(jsonsent.getJSONObject("parameters").getJSONObject("terminal")
            .getString("value"));

    return "select pos.id as id, pos.store.obretcoCBpartner.id as businessPartner, pos.name as _identifier, pos.searchKey as searchKey, pos.store.obretcoCBpLocation.id as partnerAddress, "
        + " pos.organization.id as organization, pos.organization.name as "
        + getIdentifierAlias("organization")
        + ", pos.client.id as client, pos.client.name as "
        + getIdentifierAlias("client")
        + ", pos.hardwareurl as hardwareurl, pos.scaleurl as scaleurl, "
        + "'"
        + pricesList.getId()
        + "' as priceList, '"
        + pricesList.getCurrency().getId()
        + "' as currency, "
        + "'"
        + pricesList.getCurrency().getIdentifier()
        + "' as "
        + getIdentifierAlias("currency")
        + ", "
        + "pos.store.id as store, "
        + "pos.store.name as "
        + getIdentifierAlias("store")
        + ", pos.documentType.id as documentType, pos.documentType.name as "
        + getIdentifierAlias("documentType")
        + " from OBPOS_Applications AS pos where pos.$readableCriteria and searchKey = :terminal";
  }

  private String getIdentifierAlias(String propertyName) {
    return propertyName + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER;
  }
}
