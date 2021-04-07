/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * Insert OBPOS_NewStateActions preference only for new customers,
 * to enable new state actions to be executed instead of old actions in several Web POS flows.
 */
public class InsertNewStateActionsPreference extends ModuleScript {

  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      InsertNewStateActionsPreferenceData.insertNewStateActionsPreference(cp);
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // Do not execute modulescript on update.database
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(0, 0, 0));
  }

  @Override
  protected boolean executeOnInstall() {
    // Execute modulescript on install.source
    return true;
  }
}
