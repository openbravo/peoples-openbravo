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

import java.util.List;

import org.openbravo.base.structure.BaseOBObject;

/**
 * The import processor is called by the DataImportService to process imported
 * imported objects before they are persisted into the database.
 * 
 * @author mtaal
 */

public interface ImportProcessor {

    /**
     * This method is called after the import process has parsed the xml and
     * created the in-memory object graph of objects which are inserted and
     * updated in the database.
     * 
     * This method can access the database using the Data Access Layer. It will
     * operate in the same transaction as the import process itself.
     * 
     * @param newObjects
     *            the list of objects which will be inserted into the database
     * @param updatedObjects
     *            the list of objects which will be updated in the database
     */
    public void process(List<BaseOBObject> newObjects,
            List<BaseOBObject> updatedObjects);
}