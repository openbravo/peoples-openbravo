package org.openbravo.service.importqueue;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface MessageProcessor {
  public JSONObject processMessage(JSONObject message) throws QueueException, JSONException;
}
