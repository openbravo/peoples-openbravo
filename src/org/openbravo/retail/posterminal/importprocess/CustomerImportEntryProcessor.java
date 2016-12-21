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
import org.openbravo.retail.posterminal.CustomerLoader;
import org.openbravo.retail.posterminal.process.SerializedByTermImportEntryProcessorRunnable;
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
  static final String BP_LOADER_AUDIT_TYPE = "OBPOS_BP_Loader";

  @Override
  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(BusinessPartnerRunnable.class);
  }

  @Override
  protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
    return "BusinessPartner".equals(importEntryInformation.getTypeofdata());
  }

  @Override
  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return importEntry.getOrganization().getId();
  }

  private static class BusinessPartnerRunnable extends SerializedByTermImportEntryProcessorRunnable {
    @Override
    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return CustomerLoader.class;
    }

    @Override
    protected String getProcessIdForAudit() {
      return BP_LOADER_AUDIT_TYPE;
    }
  }

}
