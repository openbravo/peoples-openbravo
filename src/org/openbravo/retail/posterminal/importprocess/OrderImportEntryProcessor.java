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

import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.OrderLoader;
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

  protected int getMaxNumberOfThreads() {
    return Runtime.getRuntime().availableProcessors() / 2;
  }

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(OrderLoaderRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntry) {
    return "Order".equals(importEntry.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return (String) DalUtil.getId(importEntry.getOrganization());
  }

  private static class OrderLoaderRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return OrderLoader.class;
    }

    protected void processEntry(ImportEntry importEntry) throws Exception {
      // check that there are no customers import entries for the same organization
      if (thereAreCustomersInImportQueue(importEntry)) {
        return;
      }
      super.processEntry(importEntry);
    }

    private boolean thereAreCustomersInImportQueue(ImportEntry importEntry) {
      final String whereClause = ImportEntry.PROPERTY_IMPORTSTATUS + "='Initial' and " + "("
          + ImportEntry.PROPERTY_TYPEOFDATA + "='BusinessPartner' or "
          + ImportEntry.PROPERTY_TYPEOFDATA + "='BusinessPartnerLocation') and "
          + ImportEntry.PROPERTY_STORED + "<:storedDate and " + ImportEntry.PROPERTY_ORGANIZATION
          + "=:org";
      final OBQuery<ImportEntry> entries = OBDal.getInstance().createQuery(ImportEntry.class,
          whereClause);

      entries.setFilterOnReadableClients(false);
      entries.setFilterOnReadableOrganization(false);
      entries.setNamedParameter("storedDate", importEntry.getStored());
      entries.setNamedParameter("org", importEntry.getOrganization());
      return 0 < entries.count();
    }
  }

}
