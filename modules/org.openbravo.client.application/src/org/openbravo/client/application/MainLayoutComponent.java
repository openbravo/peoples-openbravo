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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.PropertyException;

/**
 * 
 * @author iperdomo
 */
public class MainLayoutComponent extends BaseTemplateComponent {

  @Inject
  private WeldUtils weldUtils;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseTemplateComponent#getComponentTemplate()
   */
  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, ApplicationConstants.MAIN_LAYOUT_TEMPLATE_ID);
  }

  @SuppressWarnings("unchecked")
  public Collection<NBComponent> getNavigationBarComponents() {
    final List<NBComponent> nbComponents = new ArrayList<NBComponent>();

    OBCriteria<NavBarComponent> obc = OBDal.getInstance().createCriteria(NavBarComponent.class);

    obc.addOrderBy(NavBarComponent.PROPERTY_RECORDSORTNO, true);
    for (NavBarComponent nbc : obc.list()) {

      if (!isAccessible(nbc)) {
        continue;
      }

      final NBComponent nbComponent = new NBComponent();

      String jsCode = "";
      try {
        final Class<BaseTemplateComponent> clz = (Class<BaseTemplateComponent>) OBClassLoader
            .getInstance().loadClass(nbc.getJavaClassName());
        final BaseTemplateComponent component = weldUtils.getInstance(clz);
        component.setId(nbc.getId());
        component.setComponentTemplate(nbc.getTemplate());
        component.setParameters(getParameters());

        jsCode = component.generate();
        nbComponent.setJscode(jsCode);
      } catch (Exception e) {
        throw new IllegalStateException("Exception when creating component " + nbc.getId(), e);
      }
      nbComponents.add(nbComponent);
    }
    return nbComponents;
  }

  private boolean isAccessible(NavBarComponent navBarComponent) {
    if (OBContext.getOBContext().getRole().getId().equals("0")) {
      return true;
    }
    if (navBarComponent.isAllroles()) {
      return true;
    }
    final String currentRoleId = OBContext.getOBContext().getRole().getId();
    for (NavbarRoleaccess roleAccess : navBarComponent.getOBUIAPPNavbarRoleaccessList()) {
      if (currentRoleId.equals(roleAccess.getRole().getId())) {
        return true;
      }
    }
    return false;
  }

  public boolean isAddProfessionalLink() {
    if (SessionFactoryController.isRunningInWebContainer()) {
      return !ActivationKey.isActiveInstance();
    }
    return true;
  }

  public String getStartPage() {
    try {
      return Preferences.getPreferenceValue(getContextUrl()
          + ApplicationConstants.START_PAGE_PROPERTY, true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    } catch (PropertyException e) {
      return getContextUrl() + "/default/Menu.html";
    }
  }

  public String getVersion() {
    return getETag();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.Component#getETag()
   */
  public String getETag() {
    // also encodes the role id in the etag
    if (getModule().isInDevelopment() != null && getModule().isInDevelopment()) {
      return super.getETag();
    } else {
      return OBContext.getOBContext().getLanguage().getId() + "_"
          + OBContext.getOBContext().getRole().getId() + "_" + getModule().getVersion();
    }
  }

  public static class NBComponent {
    // NB stands for: Navigation Bar
    private String jscode;

    public void setJscode(String jscode) {
      this.jscode = jscode;
    }

    public String getJscode() {
      return jscode;
    }

    public String toString() {
      return jscode;
    }
  }
}
