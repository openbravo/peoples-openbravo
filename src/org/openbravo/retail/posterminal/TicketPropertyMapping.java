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
  default JSONObject getBusinessPartner(JSONObject json) throws JSONException {
    return json.has("businessPartner") ? json.getJSONObject("businessPartner")
        : json.getJSONObject("bp");
  }

  /**
   * Returns grossAmount property from json if exists. Otherwise returns gross property.
   * 
   * @throws JSONException
   */
  default double getGrossAmount(JSONObject json) throws JSONException {
    return json.has("grossAmount") ? json.getDouble("grossAmount") : json.getDouble("gross");
  }

  /**
   * Returns netAmount property from json if exists. Otherwise returns net property.
   * 
   * @throws JSONException
   */
  default double getNetAmount(JSONObject json) throws JSONException {
    return json.has("netAmount") ? json.getDouble("netAmount") : json.getDouble("net");
  }

  /**
   * Returns getGrossUnitAmount property from json if exists. Otherwise returns lineGrossAmount
   * property.
   * 
   * @throws JSONException
   */
  default double getGrossUnitAmount(JSONObject json) throws JSONException {
    return json.has("grossUnitAmount") ? json.getDouble("grossUnitAmount")
        : json.getDouble("lineGrossAmount");
  }

  /**
   * Returns netUnitAmount property from json if exists. Otherwise returns net property.
   * 
   * @throws JSONException
   */
  default double getNetUnitAmount(JSONObject json) throws JSONException {
    return json.has("netUnitAmount") ? json.getDouble("netUnitAmount") : json.getDouble("net");
  }

  /**
   * Returns getGrossUnitAmount property from json.
   * 
   * @throws JSONException
   */
  default double getGrossUnitPrice(JSONObject json) throws JSONException {
    return json.getDouble("grossUnitPrice");
  }

  /**
   * Returns netUnitAmount property from json if exists. Otherwise returns unitPrice property if
   * exists. Otherwise returns pricenet property.
   * 
   * @throws JSONException
   */
  default double getNetUnitPrice(JSONObject json) throws JSONException {
    if (json.has("netUnitPrice")) {
      return json.getDouble("netUnitPrice");
    }
    return json.has("unitPrice") ? json.getDouble("unitPrice") : json.getDouble("pricenet");
  }

  /**
   * Returns taxes property from json if exists. Otherwise returns taxLines property.
   * 
   * @throws JSONException
   */
  default JSONObject getTaxes(JSONObject json) throws JSONException {
    return json.has("taxes") ? json.getJSONObject("taxes") : json.getJSONObject("taxLines");
  }

}
