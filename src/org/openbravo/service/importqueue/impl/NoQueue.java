package org.openbravo.service.importqueue.impl;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.importqueue.ImportEntryProcessor;
import org.openbravo.service.importqueue.QueueException;
import org.openbravo.service.importqueue.QueueImplementation;

public class NoQueue implements QueueImplementation {

  private ImportEntryProcessor processor;

  @Override
  public void start(ImportEntryProcessor p) {
    processor = p;
  }

  @Override
  public void close() {
  }

  @Override
  public void publish(JSONObject message) {
    try {
      processor.processImportEntry(message);
    } catch (QueueException | JSONException e) {
      // PROTOTYPE:
      throw new RuntimeException(e);
    }
  }
}
