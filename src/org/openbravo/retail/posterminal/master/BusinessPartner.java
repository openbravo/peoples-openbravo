/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BusinessPartner extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    return "select bp as BusinessPartner, loc as BusinessPartnerLocation "
        + "from BusinessPartner bp, BusinessPartnerLocation loc "
        + "where bp.id = loc.businessPartner.id and bp.customer = true and bp.$readableClientCriteria and bp.$naturalOrgCriteria";
  }
}
