package org.openbravo.retail.posterminal.term;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.login.LabelsComponent;
import org.openbravo.mobile.core.process.JSONProcessSimple;
import org.openbravo.retail.posterminal.POSConstants;
import org.openbravo.service.json.JsonConstants;

public class Labels extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(true);
    String languageId = null;
    try {
      if (jsonsent.has("parameters") && jsonsent.getJSONObject("parameters").has("languageId")) {
        if (jsonsent.getJSONObject("parameters").get("languageId") instanceof JSONObject) {
          languageId = jsonsent.getJSONObject("parameters").getJSONObject("languageId")
              .getString("value");
        } else {
          languageId = null;
        }
      }
      JSONObject result = new JSONObject();
      result.put("data", LabelsComponent.getLabels(languageId, POSConstants.MODULE_ID));
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      result.put("result", "0");
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
