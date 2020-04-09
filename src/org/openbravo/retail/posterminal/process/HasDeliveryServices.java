/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.service.json.JsonConstants;

public class HasDeliveryServices extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws ServletException, JSONException {

    final JSONObject result = new JSONObject();

    final JSONArray resultLines = new JSONArray();
    final HasServices hasServicesProcess = WeldUtils
        .getInstanceFromStaticBeanManager(HasServices.class);

    final JSONArray lines = jsonsent.getJSONArray("lines");

    JSONObject args;
    JSONObject params;
    JSONObject line;
    for (int i = 0; i < lines.length(); i++) {
      // build args for HasServices
      args = new JSONObject();
      params = new JSONObject();
      params.put("terminalTime", jsonsent.getString("terminalTime"));
      params.put("terminalTimeOffset", jsonsent.getString("terminalTimeOffset"));
      args.put("parameters", params);
      args.put("pos", jsonsent.getString("pos"));
      args.put("remoteFilters", jsonsent.getJSONArray("remoteFilters"));
      line = lines.getJSONObject(i);
      args.put("product", line.getString("product"));
      args.put("productCategory", line.getString("productCategory"));
      final JSONObject response = hasServicesProcess.exec(args);
      final JSONObject resultLine = parseResponse(response, line);
      if (resultLine != null) {
        resultLines.put(resultLine);
      }
    }

    result.put(JsonConstants.RESPONSE_DATA, resultLines);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return result;
  }

  private JSONObject parseResponse(JSONObject response, JSONObject line) throws JSONException {
    if (response == null || !response.has(JsonConstants.RESPONSE_DATA)) {
      return null;
    }
    JSONObject result = new JSONObject();
    result.put("lineId", line.getString("lineId"));
    result.put("hasDeliveryServices", response.getJSONObject("data").getBoolean("hasservices"));
    return result;
  }

}
