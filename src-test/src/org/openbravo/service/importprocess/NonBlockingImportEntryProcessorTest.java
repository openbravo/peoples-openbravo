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

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

@ImportEntryManager.ImportEntryQualifier(entity = "OB_NonBlockingTest")
@ApplicationScoped
public class NonBlockingImportEntryProcessorTest extends ImportEntryProcessor {

  @Override
  protected ImportEntryProcessRunnable createImportEntryProcessRunnable() {
    return new NonBlockingImportEntryProcessRunnableTest();
  }

  @Override
  protected boolean canHandleImportEntry(ImportEntry importEntryInformation) {
    return "OB_NonBlockingTest".equals(importEntryInformation.getTypeofdata());
  }

  @Override
  protected String getProcessSelectionKey(ImportEntry importEntry) {
    return importEntry.getOrganization().getId();
  }

  static class NonBlockingImportEntryProcessRunnableTest
      extends NonBlockingImportEntryProcessRunnable {
    private final Logger log = LogManager.getLogger();

    @Override
    public CompletableFuture<?> processAsync(ImportEntry importEntry) {
      CompletableFuture<JSONObject> completableFuture = new CompletableFuture<>();
      this.getExecutorService().submit(() -> {
        log.info("Executing async completable future logic.");
        completableFuture.complete(new JSONObject());
      });
      log.info("Submitted completable future to executor.");
      return completableFuture;
    }
  }
}
