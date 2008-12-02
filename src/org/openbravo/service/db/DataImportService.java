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
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.XMLEntityConverter;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.ReferenceDataStore;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Imports business objects from XML. The business objects can be imported in a
 * specific Client and Organization or as a complete Client import.
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

    /**
     * Imports the business objects using the client/organization. If there are
     * no error messages and no Exception occurred then the import method will
     * persist the imported business objects. However, the import method does
     * not do a commit. It is the callers responsibility to call commit (if the
     * ImportResult does not contain error messages).
     * 
     * @param client
     *            the client in which the business objects are created/updated
     * @param organization
     *            the organization in which the business objects are
     *            created/updated
     * @param xml
     *            the xml containing the data
     * @return the ImportResult object contains error, log and warning messages
     *         and lists with the inserted and updated business objects
     */
    public ImportResult importDataFromXML(Client client,
            Organization organization, String xml) {
        return importDataFromXML(client, organization, xml, null);
    }

    /**
     * Imports the business objects using the client/organization. If there are
     * no error messages and no Exception occurred then the import method will
     * persist the imported business objects. However, the import method does
     * not do a commit. It is the callers responsibility to call commit (if the
     * ImportResult does not contain error messages).
     * 
     * @param client
     *            the client in which the import takes place
     * @param organization
     *            the organization in which the business objects are created
     * @param xml
     *            the xml containing the data
     * @param module
     *            the module is used to update the AD_REF_DATA_LOADED table
     *            during the import action
     * @return the result of the import (error, log and warning messages,
     *         to-be-inserted and to-be-updated business objects
     */
    public ImportResult importDataFromXML(Client client,
            Organization organization, String xml, Module module) {
        try {
            final Document doc = DocumentHelper.parseText(xml);
            return importDataFromXML(client, organization, doc, true, module,
                    null, false);
        } catch (final Exception e) {
            throw new OBException(e);
        }
    }

    /**
     * Imports a complete client. This import method behaves slightly
     * differently than the other import methods because it does not use the
     * client/organization of the current user but uses the client and
     * organization of the data in the import file itself. In addition no
     * unique-constraint checking is done.
     * 
     * @param xml
     *            the xml string containing the objects to import
     * @param importProcessor
     *            the importProcessor is called after the xml has been parsed
     *            and before the new/updated objects are persisted in the
     *            database, is allowed to be null
     * 
     * @return ImportResult which contains the updated/inserted objects and log
     *         and error messages
     * 
     * @see #importDataFromXML(Client, Organization, String)
     */
    public ImportResult importClientData(String xml,
            ImportProcessor importProcessor, Module module) {
        try {
            final Document doc = DocumentHelper.parseText(xml);
            return importDataFromXML(null, null, doc, true, module,
                    importProcessor, true);
        } catch (final Exception e) {
            throw new OBException(e);
        }

    }

    private ImportResult importDataFromXML(Client client,
            Organization organization, Document doc,
            boolean createReferencesIfNotFound, Module module,
            ImportProcessor importProcessor, boolean isClientImport) {

        log.debug("Importing data for client " + client.getId()
                + (organization != null ? "/" + organization.getId() : ""));

        final ImportResult ir = new ImportResult();

        try {
            // disable the triggers to prevent unexpected extra db actions
            // during import
            TriggerHandler.getInstance().disable();

            final XMLEntityConverter xec = XMLEntityConverter.newInstance();
            xec.setClient(client);
            xec.setOrganization(organization);
            xec.setOptionClientImport(isClientImport);
            xec.getEntityResolver().setOptionCreateReferencedIfNotFound(
                    createReferencesIfNotFound);
            xec.process(doc);

            ir.setLogMessages(xec.getLogMessages());
            ir.setErrorMessages(xec.getErrorMessages());
            ir.setWarningMessages(xec.getWarningMessages());

            if (ir.hasErrorOccured()) {
                OBDal.getInstance().rollbackAndClose();
                return ir;
            }

            if (importProcessor != null) {
                try {
                    importProcessor.process(xec.getToInsert(), xec
                            .getToUpdate());
                } catch (final Exception e) {
                    // note on purpose caught and set in ImportResult
                    ir.setErrorMessages(e.getMessage());
                    OBDal.getInstance().rollbackAndClose();
                    return ir;
                }
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
                OBDal.getInstance().flush();
            } finally {
                if (TriggerHandler.getInstance().isDisabled()) {
                    TriggerHandler.getInstance().enable();
                }
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
