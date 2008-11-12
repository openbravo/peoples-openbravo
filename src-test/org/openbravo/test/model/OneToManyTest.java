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
	final OBCriteria<Order> order = OBDal.getInstance().createCriteria(
		Order.class);
	// order.add(Expression.eq("id", "1000019"));
	for (Order o : order.list()) {
	    System.out.println("Order: " + o.toString());
	    for (OrderLine l : o.getOrderLineList()) {
		System.out.println("Line: " + l.toString());
	    }
	    System.out.println("-----");
	}
	setErrorOccured(false);
    }

    public void testDeleteChild() {

	setErrorOccured(true);
	setUserContext("1000001");
	final OBCriteria<Order> orders = OBDal.getInstance().createCriteria(
		Order.class);
	orders.add(Expression.eq(Order.PROPERTY_DOCSTATUS, "DR")); // Draft
	// document

	for (Order o : orders.list()) {
	    System.out.println("Order: " + o.get(Order.PROPERTY_DOCUMENTNO)
		    + " - no. lines: " + o.getOrderLineList().size());

	    if (o.getOrderLineList().size() > 0) {
		OrderLine l = o.getOrderLineList().get(0);
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

	final OBCriteria<OrderLine> lines = OBDal.getInstance().createCriteria(
		OrderLine.class);
	lines.add(Expression.eq(OrderLine.PROPERTY_ID, lineId));

	assertEquals(0, lines.list().size());

	setErrorOccured(false);
    }

    public void testAddChild() throws Exception {
	setErrorOccured(true);
	setUserContext("1000001");
	final OBCriteria<BusinessPartner> bpartners = OBDal.getInstance()
		.createCriteria(BusinessPartner.class);
	bpartners.add(Expression.eq(BusinessPartner.PROPERTY_VALUE, "mafalda"));

	if (bpartners.list().size() > 0) {
	    BusinessPartner partner = bpartners.list().get(0);
	    final User user1 = OBProvider.getInstance().get(User.class);
	    user1.setName("test");
	    user1.setEmail("email@domain.com");
	    user1.setActive(true);
	    user1.setFirstname("Firstname");
	    user1.setLastname("Lastname");
	    user1.setBusinessPartner(partner);
	    user1.setClient(partner.getClient());
	    user1.setOrganization(partner.getOrganization());
	    // adding the user1 to the users collection
	    int count = partner.getUserList().size();
	    partner.getUserList().add(user1);
	    assertEquals(count + 1, partner.getUserList().size());
	} else
	    throw new Exception("malfalda not found in business partners list");
	setErrorOccured(false);
    }
}
