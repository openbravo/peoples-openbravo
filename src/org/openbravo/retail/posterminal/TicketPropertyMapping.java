/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface TicketPropertyMapping {

  /**
   * Returns businessPartner property from json if exists. Otherwise returns bp property.
   * 
   * @throws JSONException
   */
  default JSONObject getBusinessPartner(JSONObject json) throws JSONException {
    return json.optJSONObject("businessPartner") != null ? json.getJSONObject("businessPartner")
        : json.getJSONObject("bp");
  }

  /**
   * Returns grossListPrice property from json.
   * 
   * @throws JSONException
   */
  default BigDecimal getGrossListPrice(JSONObject json) throws JSONException {
    return BigDecimal.valueOf(json.optDouble("grossListPrice", 0));
  }

  /**
   * Returns netListPrice property from json if exists. Otherwise returns listPrice property.
   * 
   * @throws JSONException
   */
  default BigDecimal getNetListPrice(JSONObject json) throws JSONException {
    return BigDecimal.valueOf(
        json.has("netListPrice") ? json.getDouble("netListPrice") : json.optDouble("listPrice", 0));
  }

  /**
   * Returns baseGrossUnitPrice property from json if exists. Otherwise returns price property.
   * 
   * @throws JSONException
   */
  default BigDecimal getBaseGrossUnitPrice(JSONObject json) throws JSONException {
    return BigDecimal.valueOf(json.has("baseGrossUnitPrice") ? json.getDouble("baseGrossUnitPrice")
        : json.optDouble("price", 0));
  }

  /**
   * Returns baseNetUnitPrice property from json if exists. Otherwise returns standardPrice
   * property.
   * 
   * @throws JSONException
   */
  default BigDecimal getBaseNetUnitPrice(JSONObject json) throws JSONException {
    return BigDecimal.valueOf(json.has("baseNetUnitPrice") ? json.getDouble("baseNetUnitPrice")
        : json.optDouble("standardPrice", 0));
  }

  /**
   * Returns getGrossUnitAmount property from json.
   * 
   * @throws JSONException
   */
  default BigDecimal getGrossUnitPrice(JSONObject json) throws JSONException {
    return BigDecimal.valueOf(json.optDouble("grossUnitPrice", 0));
  }

  /**
   * Returns netUnitAmount property from json if exists. Otherwise returns unitPrice property if
   * exists. Otherwise returns pricenet property.
   * 
   * @throws JSONException
   */
  default BigDecimal getNetUnitPrice(JSONObject json) throws JSONException {
    if (json.has("netUnitPrice")) {
      return BigDecimal.valueOf(json.getDouble("netUnitPrice"));
    }
    return BigDecimal.valueOf(
        json.has("unitPrice") ? json.getDouble("unitPrice") : json.optDouble("pricenet", 0));
  }

  /**
   * Returns getGrossUnitAmount property from json if exists. Otherwise returns lineGrossAmount
   * property.
   * 
   * @throws JSONException
   */
  default BigDecimal getGrossUnitAmount(JSONObject json) throws JSONException {
    return BigDecimal.valueOf(json.has("grossUnitAmount") ? json.getDouble("grossUnitAmount")
        : json.getDouble("lineGrossAmount"));
  }

  /**
   * Returns netUnitAmount property from json if exists. Otherwise returns net property.
   * 
   * @throws JSONException
   */
  default BigDecimal getNetUnitAmount(JSONObject json) throws JSONException {
    return BigDecimal.valueOf(
        json.has("netUnitAmount") ? json.getDouble("netUnitAmount") : json.getDouble("net"));
  }

  /**
   * Returns grossAmount property from json if exists. Otherwise returns gross property.
   * 
   * @throws JSONException
   */
  default BigDecimal getGrossAmount(JSONObject json) throws JSONException {
    return BigDecimal
        .valueOf(json.has("grossAmount") ? json.getDouble("grossAmount") : json.getDouble("gross"));
  }

  /**
   * Returns netAmount property from json if exists. Otherwise returns net property.
   * 
   * @throws JSONException
   */
  default BigDecimal getNetAmount(JSONObject json) throws JSONException {
    return BigDecimal
        .valueOf(json.has("netAmount") ? json.getDouble("netAmount") : json.getDouble("net"));
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
