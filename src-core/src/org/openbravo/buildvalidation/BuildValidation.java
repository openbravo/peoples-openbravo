package org.openbravo.buildvalidation;

import java.io.File;
import java.util.List;

import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;

public abstract class BuildValidation {

  private ConnectionProvider cp;

  public abstract List<String> execute();

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