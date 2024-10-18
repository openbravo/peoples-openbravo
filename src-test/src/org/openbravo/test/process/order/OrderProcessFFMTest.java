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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.process.order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.test.base.OBBaseTest;

/**
 * Tests cases to check c_order_post1 executions with Free Freigh Minimum checks
 * 
 */
@RunWith(Parameterized.class)
public class OrderProcessFFMTest extends OBBaseTest {
  private static final Logger log = LogManager.getLogger();

  private static final String SALESORDER_ID = "D8783EA69C3C4671A8AB0882D26FD991";

  private static final String PRICEINCLUDINGTAXES_PRICELIST_SALES = "4028E6C72959682B01295ADC1769021B";

  private static final String LINE_TAX = "37222B873B6447D7A1FEC837799B222F";

  private static final String ORDER_COMPLETE_PROCEDURE_NAME = "c_order_post1";

  private String testNumber;
  private String testDescription;
  private boolean meetFFM;
  private boolean duplicateLines;
  private boolean skipFFMValidation;
  private boolean shouldThrow;
  private BigDecimal amount;
  private BigDecimal orderedQty;

  public OrderProcessFFMTest(String testNumber, String testDescription, boolean meetFFM,
      boolean duplicateLines, boolean skipFFMValidation, boolean shouldThrow, BigDecimal amount,
      BigDecimal orderedQty) {
    this.testNumber = testNumber;
    this.testDescription = testDescription;
    this.meetFFM = meetFFM;
    this.duplicateLines = duplicateLines;
    this.skipFFMValidation = skipFFMValidation;
    this.shouldThrow = shouldThrow;
    this.amount = amount;
    this.orderedQty = orderedQty;
  }

  @Parameters(name = "idx:{0} name:{1}")
  public static Collection<Object[]> params() {
    Object[][] params = new Object[][] {
        { "01", "Throw exception if FFM is not met", false, false, false, true, BigDecimal.ZERO,
            BigDecimal.ZERO }, //
        { "02", "Check Order is OK if FFM is met", true, false, false, false, new BigDecimal("50"),
            new BigDecimal("10") }, //
        { "03", "Check Order is OK if FFM validation is skipped", false, false, true, false,
            new BigDecimal("20"), new BigDecimal("4") }, //
        { "04", "Check Order is OK if several lines with the same product meet FFM", false, true,
            false, false, new BigDecimal("40"), new BigDecimal("4") } //
    };
    return Arrays.asList(params);
  }

  @Before
  public void executeBeforeTests() {
    setQAAdminContext();
  }

  @Test
  public void testFFMCheck() {
    Order testOrder = createOrder(testNumber, meetFFM, duplicateLines);
    if (this.shouldThrow) {
      assertThrows(Exception.class, () -> {
        updateOrderStatus(testOrder, skipFFMValidation);
      });
    } else {
      updateOrderStatus(testOrder, skipFFMValidation);
      assertOrder(testOrder);
    }

  }

  private Order createOrder(String docNumber, boolean shouldMeetFFM, boolean shouldDuplicateLines) {
    Order order = OBDal.getInstance().get(Order.class, SALESORDER_ID);
    Order testOrder = (Order) DalUtil.copy(order, false);
    testOrder.setDocumentNo(docNumber);
    testOrder.setSummedLineAmount(BigDecimal.ZERO);
    testOrder.setGrandTotalAmount(BigDecimal.ZERO);
    testOrder.setPriceIncludesTax(true);
    testOrder.setPriceList(
        OBDal.getInstance().getProxy(PriceList.class, PRICEINCLUDINGTAXES_PRICELIST_SALES));
    OBDal.getInstance().save(testOrder);

    order.getOrderLineList().forEach(line -> {
      OrderLine newLine = (OrderLine) DalUtil.copy(line, false);
      newLine.setSalesOrder(testOrder);
      newLine.setBusinessPartner(testOrder.getBusinessPartner());
      newLine.setGrossUnitPrice(BigDecimal.TEN);
      newLine.setGrossListPrice(BigDecimal.TEN);
      newLine.setBaseGrossUnitPrice(BigDecimal.TEN);

      newLine.setTax(OBDal.getInstance().getProxy(TaxRate.class, LINE_TAX));
      if (shouldMeetFFM) {
        newLine.setOrderedQuantity(new BigDecimal("10"));
      } else {
        newLine.setOrderedQuantity(new BigDecimal("4"));
      }
      testOrder.getOrderLineList().add(newLine);
      OBDal.getInstance().save(newLine);
      OBDal.getInstance().flush();
      if (shouldDuplicateLines) {
        OrderLine duplicateProductLine = (OrderLine) DalUtil.copy(newLine, false);
        testOrder.getOrderLineList().add(duplicateProductLine);
        OBDal.getInstance().save(duplicateProductLine);
        OBDal.getInstance().flush();
      }
    });

    OBDal.getInstance().save(testOrder);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(testOrder);

    log.debug(testDescription);
    log.debug("Order Created: %s", testOrder.getDocumentNo());

    return testOrder;
  }

  private void updateOrderStatus(Order testOrder, boolean skipFFMCheck) {
    testOrder.setDocumentStatus("DR");
    testOrder.setDocumentAction("CO");
    testOrder.setSkipffmvalidation(skipFFMCheck);

    OBDal.getInstance().save(testOrder);
    OBDal.getInstance().flush();
    processOrder(testOrder);
    OBDal.getInstance().refresh(testOrder);
  }

  private void processOrder(Order testOrder) {
    final List<Object> params = new ArrayList<>();
    params.add(null);
    params.add(testOrder.getId());
    CallStoredProcedure.getInstance()
        .call(ORDER_COMPLETE_PROCEDURE_NAME, params, null, true, false);

  }

  private void assertOrder(Order testOrder) {
    assertOrderIsCompleted(testOrder, amount, orderedQty);
  }

  private void assertOrderIsCompleted(Order testOrder, BigDecimal totalAmount,
      BigDecimal orderedQuantity) {
    assertOrderHeader(testOrder, totalAmount);
    assertOrderLines(testOrder, orderedQuantity);
  }

  private void assertOrderLines(Order testOrder, BigDecimal orderedQuantity) {
    testOrder.getOrderLineList().forEach(line -> {
      OBDal.getInstance().refresh(line);
      assertThat("Line ordered quantity should be " + orderedQuantity, line.getOrderedQuantity(),
          comparesEqualTo(orderedQuantity));
    });
  }

  private void assertOrderHeader(Order testOrder, BigDecimal totalAmount) {
    assertThat("Order should be Booked", testOrder.getDocumentStatus(), equalTo("CO"));
    assertThat("Order Total amount should be " + totalAmount, testOrder.getGrandTotalAmount(),
        comparesEqualTo(totalAmount));
  }
}
