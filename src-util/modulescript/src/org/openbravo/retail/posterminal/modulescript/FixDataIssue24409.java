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
 * Fixes issue 24409: "Invoice Paid" field at Payment schedule Details level is not properly set by Cash Up process
 */
public class FixDataIssue24409 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue24409.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      String exists = FixDataIssue24409Data.selectExistsPreference(cp);
      // if there are not records affected, do not execute
      if (!exists.equals("0")) {
        log4j.debug("Fix 24409 not needed.");
        return;
      }

      int count = FixDataIssue24409Data.fixPOSQuotationDelivery(cp);
      log4j.debug("Fixed " + count + " quotations lines created with delivered quantity.");
      FixDataIssue24409Data.insertPreference(cp);

    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {
    // This method is provided for testing purposes.
    FixDataIssue24409 t = new FixDataIssue24409();
    t.execute();
  }
}
