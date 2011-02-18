package org.openbravo.userinterface.smartclient;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.openbravo.client.kernel.ApplicationComponent;
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProviderRegistry;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.StaticResourceComponent;

/**
 * Is used to provide the global resources needed for smartclient.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@Name("org.openbravo.userinterface.smartclient.SmartClientComponentProvider")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
public class SmartClientComponentProvider extends BaseComponentProvider {
  public static final String SC_COMPONENT_TYPE = "OBUISC_Smartclient";

  @Create
  public void initialize() {
    ComponentProviderRegistry.getInstance().registerComponentProvider(this);
  }

  /**
   * @return the {@link ApplicationComponent}.
   * @throws IllegalArgumentException
   */
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(TypesComponent.SC_TYPES_COMPONENT_ID)) {
      final TypesComponent component = new TypesComponent();
      component.setId(TypesComponent.SC_TYPES_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  /**
   * @return a set of global resources
   * @see org.openbravo.client.kernel.ComponentProvider#getGlobalResources()
   */
  public List<String> getGlobalResources() {
    final List<String> globalResources = new ArrayList<String>();
    globalResources.add(KernelConstants.RESOURCE_STRING_TAG + " var isomorphicDir='"
        + KernelConstants.RESOURCE_CONTEXT_URL_PARAMETER
        + "web/org.openbravo.userinterface.smartclient/isomorphic/';");
    globalResources.add("web/org.openbravo.userinterface.smartclient/isomorphic/ISC_Combined.js");

    // note need to use document.write otherwise the isc.autoDraw is not done
    // after the ISC_Combined.js load
    // globalResources.add(KernelConstants.RESOURCE_STRING_TAG
    // + " document.write('<' + 'script>isc.setAutoDraw(false);<' + '/script>');");
    // NOTE: isc.setAutoDraw(false); is now placed inside the smartclient-labels.js

    // a trick to let classic OB windows with a selector show the classic OB skin
    // needs to be removed later
    globalResources
        .add(KernelConstants.RESOURCE_STRING_TAG
            + " if ('undefined' === (typeof OB_smartClientSkinLocation) || !OB_smartClientSkinLocation) OB_smartClientSkinLocation = 'web/org.openbravo.userinterface.smartclient/openbravo/skins/2.50_emulation/smartclient/';");
    globalResources.add("\"+ OB_smartClientSkinLocation + \"load_skin.js");
    globalResources.add("web/org.openbravo.userinterface.smartclient/js/ob-smartclient-labels.js");
    globalResources.add("org.openbravo.client.kernel/" + SC_COMPONENT_TYPE + "/"
        + TypesComponent.SC_TYPES_COMPONENT_ID);

    // final Component component = new ApplicationComponent();
    // globalResources.add(KernelConstants.RESOURCE_STRING_TAG
    // + ComponentGenerator.getInstance().generate(component));
    return globalResources;
  }

  /**
   * @return
   */
  public String getComponentType() {
    return SC_COMPONENT_TYPE;
  }

  /**
   * @return the package name of the module to which this provider belongs
   */
  public String getModulePackageName() {
    return this.getClass().getPackage().getName();
  }

}
