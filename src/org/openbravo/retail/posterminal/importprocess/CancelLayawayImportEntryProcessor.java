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

import org.openbravo.base.weld.WeldUtils;
import org.openbravo.mobile.core.process.DataSynchronizationProcess;
import org.openbravo.mobile.core.process.MobileImportEntryProcessorRunnable;
import org.openbravo.retail.posterminal.CancelLayawayLoader;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryManager.ImportEntryQualifier;
import org.openbravo.service.importprocess.ImportEntryProcessor;

/**
 * Encapsulates the {@link CancelLayawayLoader} in a thread.
 */
@ImportEntryQualifier(entity = "OBPOS_CancelLayaway")
@ApplicationScoped
public class CancelLayawayImportEntryProcessor extends ImportEntryProcessor {

  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return WeldUtils.getInstanceFromStaticBeanManager(CancelLayawayRunnable.class);
  }

  protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
    return "OBPOS_CancelLayaway".equals(importEntryInformation.getTypeofdata());
  }

  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return importEntry.getOrganization().getId();
  }

  private static class CancelLayawayRunnable extends MobileImportEntryProcessorRunnable {
    private static final String CANCEL_LAYAWAY_AUDIT_TYPE = "OBPOS_CancelLayaway";

    protected Class<? extends DataSynchronizationProcess> getDataSynchronizationClass() {
      return CancelLayawayLoader.class;
    }

    @Override
    protected String getProcessIdForAudit() {
      return CANCEL_LAYAWAY_AUDIT_TYPE;
    }
  }

}
