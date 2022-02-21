package org.openbravo.service.importqueue;

public interface ImportEntryProcessor {
  void processImportEntry(String qualifier, String json) throws QueueException;
}
