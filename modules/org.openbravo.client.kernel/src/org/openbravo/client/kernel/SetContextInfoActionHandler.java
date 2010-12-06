package org.openbravo.client.kernel;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@ApplicationScoped
public class SetContextInfoActionHandler extends BaseActionHandler {

  private static final Logger log = Logger.getLogger(SetContextInfoActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {

    RequestContext rc = RequestContext.get();

    // TODO Auto-generated method stub
    try {
      JSONObject p = new JSONObject((String) parameters.get("params"));

      String windowId = p.getString("_windowId");
      System.out.println("window: " + windowId);

      JSONArray names = p.names();
      for (int i = 0; i < names.length(); i++) {
        String name = names.getString(i);
        String value = p.getString(name);

        rc.setSessionAttribute((windowId + "|" + name).toUpperCase(), value);
        System.out.println((windowId + "|" + name).toUpperCase() + ":" + value);
        System.out.println(name + ": " + value);
      }

    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("set context info");
    return new JSONObject();
  }

}
