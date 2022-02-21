package org.openbravo.service.importqueue;

public interface ImportEntryProcessor {
  void processImportEntry(String qualifier, String data) throws QueueException;
}
