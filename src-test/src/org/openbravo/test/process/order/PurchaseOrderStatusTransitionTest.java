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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.test.process.order;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.Process;
import org.openbravo.client.application.ProcessAccess;
import org.openbravo.common.actionhandler.ProcessPurchaseOrderUtility;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.test.base.OBBaseTest;

/**
 * Tests cases to check purchase order status
 */
public class PurchaseOrderStatusTransitionTest extends OBBaseTest {

  final static private Logger log = LogManager.getLogger();

  // User Openbravo
  private final String USER_ID = "100";
  // Client QA Testing
  private final String CLIENT_ID = "4028E6C72959682B01295A070852010D";
  // Organization Spain
  private final String ORGANIZATION_ID = "357947E87C284935AD1D783CF6F099A1";
  // Role QA Testing Admin
  private final String ROLE_ID = "4028E6C72959682B01295A071429011E";
  // Purchase Order: 800010
  private final String PURCHASEORDER_ID = "2C9CEDC0761A41DCB276A5124F8AAA90";

  /**
   * Test to check Booked Status when Purchase Order is in Pending Approval status and action is
   * Approve.
   */

  @Test
  public void testBookedPurchaseOrderStatus() {
    Order testOrder = createOrderinPendingApprovalStatus();
    createRoleAcessForProcessDefinition(ROLE_ID);
    ProcessPurchaseOrderUtility.manageDocumentStatusBasedOnAction(testOrder,
        ProcessPurchaseOrderUtility.DOCACTION_APPROVE, ROLE_ID);
    OBDal.getInstance().refresh(testOrder);
    assertThat("Purchase Order Status is Booked': ", testOrder.getDocumentStatus(),
        equalTo(ProcessPurchaseOrderUtility.DOCSTATUS_BOOKED));
  }

  /**
   * Test to check Rejected Status when Purchase Order is in Pending Approval status and action is
   * Reject.
   */
  @Test
  public void testRejectedPurchaseOrderStatus() {
    Order testOrder = createOrderinPendingApprovalStatus();
    createRoleAcessForProcessDefinition(ROLE_ID);
    ProcessPurchaseOrderUtility.manageDocumentStatusBasedOnAction(testOrder,
        ProcessPurchaseOrderUtility.DOCACTION_REJECT, ROLE_ID);
    OBDal.getInstance().refresh(testOrder);
    assertThat("Purchase Order Status is Rejected': ", testOrder.getDocumentStatus(),
        equalTo(ProcessPurchaseOrderUtility.DOCSTATUS_REJECTED));
  }

  /**
   * Test to check Draft Status when Purchase Order is in Rejected Status and action is Reactivate.
   */
  @Test
  public void testDraftPurchaseOrderStatus() {
    Order testOrder = createOrderinRejectedStatus();
    ProcessPurchaseOrderUtility.manageDocumentStatusBasedOnAction(testOrder,
        ProcessPurchaseOrderUtility.DOCACTION_REACTIVATE, ROLE_ID);
    OBDal.getInstance().refresh(testOrder);
    assertThat("Purchase Order Status is Draft': ", testOrder.getDocumentStatus(),
        equalTo(ProcessPurchaseOrderUtility.DOCSTATUS_DRAFT));
  }

  /**
   * Test to check Closed Status when Purchase Order is in Rejected Status and action is Close.
   */

  @Test
  public void testClosedPurchaseOrderStatus() {
    Order testOrder = createOrderinRejectedStatus();
    ProcessPurchaseOrderUtility.manageDocumentStatusBasedOnAction(testOrder,
        ProcessPurchaseOrderUtility.DOCACTION_CLOSE, ROLE_ID);
    OBDal.getInstance().refresh(testOrder);
    assertThat("Purchase Order Status is Closed': ", testOrder.getDocumentStatus(),
        equalTo(ProcessPurchaseOrderUtility.DOCSTATUS_CLOSE));
  }

  /**
   * Create Order in Pending Approval Status
   */

  private Order createOrderinPendingApprovalStatus() {
    // Set QA context
    OBContext.setOBContext(USER_ID, ROLE_ID, CLIENT_ID, ORGANIZATION_ID);
    Order testOrder = createOrder();
    removeRoleAcessForProcessDefinition(ROLE_ID);
    ProcessPurchaseOrderUtility.manageDocumentStatusBasedOnAction(testOrder,
        ProcessPurchaseOrderUtility.DOCACTION_BOOK, ROLE_ID);
    OBDal.getInstance().refresh(testOrder);
    assertThat("Purchase Order Status is Pending Approval': ", testOrder.getDocumentStatus(),
        equalTo(ProcessPurchaseOrderUtility.DOCSTATUS_PENDINGAPPROVAL));
    return testOrder;
  }

