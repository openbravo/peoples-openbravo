package org.openbravo.service.importqueue;

public interface QueueImplementation {
  public void start(ImportEntryProcessor processor);

  public void publish(String qualifier, String json);
}
