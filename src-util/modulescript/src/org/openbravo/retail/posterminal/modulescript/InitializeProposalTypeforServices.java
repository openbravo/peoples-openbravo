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

public class InitializeProposalTypeforServices extends ModuleScript {
  
  private static final Logger log4j = Logger.getLogger(InitializeProposalTypeforServices.class);

  @Override
  // Initialize Proposal Type for Services Linked to Products.
  // This update is necessary due to EM_OBPOS_LINKPROD_PROPTYPE_CHK constraint in M_PRODUCT table.
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      
      String exists = InitializeProposalTypeforServicesData.selectExistsPreference(cp);
      // if preference not exists then preference "Initialize Proposal Type executed" is inserted
      if (!exists.equals("0")) {
        log4j.debug("There is no need to initialize Proposal Type for M_Product");
        return;
      }
      int count = InitializeProposalTypeforServicesData.initializeProposalType(cp);
      log4j.debug("Updated " + count + " M_Product records EM_OBPOS_PROPOSAL_TYPE");  
      InitializeProposalTypeforServicesData.insertPreference(cp);
      
    } catch (Exception e) {
      handleError(e);
    }
  }
}
