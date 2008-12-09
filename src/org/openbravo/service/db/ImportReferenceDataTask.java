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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;

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
        for (final String clientStr : getClients().split(",")) {
            final File importFile = new File(importDir, clientStr.trim()
                    + ".xml");
            if (!importFile.exists()) {
                throw new OBException("No import file present for client "
                        + clientStr + " complete path: "
                        + importFile.getAbsolutePath());
            }
            log.info("Importing from file " + importFile.getAbsolutePath());
            final String xml = readFile(importFile);
            final ClientImportProcessor importProcessor = new ClientImportProcessor();
            importProcessor.setNewName(null);
            DataImportService.getInstance().importClientData(xml,
                    importProcessor);
        }
    }

    private String readFile(File file) {
        final StringBuilder contents = new StringBuilder();

        try {
            final BufferedReader input = new BufferedReader(
                    new FileReader(file));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (final IOException e) {
            throw new OBException(e);
        }
        return contents.toString();
    }
}
