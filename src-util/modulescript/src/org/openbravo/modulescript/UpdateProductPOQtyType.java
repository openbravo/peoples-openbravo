/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.modulescript;

import java.sql.PreparedStatement;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class UpdateProductPOQtyType extends ModuleScript {

  public void execute() {
    try {
      
      ConnectionProvider cp = getConnectionProvider();
      UpdateProductPOQtyTypeData.update(cp);  
    } catch (Exception e) {
      handleError(e);
    }
  }
}