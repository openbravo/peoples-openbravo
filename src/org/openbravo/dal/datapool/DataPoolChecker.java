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
package org.openbravo.dal.datapool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.DataPoolSelection;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ExternalConnectionPool;

/**
 * Helper class used to determine if a request should use the read-only pool to retrieve data
 */
@ApplicationScoped
public class DataPoolChecker {

  private Map<String, String> dataPoolProcesses = new HashMap<>();
  private String defaultReadOnlyPool = ExternalConnectionPool.READONLY_POOL;

  public static DataPoolChecker getInstance() {
    return WeldUtils.getInstanceFromStaticBeanManager(DataPoolChecker.class);
  }

  /**
   * Initializes the checker caching the list of processes with a database pool assigned and the
   * default preference as well
   */
  public void initialize() {
    refreshDefaultPoolPreference();
    refreshDataPoolProcesses();
  }

  /**
   * Reload from DB the processes that should use the Read-only pool
   */
  public void refreshDataPoolProcesses() {
    setDataPoolProcesses(convertToMap(findActiveDataPoolSelection()));
  }

  private List<DataPoolSelection> findActiveDataPoolSelection() {
    OBContext.setAdminMode(false);
    OBCriteria<DataPoolSelection> criteria = OBDal.getInstance().createCriteria(
        DataPoolSelection.class);
    criteria.setFilterOnActive(true);
    List<DataPoolSelection> selection = criteria.list();
    OBContext.restorePreviousMode();

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
   * @param selectionList
   *          a list of DataPoolSelection objects
   * @return A Map<String,String> with the Process ID (either for Process and ProcessDefinition) as
   *         key, and their corresponding DataPool as the value
   */
  private Map<String, String> convertToMap(List<DataPoolSelection> selectionList) {
    Map<String, String> processes = new HashMap<>();

    for (DataPoolSelection selection : selectionList) {
      processes.put(selection.getReport().getId(), selection.getDataPool());
    }

    return processes;
  }

  /**
   * Verifies whether the current process from SessionInfo.getProcessId() should use the default
   * pool
   *
   * @return true if the current process should use the default pool
   */
  public boolean shouldUseDefaultPool(String processId) {
    if (processIsNotAvailable(processId)) {
      return preferenceIsSetToDefaultPool();
    } else {
      String poolForProcess = dataPoolProcesses.get(processId);
      if (poolForProcess != null) {
        return poolForProcess.equals(ExternalConnectionPool.DEFAULT_POOL);
      }

      return preferenceIsSetToDefaultPool();
    }
  }

  private boolean preferenceIsSetToDefaultPool() {
    return ExternalConnectionPool.DEFAULT_POOL.equals(defaultReadOnlyPool);
  }

  private boolean processIsNotAvailable(String processId) {
    return StringUtils.isBlank(processId);
  }

  /**
   * Set the default pool used when requesting the read-only pool
   *
   * @param defaultPool
   *          the ID of the default pool returned when requesting read-only
   */
  public void setDefaultReadOnlyPool(String defaultPool) {
    defaultReadOnlyPool = defaultPool;
  }

  /**
   * Set the map of processes with their assigned data pool
   *
   * @param processes
   *          a map with the ID of the process as key and the pool type (DEFAULT|RO) as the value
   */
  public void setDataPoolProcesses(Map<String, String> processes) {
    dataPoolProcesses = processes;
  }

}
