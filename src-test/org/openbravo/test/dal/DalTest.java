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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.CategoryAccounts;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.cashmgmt.CashBook;
import org.openbravo.model.financialmgmt.cashmgmt.CashBookAccounts;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.test.base.BaseTest;

/**
 * Test different parts of the dal api.
 * 
 * Note the testcases assume that they are run in the order defined in this
 * class.
 * 
 * @author mtaal
 */

public class DalTest extends BaseTest {
    private static final Logger log = Logger.getLogger(DalTest.class);

    public void testShowInPreciseDouble() {
        final double a = 0.58;
        System.err.println(a * 100);
    }

    // creates a new BPGroup, test simple save, BPGroup is removed in next test
    public void testCreateBPGroup() {
        setErrorOccured(true);
        setUserContext("1000001");
        final Category bpg = OBProvider.getInstance().get(Category.class);
        bpg.setDefault(true);
        bpg.setDescription("testdescription");
        bpg.setName("testname");
        bpg.setValue("testvalue");
        OBDal.getInstance().save(bpg);
        setErrorOccured(false);
    }

    // query for the BPGroup again and remove it
    public void testRemoveBPGroup() {
        setErrorOccured(true);
        setUserContext("1000001");
        final OBCriteria<Category> obCriteria = OBDal.getInstance()
                .createCriteria(Category.class);
        obCriteria.add(Expression.eq(Category.PROPERTY_NAME, "testname"));
        final List<Category> bpgs = obCriteria.list();
        assertEquals(1, bpgs.size());
        final Category bpg = bpgs.get(0);
        final OBContext obContext = OBContext.getOBContext();
        assertEquals(obContext.getUser().getId(), bpg.getCreatedBy().getId());
        assertEquals(obContext.getUser().getId(), bpg.getUpdatedBy().getId());
        // update and create have occured less than one second ago
        // note that if the delete fails for other reasons that you will have a
        // currency in the database which has for sure a created/updated time
        // longer in the past, You need to manually delete the currency record
        if (true) {
            assertTrue(
                    "Created time not updated",
                    (System.currentTimeMillis() - bpg.getCreated().getTime()) < 2000);
            assertTrue(
                    "Updated time not updated",
                    (System.currentTimeMillis() - bpg.getUpdated().getTime()) < 2000);
        }

        // first delete the related accounts
        final OBCriteria<CategoryAccounts> obc2 = OBDal.getInstance()
                .createCriteria(CategoryAccounts.class);
        obc2
                .add(Expression.eq(CategoryAccounts.PROPERTY_CATEGORY, bpgs
                        .get(0)));
        final List<CategoryAccounts> bpgas = obc2.list();
        for (final CategoryAccounts bga : bpgas) {
            OBDal.getInstance().remove(bga);
        }
        OBDal.getInstance().remove(bpgs.get(0));
        setErrorOccured(false);
    }

    // check if the BPGroup was removed
    public void testCheckBPGroupRemoved() {
        setErrorOccured(true);
        setUserContext("1000001");
        final OBCriteria<Category> obc = OBDal.getInstance().createCriteria(
                Category.class);
        obc.add(Expression.eq(Category.PROPERTY_NAME, "testname"));
        final List<Category> bpgs = obc.list();
        assertEquals(0, bpgs.size());
        setErrorOccured(false);
    }

