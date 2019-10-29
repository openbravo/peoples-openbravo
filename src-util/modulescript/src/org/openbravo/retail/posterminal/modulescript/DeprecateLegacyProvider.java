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
import java.sql.ResultSet;
import java.sql.Timestamp;

public class DeprecateLegacyProvider extends ModuleScript {
  private static final String RETAIL_MODULE_ID = "FF808181326CC34901326D53DBCF0018";

  @Override
  public void execute() {
    // 0042065: Deprecate legacy Payment Provider and Refund Provider
    try {
      ConnectionProvider cp = getConnectionProvider();
      Timestamp now = new Timestamp(System.currentTimeMillis());
      
      PreparedStatement select = cp.getPreparedStatement("select 1 from OBPOS_PAYMENTGROUP where obpos_paymentgroup_id = '0'");
      ResultSet s = select.executeQuery();
      boolean exists = s.next();
      s.close();
      select.close();
      
      if (!exists) {
        PreparedStatement ps0 = cp
            .getPreparedStatement(      
        "insert into OBPOS_PAYMENTGROUP (" + 
        "  obpos_paymentgroup_id," + 
        "  ad_client_id," + 
        "  ad_org_id," + 
        "  isactive," + 
        "  created," + 
        "  createdby," + 
        "  updated," + 
        "  updatedby," + 
        "  provider," + 
        "  name," + 
        "  description" + 
        ") values (?,?,?,?,?,?,?,?,?,?,?)");
        ps0.setString(1, "0");
        ps0.setString(2, "0");
        ps0.setString(3, "0");
        ps0.setString(4, "Y");
        ps0.setTimestamp(5, now);
        ps0.setString(6, "100");
        ps0.setTimestamp(7, now);
        ps0.setString(8, "100");
        ps0.setString(9, "OBPOS_LEGACYPROVIDER");
        ps0.setString(10, "Legacy Payment Provider");
        ps0.setString(11, "System legacy provider for backward compatibility with then Payment Provider and Refund Provider definitions.");
      
        ps0.executeUpdate();
        ps0.close();
      }
      
      PreparedStatement ps1 = cp
          .getPreparedStatement( //
              "UPDATE OBPOS_APP_PAYMENT_TYPE SET OBPOS_PAYMENTGROUP_ID = '0' " + //
              "WHERE OBPOS_PAYMENTGROUP_ID IS NULL AND (PAYMENTPROVIDER IS NOT NULL OR REFUNDPROVIDER IS NOT NULL)");
      ps1.executeUpdate();
      ps1.close();
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // Always, as it contains a record that must exist
    return new ModuleScriptExecutionLimits(RETAIL_MODULE_ID, null, null);
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
