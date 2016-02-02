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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import java.sql.PreparedStatement;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class FixDataIssue31842 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue31842.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      String exists = FixDataIssue31842Data.selectExistsPreference(cp);
      // if preference created, do not execute
      if (!exists.equals("0")) {
        log4j.debug("Fix 31842 not needed.");
        return;
      }

      FixDataIssue31842Data.fixPOSOverpaymentLimit(cp);
      FixDataIssue31842Data.insertPreference(cp);

    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {
    FixDataIssue31842 t = new FixDataIssue31842();
    t.execute();
  }
}