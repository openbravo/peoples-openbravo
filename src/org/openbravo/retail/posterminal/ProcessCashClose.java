/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonConstants;

public class ProcessCashClose implements JSONProcess {

  private static final Logger log = Logger.getLogger(ProcessCashClose.class);

  @Override
  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {
    try {
      JSONObject result = new JSONObject();
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      String s = result.toString();
      w.write(s.substring(1, s.length()) + "}");
      w.flush();
      w.close();
      OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          jsonsent.getString("terminalId"));

      new OrderGroupingProcessor().groupOrders(posTerminal);
      JSONArray arrayCashCloseInfo = jsonsent.getJSONArray("cashCloseInfo");
      new CashCloseProcessor().processCashClose(arrayCashCloseInfo);
    } catch (Exception e) {
      log.error("Error processing cash close", e);
      return;
    }

  }
}
