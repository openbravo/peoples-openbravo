/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.actionHandler;

import java.sql.PreparedStatement;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

public class DeletionAutoRegisterHwmUrlActionHandler extends BaseProcessActionHandler {
  private static final Logger logger = Logger
      .getLogger(DeletionAutoRegisterHwmUrlActionHandler.class);
  private static final String HARDWARE_MANAGER_AUTO_REGISTER = "HardwareManagerAutoRegister";
  private static final String HARDWAREID = "hardwareid";
  private static final String ERROR = "Error in delete hardware manager process";
  private static final String ERROR_RESPONSE = "Error in delete hardware manager create response";
  private static final String ERROR_DEPENDENCIES = "Exception in delete hardware manager dependencies process";
  private static final String PARAMS = "_params";
  private static final String GRID_NAME = "gridName";
  private static final String SELECTION = "_selection";
  private static final String MSGTYPE = "msgType";
  private static final String MSGTITLE = "msgTitle";
  private static final String MSGTEXT = "msgText";
  private static final String REFRESH_GRID_PARAMETER = "refreshGridParameter";
  private static final String RESPONSE_ACTIONS = "responseActions";
  private static final String SHOW_MSG_IN_GRID_VIEW = "showMsgInProcessView";
  private static final String INFO = "info";
  private JSONArray hardwaremngArray;

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {
      JSONObject request = new JSONObject(content);
      Integer hardwaremngDeleted = 0;
      String hardwareid = null;
      hardwaremngArray = request.getJSONObject(PARAMS).getJSONObject(HARDWARE_MANAGER_AUTO_REGISTER)
          .getJSONArray(SELECTION);
      for (int j = 0; j < hardwaremngArray.length(); j++) {
        JSONObject selectedElem = hardwaremngArray.getJSONObject(j);
        hardwareid = selectedElem.getString(HARDWAREID);
        deleteHardwareUrl(hardwareid);
        deleteHardwareManager(hardwareid);
        hardwaremngDeleted++;
      }
      return createResponse(hardwaremngDeleted);
    } catch (JSONException e) {
      logger.error(ERROR, e);
      return new JSONObject();
    }
  }

  private void deleteHardwareManager(String hardwaremngid) {
    try {
      String sql = "delete from obpos_hardwaremng where obpos_hardwaremng_id = '" + hardwaremngid
          + "'";
      PreparedStatement ps = new DalConnectionProvider(false).getPreparedStatement(sql);
      ps.executeUpdate();
    } catch (Exception e) {
      logger.error(ERROR);
    }
  }

  private void deleteHardwareUrl(String hardwaremanagerid) {
    try {
      String sql = "delete from obpos_hardwareurl " + "where obpos_hardwaremng_id = '"
          + hardwaremanagerid + "'\n";
      PreparedStatement ps = new DalConnectionProvider(false).getPreparedStatement(sql);
      ps.executeUpdate();
    } catch (Exception e) {
      logger.error(ERROR_DEPENDENCIES);
    }
  }

  private JSONObject createResponse(Integer hardwaremngDeleted) {
    JSONObject result = new JSONObject();
    JSONArray responseActions = new JSONArray();
    try {
      JSONObject showMessage = new JSONObject();
      JSONObject msg = new JSONObject();
      msg.put(MSGTYPE, INFO);
      msg.put(MSGTITLE, OBMessageUtils.getI18NMessage("OBPOS_DeletionHardwareManagerTitle", null));
      String[] params = { hardwaremngDeleted.toString() };
      msg.put(MSGTEXT, OBMessageUtils.getI18NMessage("OBPOS_DeletionHardwaremngProcess", params));
      showMessage.put(SHOW_MSG_IN_GRID_VIEW, msg);
      responseActions.put(showMessage);

      JSONObject refreshGridParameter = new JSONObject();
      JSONObject refreshGridParameterValue = new JSONObject();
      refreshGridParameterValue.put(GRID_NAME, HARDWARE_MANAGER_AUTO_REGISTER);
      refreshGridParameter.put(REFRESH_GRID_PARAMETER, refreshGridParameterValue);
      responseActions.put(refreshGridParameter);
      result.put(RESPONSE_ACTIONS, responseActions);
    } catch (JSONException ignore) {
      logger.error(ERROR_RESPONSE, ignore);
    }
    return result;
  }
}