/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.importprocess;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.retail.posterminal.ProcessVoidLayaway;
import org.openbravo.retail.posterminal.process.SerializedByTermImportEntryProcessorRunnable;
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

  private static class VoidLayawayRunnable extends SerializedByTermImportEntryProcessorRunnable {
    private static final String VOID_LAYAWAY_AUDIT_TYPE = "OBPOS_VoidLayaway";

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

    @Override
    protected String getProcessIdForAudit() {
      return VOID_LAYAWAY_AUDIT_TYPE;
    }
  }

}
