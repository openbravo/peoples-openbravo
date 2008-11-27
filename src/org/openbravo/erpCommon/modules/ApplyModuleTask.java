package org.openbravo.erpCommon.modules;

import org.apache.tools.ant.BuildException;
import org.openbravo.dal.core.DalInitializingTask;
import org.openbravo.database.CPStandAlone;

/**
 * Ant task for ApplyModule class
 *
 */
public class ApplyModuleTask extends DalInitializingTask{
 // private String propertiesFile;
  private String obDir;
  
@Override
public void doExecute() {
    try {
      if (obDir==null||obDir.equals("")) obDir= getProject().getBaseDir().toString();
      if (propertiesFile == null||propertiesFile.equals("")) propertiesFile=obDir+"/config/Openbravo.properties";
      final ApplyModule am = new ApplyModule(new CPStandAlone (propertiesFile), obDir);
      am.execute();
    } catch (final Exception e) {
      throw new BuildException(e);
    }
  }
  
/*public void setPropertiesFile(String propertiesFile) {
    this.propertiesFile = propertiesFile;
  }*/
  public void setObDir(String obDir) {
    this.obDir = obDir;
  }
  
  public static void main(String[] args) {
    final ApplyModuleTask t = new ApplyModuleTask();
    t.setObDir("/ws/trunk/openbravo");
    t.setUserId("0");
    t.setPropertiesFile("/ws/trunk/openbravo/config/Openbravo.properties");
    t.execute();
    
}
  
  
}
