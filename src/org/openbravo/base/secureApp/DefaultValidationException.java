package org.openbravo.base.secureApp;

/**
 * Class used to catch invalid settings during the login process
 * @author Openbravo
 *
 */
public class DefaultValidationException extends Exception {

  private static final long serialVersionUID = 1L;
  private String defaultField;
  
  public DefaultValidationException (String message, String fieldName) {
    super(message);
    setDefaultField(fieldName);
  }
  
  /**
   * Method used to determine the field for which the default setting failed
   * @return
   */
  public String getDefaultField() { return defaultField; }
  private void setDefaultField(String fieldName) { defaultField = fieldName; }
}
