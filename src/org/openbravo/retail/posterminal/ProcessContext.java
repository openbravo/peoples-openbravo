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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.process.JSONProcessSimple;
import org.openbravo.mobile.core.process.JSONRowConverter;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.JsonConstants;

public class ProcessContext extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    final JSONRowConverter converter = new JSONRowConverter(DataResolvingMode.SHORT);

    final OBContext cnx = OBContext.getOBContext();

    final JSONObject jsonResponse = new JSONObject();
    final JSONObject jsonData = new JSONObject();

    jsonData.put("user", converter.convert(cnx.getUser()));
    jsonData.put("role", converter.convert(cnx.getRole()));

    jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return jsonResponse;
  }
}
