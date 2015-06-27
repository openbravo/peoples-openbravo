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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.client.application.window.AttachmentUtils;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
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
      final String strAttachmentId = (String) parameters.get("attachmentId");
      final String strAction = (String) parameters.get("action");
      final Tab tab = OBDal.getInstance().get(Tab.class, strTabId);
      final Attachment attachment = OBDal.getInstance().get(Attachment.class, strAttachmentId);
      final AttachmentMethod attMethod = OBDal.getInstance().get(AttachmentMethod.class,
          strAttMethodID);

      JSONObject context = new JSONObject();
      if (parameters.get("context") != null) {
        context = new JSONObject((String) parameters.get("context"));
      }
      final Map<String, String> fixedParameters = ParameterUtils.fixRequestMap(parameters);

      // The parameter list is sorted so the fixed parameters are evaluated before. This is needed
      // to be able to define parameters with default values based on the fixed parameters.
      for (Parameter param : AttachmentUtils.getMethodMetadataParameters(attMethod, tab)) {
        if (param.isFixed()) {
          if (param.getPropertyPath() != null) {
            parameters.put(param.getDBColumnName(), "Property Path");
          } else if (param.isEvaluateFixedValue()) {
            parameters.put(param.getDBColumnName(), ParameterUtils.getParameterFixedValue(
                ParameterUtils.fixRequestMap(parameters), param));
          } else {
            parameters.put(param.getDBColumnName(), param.getFixedValue());
          }
          continue;
        }

        if ("edit".equals(strAction)) {
          // Calculate stored value.
          OBCriteria<ParameterValue> parameterValueCriteria = OBDal.getInstance().createCriteria(
              ParameterValue.class);
          parameterValueCriteria.add(Restrictions.eq(ParameterValue.PROPERTY_FILE, attachment));
          parameterValueCriteria.add(Restrictions.eq(ParameterValue.PROPERTY_PARAMETER, param));
          ParameterValue parameterValue = (ParameterValue) parameterValueCriteria.uniqueResult();
          if (parameterValue != null) {
            // If the parameter has a previous value set it on the defaults map and continue with
            // next parameter.
            Object objValue = ParameterUtils.getParameterValue(parameterValue);
            Object parsedValue = "";
            if (objValue == null) {
              parsedValue = "";
            } else if (objValue instanceof Date) {
              parsedValue = OBDateUtils.formatDate((Date) objValue);
            } else if (objValue instanceof BigDecimal) {
              parsedValue = ((BigDecimal) objValue).toPlainString();
            } else if (objValue instanceof Boolean) {
              parsedValue = ((Boolean) objValue);
            } else {
              parsedValue = objValue.toString();
            }

            defaults.put(param.getDBColumnName(), parsedValue);
            continue;
          }
        }
        if (param.getDefaultValue() != null) {
          Object defValue = ParameterUtils.getParameterDefaultValue(fixedParameters, param,
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
}
