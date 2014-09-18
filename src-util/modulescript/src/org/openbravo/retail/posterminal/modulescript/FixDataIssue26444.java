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

public class FixDataIssue26444 extends ModuleScript {

  @Override
  // Inserting m_inoutline_id for invoicelines which have this field as null
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      CallableStatement cs = cp.getConnection().prepareCall("{call OBPOS_FIXISSUE26444()}");
      cs.execute();
      cs.close();
    } catch (Exception e) {
      System.out.println("error");
      handleError(e);
    }
  }
}
