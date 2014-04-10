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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * Tests Property selector datasource. Checking issue #26238 is not reproduced anymore.
 * 
 * @author alostale
 * 
 */
public class SelectorFieldPropertySelectorDSTest extends BaseDataSourceTestNoDal {
  private boolean sysAdminProfileSet = false;

  /**
   * Performs a request for properties without filtering
   */
  public void testFullList() throws Exception {
    JSONObject resp = executeDSRequest(false);

    System.out.println(resp.toString(2));

    JSONArray data = resp.getJSONArray("data");

    assertTrue("data should contain several values, it has " + data.length(), data.length() > 1);
    assertTrue("totalRows should be bigger than 1, it is " + resp.getInt("totalRows"),
        resp.getInt("totalRows") > 1);
  }

  /**
   * Performs a request for properties filtering by property "id"
   */
  public void testFilter() throws Exception {
    JSONObject resp = executeDSRequest(true);

    JSONArray data = resp.getJSONArray("data");

    assertEquals("data length", data.length(), 1);
    assertEquals("totalRows", resp.getInt("totalRows"), 1);

    System.out.println(resp.toString(2));
  }

  private JSONObject executeDSRequest(boolean filter) throws Exception {
    if (!sysAdminProfileSet) {
      changeProfile("0", "192", "0", null);
      sysAdminProfileSet = true;
    }

    Map<String, String> params = new HashMap<String, String>();

    // this is how value is sent when in new, regardless typed filter
    params.put("inpproperty", "null");

    params.put("_operationType", "fetch");
    params.put("filterClass", "org.openbravo.userinterface.selector.SelectorDataSourceFilter");
    params.put("_sortBy", "property");

    params.put("inpobuiselSelectorId", "1F051395F1CC4A40ADFE5C440EBCAA7F");

    if (filter) {
      params.put("operator", "or");
      params.put("_constructor", "AdvancedCriteria");
      JSONObject criteria = new JSONObject();
      criteria.put("fieldName", "property");
      criteria.put("operator", "iContains");
      criteria.put("value", "id");
      params.put("criteria", criteria.toString());
    }

    JSONObject resp = new JSONObject(doRequest(
        "/org.openbravo.service.datasource/83B60C4C19AE4A9EBA947B948C5BA04D", params, 200, "POST"));

    assertTrue("expecting response, got: " + resp, resp.has("response"));

    JSONObject r = resp.getJSONObject("response");
    assertTrue("expecting data in response, got: " + r, r.has("data"));

    return r;
  }
}