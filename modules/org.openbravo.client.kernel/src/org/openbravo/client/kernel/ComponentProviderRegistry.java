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
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openbravo.base.util.Check;

/**
 * The ComponentProviderRegistry is the global registry for all {@link ComponentProvider} instances.
 * Component providers should be registered at application startup/initialization.
 * 
 * @author mtaal
 */
public class ComponentProviderRegistry {
  private static final Logger log = Logger.getLogger(ComponentProviderRegistry.class);

  private static ComponentProviderRegistry instance = new ComponentProviderRegistry();

  public static synchronized ComponentProviderRegistry getInstance() {
    if (instance == null) {
      instance = new ComponentProviderRegistry();
    }
    return instance;
  }

  public static synchronized void setInstance(ComponentProviderRegistry instance) {
    ComponentProviderRegistry.instance = instance;
  }

  private Map<String, ComponentProvider> registry = new ConcurrentHashMap<String, ComponentProvider>();

/**
   * Returns a component provider using the passed name. Component providers are registered
   * using their name ({@link ComponentProvider#getName()).
   * @param name
   *          the name of the {@link ComponentProvider}
   * @return an instance of {@link ComponentProvider} or an exception if not found
   */
  public synchronized ComponentProvider getComponentProvider(String name) {
    final ComponentProvider componentProvider = registry.get(name);
    Check.isNotNull(componentProvider, "No component provider found using name " + name);
    return componentProvider;
  }

  /**
   * Registers a new component provider. An error is logged if there is already a component provider
   * for that name (@link {@link ComponentProvider#getComponentType()}.
   * 
   * @param componentProvider
   */
  public synchronized void registerComponentProvider(ComponentProvider componentProvider) {
    if (registry.get(componentProvider.getComponentType()) != null) {
      log.error("There is already a component provider registered for the component type "
          + componentProvider.getComponentType() + " its class "
          + registry.get(componentProvider.getComponentType()));
    }
    registry.put(componentProvider.getComponentType(), componentProvider);
  }

  /**
   * @return all component providers registered here.
   */
  public List<ComponentProvider> getComponentProviders() {
    return new ArrayList<ComponentProvider>(registry.values());
  }
}
