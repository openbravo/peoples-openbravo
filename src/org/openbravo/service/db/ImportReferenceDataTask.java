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
