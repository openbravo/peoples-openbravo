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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * The component responsible for generating the application js file.
 * 
 * @author mtaal
 */
public class ApplicationComponent extends BaseTemplateComponent {

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, KernelConstants.APPLICATION_TEMPLATE_ID);
  }

  public String getSystemVersion() {
    return KernelUtils.getInstance().getVersionParameters(getModule());
  }

  public String getLanguageId() {
    return OBContext.getOBContext().getLanguage().getId();
  }

  public String getDateFormat() {
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    return props.getProperty(KernelConstants.DATE_FORMAT_PROPERTY, "dd-MM-yyyy");
  }

  public String getDateTimeFormat() {
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    return props.getProperty(KernelConstants.DATETIME_FORMAT_PROPERTY, "dd-MM-yyyy HH:mm:ss");
  }

  public User getUser() {
    return OBContext.getOBContext().getUser();
  }

  public Client getClient() {
    return OBContext.getOBContext().getCurrentClient();
  }

  public Organization getOrganization() {
    return OBContext.getOBContext().getCurrentOrganization();
  }

  public Role getRole() {
    return OBContext.getOBContext().getRole();
  }

  // Module version parameters are used in hyperlinks to prevent caching in
  // development situations
  public List<ModuleVersionParameter> getModuleVersionParameters() {
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    final List<ModuleVersionParameter> result = new ArrayList<ModuleVersionParameter>();
    for (Module module : modules) {
      final ModuleVersionParameter version = new ModuleVersionParameter();
      final String versionParameter = KernelUtils.getInstance().getVersionParameters(module);
      version.setId(module.getId());
      version.setValue(versionParameter);
      result.add(version);
    }
    return result;
  }

  public static class ModuleVersionParameter {
    private String id;
    private String value;

    @SuppressWarnings("unused")
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @SuppressWarnings("unused")
    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

  }
}
