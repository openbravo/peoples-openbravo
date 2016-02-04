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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.json.JsonConstants;

/**
 * Test cases to ensure that mechanism of security DataSource access is working properly.
 *
 * @author inigo.sanchez
 *
 */
@RunWith(Parameterized.class)
public class DataSourceSecurity extends BaseDataSourceTestDal {
  private static final String ASTERISK_ORG_ID = "0";
  private static final String CONTEXT_USER = "100";
  private static final String LANGUAGE_ID = "192";
  private static final String WAREHOUSE_ID = "B2D40D8A5D644DD89E329DC297309055";
  private static final String ROLE_INTERNATIONAL_ADMIN = "42D0EEB1C66F497A90DD526DC597E6F0";
  private static final String ROLE_NO_ACCESS = "1";
  private static final String ROLE_SYSTEM_ADMIN = "0";
  private static final String ESP_ORG = "E443A31992CB4635AFCAEABE7183CE85";

  private static final String DS_ORDER = "Order";
  private static final String DS_PROD_BY_PRICE_WAREHOUSE = "ProductByPriceAndWarehouse";

  private RoleType role;
  private String dataSource;
  private int expectedResponseStatus;

  private enum RoleType {
    ADMIN_ROLE(ROLE_INTERNATIONAL_ADMIN, ESP_ORG), //
    NO_ACCESS_ROLE(ROLE_NO_ACCESS, ESP_ORG), //
    SYSTEM_ROLE(ROLE_SYSTEM_ADMIN, ASTERISK_ORG_ID);

    private String roleId;
    private String orgId;

    private RoleType(String roleId, String orgId) {
      this.roleId = roleId;
      this.orgId = orgId;
    }
  }

  public DataSourceSecurity(RoleType role, String dataSource, int expectedResponseStatus) {
    this.role = role;
    this.dataSource = dataSource;
    this.expectedResponseStatus = expectedResponseStatus;
  }

  @Parameters(name = "{0} - dataSource: {1}")
  public static Collection<Object[]> parameters() {
    List<Object[]> testCases = new ArrayList<Object[]>();
    for (RoleType type : RoleType.values()) {
      testCases.add(new Object[] {
          type,
          DS_ORDER,
          type == RoleType.ADMIN_ROLE ? JsonConstants.RPCREQUEST_STATUS_SUCCESS
              : JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR });
    }
    // testing a problem detected in how properties are initialized.
    testCases.add(new Object[] { RoleType.ADMIN_ROLE, DS_PROD_BY_PRICE_WAREHOUSE,
        JsonConstants.RPCREQUEST_STATUS_SUCCESS });
    return testCases;
  }

  /** Creates dummy role without any access for testing purposes */
  @BeforeClass
  public static void createNoAccessRole() {
    OBContext.setOBContext(CONTEXT_USER);

    Role noAccessRole = OBProvider.getInstance().get(Role.class);
    noAccessRole.setId("1");
    noAccessRole.setNewOBObject(true);
    noAccessRole.setOrganization(OBDal.getInstance().get(Organization.class, ASTERISK_ORG_ID));
    noAccessRole.setName("Test No Access");
    noAccessRole.setManual(true);
    noAccessRole.setUserLevel(" CO");
    noAccessRole.setClientList(OBContext.getOBContext().getCurrentClient().getId());
    noAccessRole.setOrganizationList(ASTERISK_ORG_ID);
    OBDal.getInstance().save(noAccessRole);

    RoleOrganization noAcessRoleOrg = OBProvider.getInstance().get(RoleOrganization.class);
    noAcessRoleOrg.setOrganization((Organization) OBDal.getInstance().getProxy(
        Organization.ENTITY_NAME, ESP_ORG));
    noAcessRoleOrg.setRole(noAccessRole);
    OBDal.getInstance().save(noAcessRoleOrg);

    UserRoles noAccessRoleUser = OBProvider.getInstance().get(UserRoles.class);
    noAccessRoleUser.setOrganization(noAccessRole.getOrganization());
    noAccessRoleUser.setUserContact(OBContext.getOBContext().getUser());
    noAccessRoleUser.setRole(noAccessRole);
    OBDal.getInstance().save(noAccessRoleUser);

    OBDal.getInstance().commitAndClose();
  }

  /** Tests datasource allows or denies fetch action based on role access */
  @Test
  public void fetchShouldBeAllowedOnlyIfRoleIsGranted() throws Exception {
    OBContext.setOBContext(CONTEXT_USER);
    changeProfile(role.roleId, LANGUAGE_ID, role.orgId, WAREHOUSE_ID);
    JSONObject jsonResponse = null;
    if (dataSource.equals(DS_PROD_BY_PRICE_WAREHOUSE)) {
      jsonResponse = selectorFilterRequest();
    } else {
      jsonResponse = fetchDataSource();
    }
    assertThat("Request status", jsonResponse.getInt("status"), is(expectedResponseStatus));
  }

  private JSONObject fetchDataSource() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "1");

    String response = doRequest("/org.openbravo.service.datasource/" + dataSource, params, 200,
        "POST");

    return new JSONObject(response).getJSONObject("response");
  }

  /**
   * This manual request must be tested to ensure it is functioning properly.
   */
  private JSONObject selectorFilterRequest() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    params.put("_selectorDefinitionId", "2E64F551C7C4470C80C29DBA24B34A5F");
    params.put("filterClass", "org.openbravo.userinterface.selector.SelectorDataSourceFilter");
    params.put("_where", "e.active='Y'");
    params.put("_sortBy", "_identifier");
    params.put("_requestType", "Window");
    params.put("_distinct", "productPrice");

    // To reproduce this problem is important not to add the targetProperty parameter. For this
    // reason targetProperty=null.
    params.put("_operationType", "fetch");
    params.put("_inpTableId", "293");
    params.put("_startRow", "0");
    params.put("_endRow", "75");
    params.put("_textMatchStyle", "substring");

    // Filter selector
    JSONObject criteria = new JSONObject();
    criteria.put("fieldName", "productPrice$priceListVersion$_identifier");
    criteria.put("operator", "iContains");
    criteria.put("value", "Tarifa");
    params.put("criteria", criteria.toString());

    String response = doRequest("/org.openbravo.service.datasource/" + dataSource, params, 200,
        "POST");

    return new JSONObject(response).getJSONObject("response");
  }

  /** Deletes dummy testing role */
  @AfterClass
  public static void cleanUp() {
    OBContext.setOBContext(CONTEXT_USER);
    OBDal.getInstance().remove(OBDal.getInstance().get(Role.class, ROLE_NO_ACCESS));
    OBDal.getInstance().commitAndClose();
  }
}
