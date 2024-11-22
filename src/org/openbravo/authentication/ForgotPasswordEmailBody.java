/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at https://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.authentication;

import java.util.Map;

import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.email.EmailEventContentGenerator;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.access.User;

/**
 * Convenience class that provides a set of common utilities for templates of emails sent by portal
 * events.
 * 
 * @see EmailEventContentGenerator
 * @author ander.flores
 * 
 */
public class ForgotPasswordEmailBody extends BaseTemplateComponent {
  private Map<String, Object> data;

  public String getClientName() {
    return OBContext.getOBContext().getCurrentClient().getName();
  }

  public String getUrl() {
    String url = "";
    try {
      url = Preferences.getPreferenceValue("PortalURL", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), null, null, null);
    } catch (PropertyException e) {
      // no preference set, ignore it
    }
    return url;
  }

  public String getContactEmail() {
    String email = "";
    try {
      email = Preferences.getPreferenceValue("PortalContactEmail", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), null, null, null);
    } catch (PropertyException e) {
      // no preference set, ignore it
    }
    return email;
  }

  void setData(Map<String, Object> data) {
    this.data = data;
  }

  @Override
  public Object getData() {
    return this;
  }

  public User getUser() {
    return (User) this.data.get("user");
  }

  public String getChangePasswordURL() {
    return (String) this.data.get("changePasswordURL");
  }

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, "B8ED789A54F74E798958D9ADD0ABCEBD");
  }

}
