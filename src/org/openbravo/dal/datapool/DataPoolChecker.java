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

import org.apache.commons.lang.StringUtils;
import org.openbravo.client.application.DataPoolSelection;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ExternalConnectionPool;

/**
 * Helper class used to determine if a request should use the read-only pool to retrieve data
 */
public class DataPoolChecker {

  private static Map<String, String> dataPoolProcesses = new HashMap<>();
  private static String defaultReadOnlyPool = ExternalConnectionPool.READONLY_POOL;

  /**
   * Reload from DB the processes that should use the Read-only pool
   */
  public static void refreshDataPoolProcesses() {
    List<DataPoolSelection> readOnlyDataPoolSelection = findActiveDataPoolSelection();
    setDataPoolProcesses(convertToMap(readOnlyDataPoolSelection));
  }

  private static List<DataPoolSelection> findActiveDataPoolSelection() {
    OBContext.setAdminMode(false);
    OBCriteria<DataPoolSelection> criteria = OBDal.getInstance().createCriteria(
        DataPoolSelection.class);
    criteria.setFilterOnActive(true);
    List<DataPoolSelection> selection = criteria.list();
    OBContext.restorePreviousMode();

    return selection;
  }

  /**
   * @param selectionList
   *          a list of DataPoolSelection objects
   * @return A Map<String,String> with the Process ID (either for Process and ProcessDefinition) as
   *         key, and their corresponding DataPool as the value
   */
  private static Map<String, String> convertToMap(List<DataPoolSelection> selectionList) {
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
  public static boolean shouldUseDefaultPool(String processId) {
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

  private static boolean preferenceIsSetToDefaultPool() {
    return ExternalConnectionPool.DEFAULT_POOL.equals(defaultReadOnlyPool);
  }

  private static boolean processIsNotAvailable(String processId) {
    return StringUtils.isBlank(processId);
  }

  /**
   * Set the default pool used when requesting the read-only pool
   *
   * @param defaultReadOnlyPool
   *          the ID of the default pool returned when requesting read-only
   */
  public static void setDefaultReadOnlyPool(String defaultReadOnlyPool) {
    DataPoolChecker.defaultReadOnlyPool = defaultReadOnlyPool;
  }

  /**
   * Set the map of processes with their assigned data pool
   *
   * @param dataPoolProcesses
   *          a map with the ID of the process as key and the pool type (DEFAULT|RO) as the value
   */
  public static void setDataPoolProcesses(Map<String, String> dataPoolProcesses) {
    DataPoolChecker.dataPoolProcesses = dataPoolProcesses;
  }
}
