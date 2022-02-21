package org.openbravo.service.importqueue;

import org.codehaus.jettison.json.JSONObject;

public interface RecordProcessor {
  public JSONObject processRecord(JSONObject record) throws QueueException;
}
