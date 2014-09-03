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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.apache.log4j.Logger;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 *
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
        log4j.debug("Fix 27425 no needed.");
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
