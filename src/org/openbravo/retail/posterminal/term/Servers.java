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
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.MobileServerDefinition;
import org.openbravo.mobile.core.MobileServerService;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.service.json.JsonConstants;

public class Servers extends JSONTerminalProperty {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);

    String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();
    OBPOSApplications posTerminal = POSUtils.getTerminalById(posId);

    try {
      JSONArray respArray = new JSONArray();

      OBQuery<MobileServerDefinition> servers = OBDal.getInstance().createQuery(
          MobileServerDefinition.class,
          MobileServerDefinition.PROPERTY_ALLORGS + "='Y' or :org in elements("
              + MobileServerDefinition.PROPERTY_OBMOBCSERVERORGSLIST + ") order by "
              + MobileServerDefinition.PROPERTY_PRIORITY);
      servers.setNamedParameter("org", posTerminal.getOrganization().getId());
      for (MobileServerDefinition server : servers.list()) {
        respArray.put(createServerJSON(server));
      }

      result.put(JsonConstants.RESPONSE_DATA, respArray);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

      return result;

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private JSONObject createServerJSON(MobileServerDefinition server) throws JSONException {
    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", server.getName());
    jsonObject.put("address", server.getURL());
    jsonObject.put("online", true);
    if ("MAIN".equals(server.getServerType())) {
      jsonObject.put("mainServer", true);
    } else {
      jsonObject.put("mainServer", true);
    }
    JSONArray services = new JSONArray();
    if (!server.isAllservices() && server.getOBMOBCSERVERSERVICESList().size() > 0) {
      for (MobileServerService service : server.getOBMOBCSERVERSERVICESList()) {
        services.put(service.getObmobcServices().getService());
      }
    }
    jsonObject.put("services", services);
    return jsonObject;
  }

  @Override
  public String getProperty() {
    return "servers";
  }
}
