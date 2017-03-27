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

public class UpdateFinancialAccountPaymentMethod extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(UpdateFinancialAccountPaymentMethod.class);
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      int count =  UpdateFinancialAccountPaymentMethodData.updateFinancialAccountPaymentMethodAutomaticDeposit(cp);
      log4j.debug("Fixed " + count + " financial account payment methods with automatic deposit different to Y");
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 2700));
  }

}
