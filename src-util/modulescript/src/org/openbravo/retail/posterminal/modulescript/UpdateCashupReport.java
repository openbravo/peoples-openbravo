/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class UpdateCashupReport extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(UpdateCashupReport.class);

  @Override
  public void execute() {
    log4j.info("Update CashUpReport structure ...");
    try {
      ConnectionProvider cp = getConnectionProvider();
      UpdateCashupReportData.updateCashupStartingCash(cp.getConnection(), cp);
      UpdateCashupReportData.insertCashManagementEvents(cp.getConnection(), cp);
    } catch (Exception e) {
      handleError(e);
    }
  }

//  @Override
//  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
//    return new ModuleScriptExecutionLimits("FF808181326CC34901326D53DBCF0018", null, 
//        new OpenbravoVersion(1,2,3110));
//  }
  
}
