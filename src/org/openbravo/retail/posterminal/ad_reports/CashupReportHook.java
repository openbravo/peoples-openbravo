/*
 ************************************************************************************
 * Copyright (C) 2013-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.ad_reports;

import java.util.HashMap;
import java.util.List;

import org.openbravo.retail.posterminal.OBPOSAppCashup;

public interface CashupReportHook {
  public CashupReportHookResult exec(OBPOSAppCashup cashup,
      List<HashMap<String, String>> hashMapList, HashMap<String, Object> parameters)
      throws Exception;
}

// Example of a hook:
//
// @ApplicationScoped
// public class CashupReportHookTest implements CashupReportHook {
//
// @Override
// public CashupReportHookResult exec(OBPOSAppCashup cashup, List<HashMap<String, String>>
// hashMapList, HashMap<String, Object> parameters) throws Exception {
// // your code here
// return null; // If not null it is an extra message and action to show in the success dialog in
// the client
// side.
// }
//
// }
