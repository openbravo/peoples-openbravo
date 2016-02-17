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
 * All portions are Copyright (C) 2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.security;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.test.base.OBBaseTest;

/**
 * Test cases covering references to cross natural tree organizations. They should not be allowed.
 * 
 * @author alostale
 *
 */
public class StandardCrossOrganizationReference extends OBBaseTest {
  private final static String SPAIN = "357947E87C284935AD1D783CF6F099A1";
  private final static String SPAIN_WAREHOUSE = "4D7B97565A024DB7B4C61650FA2B9560";
  private final static String USA_WAREHOUSE = "4028E6C72959682B01295ECFE2E20270";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  /** References from org Spain to USA should not be allowed on insertion */
  @Test
  @Ignore("Expected exception is not thrown on isert, see issue #32063")
  public void crossOrgRefShouldBeIllegalOnInsert() {
    setTestAdminContext();
    createOrder(SPAIN, USA_WAREHOUSE);

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  /** References from org Spain to USA should not be allowed on update */
  @Test
  public void crossOrgRefShouldBeIllegalOnUpdate() {
    setTestAdminContext();
    Order order = createOrder(SPAIN, SPAIN_WAREHOUSE);
    order.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, USA_WAREHOUSE));

    exception.expect(OBSecurityException.class);

    OBDal.getInstance().commitAndClose();
  }

  private Order createOrder(String orgId, String warehouseId) {
    String CREDIT_ORDER_DOC_TYPE = "FF8080812C2ABFC6012C2B3BDF4C0056";
    String CUST_A = "4028E6C72959682B01295F40C3CB02EC";
    String CUST_A_LOCATION = "4028E6C72959682B01295F40C43802EE";
    String EUR = "102";
    String PAYMENT_TERM = "7B308C5CB9674BB3A56E63D85887058A";
    String PRICE_LIST = "4028E6C72959682B01295B03CE480243";

    Order order = OBProvider.getInstance().get(Order.class);
    order.setOrganization(OBDal.getInstance().getProxy(Organization.class, orgId));
    order.setDocumentType(OBDal.getInstance().getProxy(DocumentType.class, CREDIT_ORDER_DOC_TYPE));
    order.setTransactionDocument(OBDal.getInstance().getProxy(DocumentType.class,
        CREDIT_ORDER_DOC_TYPE));
    order.setDocumentNo("TestCrossOrg");

    order.setBusinessPartner(OBDal.getInstance().getProxy(BusinessPartner.class, CUST_A));
    order.setPartnerAddress(OBDal.getInstance().getProxy(Location.class, CUST_A_LOCATION));
    order.setCurrency(OBDal.getInstance().getProxy(Currency.class, EUR));
    order.setPaymentTerms(OBDal.getInstance().getProxy(PaymentTerm.class, PAYMENT_TERM));
    order.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, warehouseId));
    order.setPriceList(OBDal.getInstance().getProxy(PriceList.class, PRICE_LIST));
    order.setOrderDate(new Date());
    order.setAccountingDate(new Date());
    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();
    return order;
  }
}
