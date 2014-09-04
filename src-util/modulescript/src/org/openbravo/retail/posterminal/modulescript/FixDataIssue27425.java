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
 * @author RAL
 * Fixes issue 0027425: Cash up invoices are created as not paid since Q2.2
 */
public class FixDataIssue27425 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue27425.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      String isFixed = FixDataIssue27425Data.isFixed(cp);
      // if there are not records affected, do not execute
      if (isFixed.equals("0")) {
        log4j.debug("Fix 27425 not needed.");
        return;
      }

      FixDataIssue27425Data.fixWebPOSCashupInvoicesWithGrossZeroI(cp);
      int count = FixDataIssue27425Data.fixWebPOSCashupInvoicesWithGrossZeroII(cp);
      log4j.debug("Fixed " + count + " WebPOS cashup invoices with gross = 0");

      count = FixDataIssue27425Data.fixWebPOSCashupInvoicesSetAsNotPaid(cp);
      log4j.debug("Fixed " + count + " WebPOS cashup invoices set as not paid");
    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {
    // This method is provided for testing purposes.
    FixDataIssue27425 t = new FixDataIssue27425();
    t.execute();
  }
}
