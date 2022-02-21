package org.openbravo.service.importqueue.impl;

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
  public void publish(String qualifier, String data) {
    try {
      processor.processImportEntry(qualifier, data);
    } catch (QueueException e) {
      // PROTOTYPE:
      throw new RuntimeException(e);
    }
  }
}
