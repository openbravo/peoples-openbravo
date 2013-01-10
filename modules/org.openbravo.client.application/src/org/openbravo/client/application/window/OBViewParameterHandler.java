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
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.Process;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;

public class OBViewParameterHandler {
  private static final Logger log = Logger.getLogger(OBViewParameterHandler.class);
  private static final String WINDOW_REFERENCE_ID = "FF80818132D8F0F30132D9BC395D0038";
  private Process process;
  private ParameterWindowComponent paramWindow;

  public void setProcess(Process process) {
    this.process = process;
  }

  public List<OBViewParameter> getParameters() {
    List<OBViewParameter> params = new ArrayList<OBViewParameterHandler.OBViewParameter>();
    for (Parameter param : process.getOBUIAPPParameterList()) {
      if (param.isActive()
          && (!param.isFixed() || param.getReference().getId().equals(WINDOW_REFERENCE_ID))
          && (!param.getReference().getId()
              .equals(ParameterWindowComponent.BUTTON_LIST_REFERENCE_ID))) {
        params.add(new OBViewParameter(param));
      }
    }
    return params;
  }

  public class OBViewParameter {
    UIDefinition uiDefinition;
    Parameter parameter;

    public OBViewParameter(Parameter param) {
      uiDefinition = UIDefinitionController.getInstance().getUIDefinition(param.getReference());
      parameter = param;
    }

    public String getType() {
      return uiDefinition != null ? uiDefinition.getName() : "--";
    }

    public String getTitle() {
      return OBViewUtil.getLabel(parameter, parameter.getOBUIAPPParameterTrlList());
    }

    public String getName() {
      return parameter.getDBColumnName();
    }

    public boolean isRequired() {
      return parameter.isMandatory();
    }

    public boolean isGrid() {
      return parameter.getReferenceSearchKey() != null
          && parameter.getReferenceSearchKey().getOBUIAPPRefWindowList().size() > 0;
    }

    public String getTabView() {
      Window window;

      if (parameter.getReferenceSearchKey().getOBUIAPPRefWindowList().size() == 0
          || parameter.getReferenceSearchKey().getOBUIAPPRefWindowList().get(0).getWindow() == null) {
        return null;
      } else {
        window = parameter.getReferenceSearchKey().getOBUIAPPRefWindowList().get(0).getWindow();
      }

      if (window.getADTabList().isEmpty()) {
        log.error("Window definition " + window.getName() + " has no tabs");
        return null;
      }

      Tab tab = window.getADTabList().get(0);

      final OBViewTab tabComponent = paramWindow.createComponent(OBViewTab.class);
      tabComponent.setTab(tab);
      return tabComponent.generate();
    }

    public String getParameterProperties() {
      String jsonString = uiDefinition.getParameterProperties(parameter).trim();
      if (jsonString == null || jsonString.trim().length() == 0) {
        return "";
      }
      // strip the first and last { }
      if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
        // note -2 is done because the first substring takes of 1 already
        return "," + jsonString.substring(1).substring(0, jsonString.length() - 2) + ",";
      } else if (jsonString.equals("{}")) {
        return "";
      }
      // be lenient just return the string as it is...
      return "," + jsonString + (jsonString.trim().endsWith(",") ? "" : ",");
    }
  }

  public void setParamWindow(ParameterWindowComponent parameterWindowComponent) {
    this.paramWindow = parameterWindowComponent;
  }
}
