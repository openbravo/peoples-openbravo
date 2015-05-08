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
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.CustomerLoader;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link CustomerLoader} in a thread.
 * 
 * @author mtaal
 */
@ImportEntryQualifier(entity = "BusinessPartner")
@ApplicationScoped
public class CustomerImportEntryProcessor extends ImportEntryProcessor {

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(BusinessPartnerRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntry) {
    return "BusinessPartner".equals(importEntry.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    // each customer gets its own process, completely parallel
    return importEntry.getId();
  }

  private static class BusinessPartnerRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return CustomerLoader.class;
    }
  }

}
