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
import org.openbravo.client.application.window.EntityWindowMappingComponent;
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProviderRegistry;

/**
 * 
 * @author iperdomo
 */
@Name("org.openbravo.client.application.ApplicationComponentProvider")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
public class ApplicationComponentProvider extends BaseComponentProvider {

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
    if (componentId.equals(ApplicationConstants.MAIN_LAYOUT_ID)) {
      final MainLayoutComponent component = new MainLayoutComponent();
      component.setId(ApplicationConstants.MAIN_LAYOUT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(EntityWindowMappingComponent.COMPONENT_ID)) {
      final EntityWindowMappingComponent component = new EntityWindowMappingComponent();
      component.setId(EntityWindowMappingComponent.COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(ApplicationConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID)) {
      final ViewComponent component = new ViewComponent();
      component.setId(ApplicationConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(ApplicationConstants.PROPERTIES_COMPONENT_ID)) {
      final PropertiesComponent component = new PropertiesComponent();
      component.setId(ApplicationConstants.PROPERTIES_COMPONENT_ID);
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
    return ApplicationConstants.COMPONENT_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  @Override
  public List<String> getGlobalResources() {
    final List<String> globalResources = new ArrayList<String>();
    globalResources.add("web/org.openbravo.client.application/js/ob-utilities.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-formitem-widgets.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-quickrun-widget.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-property-store.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-test-registry.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-remote-call-manager.js");
    globalResources.add("org.openbravo.client.kernel/" + ApplicationConstants.COMPONENT_TYPE + "/"
        + ApplicationConstants.PROPERTIES_COMPONENT_ID);
    globalResources.add("web/org.openbravo.client.application/js/ob-classic-window.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-classic-help.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-external-page.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-standard-view.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-popup.js");
    globalResources
        .add("\"+ OB_smartClientSkinLocation + \"../org.openbravo.client.application/ob-popup-styles.js");
    globalResources
        .add("\"+ OB_smartClientSkinLocation + \"../org.openbravo.client.application/ob-dialog-styles.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-classic-popup.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-toolbar.js");
    globalResources
        .add("\"+ OB_smartClientSkinLocation + \"../org.openbravo.client.application/ob-toolbar-styles.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-view-form.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-view-grid.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-keyboard-manager.js");
    // globalResources.add("web/org.openbravo.client.application/js/ob-my-openbravo-static.js");
    globalResources.add("org.openbravo.client.kernel/" + ApplicationConstants.COMPONENT_TYPE + "/"
        + EntityWindowMappingComponent.COMPONENT_ID);
    // globalResources
    // .add("web/org.openbravo.client.application/js/ob-navigationbarcomponents-application.js");

    globalResources.add("web/org.openbravo.client.application/js/ob-recent-utilities.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-alert-manager.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-user-profile-widget.js");
    globalResources.add("web/org.openbravo.client.application/js/ob-help-about-widget.js");
    return globalResources;
  }

  @Override
  public List<String> getTestResources() {
    final List<String> testResources = new ArrayList<String>();
    testResources.add("web/org.openbravo.client.application/js/test/ob-ui-test.js");
    testResources.add("web/org.openbravo.client.application/js/test/ob-property-store-test.js");
    testResources.add("web/org.openbravo.client.application/js/test/ob-utilities-date-test.js");
    testResources.add("web/org.openbravo.client.application/js/test/ob-navbar-test.js");
    return testResources;
  }
}
