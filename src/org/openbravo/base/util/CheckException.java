package org.openbravo.base.util;

import org.openbravo.base.exception.OBException;

/**
 * Unchecked state exception which also logs itself.
 * 
 * @author mtaal
 */
public class CheckException extends OBException {
  
  /**
   * Default serial
   */
  private static final long serialVersionUID = 1L;
  
  /** Call super constructor and log the cause. */
  public CheckException() {
    super();
  }
  
  /** Call super constructor and log the cause. */
  public CheckException(String message, Throwable cause) {
    super(message, cause);
  }
  
  /** Call super constructor and log the cause. */
  public CheckException(String message) {
    super(message);
  }
  
  /** Call super constructor and log the cause. */
  public CheckException(Throwable cause) {
    super(cause);
  }
}
