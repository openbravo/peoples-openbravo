/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

public class ClearErrorActionHandler extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONArray errorIds = null;
    try {
      errorIds = new JSONArray(content);
      for (int i = 0; i < errorIds.length(); i++) {
        String errorId = errorIds.getString(i);
        OBPOSErrors error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
        OBDal.getInstance().remove(error);
      }
      JSONObject result = new JSONObject();
      result.put(
          "message",
          Utility.messageBD(new DalConnectionProvider(false), "Success", RequestContext.get()
              .getVariablesSecureApp().getLanguage()));
      return result;
    } catch (Exception e) {
      JSONObject result = new JSONObject();
      try {
        result.put(
            "message",
            Utility.messageBD(new DalConnectionProvider(false), "Error", RequestContext.get()
                .getVariablesSecureApp().getLanguage()));
      } catch (JSONException e1) {
        // won't happen
      }
      return result;
    }
  }

}
