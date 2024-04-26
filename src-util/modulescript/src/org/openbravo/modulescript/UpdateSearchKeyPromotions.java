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

public class UpdateSearchKeyPromotions extends ModuleScript {

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      PreparedStatement ps = cp.getPreparedStatement(
          "UPDATE M_Offer SET value = SUBSTRING(name, 1, 30) WHERE value IS NULL");
      ps.executeUpdate();
    } catch (Exception e) {
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits("0", null, new OpenbravoVersion(3, 0, 243000));
  }
}
