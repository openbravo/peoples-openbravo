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
package org.openbravo.test.dal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openbravo.dal.datapool.DataPoolChecker;
import org.openbravo.database.ExternalConnectionPool;
import org.openbravo.test.base.OBBaseTest;

/**
 * Test data pool checker behavior on various situations
 *
 * @author jarmendariz
 */
public class DataPoolCheckerTest extends OBBaseTest {

  private final String PROCESS_ID = "process_id";

  @Before
  public void resetDataPoolChecker() {
    DataPoolChecker.setDataPoolProcesses(new HashMap<String, String>());
    DataPoolChecker.setDefaultReadOnlyPool("");
  }

  @Test
  public void testWhenProcessIsNotAvailablePreferencePoolIsUsed() {
    DataPoolChecker.setDataPoolProcesses(new HashMap<String, String>());
    DataPoolChecker.setDefaultReadOnlyPool(ExternalConnectionPool.DEFAULT_POOL);

    assertTrue("Should use the pool defined in preference, that is, Default",
        DataPoolChecker.shouldUseDefaultPool(null));
  }

  @Test
  public void testWhenProcessHasNoRulePreferencePoolIsUsed() {
    DataPoolChecker.setDataPoolProcesses(new HashMap<String, String>());
    DataPoolChecker.setDefaultReadOnlyPool(ExternalConnectionPool.READONLY_POOL);

    assertFalse("No rule defined for process, should use pool defined in preference: read-only",
        DataPoolChecker.shouldUseDefaultPool(PROCESS_ID));
  }

  @Test
  public void testWhenProcessHasRuleItsPoolIsUsed() {
    Map<String, String> processes = new HashMap<>();
    processes.put(PROCESS_ID, ExternalConnectionPool.DEFAULT_POOL);
    DataPoolChecker.setDataPoolProcesses(processes);
    DataPoolChecker.setDefaultReadOnlyPool(ExternalConnectionPool.READONLY_POOL);

    assertTrue("Should use pool defined in rule for process: Default",
        DataPoolChecker.shouldUseDefaultPool(PROCESS_ID));
  }
}
