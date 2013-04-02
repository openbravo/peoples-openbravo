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

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

public class SaveOrderActionHandler extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(SaveOrderActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONArray errorIds = null;
    String errorId = null;
    String posTerminalId = null;
    try {
      errorIds = new JSONArray(content);

    } catch (Exception e) {// won't' happen
    }
    boolean errorb = false;
    String currentClient = OBContext.getOBContext().getCurrentClient().getId();
    String currentOrg = OBContext.getOBContext().getCurrentOrganization().getId();
    String currentUser = OBContext.getOBContext().getUser().getId();
    String currentRole = OBContext.getOBContext().getRole().getId();
    try {
      OBContext.setAdminMode(true);

      for (int i = 0; i < errorIds.length(); i++) {
        try {
          errorId = errorIds.getString(i);
          OBPOSErrors error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
          JSONObject jsonorder = new JSONObject(error.getJsoninfo());
          posTerminalId = jsonorder.getString("posTerminal");
          OBContext.setOBContext(jsonorder.getString("createdBy"), currentRole,
              jsonorder.getString("client"), jsonorder.getString("organization"));
          OrderLoader loader = WeldUtils.getInstanceFromStaticBeanManager(OrderLoader.class);
          loader.saveOrder(jsonorder);
          error.setOrderstatus("Y");
          OBDal.getInstance().flush();
          OBDal.getInstance().commitAndClose();

        } catch (Exception e1) {
          errorb = true;
          OBDal.getInstance().rollbackAndClose();
          if (TriggerHandler.getInstance().isDisabled()) {
            TriggerHandler.getInstance().enable();
          }
          OBPOSErrors error = OBDal.getInstance().get(OBPOSErrors.class, errorId);
          error.setError(OrderLoader.getErrorMessage(e1));
          error.setTypeofdata("order");
          error.setObposApplications(OBDal.getInstance()
              .get(OBPOSApplications.class, posTerminalId));
          OBDal.getInstance().flush();
          OBDal.getInstance().commitAndClose();
          log.error("Error while generating the JSON object", e1);
        }
      }
    } finally {
      OBContext.restorePreviousMode();
      OBContext.setOBContext(currentUser, currentRole, currentClient, currentOrg);
    }

    if (errorb) {
      JSONObject result = new JSONObject();
      try {
        result.put("message", Utility.messageBD(new DalConnectionProvider(false),
            "OBPOS_ErrorWhileSaving", RequestContext.get().getVariablesSecureApp().getLanguage()));
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
  }
}
