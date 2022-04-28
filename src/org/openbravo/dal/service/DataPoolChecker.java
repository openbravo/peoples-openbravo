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
 * All portions are Copyright (C) 2018-2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.dal.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.database.ExternalConnectionPool;

/**
 * Helper class used to determine if a entity should use the read-only pool to retrieve data
 */
public class DataPoolChecker implements OBSingleton {
  private static final Logger log = LogManager.getLogger();

  private Map<String, String> confPoolMap = new HashMap<>();
  private final List<String> validPoolValues = Arrays.asList(ExternalConnectionPool.DEFAULT_POOL,
      ExternalConnectionPool.READONLY_POOL);
  private Map<String, String> defaultReadOnlyPool = new HashMap<>();
  private final List<DataPoolConfiguration> dataPoolConfigurations = WeldUtils
      .getInstances(DataPoolConfiguration.class);
  private final String DEFAULT_TYPE = "REPORT";

  private static DataPoolChecker instance;

  public static synchronized DataPoolChecker getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(DataPoolChecker.class);
      instance.initialize();
    }
    return instance;
  }

  /**
   * Initializes the checker caching the list of entities with a database pool assigned and the
   * default preference as well
   */
  private void initialize() {
    dataPoolConfigurations.add(new ReportDataPoolConfiguration());
    dataPoolConfigurations.stream().forEach(config -> {
      refreshDefaultPoolPreference(config);
      refreshDataPoolEntitiesValues(config);
    });
  }

  /**
   * Reload from DB the entities that should use the Read-only pool
   */
  public void refreshDataPoolEntities() {
    confPoolMap = new HashMap<>();
    dataPoolConfigurations.stream().forEach(config -> {
      refreshDataPoolEntitiesValues(config);
    });
  }

  private void refreshDataPoolEntitiesValues(DataPoolConfiguration config) {
    config.getDataPoolSelection()
        .forEach((k, v) -> confPoolMap.put(config.getDataType() + " - " + k, v));
  }

  private void refreshDefaultPoolPreference(DataPoolConfiguration config) {
    //@formatter:off
    String hql = 
            "select p.searchKey " +
            "  from ADPreference p " +
            " where p.property='"+ config.getPreferenceName() +"' " +
            "   and p.active = true " +
            "   and p.visibleAtClient.id = '0' " +
            "   and p.visibleAtOrganization.id = '0' ";
    //@formatter:on
    Query<String> defaultPoolQuery = OBDal.getInstance()
        .getSession()
        .createQuery(hql, String.class)
        .setMaxResults(1);
    setDefaultReadOnlyPool(config.getDataType(), defaultPoolQuery.uniqueResult());
  }

  /**
   * Set the default pool used when requesting the read-only pool
   *
   * @param defaultPool
   *          the ID of the default pool returned when requesting a read-only instance
   */
  private void setDefaultReadOnlyPool(String configType, String defaultPool) {
    if (validPoolValues.contains(defaultPool)) {
      log.debug("Pool {} is set as the default to use with {}", defaultPool, configType);
      defaultReadOnlyPool.put(configType, defaultPool);
    } else {
      defaultReadOnlyPool.put(configType, ExternalConnectionPool.READONLY_POOL);
      log.warn(
          "Preference value {} is not a valid Database pool. Using READONLY_POOL as the default value",
          defaultPool);
    }
  }

  /**
   * Verifies whether the current entity should use the default pool.
   *
   * @return true if the current entity should use the default pool
   */
  boolean shouldUseDefaultPool(String entityId, String dataType, String poolExtraProperty) {
    String configPool = null;

    if (!StringUtils.isBlank(entityId) && !StringUtils.isBlank(dataType)
        && !StringUtils.isBlank(poolExtraProperty)) {
      configPool = confPoolMap.get(dataType + " - " + entityId + " - " + poolExtraProperty);
    } else if (!StringUtils.isBlank(entityId) && !StringUtils.isBlank(dataType)) {
      configPool = confPoolMap.get(dataType + " - " + entityId);
      if (configPool == null) {
        configPool = confPoolMap.get(DEFAULT_TYPE + " - " + entityId);
      }
    } else if (!StringUtils.isBlank(entityId)) {
      configPool = confPoolMap.get(DEFAULT_TYPE + " - " + entityId);
    }

    String poolUsedForEntity = configPool != null ? configPool
        : !defaultReadOnlyPool.containsKey(dataType) ? defaultReadOnlyPool.get(DEFAULT_TYPE)
            : defaultReadOnlyPool.get(dataType);

    if (entityId != null) {
      log.debug("Using pool {} for entity with id {}", poolUsedForEntity, entityId);
    }

    return ExternalConnectionPool.DEFAULT_POOL.equals(poolUsedForEntity);
  }

}
