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
 * All portions are Copyright (C) 2023 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.SessionInfo;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.service.json.JsonUtils;

/**
 * Action handler which can enable/disable multiple records in one transaction.
 */
@ApplicationScoped
public class EnableOrDisableMultipleRecords extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    OBContext.setAdminMode(true);
    try {
      final JSONObject dataObject = new JSONObject(data);
      final String tabId = dataObject.getString("tabId");
      final boolean action = Boolean.parseBoolean(dataObject.getString("action"));
      final JSONArray jsonRecordIds = dataObject.getJSONArray("recordIds");
      HashSet<String> recordIds = new HashSet<String>();
      for (int i = 0; i < jsonRecordIds.length(); i++) {
        recordIds.add((String) jsonRecordIds.get(i));
      }
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      String tableId = tab.getTable().getId();
      Entity entity = ModelProvider.getInstance().getEntityByTableId(tableId);
      String entityName = entity.getName();

      int updateCount = 0;
      final JSONObject jsonResponse = new JSONObject();

      if (OBContext.getOBContext().getEntityAccessChecker().isWritable(entity)) {

        //@formatter:off
              final String hql =
                  "update " + entityName +
                  "   set active = :action," +
                  "       updated = :newDate," +
                  "       updatedBy = :user" +
                  " where id in (:recordIds)"+
                  "   and active != :action"+
                  "   and client = :clientId"+
                  "   and organization.id in (:writableOrgs)";
              //@formatter:on
        updateCount = OBDal.getInstance()
            .getSession()
            .createQuery(hql)
            .setParameter("action", action)
            .setParameter("newDate", new Date())
            .setParameter("user", OBContext.getOBContext().getUser())
            .setParameter("recordIds", recordIds)
            .setParameter("clientId", OBContext.getOBContext().getCurrentClient())
            .setParameter("writableOrgs", OBContext.getOBContext().getWritableOrganizations())
            .executeUpdate();

      }

      // Set information for audit trail
      SessionInfo.setProcessType("W");
      SessionInfo.setProcessId(tab.getWindow().getId());
      SessionInfo.setUserId(OBContext.getOBContext().getUser().getId());
      SessionInfo.saveContextInfoIntoDB(OBDal.getInstance().getConnection(false));

      jsonResponse.put("updateCount", updateCount);
      return jsonResponse;
    } catch (Exception e) {
      try {
        return new JSONObject(JsonUtils.convertExceptionToJson(e));
      } catch (JSONException t) {
        throw new OBException(t);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
