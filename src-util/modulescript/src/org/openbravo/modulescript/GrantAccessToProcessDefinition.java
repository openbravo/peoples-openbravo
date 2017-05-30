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
 * All portions are Copyright (C) 2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class GrantAccessToProcessDefinition extends ModuleScript {
  private static final Logger log4j = Logger.getLogger(GrantAccessToProcessDefinition.class);
  private static final String AD_PROCESS_ACCESS_TABLE_ID = "197";
  private static final String NEW_PURCHASE_ORDER_REPORT_ID = "4BDE0AF5E8C44B6C9575E388AAECDF69";
  private static final String OLD_PURCHASE_ORDER_REPORT_ID = "800171";

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      String[] newIdArray = { NEW_PURCHASE_ORDER_REPORT_ID };
      String[] oldIdArray = { OLD_PURCHASE_ORDER_REPORT_ID };
      for (int i = 0; i < newIdArray.length; i++) {
        String newId = newIdArray[i];
        String oldId = oldIdArray[i];
        GrantAccessToProcessDefinitionData
            .grantAccess(cp, newId, oldId, AD_PROCESS_ACCESS_TABLE_ID);
        GrantAccessToProcessDefinitionData[] rolesToBeUpdated = GrantAccessToProcessDefinitionData
            .getRolesToBeUpdated(cp, oldId, AD_PROCESS_ACCESS_TABLE_ID);
        for (int j = 0; j < rolesToBeUpdated.length; j++) {
          log4j
              .info("Has been detected that the role "
                  + rolesToBeUpdated[j].getField("role_name")
                  + " has access to "
                  + rolesToBeUpdated[j].getField("process_name")
                  + ". Notice that you should apply the corresponding dataset again in order to let that role access to the new report version.");
        }

      }

    } catch (Exception e) {
      handleError(e);
    }
  }
}
