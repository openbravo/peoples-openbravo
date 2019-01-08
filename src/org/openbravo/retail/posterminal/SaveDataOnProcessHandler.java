/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

@ApplicationScoped
public class SaveDataOnProcessHandler extends BaseActionHandler {
  private static final Logger log = LogManager.getLogger();

  @Override
  public JSONObject execute(Map<String, Object> parameters, String content) {
    JSONArray errorIds = null;
    try {
      errorIds = new JSONArray(content);
      JSONObject result = new JSONObject();
      for (int i = 0; i < errorIds.length(); i++) {
        String errorId = errorIds.getString(i);
        OBPOSErrors error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
        try {
          if (error.isProcessNow()) {
            result.put("title", "Error");
            result.put("message", Utility.messageBD(new DalConnectionProvider(false),
                "OtherProcessActive", RequestContext.get().getVariablesSecureApp().getLanguage()));
            return result;
          } else {
            error.setProcessNow(true);
            result.put("title", "Success");
          }
        } catch (JSONException e) {
          // won't happen
        }
      }
      return result;
    } catch (Exception e) {
      // won't happen
      log.error("Error while Saving the record", e);
      JSONObject result = new JSONObject();
      try {
        result.put("title", "Error");
        result.put("message", Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_ErrorWhileSaving", RequestContext.get().getVariablesSecureApp().getLanguage()));
      } catch (JSONException je) {
        // won't happen
      }
      return result;
    }
  }
}
