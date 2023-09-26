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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base;

import static java.util.Comparator.comparing;

import java.time.Duration;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.cache.TimeInvalidatedCache;
import org.openbravo.client.application.GCSystem;
import org.openbravo.client.application.GCTab;
import org.openbravo.client.application.window.OBViewUtil;
import org.openbravo.client.application.window.StandardWindowComponent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

/**
 * TO BE DONE
 */
public class GridConfigurationCache implements OBSingleton {

  private static GridConfigurationCache instance;

  private static TimeInvalidatedCache<String, Optional<GCSystem>> systemGridConfigurationCache = TimeInvalidatedCache
      .newBuilder()
      .name("System_GCC")
      .expireAfterDuration(Duration.ofMinutes(5))
      .build(GridConfigurationCache::initializeSystemConfig);
  private static TimeInvalidatedCache<String, Optional<GCTab>> tabGridConfigurationCache = TimeInvalidatedCache
      .newBuilder()
      .name("Tab_GCC")
      .expireAfterDuration(Duration.ofMinutes(5))
      .build(GridConfigurationCache::initializeTabConfig);

  private static final Logger log = LogManager.getLogger();

  /**
   * @return the singleton instance of OrganizationNodeCache
   */
  public static GridConfigurationCache getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(GridConfigurationCache.class);
    }
    return instance;
  }

  private static Optional<GCSystem> initializeSystemConfig(String key) {
    return StandardWindowComponent.getSystemGridConfig();
  }

  private static Optional<GCTab> initializeTabConfig(String tabId) {
    OBQuery<GCTab> qGCTab = OBDal.getInstance()
        .createQuery(GCTab.class, "as g where g.tab.id = :tabId");
    qGCTab.setNamedParameter("tabId", tabId);
    return qGCTab.stream()
        .sorted( //
            comparing(GCTab::getSeqno) //
                .thenComparing(GCTab::getId)) //
        .findFirst();
  }

  /**
   * TODO Explain why we wont cache the composed configuration as JSONObject (to avoid caching
   * multiple copies of the same configuration)
   */
  public JSONObject getGridConfigurationForTab(String tabId) {
    Optional<GCSystem> sysConf = systemGridConfigurationCache.get("system");
    Optional<GCTab> tabConf = tabGridConfigurationCache.get(tabId);
    return OBViewUtil.getGridConfigurationSettings(sysConf, tabConf);
  }

  /**
   */
  // TODO Use on eventHandler
  void clearSystemGridConfiguration() {
    systemGridConfigurationCache.invalidateAll();
  }

  // TODO Use on eventHandler
  void clearTabGridConfiguration(String tabId) {
    tabGridConfigurationCache.invalidate(tabId);
  }
}
