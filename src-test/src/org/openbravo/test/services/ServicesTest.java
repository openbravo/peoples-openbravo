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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.services;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import org.hibernate.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderlineServiceRelation;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.test.base.OBBaseTest;
import org.openbravo.test.services.data.ServiceTestData;
import org.openbravo.test.services.data.ServiceTestData1;
import org.openbravo.test.services.data.ServiceTestData2;
import org.openbravo.test.services.data.ServiceTestData3;
import org.openbravo.test.services.data.ServiceTestData4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests cases to check service Price computation
 * 
 * 
 */
@RunWith(Parameterized.class)
public class ServicesTest extends OBBaseTest {
  final static private Logger log = LoggerFactory.getLogger(ServicesTest.class);
  // User Openbravo
  private final String USER_ID = "100";
  // Client QA Testing
  private final String CLIENT_ID = "4028E6C72959682B01295A070852010D";
  // Organization Spain
  private final String ORGANIZATION_ID = "357947E87C284935AD1D783CF6F099A1";
  // Role QA Testing Admin
  private final String ROLE_ID = "4028E6C72959682B01295A071429011E";
  // Sales order: 50012
  private final String SALESORDER_ID = "8B53B7E6CF3B4D8D9BCF3A49EED6FCB4";
  // Tax Exempt
  private static final String TAX_EXEMPT = "BA7059430C0A43A9B86A21C4EECF3A21";

  private String testNumber;
  private String testDescription;
  private String serviceId;
  private BigDecimal quantity;
  private BigDecimal price;
  private BigDecimal servicePriceResult;
  private BigDecimal serviceAmountResult;
  private String[][] products;
  private String bpartnerId;
  private String pricelistId;
  private boolean isPriceIncludingTaxes;

  public ServicesTest(String testNumber, String testDescription, ServiceTestData data) {
    this.testNumber = testNumber;
    this.testDescription = testDescription;
    this.serviceId = data.getServiceId();
    this.quantity = data.getQuantity();
    this.price = data.getPrice();
    this.products = data.getProducts();
    this.bpartnerId = data.getBpartnerId();
    this.pricelistId = data.getPricelistId();
    this.servicePriceResult = data.getServicePriceResult();
    this.serviceAmountResult = data.getServiceAmountResult();
  }

  /** parameterized possible combinations for service price computation */
  @Parameters(name = "idx:{0} name:{1}")
  public static Collection<Object[]> params() {
    return Arrays.asList(new Object[][] {//
            { "01", "Service with one related product and regular pricelist",
                new ServiceTestData1() },
            { "02", "Service with three related products and regular pricelist",
                new ServiceTestData2() },
            { "03", "Service with one related product and price including taxes",
                new ServiceTestData3() },
            { "04", "Service with three related products and price including taxes",
                new ServiceTestData4() } //
        });
  }

