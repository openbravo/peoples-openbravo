package org.openbravo.buildvalidation;

import java.io.File;
import java.util.ArrayList;

import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;

public abstract class Validation {

  private ConnectionProvider cp = null;
  private File basedir;

  public abstract ArrayList<String> execute();

  public ConnectionProvider getConnectionProvider() {
    if (cp == null) {
      File f = new File("");
      f = new File(f.getAbsolutePath());
      File fProp = null;
      if (new File("../../config/Openbravo.properties").exists())
        fProp = new File("../../config/Openbravo.properties");
      else if (new File("../config/Openbravo.properties").exists())
        fProp = new File("../config/Openbravo.properties");
      else if (new File("config/Openbravo.properties").exists())
        fProp = new File("config/Openbravo.properties");
      cp = new CPStandAlone(fProp.getAbsolutePath());
    }
    return cp;
  }
}