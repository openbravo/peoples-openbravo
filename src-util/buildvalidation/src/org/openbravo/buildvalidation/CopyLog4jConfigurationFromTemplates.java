/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.buildvalidation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openbravo.base.ExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * This script will be executed only when migrating from a version which still supports log4j 1.x
 * and copies all new configuration files from the template
 */
public class CopyLog4jConfigurationFromTemplates extends BuildValidation {

  private static final String CORE_MODULE_ID = "0";
  private static final String CONFIG_DIR = "/config/";
  private static final String TEST_SRC_DIR = "/src-test/src/";
  private static final String LOG4J_CONF_FILE = "log4j2.xml";
  private static final String LOG4J_WEB_CONF_FILE = "log4j2-web.xml";
  private static final String LOG4J_TEST_CONF_FILE = "log4j2-test.xml";

  @Override
  public List<String> execute() {
    try {
      String sourcePath = getSourcePathFromProperties(getOpenbravoPropertiesFile());
      copyFromTemplateFile(sourcePath + CONFIG_DIR + LOG4J_CONF_FILE);
      copyFromTemplateFile(sourcePath + CONFIG_DIR + LOG4J_WEB_CONF_FILE);
      copyFromTemplateFile(sourcePath + TEST_SRC_DIR + LOG4J_TEST_CONF_FILE);
    } catch (Exception e) {
      return handleError(e);
    }

    return new ArrayList<>();
  }

  private void copyFromTemplateFile(String targetPath) throws Exception {
    Path source = Paths.get(targetPath + ".template");
    Path target = Paths.get(targetPath);

    if (Files.notExists(target)) {
      Files.copy(source, target);
      System.out.println(targetPath
          + " is copied from template file. Please check this configuration is correct.");
    }
  }

  /**
   * Starting from the location of this class, navigates backwards through the file hierarchy until
   * the config/Openbravo.properties is found
   * 
   * @return a File descriptor of Openbravo.properties
   * @throws FileNotFoundException
   *           if Openbravo.properties cannot be found
   */
  private File getOpenbravoPropertiesFile() throws FileNotFoundException {
    final URL url = this.getClass().getResource(getClass().getSimpleName() + ".class");
    File f = new File(url.getPath());
    File propertiesFile;
    while (f.getParentFile() != null && f.getParentFile().exists()) {
      f = f.getParentFile();
      final File configDirectory = new File(f, "config");
      if (configDirectory.exists()) {
        propertiesFile = new File(configDirectory, "Openbravo.properties");
        if (propertiesFile.exists()) {
          return propertiesFile;
        }
      }
    }

    throw new FileNotFoundException("Openbravo.properties file not found");
  }

  private String getSourcePathFromProperties(File openbravoProperties) throws Exception {
    Properties properties = new Properties();
    properties.load(new FileReader(openbravoProperties));

    return properties.getProperty("source.path");
  }

  @Override
  protected ExecutionLimits getBuildValidationLimits() {
    return new ExecutionLimits(CORE_MODULE_ID, null, new OpenbravoVersion(3, 0, 34826));
  }

}
