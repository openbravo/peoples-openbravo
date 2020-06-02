/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface TicketPropertyMapping {

  /**
   * Returns businessPartner property from json if exists. Otherwise returns bp property.
   * 
   * @throws JSONException
   */
  default JSONObject getBusinessPartnerJson(JSONObject json) throws JSONException {
    return json.has("businessPartner") ? json.getJSONObject("businessPartner")
        : json.getJSONObject("bp");
  }
}
