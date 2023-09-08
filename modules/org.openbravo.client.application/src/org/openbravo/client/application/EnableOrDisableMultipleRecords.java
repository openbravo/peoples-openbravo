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
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
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
      final JSONArray recordIds = dataObject.getJSONArray("recordIds");
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      String tableId = tab.getTable().getId();
      String entityName = (ModelProvider.getInstance().getEntityByTableId(tableId)).getName();

      for (int i = 0; i < recordIds.length(); i++) {
        final BaseOBObject object = OBDal.getInstance().get(entityName, recordIds.get(i));
        if (object != null) {
        //@formatter:off
            final String hql =
                "update " + entityName +
                "   set active = :action," +
                "       updated = :newDate," +
                "       updatedBy = :user" +
                " where id = :recordId"+
                "   and active != :action"+
                "   and client = :clientId"+
                "   and organization in ( :writableOrgs )";
            //@formatter:on
          OBDal.getInstance()
              .getSession()
              .createQuery(hql)
              .setParameter("action", action)
              .setParameter("newDate", new Date())
              .setParameter("user", OBContext.getOBContext().getUser())
              .setParameter("recordId", recordIds.get(i))
              .setParameter("clientId", OBContext.getOBContext().getCurrentClient())
              .setParameter("writableOrgs", OBContext.getOBContext().getWritableOrganizations())
              .executeUpdate();
        }
      }
      return new JSONObject();
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
