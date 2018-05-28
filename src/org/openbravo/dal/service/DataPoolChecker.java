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
 * All portions are Copyright (C) 2018 Openbravo SLU
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
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ExternalConnectionPool;

/**
 * Helper class used to determine if a request should use the read-only pool to retrieve data
 */
public class DataPoolChecker implements OBSingleton {
  private static final Logger log = Logger.getLogger(DataPoolChecker.class);
  private static final int REPORT_ID = 0;
  private static final int DATA_POOL = 1;

  private Map<String, String> dataPoolProcesses = new HashMap<>();
  private final List<String> validPoolValues = Arrays.asList(ExternalConnectionPool.DEFAULT_POOL,
      ExternalConnectionPool.READONLY_POOL);
  private String defaultReadOnlyPool = ExternalConnectionPool.READONLY_POOL;

  private static DataPoolChecker instance;

  public static synchronized DataPoolChecker getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(DataPoolChecker.class);
      instance.initialize();
    }
    return instance;
  }

  /**
   * Initializes the checker caching the list of processes with a database pool assigned and the
   * default preference as well
   */
  private void initialize() {
    refreshDefaultPoolPreference();
    refreshDataPoolProcesses();
  }

  /**
   * Reload from DB the processes that should use the Read-only pool
   */
  public void refreshDataPoolProcesses() {
    dataPoolProcesses = findActiveDataPoolSelection();
  }

  private Map<String, String> findActiveDataPoolSelection() {
    Map<String, String> selection = new HashMap<>();
    OBContext.setAdminMode(false);
    try {
      final StringBuilder hql = new StringBuilder();
      hql.append("select dps.report.id, dps.dataPool from OBUIAPP_Data_Pool_Selection dps ");
      hql.append("where dps.active = true");

      Query query = OBDal.getInstance().getSession().createQuery(hql.toString());
      for (Object item : query.list()) {
        final Object[] values = (Object[]) item;
        selection.put(values[REPORT_ID].toString(), values[DATA_POOL].toString());
      }
    } catch (Exception e) {
      log.error("Cannot fetch DataPoolSelection", e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return selection;
  }

  private void refreshDefaultPoolPreference() {
    final StringBuilder hql = new StringBuilder();
    hql.append("select p.searchKey from ADPreference p ");
    hql.append("where p.property='DefaultDBPoolForReports' and p.active=true and p.client.id='0' and p.organization.id='0' ");
    Query defaultPoolQuery = OBDal.getInstance().getSession().createQuery(hql.toString());
    defaultPoolQuery.setMaxResults(1);

    setDefaultReadOnlyPool((String) defaultPoolQuery.uniqueResult());
  }

  /**
   * Verifies whether the current process should use the default pool. Process can be either a
   * instance of Report and Process or a Process Definition.
   *
   * @return true if the current process should use the default pool
   */
  protected boolean shouldUseDefaultPool(String processId) {
    String poolUsedForProcess = defaultReadOnlyPool;
    if (!StringUtils.isBlank(processId)) {
      String poolForProcess = dataPoolProcesses.get(processId);
      if (poolForProcess != null) {
        poolUsedForProcess = poolForProcess;
      }
    }

    log.debug("Using pool " + poolUsedForProcess + " for report with id " + processId);
    return ExternalConnectionPool.DEFAULT_POOL.equals(poolUsedForProcess);
  }

  /**
   * Set the default pool used when requesting the read-only pool
   *
   * @param defaultPool
   *          the ID of the default pool returned when requesting a read-only instance
   */
  private void setDefaultReadOnlyPool(String defaultPool) {
    if (validPoolValues.contains(defaultPool)) {
      defaultReadOnlyPool = defaultPool;
    } else {
      log.warn("Preference value " + defaultPool
          + " is not a valid Database pool. Using READONLY_POOL as the default value");
      defaultReadOnlyPool = ExternalConnectionPool.READONLY_POOL;
    }
  }
}
