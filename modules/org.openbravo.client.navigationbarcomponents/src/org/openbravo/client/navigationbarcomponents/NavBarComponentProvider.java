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
package org.openbravo.client.navigationbarcomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProviderRegistry;

/**
 * The navigation bar component provider.
 * 
 * @author mtaal
 */
@Name("org.openbravo.client.navigationbarcomponents.NavBarComponentProvider")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
@Startup
@AutoCreate
public class NavBarComponentProvider extends BaseComponentProvider {

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
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getComponentType()
   */
  @Override
  public String getComponentType() {
    return "UINAVBA_Component";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  @Override
  public List<String> getGlobalResources() {
    final List<String> globalResources = new ArrayList<String>();
    globalResources
        .add("web/org.openbravo.client.navigationbarcomponents/js/navigationbarcomponents-application.js");

    globalResources.add("web/org.openbravo.client.navigationbarcomponents/js/recent-utilities.js");
    globalResources.add("web/org.openbravo.client.navigationbarcomponents/js/alert-manager.js");
    globalResources
        .add("web/org.openbravo.client.navigationbarcomponents/js/ob-user-profile-widget.js");
    globalResources
        .add("web/org.openbravo.client.navigationbarcomponents/js/ob-help-about-widget.js");
    return globalResources;
  }

  @Override
  public List<String> getTestResources() {
    final List<String> testResources = new ArrayList<String>();
    testResources.add("web/org.openbravo.client.navigationbarcomponents/js/test/navbar-test.js");
    return testResources;
  }
}
