/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
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
    final org.openbravo.model.pricing.pricelist.PriceList pricesList = getPriceList(jsonsent);

    return "select pos.id as id, pos.store.obretcoCBpartner.id as businessPartner, pos.name as _identifier, pos.store.obretcoCBpLocation.id as partnerAddress, "
        + " pos.organization.id as organization, pos.organization.name as organization"
        + DalUtil.FIELDSEPARATOR
        + JsonConstants.IDENTIFIER
        + ", pos.client.id as client, pos.client.name as client"
        + DalUtil.FIELDSEPARATOR
        + JsonConstants.IDENTIFIER
        + ", pos.hardwareurl as hardwareurl, pos.scaleurl as scaleurl, "
        + "'"
        + pricesList.getId()
        + "' as priceList, '"
        + pricesList.getCurrency().getId()
        + "' as currency "
        + " from OBPOS_Applications AS pos where pos.$readableCriteria and searchKey = :terminal";
  }

  private org.openbravo.model.pricing.pricelist.PriceList getPriceList(JSONObject obj)
      throws JSONException {
    // {"parameters":{"terminal":{"value":"POS-1","type":"string"}}}
    final String searchKey = obj.getJSONObject("parameters").getJSONObject("terminal")
        .getString("value");
    final List<String> storeList = POSUtils.getStoreList(searchKey);

    for (String storeId : storeList) {
      final Organization org = OBDal.getInstance().get(Organization.class, storeId);
      if (org.getObretcoPricelist() != null) {
        return org.getObretcoPricelist();
      }
    }
    return null;
  }
}