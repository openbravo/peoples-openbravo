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
 * All portions are Copyright (C) 2016-2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.cancelandreplace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.openbravo.base.weld.test.ParameterCdiTest;
import org.openbravo.base.weld.test.ParameterCdiTestRule;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceUtils;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.test.base.TestConstants.Roles;
import org.openbravo.test.base.TestConstants.Users;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData1;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData10;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData11;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData2;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData3;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData4;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData5;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData6;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData7;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData8;
import org.openbravo.test.cancelandreplace.data.CancelAndReplaceTestData9;

/**
 * Tests cases to check Cancel and Replace development
 * 
 * 
 */
public class CancelAndReplaceTest extends WeldBaseTest {
  private static final Logger log = LogManager.getLogger();
  // Sales order: 50017
  private static final String SALESORDER_ID = "F1AAB8C608AA434C9FC7FC1D685BA016";
  // Goods Shipment: 500014
  private static final String M_INOUT_ID = "09658144E1AF40AC81A3E5F5C3D0F132";
  // Organization Spain
  private static final String ORGANIZATION_ID = "357947E87C284935AD1D783CF6F099A1";

  public CancelAndReplaceTest() {
  }

  public static final List<CancelAndReplaceTestData> PARAMS = Arrays.asList(
      new CancelAndReplaceTestData1(), new CancelAndReplaceTestData2(),
      new CancelAndReplaceTestData3(), new CancelAndReplaceTestData4(),
      new CancelAndReplaceTestData5(), new CancelAndReplaceTestData6(),
      new CancelAndReplaceTestData7(), new CancelAndReplaceTestData8(),
      new CancelAndReplaceTestData9(), new CancelAndReplaceTestData10(),
      new CancelAndReplaceTestData11());

  /** Defines the values the parameter will take. */
  @Rule
  public ParameterCdiTestRule<CancelAndReplaceTestData> parameterValuesRule = new ParameterCdiTestRule<>(
      PARAMS);

  /** This field will take the values defined by parameterValuesRule field. */
  private @ParameterCdiTest CancelAndReplaceTestData parameter;

