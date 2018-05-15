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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.datapool;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.ExtraWindowSettingsInjector;
import org.openbravo.dal.security.EntityAccessChecker;

public class DataPoolSelectionWindowInjector implements ExtraWindowSettingsInjector {

  private static final Logger log = Logger.getLogger(EntityAccessChecker.class);
  private static final String dataPoolSelectionWindowId = "48B7215F9BF6458E813E6B280DEDB958";

  @Override
  public Map<String, Object> doAddSetting(Map<String, Object> parameters, JSONObject json)
      throws OBException {
    Map<String, Object> extraSettings = new HashMap<>();
    String windowId = (String) parameters.get("windowId");
    if (dataPoolSelectionWindowId.equals(windowId)) {
      extraSettings.put("messageKey", "OBUIAPP_ROPoolNotAvailable");
    }
    return extraSettings;
  }
}
