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
 * All portions are Copyright (C) 2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * This class generates the javascript code of the navigation bar components.
 * 
 */
public class NavigationBarComponentGenerator implements OBSingleton {

  @Inject
  private WeldUtils weldUtils;

  private static NavigationBarComponentGenerator instance;

  protected static synchronized NavigationBarComponentGenerator getInstance() {
    if (instance == null) {
      instance = WeldUtils.getInstanceFromStaticBeanManager(NavigationBarComponentGenerator.class);
    }
    return instance;
  }

  /**
   * Generates the code of the navigation bar components.
   * 
   * @param parameters
   *          A map with the parameters to be used in the generation of the component.
   * 
   * @return a Collection with the generated navigation bar components
   */
  protected Collection<NBComponent> getNavigationBarComponents(Map<String, Object> parameters) {
    OBCriteria<NavBarComponent> obc = getNavigationBarComponentCriteria();

    return generateNavigationBarComponents(obc.list(), parameters);
  }

  /**
   * Generates the code of the dynamic navigation bar components.
   * 
   * @param parameters
   *          A map with the parameters to be used in the generation of the component.
   * 
   * @return a Collection with the generated dynamic navigation bar components
   */
  protected Collection<NBComponent> getDynamicNavigationBarComponents(Map<String, Object> parameters) {
    OBCriteria<NavBarComponent> obc = getNavigationBarComponentCriteria();
    obc.add(Restrictions.eq(NavBarComponent.PROPERTY_ISSTATICCOMPONENT, false));

    return generateNavigationBarComponents(obc.list(), parameters);
  }

  private OBCriteria<NavBarComponent> getNavigationBarComponentCriteria() {
    OBCriteria<NavBarComponent> criteria = OBDal.getInstance()
        .createCriteria(NavBarComponent.class);
    criteria.addOrderBy(NavBarComponent.PROPERTY_RECORDSORTNO, true);
    return criteria;
  }

  @SuppressWarnings("unchecked")
  private Collection<NBComponent> generateNavigationBarComponents(
      List<NavBarComponent> navigationBarComponents, Map<String, Object> parameters) {
    final List<NBComponent> nbComponents = new ArrayList<NBComponent>();
    for (NavBarComponent nbc : navigationBarComponents) {

      if (!isAccessible(nbc)) {
        continue;
      }

      final NBComponent nbComponent = new NBComponent();

      if (!nbc.isStaticcomponent()) {
        nbComponent.setJscode("{className: '_OBDynamicComponent'}");
      } else {
        String jsCode = "";
        try {
          final Class<BaseTemplateComponent> clz = (Class<BaseTemplateComponent>) OBClassLoader
              .getInstance().loadClass(nbc.getJavaClassName());
          final BaseComponent component = weldUtils.getInstance(clz);
          component.setId(nbc.getId());
          if (component instanceof BaseTemplateComponent && nbc.getTemplate() != null) {
            ((BaseTemplateComponent) component).setComponentTemplate(nbc.getTemplate());
          }
          component.setParameters(parameters);

          jsCode = component.generate();
          nbComponent.setJscode(jsCode);
        } catch (Exception e) {
          throw new IllegalStateException("Exception when creating component " + nbc.getId(), e);
        }
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
