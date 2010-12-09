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

package org.openbravo.erpCommon.modules;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalInitializingTask;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.services.webservice.Module;

public class CheckLocalConsistency extends DalInitializingTask {
  private static final Logger log4j = Logger.getLogger(CheckLocalConsistency.class);

  @Override
  protected void doExecute() {
    VariablesSecureApp vars = new VariablesSecureApp("0", "0", "0");

    OBError msg = new OBError();

    try {
      Module[] modulesToInstall = new Module[0];
      Module[] modulesToUpdate = new Module[0];
      Module[] modulesToMerge = new Module[0];

      VersionUtility.setPool(new DalConnectionProvider());

      boolean checked = VersionUtility.checkLocal(vars, modulesToInstall, modulesToUpdate,
          modulesToMerge, msg);
      if (checked) {
        System.out.println("Local Dependencies are OK");
        System.out.println("=========================");

      } else {
        System.out.println("Local Dependencies are not satisfied");
        System.out.println("====================================\n");

        System.out.println(msg.getMessage());
      }
    } catch (Exception e) {
      log4j.error("Error", e);
    }

  }

}
