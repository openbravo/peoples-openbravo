/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;

public class HardwareURL extends QueryTerminalProperty {
  public static final String hardwareUrlPropertyExtension = "OBPOS_HardwareMng";

  @Inject
  @Any
  @ComponentProvider.Qualifier(hardwareUrlPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    HQLPropertyList hardwareMngHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    return Arrays.asList(new String[] { //
        "select " + hardwareMngHQLProperties.getHqlSelect() + "from OBPOS_HardwareURL as p " //
            + "where p.pOSTerminalType.id = :terminalTypeID " //
            + "and p.$readableSimpleCriteria and p.$activeCriteria " //
            + "ORDER BY p.obposHardwaremng.name" });
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    final OBPOSApplications posDetail = POSUtils.getTerminalById(jsonsent.getString("pos"));
    Map<String, Object> paramValues = new HashMap<>();
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
