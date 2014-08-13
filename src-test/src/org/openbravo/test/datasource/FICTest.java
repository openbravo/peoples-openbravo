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
 * All portions are Copyright (C) 2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.datasource;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

/**
 * Test cases for FormInitializationComponent
 * 
 * @author alostale
 * 
 */
public class FICTest extends BaseDataSourceTestNoDal {

  /**
   * Auxiliary Input for which SQL can't be evaluated on NEW and the value is correctly set by
   * callouts, should be populated when creating a new record.
   * 
   * See issue #27234
   */
  @Test
  public void testAuxiliaryInputBasedOnCallout() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    params.put("MODE", "NEW");
    params.put("_action", "org.openbravo.client.application.window.FormInitializationComponent");
    params.put("TAB_ID", "186"); // Sales Order
    params.put("PARENT_ID", null);
    String response = doRequest("/org.openbravo.client.kernel", params, 200, "POST");

    JSONObject auxiliaryInputs = new JSONObject(response).getJSONObject("auxiliaryInputValues");
    assertTrue("ORDERTYPE should be set", auxiliaryInputs.has("ORDERTYPE"));

    JSONObject orderType = auxiliaryInputs.getJSONObject("ORDERTYPE");
    assertTrue("ORDERTYPE should have value",
        orderType.has("value") && StringUtils.isNotEmpty(orderType.getString("value")));
  }
}