  /**
   * Verifies price computation for services. Add a relation line, update it and delete it. Review
   * price computation for the service is correct
   */
  @Test
  public void ServiceTest() {
    // Set QA context
    OBContext.setOBContext(USER_ID, ROLE_ID, CLIENT_ID, ORGANIZATION_ID);
    String testOrderId = null;
    try {
      Order order;
      order = OBDal.getInstance().get(Order.class, SALESORDER_ID);
      Order testOrder = (Order) DalUtil.copy(order, false);
      testOrderId = testOrder.getId();
      testOrder.setDocumentNo("Service Test " + testNumber);
      testOrder.setBusinessPartner(OBDal.getInstance().get(BusinessPartner.class, bpartnerId));
      PriceList priceList = OBDal.getInstance().get(PriceList.class, pricelistId);
      testOrder.setPriceList(priceList);
      this.isPriceIncludingTaxes = priceList.isPriceIncludesTax();
      testOrder.setPriceIncludesTax(this.isPriceIncludingTaxes);
      testOrder.setSummedLineAmount(BigDecimal.ZERO);
      testOrder.setGrandTotalAmount(BigDecimal.ZERO);
      testOrder.setId(SequenceIdData.getUUID());
      testOrder.setNewOBObject(true);
      OBDal.getInstance().save(testOrder);
      OBDal.getInstance().flush();
      log.debug("Order Created:" + testOrder.getDocumentNo());
      log.debug(testDescription);
      OBDal.getInstance().refresh(testOrder);
      testOrderId = testOrder.getId();
      // Insert Service Line
      OrderLine serviceOrderLine = insertLine(order, testOrder, serviceId, quantity, price);
      for (String[] product : products) {
        OrderLine orderLine = insertLine(order, testOrder, product[0], new BigDecimal(product[1]),
            new BigDecimal(product[2]));
        insertRelation(serviceOrderLine, orderLine, new BigDecimal(product[3]));
      }
      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(testOrder);
      OBDal.getInstance().refresh(serviceOrderLine);

      if (isPriceIncludingTaxes) {
        assertThat("Wrong Service Gross Price", serviceOrderLine.getGrossUnitPrice(),
            closeTo(servicePriceResult, BigDecimal.ZERO));
        assertThat("Wrong Line Gross amount for service", serviceOrderLine.getLineGrossAmount(),
            closeTo(serviceAmountResult, BigDecimal.ZERO));
      } else {
        assertThat("Wrong Service Price", serviceOrderLine.getUnitPrice(),
            closeTo(servicePriceResult, BigDecimal.ZERO));
        assertThat("Wrong Line Net amount for service", serviceOrderLine.getLineNetAmount(),
            closeTo(serviceAmountResult, BigDecimal.ZERO));
      }
      updateServiceRelationAmounts(serviceOrderLine, BigDecimal.ZERO);
      if (isPriceIncludingTaxes) {
        assertThat("Wrong Service Price", serviceOrderLine.getUnitPrice(),
            closeTo(price, BigDecimal.ZERO));
        assertThat("Wrong Line Net amount for service", serviceOrderLine.getLineNetAmount(),
            closeTo(price.multiply(quantity), BigDecimal.ZERO));
      } else {
        assertThat("Wrong Service Gross Price", serviceOrderLine.getGrossUnitPrice(),
            closeTo(price, BigDecimal.ZERO));
        assertThat("Wrong Line Gross amount for service", serviceOrderLine.getLineGrossAmount(),
            closeTo(price.multiply(quantity), BigDecimal.ZERO));
      }
      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(testOrder);
      OBDal.getInstance().refresh(serviceOrderLine);
      updateServiceRelationAmounts(serviceOrderLine, BigDecimal.ONE);
      removeServiceRelations(serviceOrderLine);
      serviceOrderLine = OBDal.getInstance().get(OrderLine.class, serviceOrderLine.getId());
      if (isPriceIncludingTaxes) {
        assertThat("Wrong Service Price", serviceOrderLine.getUnitPrice(),
            closeTo(price, BigDecimal.ZERO));
        assertThat("Wrong Line Net amount for service", serviceOrderLine.getLineNetAmount(),
            closeTo(price.multiply(quantity), BigDecimal.ZERO));
      } else {
        assertThat("Wrong Service Gross Price", serviceOrderLine.getGrossUnitPrice(),
            closeTo(price, BigDecimal.ZERO));
        assertThat("Wrong Line Gross amount for service", serviceOrderLine.getLineGrossAmount(),
            closeTo(price.multiply(quantity), BigDecimal.ZERO));
      }
      assertThat("Wrong Service Relations", new BigDecimal(serviceOrderLine
          .getOrderlineServiceRelationCOrderlineRelatedIDList().size()),
          closeTo(BigDecimal.ZERO, BigDecimal.ZERO));

    } catch (Exception e) {
      log.error("Error when executing: " + testDescription, e);
      assertFalse(true);
    } finally {
      if (testOrderId != null) {
        System.out.println(testOrderId);
        OBDal.getInstance().flush();
        OBDal.getInstance().getSession().clear();
        // OBDal.getInstance().remove(OBDal.getInstance().get(Order.class, testOrderId));
        OBDal.getInstance().flush();
      }
    }
  }

