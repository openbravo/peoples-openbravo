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
 * All portions are Copyright (C) 2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.cancelandreplace;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.test.ParameterCdiTest;
import org.openbravo.base.weld.test.ParameterCdiTestRule;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceUtils;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.test.base.TestConstants.Roles;
import org.openbravo.test.base.TestConstants.Users;
import org.openbravo.test.cancelandreplace.data.CancelAndMultipleReplaceTestData;
import org.openbravo.test.cancelandreplace.data.CancelAndMultipleReplaceTestData1;
import org.openbravo.test.cancelandreplace.data.CancelAndMultipleReplaceTestData2;
import org.openbravo.test.cancelandreplace.data.CancelAndMultipleReplaceTestData3;
import org.openbravo.test.cancelandreplace.data.CancelAndMultipleReplaceTestData4;
import org.openbravo.test.cancelandreplace.data.CancelAndMultipleReplaceTestData5;
import org.openbravo.test.cancelandreplace.data.CancelAndMultipleReplaceTestData6;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceOrderTestData;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceOrderTestData.Line;

/**
 * Tests cases to check Cancel and Replace 1-N
 * 
 */
public class CancelAndMultipleReplaceTest extends WeldBaseTest {

  private Logger log = LogManager.getLogger();

  // Organization Spain
  private static final String ORGANIZATION_ID = "357947E87C284935AD1D783CF6F099A1";
  // Sales order: 50011
  private static final String SALESORDER_ID = "80AAF8AC57EC4A7EB2F85B3B2675F88C";
  // Goods Shipment: 500014
  private static final String M_INOUT_ID = "09658144E1AF40AC81A3E5F5C3D0F132";

  static final List<CancelAndMultipleReplaceTestData> PARAMS = Arrays.asList(
      new CancelAndMultipleReplaceTestData1(), new CancelAndMultipleReplaceTestData2(),
      new CancelAndMultipleReplaceTestData3(), new CancelAndMultipleReplaceTestData4(),
      new CancelAndMultipleReplaceTestData5(), new CancelAndMultipleReplaceTestData6());

  /** Defines the values the parameter will take. */
  @Rule
  public ParameterCdiTestRule<CancelAndMultipleReplaceTestData> parameterValuesRule = new ParameterCdiTestRule<>(
      PARAMS);

  /** This field will take the values defined by parameterValuesRule field. */
  private @ParameterCdiTest CancelAndMultipleReplaceTestData testData;

