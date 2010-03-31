package org.openbravo.buildvalidation;

import java.io.File;
import java.util.List;

import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;

/**
 * A class extending the BuildValidation class can be used to implement a validation which will be
 * executed before applying a module, or even Openbravo Core.
 * 
 */
public abstract class BuildValidation {

  private ConnectionProvider cp;

  /**
   * This method must be implemented by the BuildValidations, and is used to define the actions that
   * the script itself will take. This method will be automatically called by the
   * BuildValidationHandler when the validation process is run (at the beginning of a rebuild,
   * before the update.database task).
   * 
   * This method needs to return a list of error messages. If one or more error messages are
   * provided, the build will stop, and the messages will be shown to the user. If an empty list is
   * provided, the validation will be considered succesful, and the build will continue
   * 
   * @Return A list of error Strings
   */
  public abstract List<String> execute();

  /**
   * This method returns a connection provider, which can be used to execute statements in the
   * database
   * 
   * @return a ConnectionProvider
   */
  protected ConnectionProvider getConnectionProvider() {
    if (cp != null) {
      return cp;
    }

    File f = new File("");
    f = new File(f.getAbsolutePath());
    File fProp = null;
    if (new File("config/Openbravo.properties").exists())
      fProp = new File("config/Openbravo.properties");
    else if (new File("../config/Openbravo.properties").exists())
      fProp = new File("../config/Openbravo.properties");
    else if (new File("../../config/Openbravo.properties").exists())
      fProp = new File("../../config/Openbravo.properties");
    cp = new CPStandAlone(fProp.getAbsolutePath());
    return cp;
  }
}