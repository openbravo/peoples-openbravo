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

  protected boolean canHandleImportEntry(ImportEntry importEntry) {
    return "BusinessPartnerLocation".equals(importEntry.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    // each customer gets its own process, completely parallel
    return importEntry.getId();
  }

  private static class BusinessPartnerLocationRunnable extends MobileImportEntryProcessorRunnable {
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return CustomerAddrLoader.class;
    }
  }

}
