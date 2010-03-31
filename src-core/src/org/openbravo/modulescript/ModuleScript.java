package org.openbravo.modulescript;

import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;

public abstract class ModuleScript {

  private ConnectionProvider cp = null;

  /**
   * This method must be implemented by the ModuleScripts, and is used to define the actions that
   * the script itself will take. This method will be automatically called by the
   * ModuleScriptHandler when the update.database or the update.database.mod tasks are being
   * executed
   */
  public abstract void execute();

  /**
   * This method returns a connection provider, which can be used to execute statements in the
   * database
   * 
   * @return a ConnectionProvider
   */
  public ConnectionProvider getConnectionProvider() {
    if (cp == null) {
      cp = new CPStandAlone("config/Openbravo.properties");
    }
    return cp;
  }
}