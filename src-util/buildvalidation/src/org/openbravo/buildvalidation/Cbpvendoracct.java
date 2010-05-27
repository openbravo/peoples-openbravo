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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.database.ConnectionProvider;

/**
 * This validation is related to this issue: https://issues.openbravo.com/view.php?id=12824 The
 * unique constraint C_BP_VENDOR_ACCT_ACCTSCHEMA_UN was made more restrictive, and it will fail if
 * the data is not correct and is not fixed before updating.
 */
public class Cbpvendoracct extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      int a = Integer.parseInt(CbpvendoracctData.countNoDistinct(cp));
      int b = Integer.parseInt(CbpvendoracctData.countDistinct(cp));
      if (a != b) {
        errors
            .add("Due to a database constraint modification, is no longer allowed to have more than one entry in business partner -> vendor -> accounting tab, for the same accounting schema and status. In previous releases, one per organization was allowed, but is not the case now. There exists data in your database that do not fit this new constraint. Please fix it before updating the database.");
      }
    } catch (Exception e) {
      return handleError(e);
    }
    return errors;
  }

}
