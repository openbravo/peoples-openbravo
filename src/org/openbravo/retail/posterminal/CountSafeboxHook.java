/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.Date;

import org.codehaus.jettison.json.JSONObject;

public interface CountSafeboxHook {
  public JSONObject exec(OBPOSSafeBox safeBox, JSONObject jsonCountSafeBox, Date countSafeBoxDate,
      JSONObject jsonBeingSent) throws Exception;
}
