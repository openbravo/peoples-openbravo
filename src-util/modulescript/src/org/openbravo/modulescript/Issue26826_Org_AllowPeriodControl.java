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
package org.openbravo.modulescript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;

public class Issue26826_Org_AllowPeriodControl extends ModuleScript {
  private static final Logger log4j = Logger.getLogger(Issue26826_Org_AllowPeriodControl.class);
  
  @Override
  // Sets AD_Org.AllowPeriodControl = N where organization type is 
  // neither a business unit nor a legal entity with accounting
  public void execute() {
    try {
      int updatedOrgs = Issue26826OrgAllowPeriodControlData.updateOrganizations(getConnectionProvider());
      if (updatedOrgs > 0 ) {
        log4j.info("Updated " + updatedOrgs+ " organizations. ");
      }
    } catch (Exception e) {
      handleError(e);
    }
  }
}