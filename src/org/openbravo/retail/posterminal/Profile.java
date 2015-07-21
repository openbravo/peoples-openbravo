/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.JSONProcessSimple;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.json.JsonConstants;

public class Profile extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    JSONObject respArray = new JSONObject();
    OBContext.setAdminMode(false);
    try {
      final String userId = OBContext.getOBContext().getUser().getId();
      final String roleId = jsonsent.getString("role");
      final User user = OBDal.getInstance().get(User.class, userId);
      user.setOBPOSDefaultPOSRole(OBDal.getInstance().get(Role.class, roleId));
      OBDal.getInstance().save(user);
      OBDal.getInstance().flush();
      respArray.put("success", true);
      result.put(JsonConstants.RESPONSE_DATA, respArray);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      return result;
    } catch (Exception e) {
      result.put("success", false);
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
