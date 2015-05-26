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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Validation;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The component which takes care of creating a class for a tab's Attachment popup.
 */
public class AttachmentWindowComponent extends BaseTemplateComponent {
  private static final String DEFAULT_TEMPLATE_ID = "01E447F740584E02BA4612F6BDFB900D";
  private static final Logger log = LoggerFactory.getLogger(AttachmentWindowComponent.class);

  private Boolean inDevelopment = null;
  private String uniqueString = "" + System.currentTimeMillis();
  private String clientId = null;
  private Tab tab;

  @Inject
  private OBViewParameterHandler paramHandler;

  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, DEFAULT_TEMPLATE_ID);
  }

  public String getWindowClientClassName() {
    // see the ViewComponent#correctViewId
    // changes made in this if statement should also be done in that method
    if (isIndevelopment()) {
      return KernelConstants.ID_PREFIX + tab.getId() + KernelConstants.ID_PREFIX + uniqueString;
    }
    return KernelConstants.ID_PREFIX + tab.getId();
  }

  public void setUniqueString(String uniqueString) {
    this.uniqueString = uniqueString;
  }

  public void setClient(String clientId) {
    this.clientId = clientId;
  }

  public boolean isIndevelopment() {
    if (inDevelopment != null) {
      return inDevelopment;
    }

    // check window, tabs and fields
    inDevelopment = Boolean.FALSE;
    if (tab.getModule().isInDevelopment() && tab.getModule().isEnabled()) {
      inDevelopment = Boolean.TRUE;
    }

    return inDevelopment;
  }

  public String generate() {
    final String jsCode = super.generate();
    return jsCode;
  }

  public void setTab(Tab tab) {
    this.tab = tab;
    paramHandler.setParameters(getTabMetadataFields());
    paramHandler.setParamWindow(this);
  }

  public OBViewParameterHandler getParamHandler() {
    return paramHandler;
  }

  public String getDynamicColumns() {
    List<Parameter> paramsWithValidation = new ArrayList<Parameter>();
    List<String> allParams = new ArrayList<String>();
    Map<String, List<String>> dynCols = new HashMap<String, List<String>>();

    for (Parameter param : getTabMetadataFields()) {
      Validation validation = param.getValidation();
      if (validation != null) {
        if (validation.getType().equals("HQL_JS")) {
          paramsWithValidation.add(param);
        } else {
          log.error("Unsupported validation type {} for param {} in tab {}", new Object[] {
              "HQL_JS", param, tab });
        }
      }
      allParams.add(param.getDBColumnName());
    }

    for (Parameter paramWithVal : paramsWithValidation) {
      parseValidation(paramWithVal.getValidation(), dynCols, allParams,
          paramWithVal.getDBColumnName());
    }

    JSONObject jsonDynCols = new JSONObject();

    for (String dynColName : dynCols.keySet()) {
      JSONArray affectedColumns = new JSONArray();
      for (String affectedCol : dynCols.get(dynColName)) {
        affectedColumns.put(affectedCol);
      }
      try {
        jsonDynCols.put(dynColName, affectedColumns);
      } catch (JSONException e) {
        log.error("Error generating dynamic columns for tab {}", tab.getIdentifier(), e);
      }
    }
    return jsonDynCols.toString();
  }

  private List<Parameter> getTabMetadataFields() {
    AttachmentConfig attConf = AttachmentUtils.getAttachmentConfig((Client) OBDal.getInstance()
        .getProxy(Client.ENTITY_NAME, clientId));
    AttachmentMethod attachMethod;
    if (attConf == null) {
      attachMethod = AttachmentUtils.getDefaultAttachmentMethod();
    } else {
      attachMethod = attConf.getAttachmentMethod();
    }
    // TODO Auto-generated method stub
    // Load attachment method in use
    return AttachmentUtils.getMethodMetadataParameters(attachMethod, tab);
  }

  /**
   * Dynamic columns is a list of columns that cause others to be modified, it includes the ones
   * causing the modification as well as the affected ones.
   * 
   * Columns are identified as strings surrounded by quotes (" or ') matching one of the names of
   * the parameters.
   */
  private void parseValidation(Validation validation, Map<String, List<String>> dynCols,
      List<String> allParams, String paramName) {
    String token = validation.getValidationCode().replace("\"", "'");

    List<String> columns;

    int i = token.indexOf("'");
    while (i != -1) {
      token = token.substring(i + 1);
      i = token.indexOf("'");
      if (i != -1) {
        String strAux = token.substring(0, i);
        token = token.substring(i + 1);
        columns = dynCols.get(token);

        if (!strAux.equals(paramName) && allParams.contains(strAux)) {
          if (dynCols.containsKey(strAux)) {
            columns = dynCols.get(strAux);
          } else {
            columns = new ArrayList<String>();
            dynCols.put(strAux, columns);
          }
          if (!columns.contains(paramName)) {
            columns.add(paramName);
          }
        }
      }
      if (token.indexOf("'") != -1) {
        token = "'" + token;
      }
      i = token.indexOf("'");
    }
  }
}
