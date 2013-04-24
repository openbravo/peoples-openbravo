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
 * All portions are Copyright (C) 2012-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.process;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.domaintype.BooleanDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.ForeignKeyDomainType;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.Process;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.domain.Reference;

/**
 * This ActionHandler is invoked when opening a Process Definition window. It is in charge of
 * computing default values for the parameters in the window.
 * 
 * @author alostale
 */
public class DefaultsProcessActionHandler extends BaseProcessActionHandler {

  private static final Logger log = Logger.getLogger(DefaultsProcessActionHandler.class);

  @Override
  protected final JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {
      OBContext.setAdminMode(true);

      final String processId = (String) parameters.get("processId");

      JSONObject context = null;
      if (parameters.get("context") != null) {
        context = new JSONObject((String) parameters.get("context"));
      }
      final Process processDefinition = OBDal.getInstance().get(Process.class, processId);
      JSONObject defaults = new JSONObject();

      for (Parameter param : processDefinition.getOBUIAPPParameterList()) {
        if (param.getDefaultValue() != null) {

          Reference reference = param.getReferenceSearchKey();
          if (reference == null) {
            reference = param.getReference();
          }

          UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(
              reference);

          String rawDefaultValue = param.getDefaultValue();

          Object defaultValue;
          if (isSessionDefaultValue(rawDefaultValue) && context != null) {
            // Transforms the default value from @columnName@ to the column inp name
            String inpName = "inp"
                + Sqlc.TransformaNombreColumna(rawDefaultValue.substring(1,
                    rawDefaultValue.length() - 1));
            defaultValue = context.get(inpName);
          } else {
            defaultValue = ParameterUtils.getJSExpressionResult(fixRequestMap(parameters),
                (HttpSession) parameters.get(KernelConstants.HTTP_SESSION), rawDefaultValue);
          }

          DomainType domainType = uiDefinition.getDomainType();
          if (defaultValue != null && defaultValue instanceof String
              && domainType instanceof ForeignKeyDomainType) {
            // default value is ID of a FK, look for the identifier
            Entity referencedEntity = ((ForeignKeyDomainType) domainType)
                .getForeignKeyColumn(param.getDBColumnName()).getProperty().getEntity();

            BaseOBObject record = OBDal.getInstance().get(referencedEntity.getName(), defaultValue);
            if (record != null) {
              String identifier = record.getIdentifier();
              JSONObject def = new JSONObject();
              def.put("value", defaultValue);
              def.put("identifier", identifier);
              defaults.put(param.getDBColumnName(), def);
            }
          } else {
            if (domainType instanceof BooleanDomainType) {
              defaultValue = ((BooleanDomainType) domainType)
                  .createFromString((String) defaultValue);
            }
            defaults.put(param.getDBColumnName(), defaultValue);
          }
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

  // Returns true if the value of the parameter default value matches "@*@"
  private boolean isSessionDefaultValue(String rawDefaultValue) {
    if ("@".equals(rawDefaultValue.substring(0, 1))
        && "@".equals(rawDefaultValue.substring(rawDefaultValue.length() - 1))
        && rawDefaultValue.length() > 2) {
      return true;
    } else {
      return false;
    }
  }
}
