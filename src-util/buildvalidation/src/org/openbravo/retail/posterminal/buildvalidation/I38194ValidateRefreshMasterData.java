/*
************************************************************************************
* Copyright (C) 2018 Openbravo S.L.U.
* Licensed under the Openbravo Commercial License version 1.0
* You may obtain a copy of the License at
http://www.openbravo.com/legal/obcl.html
* or in the legal folder of this module distribution.
************************************************************************************
*/
package org.openbravo.retail.posterminal.buildvalidation;

import java.util.ArrayList;
import java.util.List;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.base.ExecutionLimits;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * This validation is related to this issue 38194. A unique constraint has been added to
 * OBPOS_TERMINALTYPE table, this validation checks MINUTESTORESHOWFRESHDATAINC column is
 * strictly less than MINUTESTOREFRESHDATAINC column.
 */
public class I38194ValidateRefreshMasterData extends BuildValidation {

  @Override
  public List<String> execute() {
    try {
      List<String> errors = new ArrayList<String>();
      ConnectionProvider cp = getConnectionProvider();
      I38194ValidateRefreshMasterDataData[] data = I38194ValidateRefreshMasterDataData.validate(cp);
      for (int i = 0; i < data.length; i++) {
        String msg = "\nPOS Terminal name: "
            + data[i].name
            + " - 'Time to Incrementally Refresh Masterdata' must be greater than 'Time to Show Incremental Refresh Popup'"
            + "\nCurrent values - Time to Incrementally Refresh Masterdata: "
            + data[i].minutestorefreshdatainc + ", Time to Show Incremental Refresh Popup: 5";
        errors.add(msg);
      }
      return errors;
    } catch (Exception e) {
      return handleError(e);
    }
  }

  @Override
  protected boolean executeOnInstall() {
    return false;
  }

  @Override
  protected ExecutionLimits getBuildValidationLimits() {
    return new ExecutionLimits("FF808181326CC34901326D53DBCF0018", null, new OpenbravoVersion(1, 2, 5500));
  }
}