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
 * All portions are Copyright (C) 2015-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 *
 * @author galvarez
 * 
 * By default there is a preference introduced in 16Q1 which enables terminal authentication. 
 * The aim of this MS is to create a preference with value = 'N' for those instances which are already running without terminal authentication.
 * We are doing that because we don't want to enforce to that instances to use terminal authentication.
 * 
 * - For new instances the default preference will be used, so Terminal Authentication will be enabled.
 * - For instances which already have preferences related to terminal authentication, the module script will not do nothing.
 * 
 * update (jan 2017): This module script will just be executed when the coming from version is lower than 16Q1.
 * the use of a preference to check if the MS has been already executed is not longer needed.
 *
 */
public class EnableTerminalAuthForNewInstances extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(EnableTerminalAuthForNewInstances.class);
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";
  
  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

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
    } catch (Exception e) {
      handleError(e);
    }
  }
  
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    // The module script needs to be executed only when updating from a version
    // lower than 16Q1 (Retail pack)(1.8.1703)
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 1703));
  }
  
  @Override
  protected boolean executeOnInstall() {
    return false;
  }

  public static void main(String[] args) {

    // This method is provided for testing purposes.

    EnableTerminalAuthForNewInstances t = new EnableTerminalAuthForNewInstances();
    t.execute();
  }
}
