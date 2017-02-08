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
 * All portions are Copyright (C) 2013-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class FixDataIssue25555 extends ModuleScript {
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";

  @Override
  // Inserting m_inoutline_id for invoicelines which have this field as null
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      CallableStatement cs = cp.getConnection().prepareCall("{call OBPOS_FIXISSUE25555()}");
      cs.execute();
      cs.close();
    } catch (Exception e) {
      System.out.println("error");
      handleError(e);
    }
  }
  
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // The module script needs to be executed only when updating from a version
    // lower than RMP31.1 (RMP31 was not published) (Retail pack)(1.8.220)
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 220));
  }
  
  @Override
  protected boolean executeOnInstall() {
    return false;
  }
}
