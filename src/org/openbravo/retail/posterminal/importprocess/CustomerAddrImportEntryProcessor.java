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

import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.CustomerAddrLoader;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link CustomerAddrLoader} in a thread.
 * 
 * @author mtaal
 */
@ImportEntryQualifier(entity = "BusinessPartnerLocation")
@ApplicationScoped
public class CustomerAddrImportEntryProcessor extends ImportEntryProcessor {

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(BusinessPartnerLocationRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
    return "BusinessPartnerLocation".equals(importEntryInformation.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return (String) DalUtil.getId(importEntry.getOrganization());
  }

  private static class BusinessPartnerLocationRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return CustomerAddrLoader.class;
    }

    protected void processEntry(ImportEntry importEntry) throws Exception {
      // check that there are no earlier customers import entries for the same organization
      if (thereAreCustomersInImportQueue(importEntry)) {
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

    private int countEntries(String importStatus, ImportEntry importEntry) {
      final String whereClause = ImportEntry.PROPERTY_IMPORTSTATUS + "='" + importStatus + "' and "
          + ImportEntry.PROPERTY_TYPEOFDATA + "='BusinessPartner' and "
          + ImportEntry.PROPERTY_CREATIONDATE + "<:creationDate and "
          + ImportEntry.PROPERTY_OBPOSPOSTERMINAL + "=:terminal";
      final Query qry = OBDal.getInstance().getSession()
          .createQuery("select count(*) from " + ImportEntry.ENTITY_NAME + " where " + whereClause);
      qry.setTimestamp("creationDate", importEntry.getCreationDate());
      qry.setParameter("terminal", importEntry.getOBPOSPOSTerminal());

      return ((Number) qry.uniqueResult()).intValue();
    }
  }

}
