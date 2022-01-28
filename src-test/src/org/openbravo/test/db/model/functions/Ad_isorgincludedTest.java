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
 * All portions are Copyright (C) 2015-2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.db.model.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.test.base.OBBaseTest;
import org.openbravo.test.base.TestConstants.Clients;
import org.openbravo.test.base.TestConstants.Orgs;

public class Ad_isorgincludedTest extends OBBaseTest {

  /**
   * All Organization *
   */
  protected static final String ORG_0 = "0";

  /**
   * QA Testing Client
   */
  protected static final String CLIENT_QA = Clients.QA_CLIENT;

  /**
   * Main Organization
   */
  protected static final String ORG_QA_MAIN = Orgs.QA_ORG;

  /**
   * Spain Organization
   */
  protected static final String ORG_QA_SPAIN = Orgs.SPAIN;

  /**
   * F&amp;B International Group Client
   */
  protected static final String CLIENT_FB = Clients.FB_GRP;

  /**
   * F&amp;B International Group Organization
   */
  protected static final String ORG_FB_FBGROUP = Orgs.FB_GROUP;

  /**
   * F&amp;B US, Inc.
   */
  protected static final String ORG_FB_US = Orgs.US;

  /**
   * F&amp;B US East Coast
   */
  protected static final String ORG_FB_EAST = Orgs.US_EST;

  /**
   * F&amp;B US West Coast
   */
  protected static final String ORG_FB_WEST = Orgs.US_WEST;

  /**
   * F&amp;B España, S.A.
   */
  protected static final String ORG_FB_SPAIN = Orgs.ESP;

  /**
   * F&amp;B España - Región Norte
   */
  protected static final String ORG_FB_NORTE = Orgs.ESP_NORTE;

  /**
   * F&amp;B España - Región Sur
   */
  protected static final String ORG_FB_SUR = Orgs.ESP_SUR;

  /**
   * Case I: Distinct Organization in the same branch with different levels.
   */

  /**
   * Case II: Distinct Organization in the different branch with different levels.
   */

  /**
   * Case III: Swap parent/child order
   */

  /**
   * Case IV: Organization with different clients
   */

  /**
   * Case V: Same Organization
   */

  /**
   * Case VI: Organization that does not exists.
   */

  @Test
  public void testIsOrgIncluded() {

    // Case I
    assertEquals("Level 1 Organization", 1, isOrgIncluded(ORG_0, ORG_0, CLIENT_FB));
    assertEquals("Level 2 Organization", 2, isOrgIncluded(ORG_FB_FBGROUP, ORG_0, CLIENT_FB));
    assertEquals("Level 3 Organization", 3, isOrgIncluded(ORG_FB_US, ORG_0, CLIENT_FB));
    assertEquals("Level 4 Organization", 4, isOrgIncluded(ORG_FB_WEST, ORG_0, CLIENT_FB));

    // Case II
    assertTrue(isOrgIncluded(ORG_FB_WEST, ORG_FB_SPAIN, CLIENT_QA) == -1);
    assertTrue(isOrgIncluded(ORG_FB_EAST, ORG_FB_SPAIN, CLIENT_QA) == -1);
    assertTrue(isOrgIncluded(ORG_FB_NORTE, ORG_FB_US, CLIENT_QA) == -1);
    assertTrue(isOrgIncluded(ORG_FB_SUR, ORG_FB_US, CLIENT_QA) == isOrgIncluded(ORG_FB_EAST,
        ORG_FB_SPAIN, CLIENT_QA));

    // Case III
    assertTrue(isOrgIncluded(ORG_QA_MAIN, ORG_QA_SPAIN, CLIENT_QA) == -1);
    assertTrue(isOrgIncluded(ORG_FB_US, ORG_FB_WEST, CLIENT_QA) == -1);

    // Case IV
    assertTrue(isOrgIncluded(ORG_QA_SPAIN, ORG_QA_MAIN, CLIENT_FB) == -1);
    assertTrue(isOrgIncluded(ORG_FB_US, ORG_FB_FBGROUP, CLIENT_QA) == -1);

    // Case V
    assertTrue(isOrgIncluded(ORG_QA_MAIN, ORG_QA_MAIN, CLIENT_QA) == 1);

    // Case VI
    assertTrue(isOrgIncluded("ABC", ORG_FB_FBGROUP, CLIENT_QA) == -1);
  }

  protected int isOrgIncluded(String orgId, String parentOrgId, String clientId) {
    return callOrgIncludedFunction(orgId, parentOrgId, clientId, "AD_ISORGINCLUDED");
  }

  protected int callOrgIncludedFunction(String orgId, String parentOrgId, String clientId,
      String functionName) {
    int value = 0;
    try {
      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(orgId);
      parameters.add(parentOrgId);
      parameters.add(clientId);
      value = ((BigDecimal) CallStoredProcedure.getInstance().call(functionName, parameters, null))
          .intValue();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return value;
  }
}
