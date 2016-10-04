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
import org.openbravo.base.ExecutionLimitBaseProcess;
import org.openbravo.base.ExecutionLimits;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * A class extending the BuildValidation class can be used to implement a validation which will be
 * executed before applying a module, or even Openbravo Core.
 * 
 */
public abstract class BuildValidation extends ExecutionLimitBaseProcess {
  private static final Logger log4j = Logger.getLogger(BuildValidation.class);

  /**
   * This method must be implemented by the BuildValidations, and is used to define the actions that
   * the script itself will take. This method will be automatically called by the
   * BuildValidationHandler when the validation process is run (at the beginning of a rebuild,
   * before the update.database task).
   * 
   * This method needs to return a list of error messages. If one or more error messages are
   * provided, the build will stop, and the messages will be shown to the user. If an empty list is
   * provided, the validation will be considered successful, and the build will continue
   * 
   * @return A list of error Strings
   */
  public abstract List<String> execute();

  /**
   * This method prints some log information before calling the execute() method
   */
  @Override
  protected List<String> doExecute() {
    log4j.info("Executing buildValidation: " + this.getClass().getName());
    ArrayList<String> errors = (ArrayList<String>) execute();
    return errors;
  }

  /**
   * This method checks whether the BuildValidation can be executed before invoke the doExecute()
   * method
   *
   * @param modulesVersionMap
   *          A data structure that contains module versions mapped by module id
   */
  public final List<String> preExecute(Map<String, OpenbravoVersion> modulesVersionMap) {
    return super.preExecute(modulesVersionMap);
  }

  /**
   * This method returns the name of the class.
   */
  protected String getTypeName() {
    return this.getClass().getName();
  }

  /**
   * This method can be overridden by the BuildValidation subclasses, to specify the module and the
   * limit versions to define whether the BuildValidation should be executed or not.
   *
   * @return a ExecutionLimits object which contains the dependent module id and the first and last
   *         versions of the module that define the execution logic.
   */
  protected ExecutionLimits getBuildValidationLimits() {
    return null;
  }

  @Override
  protected ExecutionLimits getExecutionLimits() {
    return getBuildValidationLimits();
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
    return super.getConnectionProvider();
  }

  protected File getPropertiesFile() {
    return super.getPropertiesFile();
  }

  protected List<String> handleError(Throwable t) {
    ArrayList<String> errors = new ArrayList<String>();
    errors.add("Error executing build-validation " + this.getClass().getName() + ": "
        + t.getMessage());
    errors.add("The build validation couldn't be properly executed");
    return errors;
  }
}