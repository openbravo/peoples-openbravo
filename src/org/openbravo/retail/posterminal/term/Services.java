/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.MobileServiceDefinition;
import org.openbravo.service.json.JsonConstants;

public class Services extends JSONTerminalProperty {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);

    try {
      JSONArray respArray = new JSONArray();

      OBQuery<MobileServiceDefinition> services = OBDal.getInstance().createQuery(
          MobileServiceDefinition.class, "");
      services.setFilterOnReadableOrganization(false);

      for (MobileServiceDefinition service : services.list()) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", service.getService());
        jsonObject.put("type", service.getRoutingtype());
        respArray.put(jsonObject);
      }

      result.put(JsonConstants.RESPONSE_DATA, respArray);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

      return result;

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public String getProperty() {
    return "services";
  }
}
