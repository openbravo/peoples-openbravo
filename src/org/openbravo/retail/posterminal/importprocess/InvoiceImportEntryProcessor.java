/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.importprocess;

import javax.enterprise.context.ApplicationScoped;

import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.InvoiceLoader;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link InvoiceLoader} in a thread.
 * 
 */
@ImportEntryQualifier(entity = "OBPOS_Invoice")
@ApplicationScoped
public class InvoiceImportEntryProcessor extends ImportEntryProcessor {

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(InvoiceLoaderRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
    return "OBPOS_Invoice".equals(importEntryInformation.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return importEntry.getOrganization().getId();
  }

  private static class InvoiceLoaderRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return InvoiceLoader.class;
    }

    protected void processEntry(ImportEntry importEntry) throws Exception {
      // check that there are no order import entries for the same organization
      if (thereAreOrdersInImportQueue(importEntry)) {
        // close and commit
        OBDal.getInstance().commitAndClose();
        return;
      }
      super.processEntry(importEntry);
    }

    private boolean thereAreOrdersInImportQueue(ImportEntry importEntry) {
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
          + ImportEntry.PROPERTY_TYPEOFDATA + " = 'Order' and " + ImportEntry.PROPERTY_CREATIONDATE
          + " < :creationDate and " + ImportEntry.PROPERTY_OBPOSPOSTERMINAL + " = :terminal";
      final Query<Number> qry = OBDal
          .getInstance()
          .getSession()
          .createQuery("select count(*) from " + ImportEntry.ENTITY_NAME + " where " + whereClause,
              Number.class);
      qry.setParameter("creationDate", importEntry.getCreationDate());
      qry.setParameter("terminal", importEntry.getOBPOSPOSTerminal());

      return (qry.uniqueResult()).intValue();
    }
  }

}
