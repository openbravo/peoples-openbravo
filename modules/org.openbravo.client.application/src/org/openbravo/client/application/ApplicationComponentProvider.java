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
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;

/**
 * 
 * @author iperdomo
 */
@ApplicationScoped
@ComponentProvider.Qualifier(ApplicationConstants.COMPONENT_TYPE)
public class ApplicationComponentProvider extends BaseComponentProvider {
  public static final String QUALIFIER = ApplicationConstants.COMPONENT_TYPE;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponent(java.lang.String,
   * java.util.Map)
   */
  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(ApplicationConstants.MAIN_LAYOUT_ID)) {
      final MainLayoutComponent component = getComponent(MainLayoutComponent.class);
      component.setId(ApplicationConstants.MAIN_LAYOUT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(ApplicationConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID)) {
      final ViewComponent component = getComponent(ViewComponent.class);
      component.setId(ApplicationConstants.MAIN_LAYOUT_VIEW_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(ApplicationConstants.PROPERTIES_COMPONENT_ID)) {
      final PropertiesComponent component = getComponent(PropertiesComponent.class);
      component.setId(ApplicationConstants.PROPERTIES_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-utilities.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-formitem-widgets.js", true));
    globalResources.add(createStaticResource("web/org.openbravo.client.application/js/ob-tab.js",
        false));
    globalResources.add(createStaticResource("web/org.openbravo.client.application/js/ob-grid.js",
        false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-quickrun-widget.js", false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-property-store.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-test-registry.js", true));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-remote-call-manager.js", true));
    globalResources
        .add(createDynamicResource("org.openbravo.client.kernel/"
            + ApplicationConstants.COMPONENT_TYPE + "/"
            + ApplicationConstants.PROPERTIES_COMPONENT_ID));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-classic-window.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-classic-help.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-external-page.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-standard-window.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-standard-view-tabset.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-standard-view.js", false));
    globalResources.add(createStaticResource("web/org.openbravo.client.application/js/ob-popup.js",
        false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-view-form-linked-items.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-view-form.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-view-grid.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-keyboard-manager.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-classic-popup.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-toolbar.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-messagebar.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-statusbar.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-recent-utilities.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-alert-manager.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-user-profile-widget.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-help-about-widget.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-action-button.js", false));

    // Styling
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-tab-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-form-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-grid-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-navigation-bar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-popup-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-toolbar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-messagebar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-statusbar-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-main-content-styles.css", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-application-styles.css", false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-tab-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-form-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-grid-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-toolbar-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-messagebar-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-statusbar-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-popup-styles.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-dialog-styles.js", false));

    // before the main layout
    globalResources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER
            + "/org.openbravo.client.application/ob-application-styles.js", false));

    // Application
    globalResources.add(createStaticResource("org.openbravo.client.kernel/"
        + ApplicationConstants.COMPONENT_TYPE + "/" + ApplicationConstants.MAIN_LAYOUT_ID, false));

    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-view-manager.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-classic-compatibility.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.client.application/js/ob-history-manager.js", false));

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
