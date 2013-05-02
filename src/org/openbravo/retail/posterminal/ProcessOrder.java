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

public class ProcessOrder extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(ProcessOrder.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    Object jsonorder = jsonsent.get("order");

    JSONArray array = null;
    if (jsonorder instanceof JSONObject) {
      array = new JSONArray();
      array.put(jsonorder);
    } else if (jsonorder instanceof String) {
      JSONObject obj = new JSONObject((String) jsonorder);
      array = new JSONArray();
      array.put(obj);
    } else if (jsonorder instanceof JSONArray) {
      array = (JSONArray) jsonorder;
    }

    long t1 = System.currentTimeMillis();
    JSONObject result = new OrderLoader().saveOrder(array);
    log.info("Final total time: " + (System.currentTimeMillis() - t1));
    return result;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

}
