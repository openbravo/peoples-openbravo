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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProviderRegistry;

/**
 * Provides Selector Components.
 * 
 * @author mtaal
 */
@Name("org.openbravo.userinterface.selector.SelectorComponentProvider")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
public class SelectorComponentProvider extends BaseComponentProvider {

  @Create
  public void initialize() {
    ComponentProviderRegistry.getInstance().registerComponentProvider(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponent(java.lang.String,
   * java.lang.String, java.util.Map)
   */
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    final SelectorComponent selectorComponent = new SelectorComponent();
    selectorComponent.setId(componentId);
    selectorComponent.setParameters(parameters);
    return selectorComponent;
  }

  /**
   * @return an empty String (no global resources)
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  public List<String> getGlobalResources() {
    ArrayList<String> resources = new ArrayList<String>();
    resources.add("web/org.openbravo.userinterface.selector/js/ob_selector_widget.js");
    resources.add("web/org.openbravo.userinterface.selector/js/ob_selector_link_widget.js");
    return resources;
  }

  /**
   * @return returns {@link SelectorConstants#SELECTOR_COMPONENT_TYPE}
   * @see org.openbravo.client.kernel.ComponentProvider#getName()
   */
  public String getComponentType() {
    return SelectorConstants.SELECTOR_COMPONENT_TYPE;
  }

  /**
   * @return the package name of the module to which this provider belongs
   */
  public String getModulePackageName() {
    return this.getClass().getPackage().getName();
  }

}
