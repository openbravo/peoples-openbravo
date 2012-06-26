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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.json.JsonConstants;

public class ProcessCashClose extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
        jsonsent.getString("terminalId"));

    JSONObject resultInvoice = null;
    try {
      resultInvoice = new OrderGroupingProcessor().groupOrders(posTerminal);
    } catch (Exception e) {
      e.printStackTrace();
      if (resultInvoice == null) {
        resultInvoice = new JSONObject();
        resultInvoice.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      }
      return resultInvoice;
    }

    JSONArray arrayCashCloseInfo = jsonsent.getJSONArray("cashCloseInfo");
    JSONObject result = new CashCloseProcessor().processCashClose(arrayCashCloseInfo);
    return result;

  }

}
