/*
 ************************************************************************************
 * Copyright (C) 2014-2017 Openbravo S.L.U.
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

public class FixDataIssue26444 extends ModuleScript {
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";
  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      FixDataIssue26444Data.fixInvoice(cp);
      FixDataIssue26444Data.fixPaymentScheduleDetail(cp);
      FixDataIssue26444Data.deletePaymentSchedule(cp);
    } catch (Exception e) {
      handleError(e);
    }
  }
  
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // The module script needs to be executed only when updating from a version
    // lower than 3.0RR14Q3 (Retail pack)(1.8.601)
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 601));
  }
  
  @Override
  protected boolean executeOnInstall() {
    return false;
  }
}
