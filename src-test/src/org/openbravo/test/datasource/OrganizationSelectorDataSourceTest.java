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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.datasource;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.dal.core.OBContext;

/**
 * Test to check that an Organization selector (both normal and custom query based) applies the
 * filter based on the role's organization access, even when an organization has been selected in
 * the field before calling to the datasource.
 * 
 * See issues: https://issues.openbravo.com/view.php?id=36151,
 * https://issues.openbravo.com/view.php?id=36863
 */
@RunWith(Parameterized.class)
public class OrganizationSelectorDataSourceTest extends BaseDataSourceTestDal {
  private static boolean defaultRoleSet = false;
  private boolean customQuerySelector;
  private boolean organizationSelected;

  public OrganizationSelectorDataSourceTest(boolean customQuerySelector,
      boolean organizationSelected) {
    this.customQuerySelector = customQuerySelector;
    this.organizationSelected = organizationSelected;
  }

  @Parameters(name = "{index}: organizationSelected = {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { { false, false }, { false, true }, { true, false },
        { true, true } });
  }

  @Before
  public void initOBContext() {
    // Set F&B España, S.A - Procurement role context
    OBContext.setOBContext("100", "9D320A774FCD4E47801DF5E03AA11F2D",
        "23C59575B9CF467C9620760EB255B389", "E443A31992CB4635AFCAEABE7183CE85");
  }

  @Test
  public void retrieveExpectedOrganizationList() throws Exception {

    JSONObject resp;
    if (customQuerySelector) {
      resp = performCustomQuerySelectorRequest();
    } else {
      resp = performSelectorRequest();
    }
    assertTrue("Retrieved the expected organizations",
        returnedExpectedOrgs(resp.getJSONArray("data")));
  }

  private boolean returnedExpectedOrgs(JSONArray returnedOrgs) {
    Set<String> readableOrgs = new HashSet<>(Arrays.asList(OBContext.getOBContext()
        .getReadableOrganizations()));
    try {
      if (readableOrgs.size() != returnedOrgs.length()) {
        return false;
      }
      for (int i = 0; i < returnedOrgs.length(); i++) {
        JSONObject org = returnedOrgs.getJSONObject(i);
        String orgId;
        if (org.has("orgid")) {
          orgId = org.getString("orgid");
        } else {
          orgId = org.getString("id");
        }
        if (!readableOrgs.contains(orgId)) {
          return false;
        }
      }
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  private JSONObject performSelectorRequest() throws Exception {
    if (!defaultRoleSet) {
      // Use F&B España, S.A - Procurement role
      changeProfile("9D320A774FCD4E47801DF5E03AA11F2D", "192", "E443A31992CB4635AFCAEABE7183CE85",
          "B2D40D8A5D644DD89E329DC297309055");
      defaultRoleSet = true;
    }

    Map<String, String> params = new HashMap<String, String>();
    params.put("_selectorDefinitionId", "4E0AC6FEC5EA4A2BB474747DB03A3A21");
    params.put("filterClass", "org.openbravo.userinterface.selector.SelectorDataSourceFilter");
    params.put("_selectedProperties", "id");
    params.put("_extraProperties", "id,");
    params.put("_noCount", "true");
    params.put("_sortBy", "_identifier");
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "75");
    params.put("_textMatchStyle", "startsWith");
    params.put("columnName", "AD_Org_ID");
    params.put("isSelectorItem", "true");
    params.put("operator", "or");
    params.put("_constructor", "AdvancedCriteria");
    JSONObject criteria = new JSONObject();
    criteria.put("fieldName", "_dummy");
    criteria.put("operator", "equals");
    criteria.put("value", "1506076927291");
    params.put("criteria", criteria.toString());

    if (organizationSelected) {
      params.put("_org", "0");
    } else {
      params.put("_calculateOrgs", "true");
    }

    String response = doRequest("/org.openbravo.service.datasource/Organization", params, 200,
        "POST");
    JSONObject resp = new JSONObject(response).getJSONObject("response");

    assertTrue("Response should have data", resp.has("data"));
    return resp;
  }

  private JSONObject performCustomQuerySelectorRequest() throws Exception {
    if (!defaultRoleSet) {
      // Use F&B España, S.A - Procurement role
      changeProfile("9D320A774FCD4E47801DF5E03AA11F2D", "192", "E443A31992CB4635AFCAEABE7183CE85",
          "B2D40D8A5D644DD89E329DC297309055");
      defaultRoleSet = true;
    }

    Map<String, String> params = new HashMap<String, String>();
    params.put("_selectorDefinitionId", "B748F356A65641D4974E5C349A16FB27");
    params.put("filterClass", "org.openbravo.userinterface.selector.SelectorDataSourceFilter");
    params.put("_selectedProperties", "id,name");
    params.put("_extraProperties", "orgid,name,");
    params.put("_noCount", "true");
    params.put("_sortBy", "_identifier");
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "75");
    params.put("_textMatchStyle", "substring");
    params.put("columnName", "AD_Org_ID");
    params.put("targetProperty", "organization");
    params.put("isSelectorItem", "true");
    params.put("operator", "or");
    params.put("_constructor", "AdvancedCriteria");
    JSONObject criteria = new JSONObject();
    criteria.put("fieldName", "_dummy");
    criteria.put("operator", "equals");
    criteria.put("value", "1506076927291");
    params.put("criteria", criteria.toString());
    params.put("adTabId", "351");

    if (organizationSelected) {
      params.put("_org", "0");
    } else {
      params.put("_calculateOrgs", "true");
    }

    String response = doRequest(
        "/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF", params, 200, "POST");
    JSONObject resp = new JSONObject(response).getJSONObject("response");

    assertTrue("Response should have data", resp.has("data"));
    return resp;
  }
}
