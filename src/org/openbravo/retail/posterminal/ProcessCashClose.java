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

public class ProcessCashClose extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    JSONArray arrayCashCloseInfo = jsonsent.getJSONArray("cashCloseInfo");
    JSONObject result = (new CashCloseProcessor().processCashClose(arrayCashCloseInfo));
    return result;

  }

}
