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

import java.util.List;

import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.test.base.BaseTest;

/**
 * Tests access on the basis of window and table definitions. Also tests derived read access.
 * 
 * @author mtaal
 */

public class EntityAccessTest extends BaseTest {

  public void testCreateCurrency() {
    setErrorOccured(true);
    setBigBazaarAdminContext();
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "TE2"));
    final List<Currency> cs = obc.list();
    if (cs.size() == 0) {
      final Currency c = OBProvider.getInstance().get(Currency.class);
      c.setCurSymbol("TE2");
      c.setDescription("test currency");
      c.setISOCode("TE2");
      c.setPricePrecision((long) 5);
      c.setStdPrecision((long) 6);
      c.setCostingPrecision((long) 4);
      OBDal.getInstance().save(c);
    }
    setErrorOccured(false);
  }

  // query for the currency again and remove it
  public void testNonDeletable() {
    setErrorOccured(true);
    setBigBazaarAdminContext();
    setUserContext("1000002");
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "TE2"));
    final List<Currency> cs = obc.list();
    assertEquals(1, cs.size());
    final Currency c = cs.get(0);
    try {
      OBDal.getInstance().remove(c);
      fail("Currency should be non-deletable");
    } catch (final OBSecurityException e) {
      assertTrue("Wrong exception thrown:  " + e.getMessage(), e.getMessage().indexOf(
          "is not deletable") != -1);
    }
    setErrorOccured(false);
  }

  // check if the currency was removed
  public void testCheckDerivedReadableCurrency() {
    setErrorOccured(true);
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "TE2"));
    final List<Currency> cs = obc.list();
    final Currency c = cs.get(0);
    System.err.println(c.getIdentifier());
    System.err.println(c.getId());
    try {
      System.err.println(c.getCurSymbol());
      fail("Derived readable not applied");
    } catch (final OBSecurityException e) {
      assertTrue("Wrong exception thrown:  " + e.getMessage(), e.getMessage().indexOf(
          "is not directly readable") != -1);
    }
    setErrorOccured(false);
  }

  // test derived readable on a set method and test save action
  public void testUpdateCurrencyDerivedRead() {
    setErrorOccured(true);
    setUserContext("1000000");
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "USD"));
    final List<Currency> cs = obc.list();
    final Currency c = cs.get(0);
    try {
      System.err.println(c.getDescription());
      fail("Derived readable not checked on set");
    } catch (final OBSecurityException e) {
      assertTrue("Wrong exception thrown:  " + e.getMessage(), e.getMessage().indexOf(
          "is not directly readable") != -1);
    }
    try {
      OBDal.getInstance().save(c);
      fail("No security check");
    } catch (final OBSecurityException e) {
      // successfull check
      assertTrue("Wrong exception thrown:  " + e.getMessage(), e.getMessage().indexOf(
          "is not writable by this user") != -1);
    }
    setErrorOccured(false);
  }

  // test non readable
  public void testNonReadable() {
    setErrorOccured(true);
    setUserContext("1000002");
    try {
      final OBCriteria<Costing> obc = OBDal.getInstance().createCriteria(Costing.class);
      obc.add(Expression.eq(Costing.PROPERTY_ID, "1000078"));
      final List<Costing> cs = obc.list();
      assertTrue(cs.size() > 0);
      fail("Non readable check not enforced");
    } catch (final OBSecurityException e) {
      assertTrue("Wrong exception thrown:  " + e.getMessage(), e.getMessage().indexOf(
          "is not readable") != -1);
    }
  }

  public void testUpdateCurrencySucces() {
    setErrorOccured(true);
    setBigBazaarAdminContext();
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "USD"));
    final List<Currency> cs = obc.list();
    final Currency c = cs.get(0);
    c.setDescription(" a test");
    OBDal.getInstance().save(c);
    setErrorOccured(false);
  }
}