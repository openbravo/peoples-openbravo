package org.openbravo.service.db;

import java.io.File;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalInitializingTask;

/**
 * Ant task which is the basis for the import and export of sample data during
 * installation of Openbravo. The files are read and exported from and to the
 * src-db/database/referencedata directory.
 * 
 */
public class ReferenceDataTask extends DalInitializingTask {
    public static final String REFERENCE_DATA_DIRECTORY = "/referencedata/sampledata";
    private String clients;

    protected File getReferenceDataDir() {
        final File mainDir = new File(getProject().getBaseDir().toString())
                .getParentFile();
        final File referenceDir = new File(mainDir, REFERENCE_DATA_DIRECTORY);
        if (!referenceDir.exists()) {
            referenceDir.mkdirs();
        }
        return referenceDir;
    }

    public String getClients() {
        if (clients == null) {
            throw new OBException(
                    "No clients defined to export, is the clients attribute set in the task definition");
        }
        return clients;
    }

    public void setClients(String clients) {
        this.clients = clients;
    }
}
