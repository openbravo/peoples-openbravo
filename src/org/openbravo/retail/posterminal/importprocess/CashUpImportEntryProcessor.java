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
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.ProcessCashClose;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link ProcessCashClose} in a thread. Also implements a check to see if a cashup
 * entry can be processed or that it should wait.
 * 
 * @author mtaal
 */
@ImportEntryQualifier(entity = "OBPOS_App_Cashup")
@ApplicationScoped
public class CashUpImportEntryProcessor extends ImportEntryProcessor {

  protected int getMaxNumberOfThreads() {
    return Runtime.getRuntime().availableProcessors() / 2;
  }

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(CashUpRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntry) {
    return "OBPOS_App_Cashup".equals(importEntry.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return (String) DalUtil.getId(importEntry.getOrganization());
  }

  private static class CashUpRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return ProcessCashClose.class;
    }

    protected void processEntry(ImportEntry importEntry) throws Exception {
      // check that there are no orders import entries for the terminal
      // which have not yet been processed

      final JSONObject json = new JSONObject(importEntry.getJsoninfo());
      if (json.has("isprocessed") && "Y".equals(json.getString("isprocessed"))
          && thereAreOrdersInImportQueue(importEntry)) {
        return;
      }
      super.processEntry(importEntry);
    }

    private boolean thereAreOrdersInImportQueue(ImportEntry importEntry) {
      final String whereClause = ImportEntry.PROPERTY_IMPORTSTATUS + "='Initial' and "
          + ImportEntry.PROPERTY_TYPEOFDATA + "='Order' and " + ImportEntry.PROPERTY_STORED
          + "<:storedDate and " + ImportEntry.PROPERTY_ORGANIZATION + "=:org and "
          + ImportEntry.PROPERTY_OBPOSPOSTERMINAL + "=:terminal";
      final OBQuery<ImportEntry> entries = OBDal.getInstance().createQuery(ImportEntry.class,
          whereClause);

      entries.setFilterOnReadableClients(false);
      entries.setFilterOnReadableOrganization(false);
      entries.setNamedParameter("storedDate", importEntry.getStored());
      entries.setNamedParameter("org", importEntry.getOrganization());
      entries.setNamedParameter("terminal", importEntry.getOBPOSPOSTerminal());
      return 0 < entries.count();
    }
  }

}
