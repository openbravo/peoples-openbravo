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

import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.XMLEntityConverter;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.ReferenceDataStore;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Imports business objects using datasets, makes use of the dataSetService.
 * 
 * @author Martin Taal
 */
public class DataImportService implements OBSingleton {
    private static final Logger log = Logger.getLogger(DataImportService.class);

    private static DataImportService instance;

    public static DataImportService getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(DataImportService.class);
        }
        return instance;
    }

    public static void setInstance(DataImportService instance) {
        DataImportService.instance = instance;
    }

    public ImportResult importDataFromXML(Client client,
            Organization organisation, String xml) {
        return importDataFromXML(client, organisation, xml, null);
    }

    public ImportResult importDataFromXML(Client client,
            Organization organisation, String xml, Module module) {
        try {
            final Document doc = DocumentHelper.parseText(xml);
            return importDataFromXML(client, organisation, doc, true);
        } catch (final Exception e) {
            throw new OBException(e);
        }
    }

    public ImportResult importDataFromXML(Client client,
            Organization organisation, Document doc,
            boolean createReferencesIfNotFound) {
        return importDataFromXML(client, organisation, doc,
                createReferencesIfNotFound, null);
    }

    public ImportResult importDataFromXML(Client client,
            Organization organisation, Document doc,
            boolean createReferencesIfNotFound, Module module) {

        log.debug("Importing data for client " + client.getId()
                + (organisation != null ? "/" + organisation.getId() : ""));

        final ImportResult ir = new ImportResult();
        try {
            final XMLEntityConverter xec = XMLEntityConverter.newInstance();
            xec.setClient(client);
            xec.setOrganization(organisation);
            xec.getEntityResolver().setOptionCreateReferencedIfNotFound(
                    createReferencesIfNotFound);
            xec.process(doc);

            ir.setLogMessages(xec.getLogMessages());
            ir.setErrorMessages(xec.getErrorMessages());
            ir.setWarningMessages(xec.getWarningMessages());

            if (ir.hasErrorOccured()) {
                // TODO: provide this method in the dal
                SessionHandler.getInstance().setDoRollback(true);
                return ir;
            }

            // now save and update
            // do inserts and updates in opposite order, this is important
            // so that the objects on which other depend are inserted first
            final List<BaseOBObject> toInsert = xec.getToInsert();
            int done = 0;
            for (int i = toInsert.size() - 1; i > -1; i--) {
                final BaseOBObject ins = toInsert.get(i);
                OBDal.getInstance().save(ins);
                ir.getInsertedObjects().add(ins);
                done++;
            }
            Check.isTrue(done == toInsert.size(),
                    "Not all objects have been inserted, check for loop: "
                            + done + "/" + toInsert.size());

            // flush to set the ids in the objects
            OBDal.getInstance().flush();

            // do the updates the other way around also
            done = 0;
            final List<BaseOBObject> toUpdate = xec.getToUpdate();
            for (int i = toUpdate.size() - 1; i > -1; i--) {
                final BaseOBObject upd = toUpdate.get(i);
                OBDal.getInstance().save(upd);
                ir.getUpdatedObjects().add(upd);
                done++;
            }
            Check.isTrue(done == toUpdate.size(),
                    "Not all objects have been inserted, check for loop: "
                            + done + "/" + toUpdate.size());

            // flush to set the ids in the objects
            OBDal.getInstance().flush();

            // store the ad_ref_data_loaded
            try {
                OBContext.getOBContext().setInAdministratorMode(true);
                for (final BaseOBObject ins : xec.getToInsert()) {
                    final String originalId = xec.getEntityResolver()
                            .getOriginalId(ins);
                    // completely new object, manually added to the xml
                    if (originalId == null) {
                        continue;
                    }
                    final ReferenceDataStore rdl = OBProvider.getInstance()
                            .get(ReferenceDataStore.class);
                    if (ins instanceof ClientEnabled) {
                        rdl.setClient(((ClientEnabled) ins).getClient());
                    }
                    if (ins instanceof OrganizationEnabled) {
                        rdl.setOrganization(((OrganizationEnabled) ins)
                                .getOrganization());
                    }
                    rdl.setGeneric(originalId);
                    rdl.setSpecific((String) ins.getId());
                    rdl.setTable(OBDal.getInstance().get(Table.class,
                            ins.getEntity().getTableId()));
                    if (module != null) {
                        rdl.setModule(module);
                    }
                    OBDal.getInstance().save(rdl);
                }
            } finally {
                OBContext.getOBContext().restorePreviousAdminMode();
            }

        } catch (final Throwable t) {
            t.printStackTrace(System.err);
            ir.setException(t);
        }

        if (ir.hasErrorOccured()) {
            SessionHandler.getInstance().setDoRollback(true);
        }

        return ir;
    }
}
