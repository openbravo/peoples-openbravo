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
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProviderRegistry;

/**
 * 
 * @author iperdomo
 */
@Name("org.openbravo.client.application.MainLayoutComponentProvider")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
public class MainLayoutComponentProvider extends BaseComponentProvider {

  @Create
  public void initialize() {
    ComponentProviderRegistry.getInstance().registerComponentProvider(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponent(java.lang.String,
   * java.util.Map)
   */
  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(MainLayoutConstants.ALL_DATASOURCES_COMPONENT_ID)) {
      return OBProvider.getInstance().get(AllDataSourcesComponent.class);
    }

    if (componentId.equals(MainLayoutConstants.MAIN_LAYOUT_ID)) {
      final MainLayoutComponent component = new MainLayoutComponent();
      component.setId(MainLayoutConstants.MAIN_LAYOUT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(MainLayoutConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID)) {
      final ViewComponent component = new ViewComponent();
      component.setId(MainLayoutConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(MainLayoutConstants.PROPERTIES_COMPONENT_ID)) {
      final PropertiesComponent component = new PropertiesComponent();
      component.setId(MainLayoutConstants.PROPERTIES_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponentType()
   */
  @Override
  public String getComponentType() {
    return MainLayoutConstants.COMPONENT_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  @Override
  public List<String> getGlobalResources() {
    final List<String> globalResources = new ArrayList<String>();
    globalResources.add("web/org.openbravo.client.application/js/property-store.js");
    globalResources.add("web/org.openbravo.client.application/js/remote-call-manager.js");
    globalResources.add("org.openbravo.client.kernel/" + MainLayoutConstants.COMPONENT_TYPE + "/"
        + MainLayoutConstants.PROPERTIES_COMPONENT_ID);
    globalResources.add("web/org.openbravo.client.application/js/classic-ob-window.js");
    globalResources.add("web/org.openbravo.client.application/js/classic-ob-help.js");
    globalResources.add("web/org.openbravo.client.application/js/popup-classic-ob-window.js");
    globalResources.add("web/org.openbravo.client.application/js/external-page.js");
    globalResources.add("web/org.openbravo.client.application/js/my-openbravo.js");
    // globalResources.add("org.openbravo.client.kernel/OBUIAPP_MainLayout/Application");
    // globalResources.add("web/org.openbravo.client.application/js/view-manager.js");
    // globalResources.add("web/org.openbravo.client.application/js/classic-ob-compatibility.js");
    // globalResources.add("web/org.openbravo.client.application/js/history-manager.js");

    // note this global resource is not placed in the datasource module
    // because the whole list of datasources is only used for the new application
    // and not for the selector
    // globalResources.add("org.openbravo.client.kernel/" + MainLayoutConstants.COMPONENT_TYPE + "/"
    // + MainLayoutConstants.ALL_DATASOURCES_COMPONENT_ID);
    return globalResources;
  }

  @Override
  public List<String> getTestResources() {
    final List<String> testResources = new ArrayList<String>();
    testResources.add("web/org.openbravo.client.application/js/test/ui-test.js");
    testResources.add("web/org.openbravo.client.application/js/test/property-store-test.js");
    return testResources;
  }
}
