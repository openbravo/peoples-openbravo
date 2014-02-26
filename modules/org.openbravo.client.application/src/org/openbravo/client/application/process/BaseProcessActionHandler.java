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
 * All portions are Copyright (C) 2010-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.util.Check;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.Process;
import org.openbravo.client.application.ProcessAccess;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.ui.Window;

/**
 * 
 * @author iperdomo
 */
public abstract class BaseProcessActionHandler extends BaseActionHandler {

  private static final Logger log = Logger.getLogger(BaseProcessActionHandler.class);

  @Override
  protected final JSONObject execute(Map<String, Object> parameters, String content) {

    try {
      OBContext.setAdminMode(true);

      final String processId = (String) parameters.get("processId");
      Check.isNotNull(processId, "Process ID missing in request");

      final Process processDefinition = OBDal.getInstance().get(Process.class, processId);
      Check.isNotNull(processDefinition, "Not valid process id");

      if (!hasAccess(processDefinition, parameters)) {
        JSONObject jsonRequest = new JSONObject();

        JSONObject err = new JSONObject();
        err.put("severity", "error");
        err.put("text", OBMessageUtils.getI18NMessage("OBUIAPP_NoAccess", null));
        jsonRequest.put("message", err);

        log.error("No access to process " + processDefinition);
        return jsonRequest;
      }

      for (Parameter param : processDefinition.getOBUIAPPParameterList()) {
        if (param.isFixed()) {
          if (param.isEvaluateFixedValue()) {
            parameters.put(param.getDBColumnName(),
                ParameterUtils.getParameterFixedValue(fixRequestMap(parameters), param));
          } else {
            parameters.put(param.getDBColumnName(), param.getFixedValue());
          }
        }
      }

      // Set information for audit trail
      SessionInfo.setProcessType("PD");
      SessionInfo.setProcessId(processId);
      SessionInfo.setDBSessionInfo(OBDal.getInstance().getConnection(false));

      return doExecute(parameters, content);

    } catch (Exception e) {
      log.error("Error trying to execute process request: " + e.getMessage(), e);
      return new JSONObject();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Permissions to processes can be given in 2 ways:
   * <p>
   * Explicit grant to process definition access:
   * <ul>
   * <li>whenever the process is directly executed from menu
   * <li>in case the process is marked as "Requires Explicit Access Permission"
   * <li>in case there is a "Secured Process" preference for current window
   * </ul>
   * <p>
   * Inherited from window access in case it is invoked from a window button and none of the
   * previous conditions is satisfied.
   * 
   */
  private boolean hasAccess(Process processDefinition, Map<String, Object> parameters) {
    String windowId = (String) parameters.get("windowId");
    if (windowId != null && !"null".equals(windowId)) {
      Window window = OBDal.getInstance().get(Window.class, windowId);

      boolean checkPermission = processDefinition.isRequiresExplicitAccessPermission();

      if (!checkPermission) {
        try {
          checkPermission = "Y".equals(Preferences.getPreferenceValue("SecuredProcess", true,
              OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                  .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                  .getOBContext().getRole(), window));
        } catch (PropertyException e) {
          // do nothing, property is not set so securedProcess is false
        }
      }
      if (!checkPermission) {
        // check if window is accessible
        OBCriteria<WindowAccess> qAccess = OBDal.getInstance().createCriteria(WindowAccess.class);
        qAccess.add(Restrictions.eq(WindowAccess.PROPERTY_WINDOW, window));
        qAccess
            .add(Restrictions.eq(WindowAccess.PROPERTY_ROLE, OBContext.getOBContext().getRole()));
        return qAccess.count() > 0;
      }
    }

    // The process now can be:
    // * Secured process invoked from window
    // * Invoked from menu (without window)
    // In any of these two cases, security is checked based on process access
    OBCriteria<ProcessAccess> qAccess = OBDal.getInstance().createCriteria(ProcessAccess.class);
    qAccess.add(Restrictions.eq(ProcessAccess.PROPERTY_OBUIAPPPROCESS, processDefinition));
    qAccess.add(Restrictions.eq(ProcessAccess.PROPERTY_ROLE, OBContext.getOBContext().getRole()));
    return qAccess.count() > 0;
  }

  /*
   * The request map is <String, Object> because includes the HTTP request and HTTP session, is not
   * required to handle process parameters
   */
  protected Map<String, String> fixRequestMap(Map<String, Object> parameters) {
    final Map<String, String> retval = new HashMap<String, String>();
    for (Entry<String, Object> entries : parameters.entrySet()) {
      if (entries.getKey().equals(KernelConstants.HTTP_REQUEST)
          || entries.getKey().equals(KernelConstants.HTTP_SESSION)) {
        continue;
      }
      retval.put(entries.getKey(), entries.getValue().toString());
    }
    return new HashMap<String, String>();
  }

  protected abstract JSONObject doExecute(Map<String, Object> parameters, String content);
}
