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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.apache.log4j.Logger;
import org.openbravo.utils.FormatUtilities;

/**
 * 
 * @author adrian
 */
public class UpdateEmailPasswords extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(UpdateEmailPasswords.class);

  @Override
  public void execute() {

    // Updating email passwords required to fix issue 13688
    // This script will be executed only if we are upgrading from a version
    // 2.50MP18 or smaller
    // This script just encripts ALL smtpserverpassword values of the table
    // C_POC_CONFIGURATION

    try {
      ConnectionProvider cp = getConnectionProvider();

      OpenbravoVersion versionCurrent = new OpenbravoVersion(CheckCoreVersionData.select(cp));
      OpenbravoVersion version250MP18 = new OpenbravoVersion(2, 50, 17410);

      log4j.info("Starting Update SMPT server passwords module script.");
      log4j.info("Current version: " + versionCurrent.toString());
      log4j.info("2.50 MP18 version:" + version250MP18.toString());

      if (versionCurrent.compareTo(version250MP18) <= 0) {
        log4j.info("Encripting SMPT server password fields.");
        UpdateEmailPasswordsData[] emails = UpdateEmailPasswordsData.select(cp);
        for (UpdateEmailPasswordsData email : emails) {
          if (email.smtpserverpassword != null) {
            UpdateEmailPasswordsData.update(cp, FormatUtilities.encryptDecrypt(
                email.smtpserverpassword, true), email.cPocConfigurationId);
          }
        }
      } else {
        log4j.info("No need to encript SMPT server password fields.");
      }

    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {

    // This method is provided for testing purposes.

    UpdateEmailPasswords t = new UpdateEmailPasswords();
    t.execute();
  }
}
