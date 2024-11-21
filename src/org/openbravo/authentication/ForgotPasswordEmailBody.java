/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
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

  public String getToken() {
    return (String) this.data.get("token");
  }

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, "B8ED789A54F74E798958D9ADD0ABCEBD");
  }

}
