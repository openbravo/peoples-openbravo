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
package org.openbravo.client.application;

import java.util.Map;

import javax.enterprise.context.RequestScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

@RequestScoped
public interface ExtraSettingsInjector {
  /**
   * This method adds extra settings to the previously defined on the WindowSettingsActionHandler.
   * 
   * Using the WindowSettingsActionHandler.EXTRA_CALLBACK key it is possible to return a
   * List<String> with JavaScript functions that are executed on the callback of the
   * WindowSettingsActionHandler execution. These functions only receive the "data" object as
   * argument.
   * 
   * All other keys are included in a "extraSettings" JavaScript object in the response "data"
   * object.
   * 
   * @param parameters
   *          the parameters Map of the current WindowSettingsActionHandler execution.
   * @param json
   *          the JSONObject instance of the response of the WindowSettingsActionHandler.
   * @return A Map<String, Object> with all the extra settings desired to be included in the
   *         WindowSettingsActionHandler response.
   * @throws OBException
   */
  Map<String, Object> doAddSetting(Map<String, Object> parameters, JSONObject json)
      throws OBException;
}
