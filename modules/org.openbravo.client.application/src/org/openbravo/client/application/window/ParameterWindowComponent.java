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
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.Process;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * The component which takes care of creating a class for a specific paramter window.
 * 
 * @author alostale
 */
public class ParameterWindowComponent extends BaseTemplateComponent {
  private static final Logger log = Logger.getLogger(ParameterWindowComponent.class);
  private static final String DEFAULT_TEMPLATE_ID = "FF80818132F916130132F9357DE10016";

  protected static final Map<String, String> TEMPLATE_MAP = new HashMap<String, String>();

  private Boolean inDevelopment = null;
  private String uniqueString = "" + System.currentTimeMillis();
  private Process process;

  @Inject
  private OBViewParameterHandler paramHandler;
  private boolean popup;

  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, DEFAULT_TEMPLATE_ID);
  }

  public String getWindowClientClassName() {
    // see the ViewComponent#correctViewId
    // changes made in this if statement should also be done in that method
    if (isIndevelopment()) {
      return KernelConstants.ID_PREFIX + process.getId() + KernelConstants.ID_PREFIX + uniqueString;
    }
    return KernelConstants.ID_PREFIX + process.getId();
  }

  public void setUniqueString(String uniqueString) {
    this.uniqueString = uniqueString;
  }

  public boolean isIndevelopment() {
    if (inDevelopment != null) {
      return inDevelopment;
    }

    // check window, tabs and fields
    inDevelopment = Boolean.FALSE;
    if (process.getModule().isInDevelopment() && process.getModule().isEnabled()) {
      inDevelopment = Boolean.TRUE;
    }

    return inDevelopment;
  }

  public String generate() {
    final String jsCode = super.generate();
    return jsCode;
  }

  public boolean isPopup() {
    return this.popup;
  }

  public void setPoup(boolean popup) {
    this.popup = popup;
  }

  public String getThreadSafe() {
    // final Boolean value = getWindow().isThreadsafe();
    // if (value != null) {
    // return value.toString();
    // }
    // TODO: review this
    return "true";
  }

  public void setProcess(org.openbravo.client.application.Process process) {
    this.process = process;
    paramHandler.setProcess(process);
    paramHandler.setParamWindow(this);
  }

  public OBViewParameterHandler getParamHandler() {
    return paramHandler;
  }

  public String getActionHandler() {
    return process.getJavaClassName();
  }

  public String getProcessId() {
    return process.getId();
  }

  public List<org.openbravo.model.ad.domain.List> getButtonList() {
    for (Parameter p : process.getOBUIAPPParameterList()) {
      if (p.isActive() && p.getReference().getId().equals("FF80818132F94B500132F9575619000A")) {
        OBCriteria<org.openbravo.model.ad.domain.List> qList = OBDal.getInstance().createCriteria(
            org.openbravo.model.ad.domain.List.class);
        qList.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE,
            p.getReferenceSearchKey()));
        qList.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_ACTIVE, true));
        qList.addOrderBy(org.openbravo.model.ad.domain.List.PROPERTY_SEQUENCENUMBER, true);
        qList.addOrderBy(org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY, true);
        return qList.list();
      }
    }
    return new ArrayList<org.openbravo.model.ad.domain.List>();
  }
}
