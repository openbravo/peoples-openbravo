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
import org.apache.log4j.Logger;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * @author MAL
 * Fixes issue 27695: "Invoice Paid" field at Payment schedule Details level is not properly set by Cash Up process
 */
public class FixDataIssue27695 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue27695.class);
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";
  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      int count = FixDataIssue27695Data.fixPaymentDetails(cp);
      log4j.debug("Fixed " + count + " payment details with invoice paid a N");
    } catch (Exception e) {
      handleError(e);
    }
  }
  
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // The module script needs to be executed only when updating from a version
    // lower than 3.0RR14Q2.4 (Retail pack)(1.8.402)
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 402));
  }
  
  @Override
  protected boolean executeOnInstall() {
    return false;
  }

  public static void main(String[] args) {
    // This method is provided for testing purposes.
    FixDataIssue27695 t = new FixDataIssue27695();
    t.execute();
  }
}
