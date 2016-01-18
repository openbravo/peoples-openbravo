/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

@ApplicationScoped
public class SaveDataActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(SaveDataActionHandler.class);

  @Inject
  @Any
  private Instance<POSDataSynchronizationProcess> syncProcesses;

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONArray errorIds = null;
    List<OBPOSErrors> errors = new ArrayList<OBPOSErrors>();
    boolean errorb = false;
    try {
      errorIds = new JSONArray(content);
      for (int i = 0; i < errorIds.length(); i++) {
        String errorId = errorIds.getString(i);
        OBPOSErrors error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
        errors.add(error);
      }

      Collections.sort(errors, new ErrorComparator());

      for (OBPOSErrors error : errors) {
        String errorId = error.getId();
        String type = error.getTypeofdata();

        POSDataSynchronizationProcess syncProcess = null;
        syncProcess = syncProcesses.select(new DataSynchronizationProcess.Selector(type)).get();
        JSONObject record = new JSONObject(error.getJsoninfo());
        record.put("posErrorId", errorId);
        JSONObject data = new JSONObject();
        data.put("data", record);
        JSONObject result = syncProcess.exec(data, true);
        if (result.get(JsonConstants.RESPONSE_STATUS).equals(
            JsonConstants.RPCREQUEST_STATUS_FAILURE)) {
          errorb = true;
          // The process may have changed the error information, we need to commit and close the
          // transaction
          OBDal.getInstance().commitAndClose();
        } else {
          OBContext.setAdminMode(true);
          try {
            error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
            error.setOrderstatus("Y");
            OBDal.getInstance().save(error);
            OBDal.getInstance().flush();
            OBDal.getInstance().commitAndClose();
          } finally {
            OBContext.restorePreviousMode();
          }
        }
      }

      if (errorb) {
        JSONObject result = new JSONObject();
        try {
          result
              .put("message", Utility.messageBD(new DalConnectionProvider(false),
                  "OBPOS_ErrorWhileSaving", RequestContext.get().getVariablesSecureApp()
                      .getLanguage()));
        } catch (JSONException e) {
          // won't happen
        }
        return result;
      } else {
        JSONObject result = new JSONObject();
        try {
          result.put("message", Utility.messageBD(new DalConnectionProvider(false),
              "OBPOS_OrderSavedSuccessfully", RequestContext.get().getVariablesSecureApp()
                  .getLanguage()));
        } catch (JSONException e) {
          // won't happen
        }
        return result;
      }
    } catch (Exception e) {// won't' happen
      log.error("Error while processing the record", e);
      JSONObject result = new JSONObject();
      try {
        result.put("message", Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_ErrorWhileSaving", RequestContext.get().getVariablesSecureApp().getLanguage()));
      } catch (JSONException je) {
        // won't happen
      }
      return result;
    }
  }
}
