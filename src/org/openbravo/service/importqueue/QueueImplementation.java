package org.openbravo.service.importqueue;

import org.codehaus.jettison.json.JSONObject;

public interface QueueImplementation {
  public void start(ImportEntryProcessor processor);

  public void close();

  public void publish(JSONObject importentry);
}
