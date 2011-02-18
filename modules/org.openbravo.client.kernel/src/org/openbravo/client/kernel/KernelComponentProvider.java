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
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.openbravo.dal.core.OBContext;

/**
 * Provides Kernel Components.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@Name("org.openbravo.client.kernel.KernelComponentProvider")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
public class KernelComponentProvider extends BaseComponentProvider {

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
    final BaseComponent component = createComponent(componentId, parameters);
    component.setParameters(parameters);
    return component;
  }

  protected BaseComponent createComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(KernelConstants.RESOURCE_COMPONENT_ID)) {
      return new StaticResourceComponent();
    } else if (componentId.equals(KernelConstants.APPLICATION_COMPONENT_ID)) {
      return new ApplicationComponent();
    } else if (componentId.equals(KernelConstants.TEST_COMPONENT_ID)) {
      return new TestComponent();
    } else if (componentId.equals(KernelConstants.DOCUMENT_COMPONENT_ID)) {
      return new DocumentationComponent();
    } else if (componentId.equals(KernelConstants.LABELS_COMPONENT_ID)) {
      return new I18NComponent();
    }
    throw new IllegalArgumentException("Component " + componentId + " not supported here");
  }

  // in case of the application component also make it role/org dependent, this
  // also covers client dependency
  public String getVersionParameters(String resource) {
    final String versionParam = super.getVersionParameters(resource);
    if (resource.contains(KernelConstants.APPLICATION_COMPONENT_ID)) {
      return versionParam + "&_role=" + OBContext.getOBContext().getRole().getId() + "&_org="
          + OBContext.getOBContext().getCurrentOrganization().getId();
    }
    return versionParam;
  }

  /**
   * @return an empty String (no global resources)
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  public List<String> getGlobalResources() {
    final List<String> globalResources = new ArrayList<String>();
    globalResources.add("org.openbravo.client.kernel/" + KernelConstants.KERNEL_COMPONENT_TYPE
        + "/" + KernelConstants.APPLICATION_COMPONENT_ID);
    globalResources.add("org.openbravo.client.kernel/" + KernelConstants.KERNEL_COMPONENT_TYPE
        + "/" + KernelConstants.LABELS_COMPONENT_ID);
    return globalResources;
  }

  /**
   * @return returns {@link KernelConstants#KERNEL_COMPONENT_TYPE}
   * @see org.openbravo.client.kernel.ComponentProvider#getName()
   */
  public String getComponentType() {
    return KernelConstants.KERNEL_COMPONENT_TYPE;
  }
}
