package org.openbravo.erpCommon.modules;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openbravo.database.CPStandAlone;

/**
 * Ant task for ApplyModule class
 *
 */
public class ApplyModuleTask extends Task{
  private String propertiesFile;
  private String obDir;
  
  public void execute() {
    try {
      if (obDir==null||obDir.equals("")) obDir= getProject().getBaseDir().toString();
      if (propertiesFile == null||propertiesFile.equals("")) propertiesFile=obDir+"/config/Openbravo.properties";
      
      ApplyModule am = new ApplyModule(new CPStandAlone (propertiesFile), obDir);
      am.execute();
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }
  
  public void setPropertiesFile(String propertiesFile) {
    this.propertiesFile = propertiesFile;
  }
  public void setObDir(String obDir) {
    this.obDir = obDir;
  }
  
  
}
