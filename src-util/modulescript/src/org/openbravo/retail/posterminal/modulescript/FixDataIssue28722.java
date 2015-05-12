/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class FixDataIssue28722 extends ModuleScript {

  @Override
  // Initialize the currency for customers created from the POS. 
  // Related to the issue https://issues.openbravo.com/view.php?id=28722
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      boolean isExecuted= FixDataIssue28722Data.isExecuted(cp);
      if (!isExecuted){
        FixDataIssue28722Data.initializeCurrency(cp);
        FixDataIssue28722Data.createPreference(cp);
      }
    } catch (Exception e) {
      handleError(e);
    }
  }
}
