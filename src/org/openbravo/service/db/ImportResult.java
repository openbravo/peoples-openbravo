/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
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