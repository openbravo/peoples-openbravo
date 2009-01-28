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

package org.openbravo.test.xml;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.payment.PaymentTermLine;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Test import of data with a business object, adding and removing childs
 * 
 * @author mtaal
 */

public class EntityXMLImportTestBusinessObject extends XMLBaseTest {

    private static int NO_OF_PT = 1;
    private static int NO_OF_PT_LINE = 1 + NO_OF_PT * NO_OF_PT;
    // add NO_OF_PT twice because it was translated to one language
    private static int TOTAL_PT_PTL = NO_OF_PT + NO_OF_PT + NO_OF_PT_LINE;

    private String[] currentPaymentTerms = new String[] { "1000000", "1000001",
	    "1000002", "1000003", "1000004" };

    public void testAPaymentTerm() {
	cleanRefDataLoaded();
	setErrorOccured(true);
	setUserContext("1000001");
	createSavePaymentTerm();
	setErrorOccured(false);
    }

    // export and create in client 100001
    public void testBPaymentTerm() {
	setErrorOccured(true);

	// read from 1000000
	setUserContext("1000001");
	final List<PaymentTerm> pts = getPaymentTerms();
	String xml = getXML(pts);

	System.err.println(xml);

	// there is a unique constraint on name
	xml = xml.replaceAll("</name>", "t</name>");

	// export to client 1000001
	setUserContext("1000019");
	final ImportResult ir = DataImportService.getInstance()
		.importDataFromXML(
			OBDal.getInstance().get(Client.class, "1000001"),
			OBDal.getInstance().get(Organization.class, "1000001"),
			xml);
	if (ir.getException() != null) {
	    ir.getException().printStackTrace(System.err);
	    fail(ir.getException().getMessage());
	}

	assertEquals(TOTAL_PT_PTL, ir.getInsertedObjects().size());
	assertEquals(0, ir.getUpdatedObjects().size());

	setErrorOccured(false);
    }

    // do the same thing again, no updates!
    public void testCPaymentTerm() {
	setErrorOccured(true);

	// read from 1000000
	setUserContext("1000001");
	final List<PaymentTerm> pts = getPaymentTerms();
	String xml = getXML(pts);

	// there is a unique constraint on name
	xml = xml.replaceAll("</name>", "t</name>");

	// export to client 1000001
	setUserContext("1000019");
	final ImportResult ir = DataImportService.getInstance()
		.importDataFromXML(
			OBDal.getInstance().get(Client.class, "1000001"),
			OBDal.getInstance().get(Organization.class, "1000001"),
			xml);
	if (ir.getException() != null) {
	    ir.getException().printStackTrace(System.err);
	    fail(ir.getException().getMessage());
	}

	assertEquals(0, ir.getInsertedObjects().size());
	assertEquals(0, ir.getUpdatedObjects().size());

	setErrorOccured(false);
    }

    // change a child so that it is updated and change a parent
    public void testDPaymentTerm() {
	setErrorOccured(true);

	// read from 1000000
	setUserContext("1000001");
	// make a copy of the paymentterms and their children so that the
	// original db is not updated
	final List<BaseOBObject> pts = DalUtil.copyAll(
		new ArrayList<BaseOBObject>(getPaymentTerms()), false);

	// change some data and export
	final PaymentTerm pt = (PaymentTerm) pts.get(0);
	pt.setName("testtest");
	pt.getFinancialMgmtPaymentTermLineList().get(0).setPaymentRule("R");

	String xml = getXML(pts);
	xml = xml.replaceAll("</name>", "t</name>");

	setUserContext("1000019");
	final ImportResult ir = DataImportService.getInstance()
		.importDataFromXML(
			OBDal.getInstance().get(Client.class, "1000001"),
			OBDal.getInstance().get(Organization.class, "1000001"),
			xml);
	if (ir.getException() != null) {
	    ir.getException().printStackTrace(System.err);
	    fail(ir.getException().getMessage());
	}

	assertEquals(0, ir.getInsertedObjects().size());
	assertEquals(2, ir.getUpdatedObjects().size());
	for (final Object o : ir.getUpdatedObjects()) {
	    assertTrue(o instanceof PaymentTerm || o instanceof PaymentTermLine);
	    if (o instanceof PaymentTermLine) {
		final PaymentTermLine ptl = (PaymentTermLine) o;
		assertTrue(ir.getUpdatedObjects()
			.contains(ptl.getPaymentTerm()));
	    }
	}

	setErrorOccured(false);
    }

