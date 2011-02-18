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
package org.openbravo.client.myob;

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
 * 
 * @author iperdomo
 */
@Name("org.openbravo.client.application.MyOpenbravoComponentProvider")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
public class MyOpenbravoComponentProvider extends BaseComponentProvider {
  public static final String COMPONENT_TYPE = "OBMYOB_MyOpenbravo";

  @Create
  public void initialize() {
    ComponentProviderRegistry.getInstance().registerComponentProvider(this);
  }

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(MyOpenbravoComponent.COMPONENT_ID)) {
      final MyOpenbravoComponent component = new MyOpenbravoComponent();
      component.setParameters(parameters);
      return component;
    }
    if (componentId.equals(MyOpenbravoWidgetComponent.COMPONENT_ID)) {
      final MyOpenbravoWidgetComponent component = new MyOpenbravoWidgetComponent();
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public String getComponentType() {
    return COMPONENT_TYPE;
  }

  @Override
  public List<String> getGlobalResources() {
    final List<String> globalResources = new ArrayList<String>();
    globalResources.add("web/org.openbravo.client.myob/js/ob-myopenbravo.js");
    globalResources.add("org.openbravo.client.kernel/" + COMPONENT_TYPE + "/"
        + MyOpenbravoComponent.COMPONENT_ID);
    globalResources.add("web/org.openbravo.client.myob/js/ob-widget.js");
    globalResources.add("web/org.openbravo.client.myob/js/ob-community-branding-widget.js");
    globalResources
        .add("\"+ OB_smartClientSkinLocation + \"../org.openbravo.client.myob/ob-widget-styles.js");
    return globalResources;
  }

  @Override
  public List<String> getTestResources() {
    final List<String> testResources = new ArrayList<String>();
    testResources.add("web/org.openbravo.client.myob/js/test/ob-myopenbravo-test.js");
    return testResources;
  }
}
