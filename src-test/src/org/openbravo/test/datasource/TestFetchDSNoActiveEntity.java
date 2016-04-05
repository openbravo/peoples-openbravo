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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.json.JsonConstants;

/**
 * Test cases for ensures that fetch works properly with active and non active entity.
 * 
 * See issue https://issues.openbravo.com/view.php?id=32584
 * 
 * @author inigo.sanchez
 *
 */
public class TestFetchDSNoActiveEntity extends BaseDataSourceTestDal {

  @Test
  public void fetchNoActiveOrganization() throws Exception {
    OBContext.setAdminMode();
    try {
      // Fetching an active organization
      JSONObject jsonResponse = doFetchOrg();
      assertThat("Request status", jsonResponse.getInt("status"),
          is(JsonConstants.RPCREQUEST_STATUS_SUCCESS));
      assertThat("Request data", jsonResponse.getJSONArray("data").length(), is(1));

      // Fetching a non active organization
      setNoActiveOrganization();

      JSONObject jsonResponseNoActive = doFetchOrg();
      assertThat("Request status", jsonResponseNoActive.getInt("status"),
          is(JsonConstants.RPCREQUEST_STATUS_SUCCESS));
      assertThat("Request data", jsonResponseNoActive.getJSONArray("data").length(), is(1));

    } finally {
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

  /** Fetching organization F&B International Group */
  private JSONObject doFetchOrg() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    params.put("windowId", "110");
    params.put("tabId", "143");
    params.put("moduleId", "0");
    params.put("_operationType", "fetch");
    params.put("id", "19404EAD144C49A0AF37D54377CF452D");
    params.put("_startRow", "0");
    params.put("_endRow", "1");

    String response = doRequest("/org.openbravo.service.datasource/Organization", params, 200,
        "POST");
    return new JSONObject(response).getJSONObject("response");
  }

  /** Setting no active F&B International Group organization */
  private void setNoActiveOrganization() {
    // Select organization for testing
    Organization orgTesting = OBDal.getInstance().get(Organization.class,
        "19404EAD144C49A0AF37D54377CF452D");
    orgTesting.setActive(false);
    OBDal.getInstance().save(orgTesting);

    OBDal.getInstance().commitAndClose();
  }

  /** Revert change in testing organization */
  @AfterClass
  public static void cleanUp() {
    OBContext.setOBContext("100");
    Organization orgTesting = OBDal.getInstance().get(Organization.class,
        "19404EAD144C49A0AF37D54377CF452D");
    orgTesting.setActive(true);
    OBDal.getInstance().save(orgTesting);

    OBDal.getInstance().commitAndClose();
  }
}