package org.openbravo.base.validation;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.model.Property;

/**
 * Is thrown when an entity is invalid. Does not extend OBException because this
 * one does not need to be logged.
 * 
 * @author mtaal
 */
public class ValidationException extends RuntimeException {
  
  /**
   * Default serial
   */
  private static final long serialVersionUID = 1L;
  
  private Map<Property, String> msgs = new HashMap<Property, String>();
  
  /** Call super constructor and log the cause. */
  public ValidationException() {
    super();
  }
  
  public void addMessage(Property p, String msg) {
    msgs.put(p, msg);
  }
  
  public boolean hasMessages() {
    return !msgs.isEmpty();
  }
  
  @Override
  public String getMessage() {
    if (msgs == null) {
      // during construction
      return "";
    }
    final StringBuffer sb = new StringBuffer();
    for (Property p : msgs.keySet()) {
      final String msg = msgs.get(p);
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(p.getName() + ": " + msg);
    }
    return sb.toString();
  }
}
