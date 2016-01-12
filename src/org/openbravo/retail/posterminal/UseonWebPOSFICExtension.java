/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.window.FICExtension;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Characteristic;

public class UseonWebPOSFICExtension implements FICExtension {

  @Override
  public void execute(String mode, org.openbravo.model.ad.ui.Tab tab,
      Map<String, JSONObject> columnValues, BaseOBObject row, List<String> changeEventCols,
      List<JSONObject> calloutMessages, List<JSONObject> attachments, List<String> jsExcuteCode,
      Map<String, Object> hiddenInputs, int noteCount, List<String> overwrittenAuxiliaryInputs) {

    if (!"D28DF93499FC45D4B1DC4C0029FFA914".equals(tab.getId())) {
      return;
    }
    // Add characteristic field to list of columns that triggers the call to the FIC
    changeEventCols.add("inpmCharacteristicId");

    if (!mode.equals("CHANGE")) {
      return;
    }
    String strChId = RequestContext.get().getRequestParameter("inpmCharacteristicId");
    final Characteristic charac = OBDal.getInstance().get(Characteristic.class, strChId);

    if (!charac.isObposUseonwebpos()) {
      JSONObject msg = new JSONObject();
      try {
        Map<String, String> map = new HashMap<String, String>();
        map.put("characteristic", charac.getName());
        msg.put("text", OBMessageUtils.parseTranslation(
            OBMessageUtils.messageBD("EM_OBPOS_DISCCHARACWARNING"), map));
        msg.put("severity", "TYPE_WARNING");
      } catch (JSONException ignore) {
      }
      calloutMessages.add(msg);
    }
  }
}
