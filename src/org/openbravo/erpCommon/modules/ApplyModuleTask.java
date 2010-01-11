/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.modules;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalInitializingTask;
import org.openbravo.database.CPStandAlone;
import org.openbravo.erpCommon.utility.AntExecutor;

/**
 * Ant task for ApplyModule class
 * 
 */
public class ApplyModuleTask extends DalInitializingTask {
  // private String propertiesFile;
  private String obDir;

  public static void main(String[] args) {
    final String srcPath = args[0];
    final File srcDir = new File(srcPath);
    final File baseDir = srcDir.getParentFile();
    try {
      final AntExecutor antExecutor = new AntExecutor(baseDir.getAbsolutePath());
      antExecutor.runTask("apply.module.forked");
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  @Override
  public void doExecute() {
    try {
      if (obDir == null || obDir.equals(""))
        obDir = getProject().getBaseDir().toString();
      if (propertiesFile == null || propertiesFile.equals(""))
        propertiesFile = obDir + "/config/Openbravo.properties";
      System.out.println("properties file: " + propertiesFile);
      final ApplyModule am = new ApplyModule(new CPStandAlone(propertiesFile), obDir);
      am.execute();
    } catch (final Exception e) {
      throw new BuildException(e);
    }
  }

  /*
   * public void setPropertiesFile(String propertiesFile) { this.propertiesFile = propertiesFile; }
   */
  public void setObDir(String obDir) {
    this.obDir = obDir;
  }
}
