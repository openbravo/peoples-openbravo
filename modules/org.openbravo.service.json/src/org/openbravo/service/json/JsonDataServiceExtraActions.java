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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 ************************************************************************
 */
package org.openbravo.service.json;

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.json.DefaultJsonDataService.DataSourceAction;

public interface JsonDataServiceExtraActions {
  /**
   * Implement this method to modify or validate the data before the action is executed.
   * 
   * @param parameters
   *          The Map with the parameters of the DataSource call.
   * @param data
   *          JSONArray with the records that are going to be inserted, updated or deleted. Modify
   *          this object in case it is required to modify the data before executing the action.
   *          Fetch operations receive an empty array.
   * @param action
   *          The action of the DataSource call. Possible values are FETCH, ADD, UPDATE and REMOVE
   * 
   */
  void doPreAction(Map<String, String> parameters, JSONArray data, DataSourceAction action);

  /**
   * Implement this method to modify or validate the data after the action is executed and before is
   * returned to the client.
   * 
   * @param parameters
   *          The Map with the parameters of the DataSource call.
   * @param content
   *          JSONObject with the current content that is returned to the client. Modify this object
   *          in case it is required to modify the data before is returned.
   * @param action
   *          The action of the DataSource call. Possible values are FETCH, ADD, UPDATE and REMOVE
   * @param originalObject
   *          JSONObject String available only on ADD and UPDATE with the original values of the
   *          data.
   */
  void doPostAction(Map<String, String> parameters, JSONObject content, DataSourceAction action,
      String originalObject);
}
