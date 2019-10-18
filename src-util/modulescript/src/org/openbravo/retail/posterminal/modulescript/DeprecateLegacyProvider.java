/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
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
import java.sql.PreparedStatement;

public class DeprecateLegacyProvider extends ModuleScript {
  private static final String RETAIL_MODULE_ID = "FF808181326CC34901326D53DBCF0018";

  @Override
  public void execute() {
    // 0042065: Deprecate legacy Payment Provider and Refund Provider
    try {
      ConnectionProvider cp = getConnectionProvider();
      PreparedStatement ps = cp
          .getPreparedStatement( //
              "UPDATE OBPOS_APP_PAYMENT_TYPE SET OBPOS_PAYMENTGROUP_ID ='0' \n" + //
              "WHERE OBPOS_PAYMENTGROUP_ID IS NULL AND (PAYMENTPROVIDER IS NOT NULL OR REFUNDPROVIDER IS NOT NULL)");
      ps.executeUpdate();
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // Before 19Q4
    return new ModuleScriptExecutionLimits(RETAIL_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 4700));
  }
  
  @Override
  protected boolean executeOnInstall() {
    return false;
  }
  
  public static void main(String[] args) {
    // This method is provided for testing purposes.
    DeprecateLegacyProvider t = new DeprecateLegacyProvider();
    t.execute();
  }
}
