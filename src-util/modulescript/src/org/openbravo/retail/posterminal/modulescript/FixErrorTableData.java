/* 
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
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
