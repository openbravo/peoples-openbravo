/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

public class HardwareURL extends QueryTerminalProperty {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Arrays.asList(new String[] { //
        "select p.id as id, p.obposHardwaremng.name as _identifier, p.obposHardwaremng.hardwareURL as hardwareURL, p.obposHardwaremng.hasReceiptPrinter as hasReceiptPrinter, " //
            + "p.obposHardwaremng.hasPDFPrinter as hasPDFPrinter " //
            + "from OBPOS_HardwareURL as p " //
            + "where p.pOSTerminalType.id = :terminalTypeID " //
            + "and p.$readableSimpleCriteria and p.$activeCriteria " //
            + "ORDER BY p.obposHardwaremng.name" });
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    final OBPOSApplications posDetail = POSUtils.getTerminalById(RequestContext.get()
        .getSessionAttribute("POSTerminal").toString());
    Map<String, Object> paramValues = new HashMap<String, Object>();
    paramValues.put("terminalTypeID", posDetail.getObposTerminaltype().getId());
    return paramValues;
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