  /**
   * Create Order in Rejected Status
   */

  private Order createOrderinRejectedStatus() {
    Order testOrder = createOrderinPendingApprovalStatus();
    createRoleAcessForProcessDefinition(ROLE_ID);
    ProcessPurchaseOrderUtility.manageDocumentStatusBasedOnAction(testOrder,
        ProcessPurchaseOrderUtility.DOCACTION_REJECT, ROLE_ID);
    OBDal.getInstance().refresh(testOrder);
    assertThat("Purchase Order Status is Rejected': ", testOrder.getDocumentStatus(),
        equalTo(ProcessPurchaseOrderUtility.DOCSTATUS_REJECTED));
    return testOrder;
  }

  /**
   * Create Purchase Order by making copy from existing PO.
   */

  private Order createOrder() {
    Order order = OBDal.getInstance().get(Order.class, PURCHASEORDER_ID);
    Order testOrder = (Order) DalUtil.copy(order, false);
    String documentNo = getNextDocNoForPurchaseOrder(order.getDocumentNo());
    testOrder.setDocumentNo(documentNo);
    testOrder.setOrderDate(new Date());
    testOrder.setScheduledDeliveryDate(new Date());
    testOrder.setSummedLineAmount(BigDecimal.ZERO);
    testOrder.setGrandTotalAmount(BigDecimal.ZERO);
    testOrder.setSkipffmvalidation(true);
    OBDal.getInstance().save(testOrder);
    for (OrderLine orderLine : order.getOrderLineList()) {
      OrderLine testOrderLine = (OrderLine) DalUtil.copy(orderLine, false);
      testOrderLine.setSalesOrder(testOrder);
      OBDal.getInstance().save(testOrderLine);
      testOrder.getOrderLineList().add(testOrderLine);
      OBDal.getInstance().save(testOrder);
    }
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(testOrder);

    log.debug("Order Created:" + testOrder.getDocumentNo());

    return testOrder;
  }

  /**
   * Returns the next Document Number for the Purchase Order with the given Document No.
   */
  private String getNextDocNoForPurchaseOrder(String testDocNo) {
    OBCriteria<Order> obc = OBDal.getInstance().createCriteria(Order.class);
    obc.add(Restrictions.like(Order.PROPERTY_DOCUMENTNO, testDocNo + "-%"));
    return testDocNo + "-" + obc.list().size();
  }

  /**
   * Create access to Process Definition: Process Purchase Order Action Handler from Role used in
   * the test
   */

  private void createRoleAcessForProcessDefinition(String roleId) {
    if (!ProcessPurchaseOrderUtility.roleHasPrivilegesToProcessPurchaseOrder(roleId)) {
      try {
        OBContext.setAdminMode(false);
        ProcessAccess processDefinitionAccess = OBProvider.getInstance().get(ProcessAccess.class);
        processDefinitionAccess.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
        processDefinitionAccess.setRole(OBDal.getInstance().get(Role.class, roleId));
        processDefinitionAccess.setObuiappProcess(OBDal.getInstance()
            .get(Process.class, ProcessPurchaseOrderUtility.PURCHASE_ORDER_PROCESS_ACCESS_ID));
        OBDal.getInstance().save(processDefinitionAccess);
        OBDal.getInstance().flush();
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }

  /**
   * Remove access to Process Definition: Process Purchase Order Action Handler from Role used in
   * the test
   */

  private void removeRoleAcessForProcessDefinition(String roleId) {
    try {
      OBContext.setAdminMode(false);
      Role role = OBDal.getInstance().get(Role.class, roleId);
      OBDal.getInstance().refresh(role);
      List<ProcessAccess> processDefnAccessList = role.getOBUIAPPProcessAccessList()
          .stream()
          .filter(pda -> StringUtils.equals(pda.getObuiappProcess().getId(),
              ProcessPurchaseOrderUtility.PURCHASE_ORDER_PROCESS_ACCESS_ID))
          .collect(Collectors.toList());
      for (ProcessAccess processDefnAccess : processDefnAccessList) {
        OBDal.getInstance().remove(processDefnAccess);
        role.getOBUIAPPProcessAccessList().remove(processDefnAccess);
        OBDal.getInstance().save(role);
        OBDal.getInstance().flush();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
