/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class AddDefaultChannel extends ModuleScript {
  private static final String RETAIL_MODULE_ID = "FF808181326CC34901326D53DBCF0018";
  @Override
  // Adds system level default channel 'Brick and mortar'. It is already contained in a dataset. 
  // When updating instances the dataset is not properly loaded when constraints are enabled. 
  // This module script fixes that problem.
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      if(!"293AB6FFE1B74DD89071AEB8B308403B".equals(AddDefaultChannelData.selectChannel(cp))){
    	  AddDefaultChannelData.insertChannel(cp);
      }

    } catch (Exception e) {
      handleError(e);
    }
  }
  
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits(RETAIL_MODULE_ID, null, 
        new OpenbravoVersion(1,2,5900));
  }
}
