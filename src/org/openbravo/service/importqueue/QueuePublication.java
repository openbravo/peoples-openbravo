package org.openbravo.service.importqueue;

import org.codehaus.jettison.json.JSONObject;

public interface QueuePublication {
  public void publish(JSONObject importentry);
}
