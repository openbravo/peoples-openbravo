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

package org.openbravo.test.dal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationMapping;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.CategoryAccounts;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.test.base.BaseTest;

/**
 * Test different parts of the dal api: {@link OBDal}, {@link OBCriteria} and {@link OBQuery}.
 * 
 * Note the testcases assume that they are run in the order defined in this class.
 * 
 * @author mtaal
 */

public class DalQueryTest extends BaseTest {
  private static final Logger log = Logger.getLogger(DalQueryTest.class);

  /**
   * Tests a left join with {@link ModelImplementation} as the main class.
   */
  public void testDalFirstWhereLeftJoinClause() {
    setBigBazaarAdminContext();
    final String where = "as mo left join mo.callout left join mo.reference left join mo.specialForm left join mo.process left join mo.workflow left join mo.tab where mo.callout.module.id='0' or mo.reference.module.id='0' or mo.specialForm.module.id='0' or mo.process.module.id='0' or mo.workflow.module.id='0' or mo.tab.module.id='0'";
    final OBQuery<ModelImplementation> obq = OBDal.getInstance().createQuery(
        ModelImplementation.class, where);
    assertTrue(obq.list().size() > 0);
  }

  /**
   * Tests a left join with {@link ModelImplementation} as the main class.
   */
  public void testDalExtraJoinWhereLeftJoinClause() {
    setBigBazaarAdminContext();
    final String where = "as mom left join mom."
        + ModelImplementationMapping.PROPERTY_MODELOBJECT
        + " as mo left join mo."
        + ModelImplementation.PROPERTY_CALLOUT
        + " left join mo."
        + ModelImplementation.PROPERTY_REFERENCE
        + " left join mo."
        + ModelImplementation.PROPERTY_SPECIALFORM
        + " left join mo."
        + ModelImplementation.PROPERTY_PROCESS
        + " left join mo."
        + ModelImplementation.PROPERTY_WORKFLOW
        + " left join mo."
        + ModelImplementation.PROPERTY_TAB
        + " where mo.callout.module.id='0' or mo.reference.module.id='0' or mo.specialForm.module.id='0' or mo.process.module.id='0' or mo.workflow.module.id='0' or mo.tab.module.id='0'";
    final OBQuery<ModelImplementationMapping> obq = OBDal.getInstance().createQuery(
        ModelImplementationMapping.class, where);
    assertTrue(obq.list().size() > 0);
  }

  /**
   * Tests a left join with {@link ModelImplementation} as the main class.
   */
  public void testDalWhereLeftJoinClause() {
    setBigBazaarAdminContext();
    final String where = "as mo left join mo.callout left join mo.reference where mo.callout.module.id='0' or mo.reference.module.id='0'";
    final OBQuery<ModelImplementation> obq = OBDal.getInstance().createQuery(
        ModelImplementation.class, where);
    assertTrue(obq.list().size() > 0);
  }

  /**
   * Tests a left join with {@link ModelImplementation} as the main class.
   */
  public void testDalOtherWhereLeftJoinClause() {
    setBigBazaarAdminContext();
    final String where = "as mo left join mo.callout left join mo.reference where (mo.callout.module.id='0' or mo.reference.module.id='0') and exists(from ADUser where id<>'0')";
    final OBQuery<ModelImplementation> obq = OBDal.getInstance().createQuery(
        ModelImplementation.class, where);
    assertTrue(obq.list().size() > 0);
  }

  /**
   * Tests a left join with {@link ModelImplementation} as the main class.
   */
  public void testDalAnOtherWhereLeftJoinClause() {
    setBigBazaarAdminContext();
    final String where = "exists(from ADUser where id<>'0')";
    final OBQuery<ModelImplementation> obq = OBDal.getInstance().createQuery(
        ModelImplementation.class, where);
    assertTrue(obq.list().size() > 0);
  }

  /**
   * Test creates a new {@link Category} and saves it. The new object is removed in the next test.
   */
  public void testCreateBPGroup() {
    setBigBazaarUserContext();
    addReadWriteAccess(Category.class);
    final Category bpg = OBProvider.getInstance().get(Category.class);
    bpg.setDefault(true);
    bpg.setDescription("testdescription");
    bpg.setName("testname");
    bpg.setSearchKey("testvalue");
    bpg.setActive(true);
    OBDal.getInstance().save(bpg);
  }

