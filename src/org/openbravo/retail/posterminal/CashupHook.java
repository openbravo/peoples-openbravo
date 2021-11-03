/*
 ************************************************************************************
 * Copyright (C) 2013-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;

public interface CashupHook {
  public CashupHookResult exec(OBPOSApplications terminal, OBPOSAppCashup cashup,
      JSONObject cashUpJsonObj) throws Exception;
}

// Example of a hook:
//
// @ApplicationScoped
// public class CashupHookTest implements CashupHook {
//
// @Override
// public CashupHookResult exec(OBPOSApplications terminal, OBPOSAppCashup cashup, JSONObject
// cashUpJsonObj) throws Exception {
// // your code here
// return null; // If not null it is an extra message and action to show in the success dialog in
// the client
// side.
// }
//
// }
