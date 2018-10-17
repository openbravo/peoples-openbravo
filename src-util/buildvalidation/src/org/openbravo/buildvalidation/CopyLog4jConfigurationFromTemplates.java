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

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * This script will be executed only when migrating from a version which still supports log4j 1.x
 * and copies all new configuration files from the template
 */
public class CopyLog4jConfigurationFromTemplates extends ModuleScript {

  private static final Logger log = LogManager.getLogger(CopyLog4jConfigurationFromTemplates.class);
  private static final String USER_DIR = getUserDir();
  private static final String CLIENT_APPLICATION_MODULE_ID = "9BA0836A3CD74EE4AB48753A47211BCC";
  private static final String CONFIG_DIR = "/config/";
  private static final String TEST_SRC_DIR = "/src-test/src/";
  private static final String LOG4J_CONF_FILE = "log4j2.xml";
  private static final String LOG4J_WEB_CONF_FILE = "log4j2-web.xml";
  private static final String LOG4J_TEST_CONF_FILE = "log4j2-test.xml";

  @Override
  public void execute() {
    copyFromTemplateFile(CONFIG_DIR + LOG4J_CONF_FILE);
    copyFromTemplateFile(CONFIG_DIR + LOG4J_WEB_CONF_FILE);
    copyFromTemplateFile(TEST_SRC_DIR + LOG4J_TEST_CONF_FILE);
  }

  private void copyFromTemplateFile(String targetRelativePath) {
    Path source = Paths.get(USER_DIR, targetRelativePath + ".template");
    Path target = Paths.get(USER_DIR, targetRelativePath);

    try {
      log.info("Copying {}", USER_DIR + targetRelativePath + ".template");
      Files.copy(source, target);
    } catch (FileAlreadyExistsException e) {
      log.info("{} already exists. Ignoring.", USER_DIR + targetRelativePath);
    } catch (IOException e) {
      handleError(e);
    }
  }

  private static String getUserDir() {
    String userDir = System.getProperty("user.dir");
    if (SystemUtils.IS_OS_WINDOWS) {
      userDir = userDir.replace("\\", "/");
    }
    return userDir;
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits(CLIENT_APPLICATION_MODULE_ID, null,
      new OpenbravoVersion(3, 0, 34825));
  }
}