  /**
   * Test queries for the created {@link Category} and removes it.
   */
  public void testRemoveBPGroup() {
    setBigBazaarUserContext();
    addReadWriteAccess(Category.class);
    addReadWriteAccess(CategoryAccounts.class);
    final OBQuery<Category> obQuery = OBDal.getInstance().createQuery(Category.class,
        Category.PROPERTY_NAME + "='testname' or " + Category.PROPERTY_SEARCHKEY + "='testvalue'");
    final List<Category> bpgs = obQuery.list();
    assertEquals(1, bpgs.size());
    final Category bpg = bpgs.get(0);
    final OBContext obContext = OBContext.getOBContext();
    assertEquals(obContext.getUser().getId(), bpg.getCreatedBy().getId());
    assertEquals(obContext.getUser().getId(), bpg.getUpdatedBy().getId());
    // update and create have occured less than one second ago
    // note that if the delete fails for other reasons that you will have a
    // currency in the database which has for sure a created/updated time
    // longer in the past, You need to manually delete the currency record
    if (false) {
      assertTrue("Created time not updated", (System.currentTimeMillis() - bpg.getCreationDate()
          .getTime()) < 2000);
      assertTrue("Updated time not updated", (System.currentTimeMillis() - bpg.getUpdated()
          .getTime()) < 2000);
    }

    // first delete the related accounts
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(bpgs.get(0));
    final OBQuery<CategoryAccounts> q2 = OBDal.getInstance().createQuery(CategoryAccounts.class,
        " " + CategoryAccounts.PROPERTY_BUSINESSPARTNERCATEGORY + "=?", parameters);
    final List<CategoryAccounts> bpgas = q2.list();
    for (final CategoryAccounts bga : bpgas) {
      OBDal.getInstance().remove(bga);
    }
    OBDal.getInstance().remove(bpgs.get(0));
  }

  /**
   * Check that the {@link Category} was indeed removed.
   */
  public void testCheckBPGroupRemoved() {
    setBigBazaarUserContext();
    addReadWriteAccess(Category.class);
    final OBQuery<Category> obQuery = OBDal.getInstance().createQuery(Category.class,
        Category.PROPERTY_NAME + "='testname' or " + Category.PROPERTY_SEARCHKEY + "='testvalue'");
    final List<Category> bpgs = obQuery.list();
    assertEquals(0, bpgs.size());
  }

  /**
   * Tests queries for a currency and then updates it. The test should fail as the user does not
   * have update authorisation.
   */
  public void testUpdateCurrencyByUser() {
    setUserContext("1000019");
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "USD"));
    final List<Currency> cs = obc.list();
    assertEquals(1, cs.size());
    final Currency c = cs.get(0);
    c.setDescription(c.getDescription() + " a test");
    try {
      OBDal.getInstance().save(c);
      fail("No security check");
    } catch (final OBSecurityException e) {
      // successfull check, do not commit the change
      rollback();
    }
  }

  /**
   * Tests a paged read of transactions and print of the identifier. The identifier of a transaction
   * has been implemented such that it reads all the references (which are non-null) and uses their
   * identifier to create the identifier of the transaction. The test sorts on product.name.
   */
  public void testTransaction25PageRead() {
    setBigBazaarUserContext();
    addReadWriteAccess(MaterialTransaction.class);
    final OBQuery<MaterialTransaction> cq = OBDal.getInstance().createQuery(
        MaterialTransaction.class, " order by product.name");
    final int count = cq.count();
    final int pageSize = 25;
    int pageCount = 1 + (count / pageSize);
    if (pageCount > 25) {
      pageCount = 25;
    }
    for (int i = 0; i < pageCount; i++) {
      final OBQuery<MaterialTransaction> obq = OBDal.getInstance().createQuery(
          MaterialTransaction.class,
          " order by " + MaterialTransaction.PROPERTY_PRODUCT + "." + Product.PROPERTY_NAME);
      final Query qry = obq.createQuery();
      qry.setMaxResults(pageSize);
      qry.setFirstResult(i * pageSize);

      log.debug("PAGE>>> " + (1 + i));
      for (final Object o : qry.list()) {
        log.debug(((MaterialTransaction) o).getIdentifier());
      }
    }
  }
}