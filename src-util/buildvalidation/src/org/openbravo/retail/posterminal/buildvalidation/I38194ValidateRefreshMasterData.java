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
package org.openbravo.retail.posterminal.buildvalidation;

import java.util.ArrayList;
import java.util.List;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.base.ExecutionLimits;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * This validation is related to this issue 25464. A unique constraint has been added to
 * ad_module.javapackage column, this validation checks there are no multiple modules with the same
 * javapackage.
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
            + data[i].minutestorefreshdatainc + ", Time to Show Incremental Refresh Popup: "
            + data[i].minutestoreshowfreshdatainc;
        errors.add(msg);
      }
      return errors;
    } catch (Exception e) {
      return handleError(e);
    }
  }

  @Override
  protected ExecutionLimits getBuildValidationLimits() {
    return new ExecutionLimits("FF808181326CC34901326D53DBCF0018", null, new OpenbravoVersion(1, 2, 5500));
  }
}