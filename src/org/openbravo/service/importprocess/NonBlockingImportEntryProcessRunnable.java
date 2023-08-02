/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.importprocess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.NonBlockingExecutorServiceProvider;
import org.openbravo.service.importprocess.ImportEntryProcessor.ImportEntryProcessRunnable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public abstract class NonBlockingImportEntryProcessRunnable extends ImportEntryProcessRunnable {
  private final Logger log = LogManager.getLogger();

  public abstract CompletableFuture<?> processAsync(ImportEntry importEntry) throws Exception;

  @Override
  protected void processEntry(ImportEntry importEntry) throws Exception {
    processAsync(importEntry).handle((result, ex) -> {
      if (ex != null) {
        cleanUpAndLogOnException(ex);
        markImportEntryWithError(importEntry.getId(), ex);
        return CompletableFuture.failedFuture(ex);
      }

      runWithDal(importEntry, () -> completed(importEntry));
      return CompletableFuture.completedFuture(null);
    });
  }

  @Override
  protected void postProcessEntry(String importEntryId, long t0, ImportEntry localImportEntry,
      String typeOfData) {
    if (!"Initial".equals(localImportEntry.getImportStatus())) {
      super.postProcessEntry(importEntryId, t0, localImportEntry, typeOfData);
    }
  }

  @Override
  protected boolean tryDeregisteringProcessThread() {
    return super.tryDeregisteringProcessThread();
  }

  @Override
  public void cleanUp(Set<String> importEntriesInExecution) {
    if (!importEntryIds.isEmpty()) {
      // In case of non-blocking import entry, we save it in the list of import entries to keep
      importEntriesInExecution.addAll(importEntryIds);
    }
  }

  private void completed(ImportEntry importEntry) {
    log.info("Completed {}", importEntry); // TODO: Remove verbose log
    ImportEntryManager.getInstance().setImportEntryProcessed(importEntry.getId());
    importEntry.setImportStatus("Processed");
    postProcessEntry(importEntry.getId(), 0L, importEntry, importEntry.getTypeofdata());
  }

  private void runWithDal(ImportEntry importEntry, Runnable task) {
    setOBContext(new ImportEntryProcessor.ImportEntryProcessRunnable.QueuedEntry(importEntry));
    task.run();
    OBDal.getInstance().commitAndClose();
  }

  /**
   * Marks the import entry with an error status in an independent transaction
   * 
   * @param importEntryId
   *          Import entry id of the import entry to change its status
   * @param t
   *          Throwable that caused the error
   */
  private void markImportEntryWithError(String importEntryId, Throwable t) {
    try {
      ImportEntryManager.getInstance().setImportEntryErrorIndependent(importEntryId, t);
    } catch (Throwable ex) {
      ImportProcessUtils.logError(log, ex);
    }
  }

  protected ExecutorService getExecutorService() {
    return NonBlockingExecutorServiceProvider.getExecutorService();
  }
}
