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
package org.openbravo.test.security;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.test.datasource.BaseDataSourceTestDal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test ensures that a deactivated role in the User Roles subtab is not appeared in the User
 * profile widget.
 */
public class RoleListForTheCurrentUser extends BaseDataSourceTestDal {
  private static Logger log = LoggerFactory.getLogger(RoleListForTheCurrentUser.class);

  private static final String USER_INFO_START = "OB.User.userInfo=";
  private static final String USER_INFO_FINISH = "};";

  private static final String ROLE_START = ",role:{value";
  private static final String ROLE_FINISH = "],roles";

  // Role: F&B US, Inc. - Employee
  private static final String TESTED_ROLE_ID = "19AE26382A674FE8946D2B8070D10122";
  // User Role: Openbravo User - F&B US, Inc. - Employee Role
  private static final String USER_ROLE_TEST = "3B960D8A87CA4F77907DF2B7F9A77366";

  @Test
  public void deactivatedRoleNotShowInUserProfile() throws Exception {
    try {
      String response = doSessionDynamicRequest();
      JSONArray rolesInfo = getRoles(response);
      assertThat("The activated role is not present in the dropdown.",
          isRoleInUserProfileWidget(rolesInfo), equalTo(true));

      setActiveUserRole(false);

      String responseDeactivatedUserRole = doSessionDynamicRequest();
      rolesInfo = getRoles(responseDeactivatedUserRole);
      assertThat("The activated role is present in the dropdown.",
          isRoleInUserProfileWidget(rolesInfo), equalTo(false));
    } finally {
      setActiveUserRole(true);
    }
  }

  private String doSessionDynamicRequest() throws Exception {
    Map<String, String> params = new HashMap<String, String>();
    return doRequest("/org.openbravo.client.kernel/OBCLKER_Kernel/SessionDynamic", params, 200,
        "POST");
  }

  /**
   * Remove the data of the response that is not necessary for the test. Then retrieve the roles
   * into a JSONArray.
   * 
   * @param resp
   *          original response
   * @return JSONArray with the roles
   */
  private JSONArray getRoles(String resp) {
    String userInfoResp = getUserInfo(resp);
    userInfoResp = userInfoResp.substring(userInfoResp.indexOf(ROLE_START) + 6);
    userInfoResp = userInfoResp.substring(0, userInfoResp.indexOf(ROLE_FINISH) + 1) + "}";
    JSONArray resultRoles = null;
    try {
      resultRoles = new JSONObject(userInfoResp).getJSONArray("valueMap");
    } catch (JSONException e) {
      log.error("Failed transforming the response in JSONArray: {}", e);
    }
    return resultRoles;
  }

  private String getUserInfo(String resp) {
    String strResponse = resp.substring(resp.indexOf(USER_INFO_START) + USER_INFO_START.length());
    strResponse = strResponse.substring(0, strResponse.indexOf(USER_INFO_FINISH) + 1);
    return strResponse;
  }

  private boolean isRoleInUserProfileWidget(JSONArray resultRoles) {
    try {
      for (int i = 0; i < resultRoles.length(); i++) {
        JSONObject role = resultRoles.getJSONObject(i);
        if (TESTED_ROLE_ID.equals(role.getString("id"))) {
          return true;
        }
      }
    } catch (JSONException e) {
      log.error("Failed retrieving the id from JSONArray: {}", e);
    }
    return false;
  }

  private void setActiveUserRole(boolean isActive) {
    OBContext.setAdminMode();
    try {
      UserRoles userRole = OBDal.getInstance().get(UserRoles.class, USER_ROLE_TEST);
      userRole.setActive(isActive);
      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}