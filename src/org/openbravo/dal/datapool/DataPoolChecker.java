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
import org.openbravo.database.SessionInfo;

/**
 * Helper class used to determine if a request should use the read-only pool to retrieve data
 */
public class DataPoolChecker {
  private static Map<String, String> dataPoolProcesses = new HashMap<>();
  private static String defaultReadOnlyPool = ExternalConnectionPool.READONLY_POOL;

  /**
   * Reload from DB the processes that should use the Read-only pool
   */
  @SuppressWarnings("unchecked")
  public static void refreshDataPoolProcesses() {
    OBContext.setAdminMode(false);
    OBCriteria<DataPoolSelection> criteria = OBDal.getInstance().createCriteria(
        DataPoolSelection.class);
    criteria.setFilterOnActive(true);
    List<DataPoolSelection> readOnlyDataPoolSelection = criteria.list();

    DataPoolChecker.dataPoolProcesses.clear();
    for (DataPoolSelection selection : readOnlyDataPoolSelection) {
      if (selection.getProcessDefintion() != null) {
        dataPoolProcesses.put(selection.getProcessDefintion().getId(), selection.getDataPool());
      } else if (selection.getProcess() != null) {
        dataPoolProcesses.put(selection.getProcess().getId(), selection.getDataPool());
      }
    }

    OBContext.restorePreviousMode();
  }

  /**
   * Verifies whether the current process from SessionInfo.getProcessId() should use the default
   * pool
   *
   * @return true if the current process should use the default pool
   */
  public static boolean shouldUseDefaultPool() {
    String processId = SessionInfo.getProcessId();
    if (StringUtils.isNotBlank(processId)) {
      String poolForProcess = dataPoolProcesses.get(processId);
      if (poolForProcess != null) {
        return poolForProcess.equals(ExternalConnectionPool.DEFAULT_POOL);
      } else {
        return ExternalConnectionPool.DEFAULT_POOL.equals(defaultReadOnlyPool);
      }
    }

    return ExternalConnectionPool.DEFAULT_POOL.equals(defaultReadOnlyPool);
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
}
