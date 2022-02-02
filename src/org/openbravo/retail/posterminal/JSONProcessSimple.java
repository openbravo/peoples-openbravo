/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.service.json.JsonConstants;

public abstract class JSONProcessSimple
    extends org.openbravo.mobile.core.process.JSONProcessSimple {
  final String WEBPOS_FORM_ID = "B7B7675269CD4D44B628A2C6CF01244F";

  @Override
  protected String getFormId() {
    return WEBPOS_FORM_ID;
  }

  @Override
  protected JSONObject createSuccessResponse(JSONObject jsonSent, JSONObject result) {
    try {
      final JSONObject response = new JSONObject();
      if (jsonSent.has("messageId")) {
        response.put("messageId", jsonSent.getString("messageId"));
      }
      if (jsonSent.has("posTerminal")) {
        response.put("posTerminal", jsonSent.getString("posTerminal"));
      }
      if (!response.has(JsonConstants.RESPONSE_STATUS)) {
        response.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      }
      response.put(JsonConstants.RESPONSE_DATA, result);
      return response;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  protected boolean isErrorJson(JSONObject checkResult) {
    try {
      return checkResult.has(JsonConstants.RESPONSE_STATUS)
          && checkResult.get(JsonConstants.RESPONSE_STATUS)
              .equals(JsonConstants.RPCREQUEST_STATUS_FAILURE);
    } catch (JSONException e) {
      return true;
    }
  }
}
