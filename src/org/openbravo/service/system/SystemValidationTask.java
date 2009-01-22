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

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.service.db.ReferenceDataTask;

/**
 * Performs different types of validations on the basis of the type parameter.
 * 
 * @author mtaal
 */
public class SystemValidationTask extends ReferenceDataTask {
    private static final Logger log = Logger.getLogger("SystemValidation");

    private String type;
    private boolean failOnError = false;
    private String moduleName;

    @Override
    protected void doExecute() {
        if (getType().contains("database")) {
            log.info("Validating Database and Application Dictionary");
            final DatabaseValidator databaseValidator = new DatabaseValidator();
            printResult(databaseValidator.validate());
        }
        if (getType().contains("module")) {
            log.info("Validating Modules");
            final ModuleValidator moduleValidator = new ModuleValidator();
            if (getModuleName() != null) {
                printResult(moduleValidator.validate(getModuleName()));
            } else {
                printResult(moduleValidator.validate());
            }
        }
    }

    private void printResult(SystemValidationResult result) {
        for (String warning : result.getWarnings()) {
            log.warn("WARNING: " + warning);
        }

        if (isFailOnError()) {
            final StringBuilder sb = new StringBuilder();
            for (String err : result.getErrors()) {
                sb.append(err);
                if (sb.length() > 0) {
                    sb.append("\n");
                }
            }
            if (sb.length() > 0) {
                throw new OBException(sb.toString());
            }
        } else {
            for (String err : result.getErrors()) {
                log.error("ERROR: " + err);
            }
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

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
