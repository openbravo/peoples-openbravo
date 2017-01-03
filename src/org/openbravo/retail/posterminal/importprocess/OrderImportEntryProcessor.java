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

import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.retail.posterminal.OrderLoader;
import org.openbravo.retail.posterminal.process.SerializedByTermImportEntryProcessorRunnable;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link OrderLoader} in a thread.
 * 
 * @author mtaal
 */
@ImportEntryQualifier(entity = "Order")
@ApplicationScoped
public class OrderImportEntryProcessor extends ImportEntryProcessor {

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(OrderLoaderRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
    return "Order".equals(importEntryInformation.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return importEntry.getOrganization().getId();
  }

  private static class OrderLoaderRunnable extends SerializedByTermImportEntryProcessorRunnable {
    private static final String ORDER_LOADER_AUDIT_TYPE = "OBPOS_OrderLoader";

    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return OrderLoader.class;
    }

    protected void processEntry(ImportEntry importEntry) throws Exception {
      // check that there are no customers import entries for the same organization
      if (thereAreCustomersInImportQueue(importEntry)) {
        // close and commit
        OBDal.getInstance().commitAndClose();
        return;
      }
      super.processEntry(importEntry);
    }

    private boolean thereAreCustomersInImportQueue(ImportEntry importEntry) {
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
      return ORDER_LOADER_AUDIT_TYPE;
    }
  }
}
