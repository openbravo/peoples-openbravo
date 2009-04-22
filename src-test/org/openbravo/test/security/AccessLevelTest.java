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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.security;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.validation.AccessLevelChecker;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.financialmgmt.cashmgmt.CashJournal;
import org.openbravo.test.base.BaseTest;

/**
 * Tests/checks the accesslevel of an entity. See the {@link AccessLevelChecker}.
 * 
 * @author mtaal
 */

public class AccessLevelTest extends BaseTest {

  public void testAccessLevelCO() {
    setBigBazaarAdminContext();
    final Client c = OBDal.getInstance().get(Client.class, "0");

    final BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, "1000005");
    bp.setClient(c);
    try {
      SessionHandler.getInstance().commitAndClose();
      fail();
    } catch (final OBSecurityException e) {
      // no fail!
      assertTrue(e.getMessage().indexOf("may not have instances with client 0") != -1);
      SessionHandler.getInstance().rollback();
    }
  }

  public void testAccessLevelSystem() {
    setUserContext("0");
    final Organization o = OBDal.getInstance().get(Organization.class, "1000002");
    final Table t = OBDal.getInstance().get(Table.class, "100");
    t.setOrganization(o);

    try {
      SessionHandler.getInstance().commitAndClose();
      fail();
    } catch (final OBSecurityException e) {
      // no fail!
      assertTrue("Invalid exception: " + e.getMessage(), e.getMessage().indexOf(
          " may only have instances with organization *") != -1);
      SessionHandler.getInstance().rollback();
    }
  }

  public void testAccessLevelOrganization() {
    setUserContext("0");
    final Organization o = OBDal.getInstance().get(Organization.class, "0");
    final CashJournal c = OBDal.getInstance().get(CashJournal.class, "1000000");
    c.setOrganization(o);

    try {
      SessionHandler.getInstance().commitAndClose();
      fail();
    } catch (final OBSecurityException e) {
      // no fail!
      assertTrue("Invalid exception " + e.getMessage(), e.getMessage().indexOf(
          " may not have instances with organization *") != -1);
      SessionHandler.getInstance().rollback();
    }
  }

  public void testAccessLevelSC() {
    setUserContext("0");
    final Organization o = OBDal.getInstance().get(Organization.class, "1000001");
    final Country c = OBDal.getInstance().get(Country.class, "100");
    c.setOrganization(o);

    try {
      SessionHandler.getInstance().commitAndClose();
      fail("The organization of a system client may not be set to a non-zero org.");
    } catch (final OBSecurityException e) {
      // no fail!
      assertTrue("Invalid exception " + e.getMessage(), e.getMessage().indexOf(
          "may only have instances with organization *") != -1);
      SessionHandler.getInstance().rollback();
    }
  }

}