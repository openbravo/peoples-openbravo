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

package org.openbravo.service.db;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;

/**
 * Contains the result of an import action, i.e. warning, error, inserted
 * objects etc.
 * 
 * @author mtaal
 */

public class ImportResult {

    private List<BaseOBObject> updatedObjects = new ArrayList<BaseOBObject>();
    private List<BaseOBObject> insertedObjects = new ArrayList<BaseOBObject>();
    private String errorMessages;
    private String logMessages;
    private String warningMessages;
    private Throwable exception;

    public boolean hasErrorOccured() {
        return exception != null
                || (errorMessages != null && errorMessages.trim().length() > 0);
    }

    public List<BaseOBObject> getUpdatedObjects() {
        return updatedObjects;
    }

    public void setUpdatedObjects(List<BaseOBObject> updatedObjects) {
        this.updatedObjects = updatedObjects;
    }

    public List<BaseOBObject> getInsertedObjects() {
        return insertedObjects;
    }

    public void setInsertedObjects(List<BaseOBObject> insertedObjects) {
        this.insertedObjects = insertedObjects;
    }

    // NOTE: returns empty string if no error messages, does not return null
    public String getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(String errorMessages) {
        this.errorMessages = errorMessages;
    }

    // NOTE: returns empty string if no error messages, does not return null
    public String getLogMessages() {
        return logMessages;
    }

    public void setLogMessages(String logMessages) {
        this.logMessages = logMessages;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    // NOTE: returns empty string if no error messages, does not return null
    public String getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(String warningMessages) {
        this.warningMessages = warningMessages;
    }

}