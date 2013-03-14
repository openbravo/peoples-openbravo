/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.event;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.PeriodControlUtility;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.calendar.PeriodControl;
import org.openbravo.model.financialmgmt.calendar.PeriodControlLog;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class OpenClosePeriodHandler extends BaseActionHandler {
  static Logger log4j = Logger.getLogger(OpenClosePeriodHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);

      final String action = request.getString("action");
      final JSONArray periodIdList = request.getJSONArray("recordIdList");
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();

      if ("ACTION_COMBO".equals(action)) {
        int size = periodIdList.length();
        if (size == 0) {
          return response;
        }
        response.put("actionComboBox", getActionComboBox("", vars));
        return response;
      } else {
        if (periodIdList.length() == 0) {
          throw new OBException("@NotSelected@");
        }
        if (OBDal.getInstance().get(Period.class, periodIdList.get(0)) == null) {
          for (String id : PeriodControlUtility.parseJSON(periodIdList)) {
            PeriodControl pc = OBDal.getInstance().get(PeriodControl.class, id);
            if (action.equals(pc.getPeriodStatus())) {
              continue;
            }
            pc.setPeriodAction(action);
            OBDal.getInstance().save(pc);
            OBDal.getInstance().flush();
            OBError error = PeriodControlUtility.openClosePeriodControl(id);
            if ("Error".equals(error.getType())) {
              throw new OBException(error.getMessage());
            }
          }
        } else {
          for (Period p : PeriodControlUtility.getOrderedPeriods(periodIdList)) {
            PeriodControlLog pcl = OBProvider.getInstance().get(PeriodControlLog.class);
            pcl.setClient(p.getClient());
            pcl.setOrganization(p.getOrganization());
            pcl.setCalendar(p.getYear().getCalendar());
            pcl.setCascade(true);
            pcl.setPeriodAction(action);
            pcl.setPeriodNo(p);
            pcl.setPeriod(p);
            pcl.setYear(p.getYear());
            pcl.setProcessed(false);
            pcl.setProcessNow(false);
            OBDal.getInstance().save(pcl);
            OBDal.getInstance().flush();
            OBError error = PeriodControlUtility.openClosePeriod(pcl);
            if ("Error".equals(error.getType())) {
              throw new OBException(error.getMessage());
            }
          }
        }
        OBDal.getInstance().commitAndClose();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "success");
        errorMessage.put("text",
            Utility.messageBD(new DalConnectionProvider(), "ProcessOK", vars.getLanguage()));
        response.put("message", errorMessage);
      }
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log4j.error("OpenCloseProcess error: " + e.getMessage(), e);

      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      try {
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        response.put("message", errorMessage);
      } catch (JSONException ignore) {
        ignore.printStackTrace();
      }
    }

    return response;

  }

  /**
   * Converts the OBError message to a JSONObject message.
   * <p>
   * Mapping: type <-> severity, message <-> text
   * 
   * @param obError
   *          OBError.
   * @return JSONObject containing the OBError information.
   */
  public static JSONObject convertOBErrorToJSON(OBError obError) {
    JSONObject errorMessage = new JSONObject();
    try {
      errorMessage.put("severity", obError.getType().toLowerCase());
      errorMessage.put("text", obError.getMessage());
    } catch (JSONException ignore) {
      ignore.printStackTrace();
    }
    return errorMessage;
  }

  private JSONObject getActionComboBox(String action, VariablesSecureApp vars) throws Exception {
    final String PERIOD_CONTROL_WINDOW_ID = "6AE1A09CAAD945F78C3E05A484A1F07A";
    final String VALID_PERIOD_CONTROL_ACTION_VALIDATION = "CA6741A18A214FE9A50FDFC398662235";
    final String ACTIONS_REF = "176";
    String defaultValue = null;
    JSONObject response = new JSONObject();
    DalConnectionProvider conn = new DalConnectionProvider(false);
    ComboTableData comboTableData = new ComboTableData(vars, conn, "LIST", "", ACTIONS_REF,
        VALID_PERIOD_CONTROL_ACTION_VALIDATION, Utility.getContext(conn, vars,
            "#AccessibleOrgTree", PERIOD_CONTROL_WINDOW_ID), Utility.getContext(conn, vars,
            "#User_Client", PERIOD_CONTROL_WINDOW_ID), 0);
    Utility.fillSQLParameters(conn, vars, null, comboTableData, PERIOD_CONTROL_WINDOW_ID, "");
    FieldProvider[] fpArray = comboTableData.select(false);
    JSONObject valueMap = new JSONObject();
    for (FieldProvider fp : fpArray) {
      String key = fp.getField("id");
      String value = fp.getField("name");
      if (defaultValue != null) {
        defaultValue = key;
      }
      valueMap.put(key, value);
    }
    response.put("valueMap", valueMap);
    response.put("defaultValue", defaultValue);
    return response;
  }

}