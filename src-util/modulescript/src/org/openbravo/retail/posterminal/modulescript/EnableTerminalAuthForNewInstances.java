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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

/**
 *
 * @author galvarez insert a new preference to new version starting on RR16Q1 to enable terminal
 *         authentication by default
 */
public class EnableTerminalAuthForNewInstances extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(EnableTerminalAuthForNewInstances.class);

  @Override
  public void execute() {

    try {
      ConnectionProvider cp = getConnectionProvider();
      String moduleScriptAlreadyExecuted = EnableTerminalAuthForNewInstancesData.selectExecutionPreference(cp);
      if (moduleScriptAlreadyExecuted.equals("1")) {
        log4j
        .debug("-EnableTerminalAuthForNewInstances- This modules script have been already executed");
        return;
      }

      String exists1 = EnableTerminalAuthForNewInstancesData.selectIsNewInstance(cp);
      String exists2 = EnableTerminalAuthForNewInstancesData
          .selectCurrentTerminalAuthenticationPreferences(cp);
      String exists3 = EnableTerminalAuthForNewInstancesData
          .selectCurrentTerminalAuthenticationPreferencesEnabled(cp);
      int numberOfTerminals = Integer.parseInt(exists1);
      int definedPrefs = Integer.parseInt(exists2);
      int prefsWithValueTrue = Integer.parseInt(exists3);
      // This MS just will be executed when terminals are defined and new preferences for terminal authentication are NOT defined
      if (numberOfTerminals > 0) {
        if (definedPrefs == 1) {
          EnableTerminalAuthForNewInstancesData[] clientIds = EnableTerminalAuthForNewInstancesData
              .selectClientIds(cp);
          for (int i = 0; i < clientIds.length; i++) {
            String clientId = clientIds[i].clientid;
            int prefs = EnableTerminalAuthForNewInstancesData
                .insertNewTerminalAuthenticationPreference(cp, clientId, "N", "0");
            log4j
                .debug("-EnableTerminalAuthForNewInstances- " + prefs + " Preference -OBPOS_TerminalAuthentication- with value 'Y' have been created for client "
                    + clientId + ".");
          }
        } else if (definedPrefs == 0) {
          //Should not happen
          int prefs = EnableTerminalAuthForNewInstancesData
              .insertNewTerminalAuthenticationPreference(cp, "0", "Y", null);
          log4j
          .debug("-EnableTerminalAuthForNewInstances- There are no preferences for property -OBPOS_TerminalAuthentication- Defaut one was created.");
        } else {
          //Customer has already defined preference for this functionality. The MS will not do nothing.
          log4j
              .debug("-EnableTerminalAuthForNewInstances- There are preferences for property -OBPOS_TerminalAuthentication- alredy defined. Nothing was done.");
        }
      } else {
        log4j
            .debug("-EnableTerminalAuthForNewInstances- Module script executed but nothing was done because there are no terminals in this instance.");
      }
      EnableTerminalAuthForNewInstancesData.insertExecutionPreference(cp);
    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {

    // This method is provided for testing purposes.

    EnableTerminalAuthForNewInstances t = new EnableTerminalAuthForNewInstances();
    t.execute();
  }
}
