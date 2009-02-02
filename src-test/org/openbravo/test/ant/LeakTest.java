/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.ant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.AntExecutor;

/**
 * Tests an ant task.
 * 
 * @author mtaal
 */

public class LeakTest extends org.openbravo.test.base.BaseTest {

  public void testLeak() {
    setBigBazaarAdminContext();
    for (int i = 0; i < 20; i++) {
      doTestLeak();
    }
  }

  private void doTestLeak() {
    try {
      final AntExecutor ant = new AntExecutor("/home/mtaal/mydata/dev/workspaces/obtrunk/openbravo");
      final String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
          + "-apply.log";
      // final OBPrintStream obps=new OBPrintStream(new
      // PrintStream(response.getOutputStream()));
      // System.setOut(obps);

      // ant.setOBPrintStreamLog(response.getWriter());
      ant.setOBPrintStreamLog(System.err);
      // obps.setLogFile(new File(fileName+".db"));
      ant.setLogFileInOBPrintStream(new File(fileName));

      final Vector<String> tasks = new Vector<String>();
      // tasks.add("apply.modules");
      tasks.add("generate.entities");

      ant.runTask(tasks);

      ant.setFinished(true);
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }
}