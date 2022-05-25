/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.Date;

import org.codehaus.jettison.json.JSONObject;

/**
 * Classes implementing this interface will be executed before count safe boxes.
 * 
 */
public interface InitialCountSafeBoxHook {
  public void exec(OBPOSSafeBox safebox, OBPOSApplications touchpoint, Date countSafeBoxDate,
      JSONObject jsonCountSafeBox) throws Exception;
}

// Example of a hook:
// /**
// * When initial counting a safebox, it save the counted coins and notes
// */
// public class UpdateInitSafeboxCountCoinsAndNotesHook implements InitialCountSafeBoxHook {
// @Override
// public void exec(OBPOSSafeBox safebox, OBPOSApplications touchpoint, Date countSafeBoxDate,
// JSONObject jsonCountSafeBox) throws Exception {
// // your code here
// }
// }
