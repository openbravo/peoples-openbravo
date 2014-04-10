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

/**
 * Test cases for ComboTableDatasourceService
 * 
 * @author Shankar Balachandran 
 */

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

public class TestComboDatasource extends BaseDataSourceTestNoDal {

  /**
   * Test to fetch paginated values from ComboTableDatasoureService using set parameters. Based on
   * field information and current context, the field values are returned as jsonObject. The test
   * case asserts whether there is a valid response.
   * 
   * @throws Exception
   */
  public void testFetchComboTableDatasourceValues() throws Exception {
    // Using values of window dropdown in menu
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "206");
    params.put("columnValue", "233");
    params.put("_operationType", "fetch");
    params.put("_startRow", "20");
    params.put("_endRow", "40");

    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    assertTrue(jsonResponse.toString() != null);
  }

  /**
   * Test to fetch a single combo value from ComboTableDatasoureService using set parameters. Based
   * on field information, recordId and current context, the field values are returned as
   * jsonObject. The test case asserts whether there is a valid response.
   * 
   * @throws Exception
   */
  public void testSingleRecordFetch() throws Exception {
    // Using values of visible at user in preference
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "927D156048246E92E040A8C0CF071D3D");
    params.put("columnValue", "927D156047B06E92E040A8C0CF071D3D");
    params.put("_operationType", "fetch");
    // try to fetch F&B Admin user
    params.put("@ONLY_ONE_RECORD@", "A530AAE22C864702B7E1C22D58E7B17B");
    params.put("@ACTUAL_VALUE@", "A530AAE22C864702B7E1C22D58E7B17B");
    params.put("_startRow", "1");
    params.put("_endRow", "2");

    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    assertTrue(jsonResponse.toString() != null);
  }
}