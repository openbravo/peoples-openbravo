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
 * All portions are Copyright (C) 2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.system;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.test.base.OBBaseTest;

/**
 * Test cases used to ensure the correct JSON serialization of different objects.
 */
public class JsonSerializationTest extends OBBaseTest {

  private static final String PROCESS_CONTEXT = "{\"org.openbravo.scheduling.ProcessContext\":"
      + "{\"user\":\"4028E6C72959682B01295A0735CB0120\",\"role\":\"\",\"language\":\"en_US\","
      + "\"theme\":\"ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp\","
      + "\"client\":\"4028E6C72959682B01295A070852010D\",\"organization\":\"357947E87C284935AD1D783CF6F099A1\","
      + "\"warehouse\":\"\",\"command\":\"DEFAULT\",\"userClient\":\"\","
      + "\"userOrganization\":\"\",\"dbSessionID\":\"\",\"javaDateFormat\":\"\",\"jsDateFormat\":\"\","
      + "\"sqlDateFormat\":\"\",\"accessLevel\":\"\",\"roleSecurity\":true}}";

  /** ProcessContext is correctly serialized */
  @Test
  public void serializeProcessContext() {
    setUserContext(QA_TEST_ADMIN_USER_ID);
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId());
    ProcessContext processContext = new ProcessContext(vars);
    assertThat(processContext.toString(), equalTo(PROCESS_CONTEXT));
  }

  /** Test correct deserialization of a JSONObject containing a ProcessContext definition */
  @Test
  public void deserializeProcessContext() {
    String obContext = OBDal.getInstance()
        .getSession()
        .createQuery(
            "SELECT openbravoContext FROM ProcessRequest WHERE id = '078147FA19124BA69786EA7374807D0D'",
            String.class)
        .uniqueResult();
    ProcessContext processContext = ProcessContext.newInstance(obContext);

    List<Object> collection = Arrays.asList(processContext.getUser(), processContext.getRole(),
        processContext.getLanguage(), processContext.getTheme(), processContext.getClient(),
        processContext.getOrganization(), processContext.getWarehouse(),
        processContext.getCommand(), processContext.getUserClient(),
        processContext.getUserOrganization(), processContext.getDbSessionID(),
        processContext.getJavaDateFormat(), processContext.getJavaDateTimeFormat(),
        processContext.getJsDateFormat(), processContext.getSqlDateFormat(),
        processContext.getAccessLevel(), processContext.isRoleSecurity());

    assertThat(collection, contains("100", "4028E6C72959682B01295A071429011E", "en_US",
        "ltr/org.openbravo.userinterface.skin.250to300Comp/250to300Comp",
        "4028E6C72959682B01295A070852010D", "0", "4028E6C72959682B01295ECFEF4502A0",
        "SAVE_BUTTONProcessing100", "'4028E6C72959682B01295A070852010D'",
        "'5EFF95EB540740A3B10510D9814EFAD5','43D590B4814049C6B85C6545E8264E37','0','357947E87C284935AD1D783CF6F099A1'",
        "A9220D77CB54469A99320051BB0D74C5", "dd-MM-yyyy", "dd-MM-yyyy HH:mm:ss", "%d-%m-%Y",
        "DD-MM-YYYY", "3", true));
  }

  /** Test consistency of ProcessContext serialization */
  @Test
  public void isConsistentSerialization() {
    ProcessContext processContext = ProcessContext.newInstance(PROCESS_CONTEXT);
    assertThat(processContext.toString(), equalTo(PROCESS_CONTEXT));
  }
}
