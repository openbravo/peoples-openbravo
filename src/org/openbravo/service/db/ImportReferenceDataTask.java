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

import java.io.File;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;

/**
 * Imports client data for the clients defined in the clients parameter from the
 * {@link ReferenceDataTask#REFERENCE_DATA_DIRECTORY} directory.
 */
public class ImportReferenceDataTask extends ReferenceDataTask {
    private static final Logger log = Logger
            .getLogger(ImportReferenceDataTask.class);

    @Override
    protected void doExecute() {
        final File importDir = getReferenceDataDir();
        if (importDir.listFiles().length == 0) {
            throw new OBException(
                    "No import files present in the import directory: "
                            + importDir.getAbsolutePath());
        }

        for (final File importFile : importDir.listFiles()) {
            if (importFile.isDirectory()) {
                continue;
            }
            log.info("Importing from file " + importFile.getAbsolutePath());

            String xml = DbUtility.readFile(importFile);
            final ClientImportProcessor importProcessor = new ClientImportProcessor();
            importProcessor.setNewName(null);
            final ImportResult ir = DataImportService.getInstance()
                    .importClientData(xml, importProcessor);
            xml = null; // set to null to make debugging faster
            if (ir.hasErrorOccured()) {
                if (ir.getException() != null) {
                    throw new OBException(ir.getException());
                }
                if (ir.getErrorMessages() != null) {
                    throw new OBException(ir.getErrorMessages());
                }
            }
        }
        OBDal.getInstance().commitAndClose();
    }

}
