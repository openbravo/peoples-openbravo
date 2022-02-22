package org.openbravo.service.importqueue;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface ImportEntryProcessor {
  void processImportEntry(JSONObject message) throws QueueException, JSONException;
}
