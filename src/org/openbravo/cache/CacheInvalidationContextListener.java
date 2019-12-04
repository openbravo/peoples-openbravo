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
 * All portions are Copyright (C) 2008-2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  Mauricio Peccorini.
 ************************************************************************
 */

package org.openbravo.cache;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openbravo.base.weld.WeldUtils;

public class CacheInvalidationContextListener implements ServletContextListener {

  CacheInvalidationBackgroundManager backgroundManager;

  /**
   * This method starts the cache invalidation background thread
   * 
   * @param event
   *          Unused
   */

  @Override
  public void contextInitialized(ServletContextEvent event) {
    WeldUtils.getStaticInstanceBeanManager()
        .fireEvent(new CacheInvalidationBackgroundManager.StartEvent());
  }

  /**
   * This method stops the cache invalidation background thread
   * 
   * @param event
   *          Unused
   */

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    backgroundManager.stop();
  }
}
