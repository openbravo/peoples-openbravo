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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.window.AttachmentUtils;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This ActionHandler is invoked when opening a Attachment window. It is in charge of computing
 * default values for the parameters in the window.
 */
public class DefaultsAttachmentActionHandler extends BaseActionHandler {

  private static final Logger log = LoggerFactory.getLogger(DefaultsAttachmentActionHandler.class);

  @Override
  protected final JSONObject execute(Map<String, Object> parameters, String content) {
    try {
      OBContext.setAdminMode(true);
      JSONObject defaults = new JSONObject();

      final String strAttMethodID = (String) parameters.get("attachmentMethod");
      final String strTabId = (String) parameters.get("tabId");
      final Tab tab = OBDal.getInstance().get(Tab.class, strTabId);
      AttachmentMethod attMethod = OBDal.getInstance().get(AttachmentMethod.class, strAttMethodID);

      JSONObject context = new JSONObject();
      if (parameters.get("context") != null) {
        context = new JSONObject((String) parameters.get("context"));
      }
      final Map<String, String> fixedParameters = fixRequestMap(parameters);

      for (Parameter param : AttachmentUtils.getMethodMetadataParameters(attMethod, tab)) {
        Object defValue = null;
        // FIXME: When edit is done retrieve first stored value
        if (param.isFixed()) {
          if (param.isEvaluateFixedValue()) {
            defValue = ParameterUtils.getParameterFixedValue(fixedParameters, param);
          } else {
            defValue = param.getFixedValue();
          }
          defaults.put(param.getDBColumnName(), defValue);
        } else if (param.getDefaultValue() != null) {
          defValue = ParameterUtils.getParameterDefaultValue(fixedParameters, param,
              (HttpSession) parameters.get(KernelConstants.HTTP_SESSION), context);
          defaults.put(param.getDBColumnName(), defValue);
        }
      }
      log.debug("Defaults for tab {} \n {}", tab, defaults.toString());
      return defaults;
    } catch (Exception e) {
      log.error("Error trying getting defaults for process: " + e.getMessage(), e);
      return new JSONObject();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private Map<String, String> fixRequestMap(Map<String, Object> parameters) {
    final Map<String, String> retval = new HashMap<String, String>();
    for (Entry<String, Object> entries : parameters.entrySet()) {
      if (entries.getKey().equals(KernelConstants.HTTP_REQUEST)
          || entries.getKey().equals(KernelConstants.HTTP_SESSION)) {
        continue;
      }
      retval.put(entries.getKey(), entries.getValue().toString());
    }
    return retval;
  }
}