    // remove the first payment line of each payment term
    public void testEPaymentTerm() {
	setErrorOccured(true);

	// read from 1000000
	setUserContext("1000001");
	// make a copy of the paymentterms and their children so that the
	// original db is not updated
	final List<BaseOBObject> pts = DalUtil.copyAll(
		new ArrayList<BaseOBObject>(getPaymentTerms()), false);

	for (final BaseOBObject bob : pts) {
	    final PaymentTerm pt = (PaymentTerm) bob;
	    final PaymentTermLine ptl = pt
		    .getFinancialMgmtPaymentTermLineList().get(1);
	    pt.getFinancialMgmtPaymentTermLineList().remove(ptl);
	}

	String xml = getXML(pts);
	// there is a unique constraint on name
	xml = xml.replaceAll("</name>", "t</name>");

	setUserContext("1000019");
	OBContext.getOBContext().setInAdministratorMode(true);
	final ImportResult ir = DataImportService.getInstance()
		.importDataFromXML(
			OBDal.getInstance().get(Client.class, "1000001"),
			OBDal.getInstance().get(Organization.class, "1000001"),
			xml);
	if (ir.getException() != null) {
	    ir.getException().printStackTrace(System.err);
	    fail(ir.getException().getMessage());
	}

	assertEquals(0, ir.getInsertedObjects().size());
	assertEquals(NO_OF_PT, ir.getUpdatedObjects().size());
	for (final Object o : ir.getUpdatedObjects()) {
	    assertTrue(o instanceof PaymentTerm);
	}

	setErrorOccured(false);
    }

    // test that the removal was successfull
    public void testFPaymentTerm() {
	setErrorOccured(true);
	setUserContext("1000019");
	final List<PaymentTerm> pts = getPaymentTerms();
	for (final PaymentTerm pt : pts) {
	    assertEquals(NO_OF_PT_LINE - 1, pt
		    .getFinancialMgmtPaymentTermLineList().size());
	    for (final PaymentTermLine ptl : pt
		    .getFinancialMgmtPaymentTermLineList()) {
		assertTrue(!ptl.getLine().equals(new Integer(1)));
	    }
	}
	setErrorOccured(false);
    }

    // and now add a line!
    public void testGPaymentTerm() {
	setErrorOccured(true);

	// read from 1000000
	setUserContext("1000001");
	// make a copy of the paymentterms and their children so that the
	// original db is not updated
	final List<BaseOBObject> pts = DalUtil.copyAll(
		new ArrayList<BaseOBObject>(getPaymentTerms()), false);

	// add one at the back
	for (final BaseOBObject bob : pts) {
	    final PaymentTerm pt = (PaymentTerm) bob;
	    final PaymentTermLine ptl = (PaymentTermLine) DalUtil.copy(pt
		    .getFinancialMgmtPaymentTermLineList().get(0));
	    ptl.setClient(null);
	    ptl.setOrganization(null);
	    ptl.setLine(NO_OF_PT_LINE);
	    pt.getFinancialMgmtPaymentTermLineList().add(ptl);
	}

	String xml = getXML(pts);
	// System.err.println(xml);
	// there is a unique constraint on name
	xml = xml.replaceAll("</name>", "t</name>");

	setUserContext("1000019");
	OBContext.getOBContext().setInAdministratorMode(true);
	final ImportResult ir = DataImportService.getInstance()
		.importDataFromXML(
			OBDal.getInstance().get(Client.class, "1000001"),
			OBDal.getInstance().get(Organization.class, "1000001"),
			xml);
	if (ir.getException() != null) {
	    ir.getException().printStackTrace(System.err);
	    fail(ir.getException().getMessage());
	}

	// Note that the check is on 2 * NO_OF_PT, because the paymentterm
	// contains 4 paymenttermlines of which 2 new ones
	// per paymentterm, 3 read from the current client, 1 new. In the target
	// database there are only 2 lines per paymentterm
	assertEquals(2 * NO_OF_PT, ir.getInsertedObjects().size());
	assertEquals(NO_OF_PT, ir.getUpdatedObjects().size());
	for (final Object o : ir.getUpdatedObjects()) {
	    assertTrue(o instanceof PaymentTerm);
	}
	for (final Object o : ir.getInsertedObjects()) {
	    assertTrue(o instanceof PaymentTermLine);
	}

	setErrorOccured(false);
    }

