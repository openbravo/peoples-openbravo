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

/**
 * @author MAC
 * Fixes issue 33744: FIN_PaymentScheduleDetail created from POS doesn't include the Business Partner
 */
public class FixDataIssue33744 extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(FixDataIssue33744.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      String exists = FixDataIssue33744Data.selectExistsPreference(cp);
      // if preference created, do not execute
      if (!exists.equals("0")) {
        log4j.debug("Fix 31842 not needed.");
        return;
      }

      FixDataIssue33744Data.fixFinPaymentScheduleDetailInvoices(cp);
      FixDataIssue33744Data.fixFinPaymentScheduleDetailOrders(cp);
      FixDataIssue33744Data.insertPreference(cp);

    } catch (Exception e) {
      handleError(e);
    }
  }
}
