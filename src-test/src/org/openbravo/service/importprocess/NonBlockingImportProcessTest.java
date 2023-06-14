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
package org.openbravo.service.importprocess;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;

public class NonBlockingImportProcessTest extends WeldBaseTest {

  @Before
  public void initialize() {
    ImportEntryManager importEntryManager = WeldUtils
        .getInstanceFromStaticBeanManager(ImportEntryManager.class);
    importEntryManager.initialWaitTime = 1;
    OBPropertiesProvider.getInstance()
        .getOpenbravoProperties()
        .setProperty("import.wait.time", "1");
    importEntryManager.start();

    System.out.println(importEntryManager);

  }

  @After
  public void finish() {
    ImportEntryManager importEntryManager = WeldUtils
        .getInstanceFromStaticBeanManager(ImportEntryManager.class);
    importEntryManager.shutdown();

    OBDal.getInstance().rollbackAndClose();
  }

  @Test
  public void shouldHandleInvCountAsynchronously() throws Exception {
    ImportEntryManager importEntryManager = WeldUtils
        .getInstanceFromStaticBeanManager(ImportEntryManager.class);
    String uuid = SequenceIdData.getUUID();
    importEntryManager.createImportEntry(uuid, "OB_NonBlockingTest", "{}");
    OBDal.getInstance().flush();
    Thread.sleep(100);
    importEntryManager.notifyNewImportEntryCreated();
    Thread.sleep(10000);

    System.out.println(importEntryManager);
    importEntryManager.getNumberOfActiveTasks();
    ImportEntry importEntry = OBDal.getInstance().get(ImportEntry.class, uuid);
    MatcherAssert.assertThat(importEntry.getImportStatus(), Matchers.equalTo("Processed"));
  }

}
