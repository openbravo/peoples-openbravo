package org.openbravo.modulescript;

import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;

public abstract class ModuleScript {

  private ConnectionProvider cp = null;

  public abstract void execute();

  public ConnectionProvider getConnectionProvider() {
    if (cp == null) {
      cp = new CPStandAlone("config/Openbravo.properties");
    }
    return cp;
  }
}