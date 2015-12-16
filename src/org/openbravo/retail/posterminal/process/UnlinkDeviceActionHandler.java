/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.process;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.OBPOSApplications;

public class UnlinkDeviceActionHandler extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject selectedObject = null;
    String terminalId = null;
    JSONObject result = new JSONObject();
    JSONArray actions = new JSONArray();
    JSONObject msg = new JSONObject();
    JSONObject showMsgInView = new JSONObject();
    try {
      selectedObject = new JSONObject(content);
      terminalId = selectedObject.getString("inpobposApplicationsId");
      OBPOSApplications terminal = OBDal.getInstance().get(OBPOSApplications.class, terminalId);
      terminal.setLinked(false);
      OBDal.getInstance().save(terminal);
      OBDal.getInstance().getConnection().commit();

      msg.put("msgType", "success");
      msg.put("msgTitle", OBMessageUtils.messageBD("OBPOS_UnlinkDeviceSuccessTitle"));
      msg.put("msgText", OBMessageUtils.messageBD("OBPOS_UnlinkDeviceSuccessMsg"));
      showMsgInView.put("showMsgInView", msg);
      actions.put(showMsgInView);
      result.put("responseActions", actions);

    } catch (Exception e) {
      try {
        msg.put("msgType", "error");
        msg.put("msgTitle", OBMessageUtils.messageBD("OBPOS_UnlinkDeviceErrorTitle"));
        msg.put("msgText", OBMessageUtils.messageBD("OBPOS_UnlinkDeviceErrorMsg"));
        showMsgInView.put("showMsgInView", msg);
        actions.put(showMsgInView);
        result.put("responseActions", actions);
        return result;
      } catch (JSONException e1) {
        // won't happen
      }
    }
    return result;
  }
}