/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import java.sql.PreparedStatement;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class FixDataIssue35364 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue35364.class);
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      FixDataIssue35364Data.fixPOSAmounttokeep(cp);
      FixDataIssue35364Data.fixPOSStartingcash(cp);
      FixDataIssue35364Data.fixPOSTotalsales(cp);
      FixDataIssue35364Data.fixPOSTotalreturns(cp);
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // The module script needs to be executed only when updating from a version
    // lower than 3.0RR17Q2 (Retail pack)(1.8.2700)
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null, new OpenbravoVersion(1, 8,
        2700));
  }

  public static void main(String[] args) {
    // This method is provided for testing purposes.
    FixDataIssue35364 t = new FixDataIssue35364();
    t.execute();
  }
}