    // test that the Addition was successfull
    public void testHPaymentTerm() {
	setErrorOccured(true);
	setUserContext("1000019");
	final List<PaymentTerm> pts = getPaymentTerms();
	for (final PaymentTerm pt : pts) {
	    assertEquals(NO_OF_PT_LINE + 1, pt
		    .getFinancialMgmtPaymentTermLineList().size());
	    int i = 0;
	    for (final PaymentTermLine ptl : pt
		    .getFinancialMgmtPaymentTermLineList()) {
		assertEquals(new Integer(i++), ptl.getLine());
	    }
	}
	setErrorOccured(false);
    }

    // cleans up everything
    public void testZPaymentTerm() {
	setErrorOccured(true);
	setUserContext("1000001");
	final List<PaymentTerm> pts = getPaymentTerms();
	OBContext.getOBContext().setInAdministratorMode(true);
	for (final PaymentTerm pt : pts) {
	    OBDal.getInstance().remove(pt);
	}
	OBDal.getInstance().commitAndClose();

	setUserContext("1000019");
	final List<PaymentTerm> pts2 = getPaymentTerms();
	OBContext.getOBContext().setInAdministratorMode(true);
	for (final PaymentTerm pt : pts2) {
	    OBDal.getInstance().remove(pt);
	}
	setErrorOccured(false);
    }

    private void createSavePaymentTerm() {
	final List<PaymentTerm> result = new ArrayList<PaymentTerm>();
	for (int i = 0; i < NO_OF_PT; i++) {
	    final PaymentTerm source = OBDal.getInstance().get(
		    PaymentTerm.class, "1000000");
	    final PaymentTerm pt = (PaymentTerm) DalUtil.copy(source);
	    pt.setName(pt.getName() + i);
	    pt.setOrganization(OBContext.getOBContext()
		    .getCurrentOrganization());

	    // force new
	    // now add a payment termline
	    for (int j = 0; j < NO_OF_PT_LINE; j++) {
		final PaymentTermLine ptl = OBProvider.getInstance().get(
			PaymentTermLine.class);
		ptl.setExcludetax(true);
		ptl.setFixMonthCutoff(new Integer(10));
		ptl.setFixMonthDay(new Integer(5));
		ptl.setFixMonthDay2(new Integer(1));
		ptl.setFixMonthDay3(new Integer(1));
		ptl.setFixMonthOffset(new Integer(j));
		ptl.setLine(j);
		ptl.setNetDay("1");
		ptl.setNetDays(10);
		ptl.setNextBusinessDay(true);
		ptl.setOnremainder(true);
		ptl.setPaymentTerm(pt);
		ptl.setPercentage(1.0f);
		pt.getFinancialMgmtPaymentTermLineList().add(ptl);
	    }
	    result.add(pt);
	}
	for (final PaymentTerm pt : result) {
	    OBDal.getInstance().save(pt);
	}
    }

    private List<PaymentTerm> getPaymentTerms() {
	final OBCriteria<PaymentTerm> obc = OBDal.getInstance().createCriteria(
		PaymentTerm.class);
	obc.add(Expression.not(Expression.in("id", currentPaymentTerms)));
	return obc.list();
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseOBObject> String getXML(List<?> pts) {
	final EntityXMLConverter exc = EntityXMLConverter.newInstance();
	exc.setOptionIncludeReferenced(true);
	exc.setOptionEmbedChildren(true);
	exc.setOptionIncludeChildren(true);
	exc.setAddSystemAttributes(false);
	return exc.toXML((List<BaseOBObject>) pts);
    }
}