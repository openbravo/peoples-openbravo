/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonConstants;

public class ProcessCashClose extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(ProcessCashClose.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(false);
    JSONObject jsonResponse = new JSONObject();
    JSONObject jsonData = new JSONObject();
    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", "0");
      OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          jsonsent.getString("terminalId"));

      new OrderGroupingProcessor().groupOrders(posTerminal);
      posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          jsonsent.getString("terminalId"));
      JSONArray arrayCashCloseInfo = jsonsent.getJSONArray("cashCloseInfo");
      new CashCloseProcessor().processCashClose(posTerminal, arrayCashCloseInfo);
      jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
      return jsonResponse;
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("Error processing cash close", e);
      jsonData.put("error", "1");
      jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
      return jsonResponse;
    } finally {
      OBDal.getInstance().rollbackAndClose();
      OBContext.restorePreviousMode();
      if (TriggerHandler.getInstance().isDisabled()) {
        TriggerHandler.getInstance().enable();
      }
    }
  }
}
