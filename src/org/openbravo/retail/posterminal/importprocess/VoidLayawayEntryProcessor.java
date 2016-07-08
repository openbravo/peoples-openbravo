/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.importprocess;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.ProcessVoidLayaway;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link ProcessVoidLayaway} in a thread.
 * 
 * @author mdejuana
 */
@ImportEntryQualifier(entity = "OBPOS_VoidLayaway")
@ApplicationScoped
public class VoidLayawayEntryProcessor extends ImportEntryProcessor {

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(VoidLayawayRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
    return "OBPOS_VoidLayaway".equals(importEntryInformation.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return importEntry.getOrganization().getId();
  }

  private static class VoidLayawayRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return ProcessVoidLayaway.class;
    }

    protected void processEntry(ImportEntry importEntry) throws Exception {
      try {
        OBContext.setAdminMode(false);

        JSONObject json = new JSONObject(importEntry.getJsonInfo());
        if (json.has("data") && json.getJSONArray("data").length() > 0) {
          json = json.getJSONArray("data").getJSONObject(0);
        }
        if (json.has("isprocessed") && "Y".equals(json.getString("isprocessed"))
            && thereIsDataInImportQueue(importEntry)) {
          // close and commit
          OBDal.getInstance().commitAndClose();
          return;
        }

      } finally {
        OBContext.restorePreviousMode();
      }
      super.processEntry(importEntry);
    }

    private boolean thereIsDataInImportQueue(ImportEntry importEntry) {
      try {
        OBContext.setAdminMode(false);

        if (0 < countEntries("Error", importEntry)) {
          // if there are related error entries before this one then this is an error
          // throw an exception to move this entry also to error status
          throw new OBException("There are error records before this record " + importEntry
              + ", moving this entry also to error status.");
        }

        return 0 < countEntries("Initial", importEntry);
      } finally {
        OBContext.restorePreviousMode();
      }
    }

    private int countEntries(String importStatus, ImportEntry importEntry) {
      final String whereClause = ImportEntry.PROPERTY_IMPORTSTATUS + "='" + importStatus + "' and "
          + ImportEntry.PROPERTY_TYPEOFDATA + "='Order' and " + ImportEntry.PROPERTY_CREATIONDATE
          + "<=:creationDate and " + ImportEntry.PROPERTY_CREATEDTIMESTAMP
          + "<:createdtimestamp and " + ImportEntry.PROPERTY_OBPOSPOSTERMINAL
          + "=:terminal and id!=:id";
      final Query qry = OBDal.getInstance().getSession()
          .createQuery("select count(*) from " + ImportEntry.ENTITY_NAME + " where " + whereClause);
      qry.setParameter("id", importEntry.getId());
      qry.setTimestamp("creationDate", importEntry.getCreationDate());
      qry.setParameter("terminal", importEntry.getOBPOSPOSTerminal());
      qry.setParameter("createdtimestamp", importEntry.getCreatedtimestamp());

      return ((Number) qry.uniqueResult()).intValue();
    }
  }

}
