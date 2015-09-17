/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.RequestContext;

public class HardwareURL extends QueryTerminalProperty {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();

    return Arrays.asList(new String[] { //
        "select terminal.obposTerminaltype.oBPOSHardwareURLList " //
            + "from OBPOS_Applications terminal where terminal.id = '" + posId + "'" //
        });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public String getProperty() {
    return "hardwareURL";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}