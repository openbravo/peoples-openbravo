/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.apache.log4j.Logger;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * @author MAL
 * Fixes issue 27695: "Invoice Paid" field at Payment schedule Details level is not properly set by Cash Up process
 */
public class FixDataIssue27695 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue27695.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      String exists = FixDataIssue27695Data.selectExistsPreference(cp);
      // if there are not records affected, do not execute
      if (!exists.equals("0")) {
        log4j.debug("Fix 27695 not needed.");
        return;
      }

      int count = FixDataIssue27695Data.fixPaymentDetails(cp);
      log4j.debug("Fixed " + count + " payment details with invoice paid a N");
      FixDataIssue27695Data.insertPreference(cp);

    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {
    // This method is provided for testing purposes.
    FixDataIssue27695 t = new FixDataIssue27695();
    t.execute();
  }
}
