/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.buildvalidation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * A class extending the BuildValidation class can be used to implement a validation which will be
 * executed before applying a module, or even Openbravo Core.
 * 
 */
public abstract class BuildValidation {
  private static final Logger log4j = Logger.getLogger(BuildValidation.class);

  private ConnectionProvider cp;

  /**
   * This method must be implemented by the BuildValidations, and is used to define the actions that
   * the script itself will take. This method will be automatically called by the
   * BuildValidationHandler when the validation process is run (at the beginning of a rebuild,
   * before the update.database task).
   * 
   * This method needs to return a list of error messages. If one or more error messages are
   * provided, the build will stop, and the messages will be shown to the user. If an empty list is
   * provided, the validation will be considered succesful, and the build will continue
   * 
   * @return A list of error Strings
   */
  public abstract List<String> execute();

  /**
   * This method prints some log information before calling the execute() method
   */
  private void doExecute() {
    log4j.info("Executing BuildValidation: " + this.getClass().getName());
    execute();
  }

  /**
   * This method checks whether the BuildValidation can be executed before invoke the doExecute()
   * method
   *
   * @param modulesVersionMap
   *          A data structure that contains module versions mapped by module id
   */
  public final void preExecute(Map<String, OpenbravoVersion> modulesVersionMap) {
    if (modulesVersionMap == null || modulesVersionMap.size() == 0) {
      // if we do not have module versions to compare with (install.source) then
      // execute depending
      // on the value of the executeOnInstall() method
      if (executeOnInstall()) {
        doExecute();
      }
      return;
    }
    BuildValidationExecutionLimits executionLimits = getBuildValidationLimits();
    if (executionLimits == null || executionLimits.getModuleId() == null) {
      doExecute();
      return;
    }
    if (!executionLimits.areCorrect()) {
      log4j.error("BuildValidation " + this.getClass().getName()
          + " not executed because its execution limits are incorrect. "
          + "Last version should be greater than first version.");
      return;
    }
    OpenbravoVersion currentVersion = modulesVersionMap.get(executionLimits.getModuleId());
    OpenbravoVersion firstVersion = executionLimits.getFirstVersion();
    OpenbravoVersion lastVersion = executionLimits.getLastVersion();
    String additionalInfo = "";
    // Installing module first time with ERP previously installed.
    if (currentVersion == null) {
      // Dependent module is being installed
      if (executeOnInstall()) {
        doExecute();
        return;
      }
      additionalInfo = this.getClass().getName()
          + " is configured to not execute it during dependent module installation.";
    } else {
      // Dependent module is already installed
      if ((firstVersion == null || firstVersion.compareTo(currentVersion) < 0)
          && (lastVersion == null || lastVersion.compareTo(currentVersion) > 0)) {
        doExecute();
        return;
      }
      additionalInfo = "Dependent module current version (" + currentVersion
          + ") is not between buildValidation execution limits: first version = " + firstVersion
          + ", last version = " + lastVersion;

    }
    log4j.debug("Not necessary to execute buildValidation: " + this.getClass().getName());
    log4j.debug(additionalInfo);
  }

  /**
   * This method can be overridden by the BuildValidation subclasses, to specify the module and the
   * limit versions to define whether the BuildValidation should be executed or not.
   *
   * @return a BuildValidationExecutionLimits object which contains the dependent module id and the
   *         first and last versions of the module that define the execution logic.
   */
  protected BuildValidationExecutionLimits getBuildValidationLimits() {
    return null;
  }

  /**
   * This method can be overridden by the BuildValidation subclasses, to specify if the
   * BuildValidation should be executed when installing the dependent module.
   *
   * @return a boolean that indicates if the BuildValidation should be executed when installing the
   *         dependent module.
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
    if (new File("config/Openbravo.properties").exists())
      fProp = new File("config/Openbravo.properties");
    else if (new File("../config/Openbravo.properties").exists())
      fProp = new File("../config/Openbravo.properties");
    else if (new File("../../config/Openbravo.properties").exists())
      fProp = new File("../../config/Openbravo.properties");
    if (fProp == null) {
      log4j.error("Could not find Openbravo.properties");
    }
    return fProp;
  }

  protected List<String> handleError(Throwable t) {
    ArrayList<String> errors = new ArrayList<String>();
    errors.add("Error executing build-validation " + this.getClass().getName() + ": "
        + t.getMessage());
    errors.add("The build validation couldn't be properly executed");
    return errors;
  }
}