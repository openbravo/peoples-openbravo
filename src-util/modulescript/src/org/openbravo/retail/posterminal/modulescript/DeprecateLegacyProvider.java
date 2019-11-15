/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;

public class DeprecateLegacyProvider extends ModuleScript {
  private static final String RETAIL_MODULE_ID = "FF808181326CC34901326D53DBCF0018";

  @Override
  public void execute() {
    // 0042065: Deprecate legacy Payment Provider and Refund Provider
    try {
      ConnectionProvider cp = getConnectionProvider();

      try (
          PreparedStatement select = cp.getPreparedStatement(
              "select 1 from OBPOS_PAYMENTGROUP where obpos_paymentgroup_id = '0'");
          ResultSet s = select.executeQuery()) {
        boolean exists = s.next();

        if (!exists) {
          try (PreparedStatement ps0 = cp.getPreparedStatement("insert into OBPOS_PAYMENTGROUP (" //
              + " obpos_paymentgroup_id,  ad_client_id,  ad_org_id,  isactive,  created,  createdby,  updated,  updatedby,  provider,  name,  description" //
              + ") values (" //
              + " '0', '0', '0', 'Y', now(), '100', now(), '100', 'OBPOS_LEGACYPROVIDER', 'Legacy Payment Provider'," //
              + " 'System legacy provider for backward compatibility with then Payment Provider and Refund Provider definitions.')")) {
            ps0.executeUpdate();
          }
          try (PreparedStatement ps1 = cp.getPreparedStatement("UPDATE OBPOS_APP_PAYMENT_TYPE" //
              + " SET OBPOS_PAYMENTGROUP_ID = '0'" //
              + " WHERE OBPOS_PAYMENTGROUP_ID IS NULL AND (PAYMENTPROVIDER IS NOT NULL OR REFUNDPROVIDER IS NOT NULL)")) {
            ps1.executeUpdate();
          }
        }
      }
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits(RETAIL_MODULE_ID, null, null);
  }

  public static void main(String[] args) {
    // This method is provided for testing purposes.
    DeprecateLegacyProvider t = new DeprecateLegacyProvider();
    t.execute();
  }
}
