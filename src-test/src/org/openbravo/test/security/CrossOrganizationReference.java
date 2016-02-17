package org.openbravo.test.security;

import java.util.Date;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
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

public class CrossOrganizationReference extends OBBaseTest {
  protected final static String SPAIN = "357947E87C284935AD1D783CF6F099A1";
  protected final static String SPAIN_WAREHOUSE = "4D7B97565A024DB7B4C61650FA2B9560";
  protected final static String USA_WAREHOUSE = "4028E6C72959682B01295ECFE2E20270";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  protected Order createOrder(String orgId, String warehouseId) {
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
