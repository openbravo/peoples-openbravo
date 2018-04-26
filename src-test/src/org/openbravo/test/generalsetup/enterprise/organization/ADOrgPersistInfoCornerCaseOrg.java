/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.test.generalsetup.enterprise.organization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openbravo.test.base.OBBaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADOrgPersistInfoCornerCaseOrg extends OBBaseTest {

  private static final Logger log = LoggerFactory.getLogger(ADOrgPersistInfoCornerCaseOrg.class);

  /**
   * Test corner case organization persist information
   */
  @Test
  public void testCornerCaseOrganization() {

    try {

      // * Organization

      assertEquals("Failed AD_Org_GetCalendarOwner for * Organization",
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getcalendarownertn",
              ADOrgPersistInfoConstants.ORG_0),
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getcalendarowner",
              ADOrgPersistInfoConstants.ORG_0));
      assertEquals("Failed AD_Org_GetPeriodControlAllow for * Organization",
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getperiodcontrolallowtn",
              ADOrgPersistInfoConstants.ORG_0),
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getperiodcontrolallow",
              ADOrgPersistInfoConstants.ORG_0));

      // Null Organization

      assertEquals("Failed AD_Org_GetCalendarOwner for null Organization",
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getcalendarownertn", null),
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getcalendarowner", null));
      assertEquals("Failed AD_Org_GetPeriodControlAllow for null Organization",
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getperiodcontrolallowtn", null),
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getperiodcontrolallow", null));

      // Organizations that does not exists in database

      assertEquals("Failed AD_Org_GetCalendarOwner for organization not in database",
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getcalendarownertn", "XX"),
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getcalendarowner", "XX"));
      assertEquals("Failed AD_Org_GetPeriodControlAllow for organization not in database",
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getperiodcontrolallowtn", "XX"),
          ADOrgPersistInfoUtility.getPersistOrgInfo("ad_org_getperiodcontrolallow", "XX"));
    } catch (Exception e) {
      log.error("Error in testCornerCaseOrganization", e);
    }
  }
}
