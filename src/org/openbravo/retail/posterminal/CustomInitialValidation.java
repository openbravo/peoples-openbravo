/*
 ************************************************************************************
 * Copyright (C) 2016-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONException;

public abstract class CustomInitialValidation {

  public abstract void validation(OBPOSApplications posTerminal) throws JSONException;

  /**
   * Returns a boolean value which will be used to execute the InitialValidation.
   * 
   * If return's true, InitialValidation will be executed and vice versa
   *
   * @param appName
   *          an application name
   * @return boolean value (by default "true")
   */
  public boolean executeForApplication(String appName) throws JSONException {
    return true;
  }
}