  /**
   * Verifies Cancel and Replace functionality API. Clone and existing Order. Click on Cancel and
   * Replace process that will create a new Order in temporary status. Update the Order depending on
   * the test executed and finally confirm this order. Different check points have been added in
   * each stage to verify the results of the processes.
   */
  @Test
  public void cancelAndMultipleReplaceTest() {
    // Set QA context
    OBContext.setAdminMode();
    try {
      OBContext.setOBContext(Users.OPENBRAVO, Roles.QA_ADMIN_ROLE, QA_TEST_CLIENT_ID,
          ORGANIZATION_ID);

      VariablesSecureApp vars = new VariablesSecureApp(Users.OPENBRAVO, QA_TEST_CLIENT_ID,
          ORGANIZATION_ID, Roles.QA_ADMIN_ROLE,
          OBContext.getOBContext().getLanguage().getLanguage());
      RequestContext.get().setVariableSecureApp(vars);

      Order oldOrder = CancelAndReplaceTestUtils.cloneAndCompleteOrder(SALESORDER_ID, testData);

      // Deliver old order if the test is for fully or partially delivered orders
      if (testData.getOldOrder().isDelivered()) {
        CancelAndReplaceTestUtils.createShipmentFromOrder(oldOrder, M_INOUT_ID, testData);
        OBDal.getInstance().refresh(oldOrder);
      }

      // Pay old order if the test is for fully or partially paid orders
      if (testData.isOrderPaid()) {
        CancelAndReplaceTestUtils.createOrderPayment(oldOrder, testData);
        OBDal.getInstance().refresh(oldOrder);
      }

      // Activate "Create netting shipment on Cancel and Replace" and
      // "Cancel and Replace - Associate shipment lines to new ticket" depending on the test
      boolean createNettingGoodsShipment = CancelAndReplaceTestUtils
          .isPreferenceEnabled(CancelAndReplaceUtils.CREATE_NETTING_SHIPMENT);

      if (testData.isActivateNettingGoodsShipmentPref() && !createNettingGoodsShipment) {
        Preferences.setPreferenceValue(CancelAndReplaceUtils.CREATE_NETTING_SHIPMENT, "Y", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            null, null, null);
      } else if (!testData.isActivateNettingGoodsShipmentPref() && createNettingGoodsShipment) {
        Preferences.setPreferenceValue(CancelAndReplaceUtils.CREATE_NETTING_SHIPMENT, "N", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            null, null, null);
      }

      boolean associateShipmentToNewReceipt = CancelAndReplaceTestUtils
          .isPreferenceEnabled(CancelAndReplaceUtils.ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET);

      if (testData.isActivateAssociateNettingGoodsShipmentPref()) {
        if (!associateShipmentToNewReceipt) {
          Preferences.setPreferenceValue(CancelAndReplaceUtils.ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET,
              "Y", true, OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
              null, null, null);
        }
      } else if (!testData.isActivateAssociateNettingGoodsShipmentPref()
          && associateShipmentToNewReceipt) {
        Preferences.setPreferenceValue(CancelAndReplaceUtils.ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET,
            "N", true, OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            null, null, null);
      }

      // Create the new replacement order
      List<Order> newOrders = new ArrayList<>();
      newOrders.add(CancelAndReplaceUtils.createReplacementOrder(oldOrder));
      newOrders.add(CancelAndReplaceUtils.createReplacementOrder(oldOrder));

      log.debug("New orders Created:{}, {}", newOrders.get(0).getDocumentNo(),
          newOrders.get(1).getDocumentNo());
      log.debug(testData.getTestDescription());

      updateNewOrders(newOrders);
      OBDal.getInstance().flush();

      // Cancel and Replace Sales Order
      Set<String> newOrderIdSet = newOrders.stream().map(Order::getId).collect(Collectors.toSet());
      CancelAndReplaceUtils.cancelAndReplaceOrder(oldOrder.getId(), newOrderIdSet,
          oldOrder.getOrganization().getId(), null, false);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

      oldOrder = getOrder(oldOrder.getId());
      Order inverseOrder = oldOrder.getOrderCancelledorderList().get(0);
      newOrders = refreshOrders(newOrders);

      // Sales Orders Grand Total Amounts
      CancelAndReplaceTestUtils.assertOrderHeader(oldOrder, testData.getOldOrder());
      CancelAndReplaceTestUtils.assertOrderHeader(inverseOrder, testData.getInverseOrder());
      for (int i = 0; i < newOrders.size(); i++) {
        CancelAndReplaceTestUtils.assertOrderHeader(newOrders.get(i),
            testData.getNewOrders().get(i));
      }

      // Relations between orders
      final String oldOrderId = oldOrder.getId();
      newOrders.forEach(newOrder -> assertThat("Wrong Cancelled Order id",
          newOrder.getReplacedorder().getId(), comparesEqualTo(oldOrderId)));
      assertThat("Wrong Cancelled Order id", inverseOrder.getCancelledorder().getId(),
          comparesEqualTo(oldOrderId));
      // FIXME Check if it's valid
      // assertNull("Replacement Order should be null", oldOrder.getReplacementorder());

      // Sales Orders Received and Outstanding payments
      CancelAndReplaceTestUtils.assertOrderPayment(oldOrder, testData.getOldOrder());
      CancelAndReplaceTestUtils.assertOrderPayment(inverseOrder, testData.getInverseOrder());
      for (int i = 0; i < newOrders.size(); i++) {
        CancelAndReplaceTestUtils.assertOrderPayment(newOrders.get(i),
            testData.getNewOrders().get(i));
      }

      // Assert Lines
      assertOrderLines(oldOrder.getOrderLineList(), testData.getOldOrder());
      assertOrderLines(inverseOrder.getOrderLineList(), testData.getInverseOrder());
      for (int i = 0; i < newOrders.size(); i++) {
        assertOrderLines(newOrders.get(i).getOrderLineList(), testData.getNewOrders().get(i));
      }
    } catch (Exception e) {
      log.error("Error when executing: " + testData.getTestDescription(), e);
      assertFalse(true);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private List<Order> refreshOrders(List<Order> newOrders) {
    List<Order> orders = new ArrayList<>(2);
    newOrders.forEach(order -> orders.add(OBDal.getInstance().get(Order.class, order.getId())));
    return orders;
  }

  private void assertOrderLines(List<OrderLine> lines,
      CancelAndReplaceOrderTestData orderTestData) {
    for (int i = 0; i < lines.size(); i++) {
      CancelAndReplaceTestUtils.assertOrderLine(lines.get(i), orderTestData.getLines()[i]);
    }
  }

  private void updateNewOrders(List<Order> newOrders) {
    for (int i = 0; i < newOrders.size(); i++) {
      updateOrder(i, newOrders.get(i), testData.getNewOrders().get(i).getLines()[0]);
    }
  }

  private void updateOrder(int index, Order order, Line lineData) {
    OrderLine orderLine = null;
    for (int i = 0; i < order.getOrderLineList().size(); i++) {
      OrderLine line = order.getOrderLineList().get(i);
      if (i == index) {
        orderLine = line;
        orderLine.setOrderedQuantity(lineData.getOrderedQuantity());
        OBDal.getInstance().save(orderLine);
      } else {
        OBDal.getInstance().remove(line);
      }
    }
    order.getOrderLineList().clear();
    order.getOrderLineList().add(orderLine);
    OBDal.getInstance().save(order);
  }

  private Order getOrder(String orderId) {
    return OBDal.getInstance().get(Order.class, orderId);
  }
}
