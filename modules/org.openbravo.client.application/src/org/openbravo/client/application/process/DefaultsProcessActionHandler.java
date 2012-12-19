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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.process;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.Process;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

/**
 * 
 * @author alostale
 */
public class DefaultsProcessActionHandler extends BaseProcessActionHandler {

  private static final Logger log = Logger.getLogger(DefaultsProcessActionHandler.class);

  @Override
  protected final JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {
      OBContext.setAdminMode();

      final String processId = (String) parameters.get("processId");
      final Process processDefinition = OBDal.getInstance().get(Process.class, processId);

      JSONObject defaults = new JSONObject();

      for (Parameter param : processDefinition.getOBUIAPPParameterList()) {
        if (param.getDefaultValue() != null) {
          defaults.put(
              param.getDBColumnName(),
              ParameterUtils.getJSExpressionResult(fixRequestMap(parameters), null,
                  param.getDefaultValue()));
        }
      }
      log.debug("Defaults for process " + processDefinition + "\n" + defaults.toString());
      return defaults;
    } catch (Exception e) {
      log.error("Error trying getting defaults for process: " + e.getMessage(), e);
      return new JSONObject();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
