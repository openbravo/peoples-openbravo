/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * ManagerBuildValidationModuleScript implements shared methods between BuildValidation and
 * ModuleScript classes.
 */
public abstract class ManagerBuildValidationModuleScript {
  private static final Logger log4j = Logger.getLogger(ManagerBuildValidationModuleScript.class);

  private ConnectionProvider cp;

  private static final String PATH_CONFIG = "config/Openbravo.properties";

  /**
   * This method allows to implement in subclasses doExecute() method.
   */
  protected List<String> doExecute() {
    return null;
  }

  /**
   * This method checks whether the BuildValidation or ModuleScript can be executed before invoke
   * the doExecute() method
   *
   * @param modulesVersionMap
   *          A data structure that contains module versions mapped by module id
   */
  public List<String> preExecute(Map<String, OpenbravoVersion> modulesVersionMap) {
    ArrayList<String> errors = null;
    if (modulesVersionMap == null || modulesVersionMap.size() == 0) {
      // if we do not have module versions to compare with (install.source) then execute depending
      // on the value of the executeOnInstall() method
      if (executeOnInstall()) {
        errors = (ArrayList<String>) doExecute();
      }
      return errors;
    }

    ExecutionLimits executionLimits = getExecutionLimits();
    if (executionLimits == null || executionLimits.getModuleId() == null) {
      errors = (ArrayList<String>) doExecute();
      return errors;
    }

    String type = getTypeName();
    if (!executionLimits.areCorrect()) {
      log4j.error(type + " " + this.getClass().getName()
          + " not executed because its execution limits are incorrect. "
          + "Last version should be greater than first version.");
      return errors;
    }
    OpenbravoVersion currentVersion = modulesVersionMap.get(executionLimits.getModuleId());
    OpenbravoVersion firstVersion = executionLimits.getFirstVersion();
    OpenbravoVersion lastVersion = executionLimits.getLastVersion();
    String additionalInfo = "";
    // Installing module first time with ERP previously installed.
    if (currentVersion == null) {
      // Dependent module is being installed
      if (executeOnInstall()) {
        errors = (ArrayList<String>) doExecute();
        return errors;
      }
      additionalInfo = this.getClass().getName()
          + " is configured to not execute it during dependent module installation.";
    } else {
      // Dependent module is already installed
      if ((firstVersion == null || firstVersion.compareTo(currentVersion) < 0)
          && (lastVersion == null || lastVersion.compareTo(currentVersion) > 0)) {
        errors = (ArrayList<String>) doExecute();
        return errors;
      }
      additionalInfo = "Dependent module current version (" + currentVersion + ") is not between "
          + type + " execution limits: first version = " + firstVersion + ", last version = "
          + lastVersion;

    }
    log4j.debug("Not necessary to execute " + type + ": " + this.getClass().getName());
    log4j.debug(additionalInfo);
    return errors;
  }

  private String getTypeName() {
    String type = "ModuleScript";
    if (this instanceof BuildValidation) {
      type = "BuildValidation";
    }
    return type;
  }

  /**
   * This method can be overridden by the BuildValidation and ModuleScript subclasses, to specify
   * the module and the limit versions to define whether the BuildValidation or ModuleScript should
   * be executed or not.
   *
   * @return a ExecutionLimits object which contains the dependent module id and the first and last
   *         versions of the module that define the execution logic.
   */
  protected ExecutionLimits getExecutionLimits() {
    return null;
  }

  /**
   * This method can be overridden by the BuildValidation or ModuleScript subclasses, to specify if
   * the BuildValidation or ModuleScript should be executed when installing the dependent module.
   *
   * @return a boolean that indicates if the BuildValidation or ModuleScript should be executed when
   *         installing the dependent module.
   */
  protected boolean executeOnInstall() {
    return true;
  }

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
    File fProp = getPropertiesFile();
    cp = new CPStandAlone(fProp.getAbsolutePath());
    return cp;
  }

  protected File getPropertiesFile() {
    File fProp = null;
    if (new File(PATH_CONFIG).exists())
      fProp = new File(PATH_CONFIG);
    else if (new File("../" + PATH_CONFIG).exists())
      fProp = new File("../" + PATH_CONFIG);
    else if (new File("../../" + PATH_CONFIG).exists())
      fProp = new File("../../" + PATH_CONFIG);
    if (fProp == null) {
      log4j.error("Could not find Openbravo.properties");
    }
    return fProp;
  }
}