/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
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
    } else if (jsonorder instanceof JSONArray) {
      array = (JSONArray) jsonorder;
    }

    long t1 = System.currentTimeMillis();
    JSONObject result = new OrderLoader().saveOrder(array);
    log.info("Final total time: " + (System.currentTimeMillis() - t1));
    return result;
  }

}