    // test querying for a specific currency and then updating it
    // should fail for a user
    public void testUpdateCurrencyByUser() {
        setErrorOccured(true);
        setUserContext("1000019");
        final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(
                Currency.class);
        obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "USD"));
        final List<Currency> cs = obc.list();
        assertEquals(1, cs.size());
        final Currency c = cs.get(0);
        c.setDescription(c.getDescription() + " a test");
        try {
            OBDal.getInstance().save(c);
            fail("No security check");
        } catch (final OBSecurityException e) {
            // successfull check
        }
        setErrorOccured(false);
    }

    public void testUpdateCurrencyByAdmin() {
        setErrorOccured(true);
        setBigBazaarAdminContext();
        final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(
                Currency.class);
        obc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "USD"));
        final List<Currency> cs = obc.list();
        assertEquals(1, cs.size());
        final Currency c = cs.get(0);
        c.setDescription(c.getDescription() + " a test");
        OBDal.getInstance().save(c);
        setErrorOccured(false);
    }

    // Test toString and using class for querying
    public void testToString() {
        setErrorOccured(true);
        setBigBazaarAdminContext();
        final List<Product> products = OBDal.getInstance().createCriteria(
                Product.class).list();
        for (final Product p : products) {
            System.err.println(p.toString());
        }
        setErrorOccured(false);
    }

    // tests a paged read of transactions and print of the identifier.
    // the identifier of a transaction has been implemented such that
    // it reads all the references (which are non-null) and uses their
    // identifier to create the identifier of the transaction.
    // test sorting on product.name
    public void testTransaction25PageRead() {
        setErrorOccured(true);
        setUserContext("1000001");
        final OBCriteria<MaterialTransaction> countObc = OBDal.getInstance()
                .createCriteria(MaterialTransaction.class);
        final int count = countObc.count();
        final int pageSize = 25;
        int pageCount = 1 + (count / pageSize);
        if (pageCount > 25) {
            pageCount = 25;
        }
        for (int i = 0; i < pageCount; i++) {
            final OBCriteria<MaterialTransaction> obc = OBDal.getInstance()
                    .createCriteria(MaterialTransaction.class);
            obc.addOrderBy(MaterialTransaction.PROPERTY_PRODUCT + "."
                    + Product.PROPERTY_NAME, false);
            obc.setMaxResults(pageSize);
            obc.setFirstResult(i * pageSize);

            System.err.println("PAGE>>> " + (1 + i));
            for (final MaterialTransaction t : obc.list()) {
                System.err.println(t.getIdentifier());
            }
        }
        setErrorOccured(false);
    }

    // test reads 500 pages of the transaction table and then prints how many
    // milliseconds one page took to retrieve
    public void testTransactionAllPagesTime() {
        setErrorOccured(true);
        setUserContext("0");
        final OBCriteria<MaterialTransaction> countObc = OBDal.getInstance()
                .createCriteria(MaterialTransaction.class);
        final int count = countObc.count();
        long time = System.currentTimeMillis();
        final int pageSize = 25;
        int pageCount = 1 + (count / pageSize);
        pageCount = 500;
        long avg = 0;
        for (int i = 0; i < pageCount; i++) {
            final OBCriteria<MaterialTransaction> obc = OBDal.getInstance()
                    .createCriteria(MaterialTransaction.class);
            obc.addOrderBy(MaterialTransaction.PROPERTY_PRODUCT + "."
                    + Product.PROPERTY_NAME, false);
            obc.setMaxResults(pageSize);
            obc.setFirstResult(i * pageSize);

            if ((i % 25) == 0) {
                System.err.println("PAGE>>> " + (1 + i) + "/" + pageCount);
            }
            for (final MaterialTransaction t : obc.list()) {
                log.debug(t.getIdentifier());
                // System.err.println(t.getIdentifier() +
                // " client/organisation " +
                // t.getClient().getName() + "/" +
                // t.getOrganisation().getName());
            }
            if (avg == 0) {
                avg = System.currentTimeMillis() - time;
            } else {
                avg = (avg + System.currentTimeMillis() - time) / 2;
            }
            time = System.currentTimeMillis();
            SessionHandler.getInstance().commitAndClose();
        }

        System.err.println("Read " + pageCount + " pages with average " + avg
                + " milliSeconds per page");
    }

    // test paged read of currencys

    public void testCurrencyPageRead() {
        setErrorOccured(true);
        setUserContext("0");
        final int count = OBDal.getInstance().createCriteria(Currency.class)
                .count();
        final int pageSize = 5;
        final int pageCount = 1 + (count / 5);
        for (int i = 0; i < pageCount; i++) {
            final OBCriteria<Currency> obc = OBDal.getInstance()
                    .createCriteria(Currency.class);
            obc.addOrderBy(Currency.PROPERTY_ISOCODE, false);
            obc.setMaxResults(pageSize);
            obc.setFirstResult(i * pageSize);

            System.err.println("PAGE>>> " + (1 + i));
            for (final Currency c : obc.list()) {
                System.err.println(c.getISOCode() + " " + c.getCurSymbol());
            }
        }
        setErrorOccured(false);
    }

    // test the read of a dynamically mapped entity
    public void testCashBookPageRead() {
        setErrorOccured(true);
        setUserContext("0");
        final int count = OBDal.getInstance().createCriteria(
                CashBook.ENTITY_NAME).count();
        final int pageSize = 5;
        final int pageCount = 1 + (count / 5);
        for (int i = 0; i < pageCount; i++) {
            final OBCriteria<CashBook> obc = OBDal.getInstance()
                    .createCriteria(CashBook.ENTITY_NAME);
            obc.setFirstResult(i * pageSize);
            obc.setMaxResults(pageSize);

            System.err.println("CashBook PAGE>>> " + (1 + i));
            for (final CashBook c : obc.list()) {
                System.err.println(c.getName() + " " + c.getDescription());
            }
        }
        setErrorOccured(false);
    }

    // Test trigger on creation of cashbook. Note that this test uses a type
    // with
    // a compositeid. The handle of composite ids has not been completely
    // implemented in the prototype. Reading is possible, persisting is not
    // possible
    public void testCashBookTrigger() {
        setErrorOccured(true);
        setUserContext("1000000");
        OBContext.getOBContext().setInAdministratorMode(true);
        String cashBookId = "";
        {
            final OBCriteria<Currency> cc = OBDal.getInstance().createCriteria(
                    Currency.class);
            cc.add(Expression.eq(Currency.PROPERTY_ISOCODE, "USD"));
            final List<Currency> cs = cc.list();
            final Currency currency = cs.get(0);
            final CashBook c = OBProvider.getInstance().get(CashBook.class);
            c.setName("c_" + System.currentTimeMillis());
            c.setDescription("test");
            c.setDefault(false);
            c.set(CashBook.PROPERTY_CURRENCY, currency);

            OBDal.getInstance().save(c);
            cashBookId = c.getId();
            SessionHandler.getInstance().commitAndClose();
        }

        // now check if the save indeed worked out by seeing if there is a
        // cashbook
        // account
        final OBCriteria<CashBookAccounts> cbc = OBDal.getInstance()
                .createCriteria(CashBookAccounts.ENTITY_NAME);
        cbc.add(Expression.eq(CashBookAccounts.PROPERTY_CASHBOOK + "."
                + CashBook.PROPERTY_ID, cashBookId));
        final List<?> cbas = cbc.list();
        assertTrue(cbas.size() > 0);
        for (final Object co : cbas) {
            final CashBookAccounts cba = (CashBookAccounts) co;
            System.err.println(cba.getUpdated() + " "
                    + cba.getCashBook().getName());
        }
        setErrorOccured(false);
    }
}