  /**
   * Verifies Cancel and Replace functionality API. Clone and existing Order. Click on Cancel and
   * Replace process that will create a new Order in temporary status. Update the Order depending on
   * the test executed and finally confirm this order. Different check points have been added in
   * each stage to verify the results of the processes.
   */
  @Test
  public void CancelAndReplaceTests() {
    // Set QA context
    OBContext.setAdminMode();
    try {
      OBContext.setOBContext(Users.OPENBRAVO, Roles.QA_ADMIN_ROLE, QA_TEST_CLIENT_ID,
          ORGANIZATION_ID);

      Order oldOrder = CancelAndReplaceTestUtils.cloneAndCompleteOrder(SALESORDER_ID, parameter);

      // Deliver old order if the test is for fully or partially delivered orders
      if (parameter.getOldOrder().isDelivered()) {
        CancelAndReplaceTestUtils.createShipmentFromOrder(oldOrder, M_INOUT_ID, parameter);
        OBDal.getInstance().refresh(oldOrder);
      }

      // Pay old order if the test is for fully or partially paid orders
      if (parameter.isOrderPaid()) {
        CancelAndReplaceTestUtils.createOrderPayment(oldOrder, parameter);
        OBDal.getInstance().refresh(oldOrder);
      }

      // Activate "Create netting shipment on Cancel and Replace" and
      // "Cancel and Replace - Associate shipment lines to new ticket" depending on the test
      boolean createNettingGoodsShipment = CancelAndReplaceTestUtils
          .isPreferenceEnabled(CancelAndReplaceUtils.CREATE_NETTING_SHIPMENT);

      if (parameter.isActivateNettingGoodsShipmentPref() && !createNettingGoodsShipment) {
        Preferences.setPreferenceValue(CancelAndReplaceUtils.CREATE_NETTING_SHIPMENT, "Y", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            null, null, null);
      } else if (!parameter.isActivateNettingGoodsShipmentPref() && createNettingGoodsShipment) {
        Preferences.setPreferenceValue(CancelAndReplaceUtils.CREATE_NETTING_SHIPMENT, "N", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            null, null, null);
      }

      boolean associateShipmentToNewReceipt = CancelAndReplaceTestUtils
          .isPreferenceEnabled(CancelAndReplaceUtils.ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET);

      if (parameter.isActivateAssociateNettingGoodsShipmentPref()) {
        if (!associateShipmentToNewReceipt) {
          Preferences.setPreferenceValue(CancelAndReplaceUtils.ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET,
              "Y", true, OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
              null, null, null);
        }
      } else if (!parameter.isActivateAssociateNettingGoodsShipmentPref()
          && associateShipmentToNewReceipt) {
        Preferences.setPreferenceValue(CancelAndReplaceUtils.ASSOCIATE_SHIPMENT_TO_REPLACE_TICKET,
            "N", true, OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            null, null, null);
      }

      // Create the new replacement order
      Order newOrder = CancelAndReplaceUtils.createReplacementOrder(oldOrder);

      log.debug("New order Created:{}", newOrder.getDocumentNo());
      log.debug(parameter.getTestDescription());

      // Set Quantity to the orderline of the new order in temporary Status
      OrderLine orderLine = newOrder.getOrderLineList().get(0);
      orderLine.setOrderedQuantity(parameter.getNewOrder().getLines()[0].getOrderedQuantity());
      OBDal.getInstance().save(orderLine);
      OBDal.getInstance().flush();

      // Cancel and Replace Sales Order
      CancelAndReplaceUtils.cancelAndReplaceOrder(newOrder.getId(), null, true);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

      oldOrder = OBDal.getInstance().get(Order.class, oldOrder.getId());
      newOrder = OBDal.getInstance().get(Order.class, newOrder.getId());
      Order inverseOrder = oldOrder.getOrderCancelledorderList().get(0);

      // Sales Orders Grand Total Amounts
      CancelAndReplaceTestUtils.assertOrderHeader(oldOrder, parameter.getOldOrder());
      CancelAndReplaceTestUtils.assertOrderHeader(inverseOrder, parameter.getInverseOrder());
      CancelAndReplaceTestUtils.assertOrderHeader(newOrder, parameter.getNewOrder());

      // Relations between orders
      assertEquals(newOrder.getReplacedorder().getId(), oldOrder.getId());
      assertEquals(inverseOrder.getCancelledorder().getId(), oldOrder.getId());
      assertEquals(oldOrder.getReplacementorder().getId(), newOrder.getId());

      // Sales Orders Received and Outstanding payments
      CancelAndReplaceTestUtils.assertOrderPayment(oldOrder, parameter.getOldOrder());
      CancelAndReplaceTestUtils.assertOrderPayment(inverseOrder, parameter.getInverseOrder());
      CancelAndReplaceTestUtils.assertOrderPayment(newOrder, parameter.getNewOrder());

      // Assert Lines
      OrderLine oldOrderLine = oldOrder.getOrderLineList().get(0);
      OrderLine inverseOrderLine = inverseOrder.getOrderLineList().get(0);
      OrderLine newOrderLine = newOrder.getOrderLineList().get(0);

      CancelAndReplaceTestUtils.assertOrderLine(oldOrderLine,
          parameter.getOldOrder().getLines()[0]);
      CancelAndReplaceTestUtils.assertOrderLine(inverseOrderLine,
          parameter.getInverseOrder().getLines()[0]);
      CancelAndReplaceTestUtils.assertOrderLine(newOrderLine,
          parameter.getNewOrder().getLines()[0]);
    } catch (Exception e) {
      log.error("Error when executing: " + parameter.getTestDescription(), e);
      assertFalse(true);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