  private void removeServiceRelations(OrderLine serviceOrderLine) {
    final StringBuilder hqlTransactions = new StringBuilder();
    hqlTransactions.append(" delete from " + OrderlineServiceRelation.ENTITY_NAME + " as olsr ");
    hqlTransactions.append(" where olsr." + OrderlineServiceRelation.PROPERTY_SALESORDERLINE
        + " = :salesorderline");
    Query updateTransactions = OBDal.getInstance().getSession()
        .createQuery(hqlTransactions.toString());
    updateTransactions.setParameter("salesorderline", serviceOrderLine);
    updateTransactions.executeUpdate();
    OBDal.getInstance().flush();

  }

  private void updateServiceRelationAmounts(OrderLine serviceOrderLine, BigDecimal amount) {
    final StringBuilder hqlTransactions = new StringBuilder();
    hqlTransactions.append(" update " + OrderlineServiceRelation.ENTITY_NAME + " as olsr set olsr."
        + OrderlineServiceRelation.PROPERTY_AMOUNT + " = :amount ");
    hqlTransactions.append(" where olsr." + OrderlineServiceRelation.PROPERTY_SALESORDERLINE
        + " = :salesorderline");
    Query updateTransactions = OBDal.getInstance().getSession()
        .createQuery(hqlTransactions.toString());
    updateTransactions.setParameter("salesorderline", serviceOrderLine);
    updateTransactions.setParameter("amount", amount);
    updateTransactions.executeUpdate();
    OBDal.getInstance().flush();

  }

  private OrderLine insertLine(Order sampleOrder, Order testOrder, String productId,
      BigDecimal _quantity, BigDecimal _price) {
    OrderLine orderLine = sampleOrder.getOrderLineList().get(0);
    OrderLine testOrderLine = (OrderLine) DalUtil.copy(orderLine, false);
    Product product = OBDal.getInstance().get(Product.class, productId);
    testOrderLine.setProduct(product);
    testOrderLine.setUOM(product.getUOM());
    testOrderLine.setOrderedQuantity(_quantity);
    testOrderLine.setInvoicedQuantity(BigDecimal.ZERO);
    if (isPriceIncludingTaxes) {
      testOrderLine.setUnitPrice(BigDecimal.ZERO);
      testOrderLine.setListPrice(BigDecimal.ZERO);
      testOrderLine.setStandardPrice(BigDecimal.ZERO);
      testOrderLine.setGrossListPrice(_price);
      testOrderLine.setGrossUnitPrice(_price);
      testOrderLine.setLineNetAmount(BigDecimal.ZERO);
      testOrderLine.setLineGrossAmount(_price.multiply(_quantity));
      testOrderLine.setTaxableAmount(_price.multiply(_quantity));
    } else {
      testOrderLine.setUnitPrice(_price);
      testOrderLine.setListPrice(_price);
      testOrderLine.setGrossListPrice(BigDecimal.ZERO);
      testOrderLine.setGrossUnitPrice(BigDecimal.ZERO);
      testOrderLine.setStandardPrice(_price);
      testOrderLine.setLineNetAmount(_price.multiply(_quantity));
      testOrderLine.setLineGrossAmount(BigDecimal.ZERO);
      testOrderLine.setTaxableAmount(_price.multiply(_quantity));
    }
    testOrderLine.setTax(OBDal.getInstance().get(TaxRate.class, TAX_EXEMPT));
    if (bpartnerId != null) {
      testOrderLine.setBusinessPartner(OBDal.getInstance().get(BusinessPartner.class, bpartnerId));
    }
    testOrderLine.setSalesOrder(testOrder);
    testOrder.getOrderLineList().add(testOrderLine);
    testOrderLine.setId(SequenceIdData.getUUID());
    testOrderLine.setNewOBObject(true);
    OBDal.getInstance().save(testOrderLine);
    OBDal.getInstance().save(testOrder);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(testOrder);
    OBDal.getInstance().refresh(testOrderLine);
    return testOrderLine;
  }

  private void insertRelation(OrderLine serviceOrderLine, OrderLine orderLine, BigDecimal amount) {
    OrderlineServiceRelation osr = OBProvider.getInstance().get(OrderlineServiceRelation.class);
    osr.setAmount(amount);
    osr.setOrderlineRelated(orderLine);
    osr.setSalesOrderLine(serviceOrderLine);
    OBDal.getInstance().save(osr);
  }
}
