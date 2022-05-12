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
 * All portions are Copyright (C) 2016-2022 Openbravo SLU 
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
import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.test.base.Issue;
import org.openbravo.test.base.TestConstants.Clients;
import org.openbravo.test.base.TestConstants.Languages;
import org.openbravo.test.base.TestConstants.Orgs;
import org.openbravo.test.base.TestConstants.Roles;
import org.openbravo.test.base.TestConstants.WareHouses;

/**
 * Tests that ensure datasources are able to fetch data with active and non active entity objects.
 * 
 * @author inigo.sanchez
 *
 */
@Issue("32584")
public class FetchDSNoActiveEntityObjects extends BaseDataSourceTestDal {

  private static final String CONTEXT_ROLE = Roles.FB_GRP_ADMIN;
  private static final String CLIENT = Clients.FB_GRP;
  private static final String AMERICAN_ORGANIZATION = Orgs.US_WEST;
  private static final String LANGUAGE_ID = Languages.EN_US_LANG_ID;
  private static final String WAREHOUSE_ID = WareHouses.US_WEST;

  private static final String ORG_ID = Orgs.FB_GROUP;

  @Test
  public void fetchNoActiveOrganizationObject() throws Exception {
    OBContext.setOBContext("100", CONTEXT_ROLE, CLIENT, AMERICAN_ORGANIZATION);
    try {
      changeProfile(CONTEXT_ROLE, LANGUAGE_ID, AMERICAN_ORGANIZATION, WAREHOUSE_ID);

      // Fetching a non active organization
      setActiveOrNoActiveOrganizationObject(false);

      JSONObject jsonResponseNoActive = doFetchOrg();
      assertThat("Response status", jsonResponseNoActive.getInt("status"),
          is(JsonConstants.RPCREQUEST_STATUS_SUCCESS));
      assertThat("Response data length", jsonResponseNoActive.getJSONArray("data").length(), is(1));

    } finally {
      setActiveOrNoActiveOrganizationObject(true);
    }
  }

  @Test
  public void fetchActiveOrganizationObject() throws Exception {
    OBContext.setOBContext("100", CONTEXT_ROLE, CLIENT, AMERICAN_ORGANIZATION);
    try {
      changeProfile(CONTEXT_ROLE, LANGUAGE_ID, AMERICAN_ORGANIZATION, WAREHOUSE_ID);

      // Fetching an active organization
      JSONObject jsonResponse = doFetchOrg();
      assertThat("Response status", jsonResponse.getInt("status"),
          is(JsonConstants.RPCREQUEST_STATUS_SUCCESS));
      assertThat("Response data length", jsonResponse.getJSONArray("data").length(), is(1));

    } finally {
    }
  }

  /** Fetching organization F&B International Group */
  private JSONObject doFetchOrg() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    params.put("windowId", "110");
    params.put("tabId", "143");
    params.put("moduleId", "0");
    params.put("_operationType", "fetch");
    params.put("id", ORG_ID);
    params.put("_startRow", "0");
    params.put("_endRow", "1");

    String response = doRequest("/org.openbravo.service.datasource/Organization", params, 200,
        "POST");
    return new JSONObject(response).getJSONObject("response");
  }

  private void setActiveOrNoActiveOrganizationObject(boolean isActive) {
    OBContext.setAdminMode();
    try {
      Organization orgTesting = OBDal.getInstance().get(Organization.class, ORG_ID);
      orgTesting.setActive(isActive);
      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
