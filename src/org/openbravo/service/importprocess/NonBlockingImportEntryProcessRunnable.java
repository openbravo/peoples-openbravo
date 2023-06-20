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
import org.openbravo.service.importprocess.ImportEntryProcessor.ImportEntryProcessRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class NonBlockingImportEntryProcessRunnable extends ImportEntryProcessRunnable {
  private final Logger log = LogManager.getLogger();

  // TODO: Parameterize
  private static ExecutorService executorService = Executors.newFixedThreadPool(10,
      new ImportEntryManager.DaemonThreadFactory("ImportEntryNonBlocking"));

  public abstract CompletableFuture<?> processAsync(ImportEntry importEntry) throws Exception;

  @Override
  protected void processEntry(ImportEntry importEntry) throws Exception {
    log.debug("Non-blocking async processing {}", importEntry.getId());
    // TODO: Add logic for processing state
    // ImportEntryManager.getInstance().setImportEntryProcessing(importEntry.getId());
    processAsync(importEntry).handle((result, ex) -> {
      if (ex != null) {
        handleException(ex);
      } else {
        runWithDal(importEntry, () -> completed(importEntry));
      }
      return CompletableFuture.completedFuture(null);
    });
    log.debug("main thread done...");
  }

  private void completed(ImportEntry importEntry) {
    log.info("Completed {}", importEntry);
    ImportEntryManager.getInstance().setImportEntryProcessed(importEntry.getId());
  }

  private void runWithDal(ImportEntry importEntry, Runnable task) {
    setOBContext(new ImportEntryProcessor.ImportEntryProcessRunnable.QueuedEntry(importEntry));
    task.run();
    OBDal.getInstance().commitAndClose();
  }

  private String handleException(Throwable exception) {
    // TODO: Implement exceptionally error handling
    log.error("It was not possible to complete processing IE: {}", exception.getMessage());
    return "";
  }

  protected ExecutorService getExecutorService() {
    return executorService;
  }
}
