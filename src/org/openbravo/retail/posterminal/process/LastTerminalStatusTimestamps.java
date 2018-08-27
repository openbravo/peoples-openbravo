/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.json.JsonConstants;

public class LastTerminalStatusTimestamps extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(true);
    JSONObject result = new JSONObject();
    try {
      String posterminalID = jsonsent.getString("posterminalId");

      Entity posterminalEntity = ModelProvider.getInstance().getEntity(OBPOSApplications.class);

      OBPOSApplications posterminal = OBDal.getInstance().get(OBPOSApplications.class,
          posterminalID);

      JSONPropertyToEntity.fillBobFromJSON(posterminalEntity, posterminal, jsonsent);

      OBDal.getInstance().flush();
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } catch (Exception e) {
      result.put("status", JsonConstants.RPCREQUEST_STATUS_FAILURE);
    } finally {
      OBContext.restorePreviousMode();
    }

    return result;
  }
}
