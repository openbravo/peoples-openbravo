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

package org.openbravo.test.webservice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Test cases for ensures that Dal Web services works properly with active and non active entity.
 * 
 * See issue https://issues.openbravo.com/view.php?id=32584
 * 
 * @author inigo.sanchez
 *
 */
public class WSWithNoActiveEntityTest extends BaseWSTest {

  private static final String ORG_ID_DAL = "<id>19404EAD144C49A0AF37D54377CF452D</id>";
  private static final String ORG_ID = "19404EAD144C49A0AF37D54377CF452D";

  @Test
  public void dalWebServiceNoActiveOrganization() {
    OBContext.setAdminMode();
    try {
      String dalResp = dalRequest("/ws/dal/Organization/" + ORG_ID);
      assertThat("Request data", dalResp.contains(ORG_ID_DAL), is(true));

      setNoActiveOrg();
      String dalRespNoActive = dalRequest("/ws/dal/Organization/" + ORG_ID);
      assertThat("Request data", dalRespNoActive.contains(ORG_ID_DAL), is(true));
    } finally {
      Organization orgTesting = OBDal.getInstance().get(Organization.class, ORG_ID);
      orgTesting.setActive(true);
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
    }
  }

  private void setNoActiveOrg() {
    Organization organization = OBDal.getInstance().get(Organization.class, ORG_ID);
    organization.setActive(false);
    OBDal.getInstance().commitAndClose();
  }

  private String dalRequest(String urlDalRequest) {
    String organizationDataString;
    OBContext.setAdminMode();
    try {
      organizationDataString = doTestGetRequest(urlDalRequest, null, 200);
    } catch (Exception e) {
      organizationDataString = e.getMessage();
    } finally {
      OBContext.restorePreviousMode();
    }
    return organizationDataString;
  }
}
