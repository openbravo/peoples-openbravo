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
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.ProcessCashMgmt;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link ProcessCashMgmt} in a thread.
 * 
 * @author mtaal
 */
@ImportEntryQualifier(entity = "FIN_Finacc_Transaction")
@ApplicationScoped
public class CashManagementImportEntryProcessor extends ImportEntryProcessor {

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(CashManagementRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntry) {
    return "FIN_Finacc_Transaction".equals(importEntry.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return (String) DalUtil.getId(importEntry.getOrganization());
  }

  private static class CashManagementRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return ProcessCashMgmt.class;
    }
  }

}
