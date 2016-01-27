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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.datasource;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.json.JsonConstants;

/**
 * Test cases to ensure that new mechanism of security entity access is working properly.
 *
 * @author inigo.sanchez
 *
 */
public class TestSecurityEntityAccess extends BaseDataSourceTestDal {

  private static final String ASTERISK_ORG_ID = "0";
  private static final String CONTEXT_USER = "100";
  private static final String LANGUAGE_ID = "192";
  private static final String ROLE_SYSTEM = "0";
  private static final String WAREHOUSE_ID = "B2D40D8A5D644DD89E329DC297309055";
  private static final String ROLE_INTERNATIONAL_ADMIN = "42D0EEB1C66F497A90DD526DC597E6F0";

  private static final String ENTITY_SALES_ORDER = "Order";
  private static final String ENTITY_SALES_INVOICE = "Invoice";

  /**
   * This test ensures that a user with no access to an entity can't call to the specific
   * DataSource. It is tested fetch operation.
   */
  @Test
  public void testNotFetchSecurityEntityAccess() throws Exception {
    OBContext.setOBContext(CONTEXT_USER);

    // ensures a role with not access to a Order entity
    changeProfile(ROLE_SYSTEM, LANGUAGE_ID, ASTERISK_ORG_ID, WAREHOUSE_ID);

    Map<String, String> params = new HashMap<String, String>();
    params.put("_operationType", "fetch");

    String response = doRequest("/org.openbravo.service.datasource/" + ENTITY_SALES_ORDER, params,
        200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    assertTrue(getStatus(jsonResponse).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR)));
  }

  @Test
  public void testFetchSecurityEntityAccess() throws Exception {
    OBContext.setOBContext(CONTEXT_USER);

    // ensures a role with an access to Invoice entity
    changeProfile(ROLE_INTERNATIONAL_ADMIN, LANGUAGE_ID, ASTERISK_ORG_ID, WAREHOUSE_ID);

    Map<String, String> params = new HashMap<String, String>();
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "1");

    String responseForRole = doRequest("/org.openbravo.service.datasource/" + ENTITY_SALES_INVOICE,
        params, 200, "POST");
    JSONObject jsonResponseForRole = new JSONObject(responseForRole);
    assertTrue(getStatus(jsonResponseForRole).equals(
        String.valueOf(JsonConstants.RPCREQUEST_STATUS_SUCCESS)));
  }

  private String getStatus(JSONObject jsonResponse) throws JSONException {
    return jsonResponse.getJSONObject("response").get("status").toString();
  }
}
