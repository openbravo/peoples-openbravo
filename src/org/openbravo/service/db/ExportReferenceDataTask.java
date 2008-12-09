package org.openbravo.service.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBException;
import org.openbravo.model.ad.system.Client;

/**
 * Export reference data of clients defined by the clients parameter.
 * 
 */
public class ExportReferenceDataTask extends ReferenceDataTask {
    private static final Logger log = Logger
            .getLogger(ExportReferenceDataTask.class);

    private String clients;

    @Override
    protected void doExecute() {
        final File exportDir = getReferenceDataDir();
        for (final Client client : getClientObjects()) {
            final Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(DataExportService.CLIENT_ID_PARAMETER_NAME, client
                    .getId());
            log.info("Exporting client " + client.getName());
            final String xml = DataExportService.getInstance()
                    .exportClientToXML(parameters);
            final File exportFile = new File(exportDir, client.getValue()
                    + ".xml");
            if (exportFile.exists()) {
                exportFile.delete();
            }
            try {
                final FileWriter fw = new FileWriter(exportFile);
                fw.write(xml);
                fw.close();
            } catch (final IOException e) {
                throw new OBException(e);
            }
        }
    }

    @Override
    public String getClients() {
        return clients;
    }

    @Override
    public void setClients(String clients) {
        this.clients = clients;
    }

    private List<Client> getClientObjects() {
        final List<Client> result = new ArrayList<Client>();
        for (final String clientStr : getClients().split(",")) {
            result.add(getClient(clientStr.trim()));
        }
        return result;
    }

    private Client getClient(String clientValue) {
        final org.openbravo.dal.service.OBCriteria<Client> obc = org.openbravo.dal.service.OBDal
                .getInstance().createCriteria(Client.class);
        obc.add(Expression.eq(Client.PROPERTY_VALUE, clientValue));
        final List<Client> result = obc.list();
        if (result.size() == 0) {
            throw new OBException("No client found using " + clientValue
                    + " as the value in the query");
        }
        if (result.size() > 1) {
            throw new OBException("More than one client found using "
                    + clientValue + " as the value in the query");
        }
        return result.get(0);
    }
}
