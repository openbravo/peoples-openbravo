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

import org.junit.Test;
import org.openbravo.test.base.OBBaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADOrgPersistInfoSetReadyTest extends OBBaseTest {

  private static final Logger log = LoggerFactory.getLogger(ADOrgPersistInfoSetReadyTest.class);

  /**
   * Create a new Organization of type Generic under F&B España, S.A Organization and test persist
   * organization info after the organization is set as ready with Cascade No.
   */
  @Test
  public void testSetReadyOneGenericOrganization() {
    try {
      ADOrgPersistInfoUtility.setTestContextFB();
      String orgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_GENERIC, ADOrgPersistInfoConstants.ORG_FB_SPAIN, true,
          ADOrgPersistInfoConstants.CUR_EURO);
      ADOrgPersistInfoUtility.setAsReady(orgId, "N");
      ADOrgPersistInfoUtility.assertPersistOrgInfo(orgId);
    } catch (Exception e) {
      log.error("Error in testSetReadyOrganization", e);
    }
  }

  /**
   * Create a new Organization of type Generic under F&B España, S.A Organization and test persist
   * organization info after the organization is set as ready with Cascade Yes.
   */
  @Test
  public void testSetReadyOneGenericOrganizationCascade() {
    try {
      ADOrgPersistInfoUtility.setTestContextFB();
      String orgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_GENERIC, ADOrgPersistInfoConstants.ORG_FB_SPAIN, true,
          ADOrgPersistInfoConstants.CUR_EURO);
      ADOrgPersistInfoUtility.setAsReady(orgId, "Y");
      ADOrgPersistInfoUtility.assertPersistOrgInfo(orgId);
    } catch (Exception e) {
      log.error("Error in testSetReadyOrganization", e);
    }
  }

  /**
   * Create a new Organization of type Generic under F&B España, S.A Organization and a child under
   * it and test persist organization info after the organization is set as ready with cascade as
   * No.
   */
  @Test
  public void testSetReadyTwoGenericOrganization() {
    try {
      ADOrgPersistInfoUtility.setTestContextFB();
      String firstOrgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_GENERIC, ADOrgPersistInfoConstants.ORG_FB_SPAIN, true,
          ADOrgPersistInfoConstants.CUR_EURO);
      String secondOrgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_GENERIC, firstOrgId, false,
          ADOrgPersistInfoConstants.CUR_EURO);
      ADOrgPersistInfoUtility.setAsReady(firstOrgId, "N");
      ADOrgPersistInfoUtility.setAsReady(secondOrgId, "N");
      ADOrgPersistInfoUtility.assertPersistOrgInfo(firstOrgId);
      ADOrgPersistInfoUtility.assertPersistOrgInfo(secondOrgId);

    } catch (Exception e) {
      log.error("Error in testSetReadyTwoGenericOrganization", e);
    }
  }

  /**
   * Create a new Organization of type Generic under F&B España, S.A Organization and a child under
   * it and test persist organization info after the organization is set as ready with cascade as
   * Yes.
   */
  @Test
  public void testSetReadyTwoGenericOrganizationCascade() {
    try {
      ADOrgPersistInfoUtility.setTestContextFB();
      String firstOrgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_GENERIC, ADOrgPersistInfoConstants.ORG_FB_SPAIN, true,
          ADOrgPersistInfoConstants.CUR_EURO);
      String secondOrgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_GENERIC, firstOrgId, false,
          ADOrgPersistInfoConstants.CUR_EURO);
      ADOrgPersistInfoUtility.setAsReady(firstOrgId, "Y");
      ADOrgPersistInfoUtility.assertPersistOrgInfo(firstOrgId);
      ADOrgPersistInfoUtility.assertPersistOrgInfo(secondOrgId);

    } catch (Exception e) {
      log.error("Error in testSetReadyTwoGenericOrganization", e);
    }
  }

  /**
   * Create a new Organization of type Organization under * Organization and test persist
   * organization information after the organization is set as ready cascade as No.
   */
  @Test
  public void testSetReadyOrganization() {
    try {
      ADOrgPersistInfoUtility.setTestContextFB();
      String orgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_ORGANIZATION, ADOrgPersistInfoConstants.ORG_FB_SPAIN,
          true, ADOrgPersistInfoConstants.CUR_EURO);
      ADOrgPersistInfoUtility.setAsReady(orgId, "N");
      ADOrgPersistInfoUtility.assertPersistOrgInfo(orgId);
    } catch (Exception e) {
      log.error("Error in testSetReadyOrganization", e);
    }
  }

  /**
   * Create a new Organization of type Organization under * Organization and test persist
   * organization information after the organization is set as ready with cascade as Yes.
   */
  @Test
  public void testSetReadyOrganizationCascade() {
    try {
      ADOrgPersistInfoUtility.setTestContextFB();
      String orgId = ADOrgPersistInfoUtility.createOrganization(
          ADOrgPersistInfoConstants.ORGTYPE_ORGANIZATION, ADOrgPersistInfoConstants.ORG_FB_SPAIN,
          true, ADOrgPersistInfoConstants.CUR_EURO);
      ADOrgPersistInfoUtility.setAsReady(orgId, "Y");
      ADOrgPersistInfoUtility.assertPersistOrgInfo(orgId);
    } catch (Exception e) {
      log.error("Error in testSetReadyOrganization", e);
    }
  }
}
