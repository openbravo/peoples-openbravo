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
 * All portions are Copyright (C) 2014-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import java.sql.PreparedStatement;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.OpenbravoVersion;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;

public class ModuleScriptPosterminal extends ModuleScript {
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";
  
  public void execute() {
    try {
      String qry = "UPDATE OBPOS_Applications SET obpos_c_bpartner_loc_id = (SELECT MAX(cbl.C_BPartner_Location_id) "
          + "FROM C_BPartner_Location cbl WHERE OBPOS_Applications.C_BPartner_id = cbl.C_BPartner_id) "
          + "WHERE C_BPartner_id IS NOT NULL AND obpos_c_bpartner_loc_id IS NULL";
      ConnectionProvider cp = getConnectionProvider();
      PreparedStatement ps = cp.getPreparedStatement(qry);
      ps.executeUpdate();
    } catch (Exception e) {
      handleError(e);
    }
  }
  
  
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // The module script needs to be executed only when updating from a version
    // lower than 3.0RR14Q2 (Retail pack)(1.8.330)
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 330));
  }
  
  @Override
  protected boolean executeOnInstall() {
    return false;
  }
}