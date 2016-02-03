/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
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