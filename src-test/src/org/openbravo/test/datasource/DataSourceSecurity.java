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
 * Test cases to ensure that new mechanism of security entity access is working properly.
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

  private RoleType role;
  private String dataSource;
  private int expectedResponseStatus;

  private enum RoleType {
    ADMIN_ROLE(ROLE_INTERNATIONAL_ADMIN), //
    NO_ACCESS_ROLE(ROLE_NO_ACCESS);

    private String roleId;

    private RoleType(String roleId) {
      this.roleId = roleId;
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
          "Order",
          type == RoleType.ADMIN_ROLE ? JsonConstants.RPCREQUEST_STATUS_SUCCESS
              : JsonConstants.RPCREQUEST_STATUS_FAILURE });
    }
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
    noAcessRoleOrg.setOrganization(OBContext.getOBContext().getCurrentOrganization());
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
    changeProfile(role.roleId, LANGUAGE_ID, OBContext.getOBContext().getCurrentOrganization()
        .getId(), WAREHOUSE_ID);
    JSONObject jsonResponse = fetchDataSource();

    assertThat("Request status", jsonResponse.getJSONObject("response").getInt("status"),
        is(expectedResponseStatus));
  }

  private JSONObject fetchDataSource() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "1");

    return new JSONObject(doRequest("/org.openbravo.service.datasource/" + dataSource, params, 200,
        "POST"));
  }

  /** Deletes dummy testing role */
  @AfterClass
  public static void cleanUp() {
    OBContext.setOBContext(CONTEXT_USER);
    OBDal.getInstance().remove(OBDal.getInstance().get(Role.class, ROLE_NO_ACCESS));
    OBDal.getInstance().commitAndClose();
  }
}
