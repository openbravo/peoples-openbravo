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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.client.application.DataPoolSelection;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ExternalConnectionPool;
import org.openbravo.database.SessionInfo;

/**
 * Helper class used to determine if a request should use the read-only pool to retrieve data
 */
public class DataPoolChecker {
  private static List<String> readOnlyPoolProcesses = new ArrayList<>();
  private static String defaultReadOnlyPool = ExternalConnectionPool.READONLY_POOL;

  /**
   * Reload from DB the processes that should use the Read-only pool
   */
  @SuppressWarnings("unchecked")
  public static void refreshReadOnlyProcesses() {
    OBContext.setAdminMode(false);
    List<DataPoolSelection> readOnlyDataPoolSelection = (List<DataPoolSelection>) OBDal
        .getInstance()
        .createCriteria(DataPoolSelection.class)
        .add(
            Restrictions.eq(DataPoolSelection.PROPERTY_DATAPOOL,
                ExternalConnectionPool.READONLY_POOL)).list();

    DataPoolChecker.readOnlyPoolProcesses.clear();
    for (DataPoolSelection selection : readOnlyDataPoolSelection) {
      if (selection.getProcessDefintion() != null) {
        readOnlyPoolProcesses.add(selection.getProcessDefintion().getId());
      } else if (selection.getProcess() != null) {
        readOnlyPoolProcesses.add(selection.getProcess().getId());
      }
    }

    OBContext.restorePreviousMode();
  }

  /**
   * Verifies whether the current process from SessionInfo.getProcessId() should use the read-only
   * pool
   *
   * @return true if the current process should use the RO pool
   */
  public static boolean shouldUseReadOnlyPool() {
    String processId = SessionInfo.getProcessId();
    if (StringUtils.isNotBlank(processId)) {
      return ExternalConnectionPool.DEFAULT_POOL.equals(defaultReadOnlyPool)
          || readOnlyPoolProcesses.contains(SessionInfo.getProcessId());
    }

    return false;
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
