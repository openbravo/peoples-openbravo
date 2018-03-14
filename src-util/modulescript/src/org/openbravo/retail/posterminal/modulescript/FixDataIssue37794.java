/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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

public class FixDataIssue37794 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue37794.class);
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      FixDataIssue37794Data.deleteDuplicatedOrganizationUserPosTerminal(cp.getConnection(), cp);
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // The module script needs to be executed only when updating from a version
    // lower than 3.0RR18Q1 (Retail pack)(1.8.3302)
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null, new OpenbravoVersion(1, 8,
        3302));
  }

  public static void main(String[] args) {
    // This method is provided for testing purposes.
    FixDataIssue37794 t = new FixDataIssue37794();
    t.execute();
  }
}