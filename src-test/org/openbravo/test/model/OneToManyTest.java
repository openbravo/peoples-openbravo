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

package org.openbravo.test.model;

import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.test.base.BaseTest;

/**
 * Test cases for one-to-many support
 * 
 * @author iperdomo
 */
public class OneToManyTest extends BaseTest {

  private String lineId;

  public void testAccessChildCollection() {
    setErrorOccured(true);
    setUserContext("1000001");
    final OBCriteria<Order> order = OBDal.getInstance().createCriteria(Order.class);
    // order.add(Expression.eq("id", "1000019"));
    for (final Order o : order.list()) {
      System.out.println("Order: " + o.toString());
      for (final OrderLine l : o.getOrderLineList()) {
        System.out.println("Line: " + l.toString());
      }
      System.out.println("-----");
    }
    setErrorOccured(false);
  }

  public void testDeleteChild() {

    setErrorOccured(true);
    setUserContext("1000001");
    final OBCriteria<Order> orders = OBDal.getInstance().createCriteria(Order.class);
    orders.add(Expression.eq(Order.PROPERTY_DOCUMENTSTATUS, "DR")); // Draft
    // document

    for (final Order o : orders.list()) {
      System.out.println("Order: " + o.get(Order.PROPERTY_DOCUMENTNO) + " - no. lines: "
          + o.getOrderLineList().size());

      if (o.getOrderLineList().size() > 0) {
        final OrderLine l = o.getOrderLineList().get(0);
        lineId = l.getId();
        System.out.println("OrderLine to remove: " + l.toString());
        o.getOrderLineList().remove(l); // or
        // OBDal.getInstance().remove(
        // l);
      }
    }

    setErrorOccured(false);
  }

  public void testConfirmDeleted() {
    setErrorOccured(true);
    setUserContext("1000001");

    final OBCriteria<OrderLine> lines = OBDal.getInstance().createCriteria(OrderLine.class);
    lines.add(Expression.eq(OrderLine.PROPERTY_ID, lineId));

    assertEquals(0, lines.list().size());

    setErrorOccured(false);
  }

  public void testAddChild() throws Exception {
    setErrorOccured(true);
    setUserContext("1000001");
    final OBCriteria<BusinessPartner> bpartners = OBDal.getInstance().createCriteria(
        BusinessPartner.class);
    bpartners.add(Expression.eq(BusinessPartner.PROPERTY_SEARCHKEY, "mafalda"));

    if (bpartners.list().size() > 0) {
      final BusinessPartner partner = bpartners.list().get(0);
      final User user1 = OBProvider.getInstance().get(User.class);
      user1.setName("test");
      user1.setEmail("email@domain.com");
      user1.setActive(true);
      user1.setFirstName("Firstname");
      user1.setLastName("Lastname");
      user1.setBusinessPartner(partner);
      user1.setClient(partner.getClient());
      user1.setOrganization(partner.getOrganization());
      // adding the user1 to the users collection
      final int count = partner.getADUserList().size();
      partner.getADUserList().add(user1);
      assertEquals(count + 1, partner.getADUserList().size());
    } else
      throw new Exception("malfalda not found in business partners list");
    setErrorOccured(false);
  }
}
