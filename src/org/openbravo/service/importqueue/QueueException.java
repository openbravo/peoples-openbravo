package org.openbravo.service.importqueue;

public class QueueException extends Exception {

  private static final long serialVersionUID = 1L;

  public QueueException(String message, Throwable cause) {
    super(message, cause);
  }

  public QueueException(String message) {
    super(message);
  }
}
