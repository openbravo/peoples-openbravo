/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class UpdateDocumentTypesinStore extends ModuleScript {

  private static final Logger log4j = LogManager.getLogger();
  private static final String RETAIL_POSTERMINAL_MODULE_ID = "FF808181326CC34901326D53DBCF0018";

  @Override
  public void execute() {
    log4j.info("Update document types in the store...");
    try {
      ConnectionProvider cp = getConnectionProvider();
      UpdateDocumentTypesinStoreData.updateDocumentType(cp.getConnection(), cp);
      UpdateDocumentTypesinStoreData.updateDocumentTypeReturns(cp.getConnection(), cp);
      UpdateDocumentTypesinStoreData.updateDocumentTypeReconciliation(cp.getConnection(), cp);
      UpdateDocumentTypesinStoreData.updateDocumentTypeQuotation(cp.getConnection(), cp);

    } catch (Exception e) {
      log4j.error("Errors when setting the document types in the store");
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits(RETAIL_POSTERMINAL_MODULE_ID, null,
        new OpenbravoVersion(1, 2, 6100));
  }

  @Override
  protected boolean executeOnInstall() {
    return false;
  }

}
