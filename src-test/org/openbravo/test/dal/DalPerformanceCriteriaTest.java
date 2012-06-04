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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.dal;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.test.base.BaseTest;

/**
 * Test case to try and test proxy loading or stateless sessions.
 * 
 * @author mtaal
 */

public class DalPerformanceCriteriaTest extends BaseTest {

  private static final int CNT = 10000;

  public void testPerformance() {
    doTestCriteriaPerformance(new QueryTest1());
    doTestCriteriaPerformance(new QueryTest2());
    doTestCriteriaPerformance(new QueryTest3());
  }

  public void doTestCriteriaPerformance(QueryTest queryTest) {
    OBDal.getInstance().commitAndClose();

    // warmup
    for (int i = 0; i < 10; i++) {
      queryTest.doCriteriaQry();
    }

    long t1 = System.currentTimeMillis();
    for (int i = 0; i < CNT; i++) {
      Assert.assertTrue(0 < queryTest.doCriteriaQry());
    }
    t1 = System.currentTimeMillis() - t1;
    OBDal.getInstance().commitAndClose();

    // warmup
    for (int i = 0; i < 10; i++) {
      queryTest.doHqlQry();
    }

    long t2 = System.currentTimeMillis();
    for (int i = 0; i < CNT; i++) {
      Assert.assertTrue(0 < queryTest.doHqlQry());
    }
    t2 = System.currentTimeMillis() - t2;
    OBDal.getInstance().commitAndClose();
    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
    System.err.println(queryTest.getId());
    System.err.println("Count: " + CNT);
    System.err.println("Criteria ms: " + t1);
    System.err.println("Hql ms: " + t2);
  }

  private abstract class QueryTest {
    public abstract int doCriteriaQry();

    public abstract int doHqlQry();

    public abstract String getId();

  }

  public void testCriteriaScrollable() {

    OBCriteria<BusinessPartner> c = OBDal.getInstance().createCriteria(BusinessPartner.class);
    ScrollableResults iterator = c.scroll(ScrollMode.FORWARD_ONLY);
    iterator.next();
  }

  private class QueryTest1 extends QueryTest {
    private String qryStr = "";

