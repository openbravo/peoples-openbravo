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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openbravo.base.util.Check;

/**
 * The ActionHandlerRegistry is the global registry for all {@link ActionHandler} instances. Action
 * Handlers should be registered at application startup/initialization.
 * 
 * @author mtaal
 */
public class ActionHandlerRegistry {
  private static final Logger log = Logger.getLogger(ActionHandlerRegistry.class);

  private static ActionHandlerRegistry instance = new ActionHandlerRegistry();

  public static synchronized ActionHandlerRegistry getInstance() {
    if (instance == null) {
      instance = new ActionHandlerRegistry();
    }
    return instance;
  }

  public static synchronized void setInstance(ActionHandlerRegistry instance) {
    ActionHandlerRegistry.instance = instance;
  }

  private Map<String, ActionHandler> registry = new ConcurrentHashMap<String, ActionHandler>();

  /**
   * Returns an {@link ActionHandler} using the passed name.
   * 
   * @param name
   *          the name of the {@link ActionHandler}
   * @return an instance of {@link ActionHandler} or an exception if not found
   */
  public synchronized ActionHandler getActionHandler(String name) {
    final ActionHandler actionHandler = registry.get(name);
    Check.isNotNull(actionHandler, "No action handler found using name " + name);
    return actionHandler;
  }

  /**
   * Registers a new {@link ActionHandler}. An error is logged if there is already an ActionHandler
   * for that name.
   * 
   * @param actionHandler
   */
  public synchronized void registerActionHandler(ActionHandler actionHandler) {
    if (registry.get(actionHandler.getName()) != null) {
      log.error("There is already an ActionHandler registered for the name "
          + actionHandler.getName() + " its class " + registry.get(actionHandler.getName()));
    }

    final String actionHandlerName = actionHandler.getName();
    registry.put(actionHandlerName, actionHandler);
  }
}
