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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.system;

import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.service.db.ReferenceDataTask;
import org.openbravo.service.system.SystemValidationResult.SystemValidationType;

/**
 * Performs different types of validations on the basis of the type parameter.
 * 
 * @author mtaal
 */
public class SystemValidationTask extends ReferenceDataTask {
  private static final Logger log = Logger.getLogger("SystemValidation");

  private String type;
  private boolean failOnError = false;
  private String moduleJavaPackage;

  @Override
  protected void doExecute() {
    if (getType().contains("database")) {
      log.info("Validating Database and Application Dictionary");
      final DatabaseValidator databaseValidator = new DatabaseValidator();
      final SystemValidationResult result = databaseValidator.validate();
      if (result.getErrors().isEmpty() && result.getWarnings().isEmpty()) {
        log.warn("Validation successfull no warnings or errors");
      } else {
        printResult(result);
      }
    }
    if (getType().contains("module")) {
      log.info("Validating Modules");
      final ModuleValidator moduleValidator = new ModuleValidator();
      final SystemValidationResult result;
      if (getModuleJavaPackage() != null) {
        result = moduleValidator.validate(getModuleJavaPackage());
      } else {
        result = moduleValidator.validate();
      }
      if (result.getErrors().isEmpty() && result.getWarnings().isEmpty()) {
        log.warn("Validation successfull no warnings or errors");
      } else {
        printResult(result);
      }
    }
  }

  private void printResult(SystemValidationResult result) {
    for (SystemValidationType validationType : result.getWarnings().keySet()) {
      log.warn("\n");
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      log.warn("Warnings for Validation type: " + validationType);
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> warnings = result.getWarnings().get(validationType);
      for (String warning : warnings) {
        log.warn(warning);
      }
    }

    final StringBuilder sb = new StringBuilder();
    for (SystemValidationType validationType : result.getErrors().keySet()) {
      sb.append("\n");
      sb.append("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      sb.append("Errors for Validation type: " + validationType);
      sb.append("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> errors = result.getErrors().get(validationType);
      for (String err : errors) {
        sb.append(err);
        if (sb.length() > 0) {
          sb.append("\n");
        }
      }
    }
    log.error(sb.toString());
    if (failOnError) {
      throw new OBException(sb.toString());
    }
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isFailOnError() {
    return failOnError;
  }

  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  public String getModuleJavaPackage() {
    return moduleJavaPackage;
  }

  public void setModuleJavaPackage(String moduleJavaPackage) {
    this.moduleJavaPackage = moduleJavaPackage;
  }
}