    public int doCriteriaQry() {
      final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
      obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "USD"));
      final List<Currency> cs = obc.list();
      return cs.size();
    }

    public int doHqlQry() {
      final OBQuery<Currency> obq = OBDal.getInstance()
          .createQuery(Currency.class, "iSOCode='USD'");
      final List<Currency> cs = obq.list();
      qryStr = "Currency: " + obq.getWhereAndOrderBy();
      return cs.size();
    }

    public String getId() {
      return qryStr;
    }
  }

  private class QueryTest2 extends QueryTest {
    private String qryStr;

    public int doCriteriaQry() {
      final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
      // obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "USD"));
      final List<Currency> cs = obc.list();
      return cs.size();
    }

    public int doHqlQry() {
      final OBQuery<Currency> obq = OBDal.getInstance().createQuery(Currency.class, "");
      final List<Currency> cs = obq.list();
      qryStr = "Currency: " + obq.getWhereAndOrderBy();
      return cs.size();
    }

    public String getId() {
      return qryStr;
    }
  }

  private class QueryTest3 extends QueryTest {
    private String qryStr;

    public int doCriteriaQry() {
      final OBCriteria<MaterialTransaction> obc = OBDal.getInstance().createCriteria(
          MaterialTransaction.class);
      obc.add(Restrictions.isNotNull(MaterialTransaction.PROPERTY_UOM));
      obc.addOrderBy(MaterialTransaction.PROPERTY_PRODUCT + "." + Product.PROPERTY_NAME, false);
      obc.setMaxResults(10);
      obc.setFirstResult(0);
      final List<MaterialTransaction> cs = obc.list();
      return cs.size();
    }

    public int doHqlQry() {
      final OBQuery<MaterialTransaction> obq = OBDal.getInstance().createQuery(
          MaterialTransaction.class, " uOM <> null order by product");
      final List<MaterialTransaction> cs = obq.list();
      qryStr = "MaterialTransaction: " + obq.getWhereAndOrderBy();
      return cs.size();
    }

    public String getId() {
      return qryStr;
    }
  }

  private void createManyBPs() {
    try {
      setTestAdminContext();

      OBDal.getInstance().commitAndClose();

      for (int i = 0; i < 10000; i++) {
        BusinessPartner bp = OBProvider.getInstance().get(BusinessPartner.class);

        // Generating random strings for testing
        UUID name = UUID.randomUUID();
        UUID key = UUID.randomUUID();

        bp.setName(name.toString());
        bp.setSearchKey(key.toString());

        final Category category = (Category) getProxy(Category.ENTITY_NAME, TEST_BP_CATEGORY_ID);
        bp.setBusinessPartnerCategory(category);

        OBDal.getInstance().save(bp);
        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          System.err.println(i);
        }
        // this all works
        // OBDal.getInstance().refresh(bp);
        // Assert.assertTrue(bp.getId() != null);

        // check that if really loading that still the proxy object is returned
        // Assert.assertTrue(category == OBDal.getInstance().get(Category.ENTITY_NAME,
        // TEST_BP_CATEGORY_ID));
      }
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Will return a non-loaded hibernate proxy if the object was not already loaded by hibernate.
   * 
   * NOTE/BEWARE: this method will not check if the object actually exists in the database. This
   * will detected when persisting a referencing object or when this proxy gets initialized!
   * 
   * This method differs from other get methods in this class, these methods will always eagerly
   * load the object and thereby also immediately check the existence of these referenced objects.
   * 
   * @param entityName
   *          the type of object to search for
   * @param id
   *          the id of the object
   * @return the object, or null if none found
   */
  private BaseOBObject getProxy(String entityName, Object id) {
    return (BaseOBObject) ((SessionImplementor) OBDal.getInstance().getSession()).internalLoad(
        entityName, (Serializable) id, false, false);
  }

  /*
   * List<FIN_PaymentSchedule> lQuery, lReturn = new ArrayList<FIN_PaymentSchedule>();
   * OBCriteria<FIN_PaymentSchedule> obcPS = OBDal.getInstance().createCriteria(
   * FIN_PaymentSchedule.class); obcPS.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE,
   * invoice)); lQuery = obcPS.list();
   * 
   * // 1) Remove not paid payment schedule detail lines OBCriteria<FIN_PaymentScheduleDetail>
   * obcPSD = OBDal.getInstance().createCriteria( FIN_PaymentScheduleDetail.class);
   * obcPSD.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
   * invoicePS));
   * obcPSD.add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
   * 
   * OBCriteria<FIN_PaymentScheduleDetail> orderedPSDs = OBDal.getInstance().createCriteria(
   * FIN_PaymentScheduleDetail.class);
   * orderedPSDs.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_ID, psdSet));
   * orderedPSDs.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);
   * 
   * OBCriteria<FinAccPaymentMethod> psdFilter = OBDal.getInstance().createCriteria(
   * FinAccPaymentMethod.class); psdFilter.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT,
   * finAcc)); psdFilter.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
   * finPmtMethod));
   * 
   * OBCriteria<FIN_Payment> obcPayment = OBDal.getInstance().createCriteria(FIN_Payment.class);
   * obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_BUSINESSPARTNER, bp));
   * obcPayment.add(Restrictions.eq(FIN_Payment.PROPERTY_RECEIPT, isReceipt));
   * obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_GENERATEDCREDIT, BigDecimal.ZERO));
   * obcPayment.add(Restrictions.ne(FIN_Payment.PROPERTY_USEDCREDIT, BigDecimal.ZERO));
   * obcPayment.addOrderBy(FIN_Payment.PROPERTY_PAYMENTDATE, false);
   * obcPayment.addOrderBy(FIN_Payment.PROPERTY_DOCUMENTNO, false); return obcPayment.list();
   * 
   * final OBCriteria<RoleOrganization> roleOrgs = OBDal.getInstance().createCriteria(
   * RoleOrganization.class); roleOrgs.add(Restrictions.eq(RoleOrganization.PROPERTY_ROLE, role));
   * roleOrgs.add(Restrictions.eq(RoleOrganization.PROPERTY_ORGADMIN, true));
   * 
   * OBCriteria<ModuleInstall> qModInstall = OBDal.getInstance().createCriteria(
   * ModuleInstall.class);
   */

}