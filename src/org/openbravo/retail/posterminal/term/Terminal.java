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
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.service.json.JsonConstants;

public class Terminal extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    return "select pos.id as id, pos.store.obretcoCBpartner.id as businessPartner, pos.name as _identifier, pos.store.obretcoCBpLocation.id as partnerAddress, "
        + " pos.organization.id as organization, pos.organization.name as organization"
        + DalUtil.FIELDSEPARATOR
        + JsonConstants.IDENTIFIER
        + ", pos.client.id as client, pos.client.name as client"
        + DalUtil.FIELDSEPARATOR
        + JsonConstants.IDENTIFIER
        + ", pos.hardwareurl as hardwareurl, pos.scaleurl as scaleurl "
        + " from OBPOS_Applications AS pos where pos.$readableCriteria and searchKey = :terminal";
  }
}