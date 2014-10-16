/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class FixErrorTableData extends ModuleScript {

  @Override
  //Will fix data error types in Q3 and subsequent releases
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      PreparedStatement fixOrders = cp.getConnection().prepareStatement("UPDATE obpos_errors SET typeofdata='Order' WHERE typeofdata='order'");
      fixOrders.executeUpdate();
      fixOrders.close();
      PreparedStatement fixCashMgmt = cp.getConnection().prepareStatement("UPDATE obpos_errors SET typeofdata='FIN_Finacc_Transaction' WHERE typeofdata='CM'");
      fixCashMgmt.executeUpdate();
      fixCashMgmt.close();
      PreparedStatement fixCashUp = cp.getConnection().prepareStatement("UPDATE obpos_errors SET typeofdata='OBPOS_App_Cashup' WHERE typeofdata='CU'");
      fixCashUp.executeUpdate();
      fixCashUp.close();
      PreparedStatement fixCustomers = cp.getConnection().prepareStatement("UPDATE obpos_errors SET typeofdata='BusinessPartner' WHERE typeofdata='BP'");
      fixCustomers.executeUpdate();
      fixCustomers.close();
    } catch (Exception e) {
      System.out.println("error");
      handleError(e);
    }
  }
}
