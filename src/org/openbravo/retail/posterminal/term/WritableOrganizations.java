package org.openbravo.retail.posterminal.term;

import java.util.Set;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.process.JSONProcessSimple;
import org.openbravo.service.json.JsonConstants;

public class WritableOrganizations extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    Set<String> Organizations = OBContext.getOBContext().getWritableOrganizations();
    try {
      JSONArray arrResult = new JSONArray(Organizations);
      JSONObject result = new JSONObject();
      result.put("data", arrResult);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      result.put("result", "0");
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}