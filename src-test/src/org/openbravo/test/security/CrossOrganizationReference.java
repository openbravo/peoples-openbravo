package org.openbravo.test.security;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.test.base.OBBaseTest;

public class CrossOrganizationReference extends OBBaseTest {
  protected final static String SPAIN_ORG = "357947E87C284935AD1D783CF6F099A1";
  protected final static String SPAIN_WAREHOUSE = "4D7B97565A024DB7B4C61650FA2B9560";

  protected final static String USA_ORG = "5EFF95EB540740A3B10510D9814EFAD5";
  protected final static String USA_WAREHOUSE = "4028E6C72959682B01295ECFE2E20270";
  protected final static String USA_BP = "4028E6C72959682B01295F40D4D20333";

  protected final static String EUR = "102";

  private static List<BaseOBObject> createdObjects = new ArrayList<BaseOBObject>();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @SuppressWarnings("serial")
  protected Order createOrder(String orgId, final String warehouseId) {
    return createOrder(orgId, new HashMap<String, Object>() {
      {
        put(Order.PROPERTY_WAREHOUSE, OBDal.getInstance().getProxy(Warehouse.class, warehouseId));
      }
    });
  }

  protected Order createOrder(String orgId, Map<String, Object> propertyValues) {
    String CREDIT_ORDER_DOC_TYPE = "FF8080812C2ABFC6012C2B3BDF4C0056";
    String CUST_A = "4028E6C72959682B01295F40C3CB02EC";
    String CUST_A_LOCATION = "4028E6C72959682B01295F40C43802EE";
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
    order.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class, SPAIN_WAREHOUSE));
    order.setPriceList(OBDal.getInstance().getProxy(PriceList.class, PRICE_LIST));
    order.setOrderDate(new Date());
    order.setAccountingDate(new Date());

    setProperties(propertyValues, order);

    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();
    createdObjects.add(order);
    return order;
  }

  protected OrderLine createOrderLine(Order order) {
    return createOrderLine(order, new HashMap<String, Object>());
  }

  protected OrderLine createOrderLine(Order order, Map<String, Object> propertyValues) {
    String OUM = "4028E6C72959682B01295ADC1A380221";
    String PRODUCT = "4028E6C72959682B01295ADC1D07022A";
    String TAX = "3271411A5AFB490A91FB618B6B789C24";

    OrderLine ol = OBProvider.getInstance().get(OrderLine.class);
    Organization org;

    if (propertyValues.containsKey(OrderLine.PROPERTY_ORGANIZATION)) {
      org = (Organization) propertyValues.get(OrderLine.PROPERTY_ORGANIZATION);
    } else {
      org = order.getOrganization();
    }

    ol.setSalesOrder(order);
    ol.setOrganization(org);
    ol.setLineNo(100L);
    ol.setOrderDate(new Date());
    ol.setWarehouse(org.getOrganizationWarehouseList().get(0).getWarehouse());
    ol.setProduct(OBDal.getInstance().getProxy(Product.class, PRODUCT));
    ol.setUOM(OBDal.getInstance().getProxy(UOM.class, OUM));
    ol.setOrderedQuantity(BigDecimal.TEN);
    ol.setCurrency(OBDal.getInstance().getProxy(Currency.class, EUR));
    ol.setTax(OBDal.getInstance().getProxy(TaxRate.class, TAX));

    setProperties(propertyValues, ol);

    OBDal.getInstance().save(ol);
    OBDal.getInstance().flush();
    createdObjects.add(ol);
    return ol;
  }

  private void setProperties(Map<String, Object> propertyValues, BaseOBObject obj) {
    for (Entry<String, Object> propertyValue : propertyValues.entrySet()) {
      obj.set(propertyValue.getKey(), propertyValue.getValue());
    }
  }

  @AfterClass
  public static void cleanUp() {
    OBContext.setAdminMode(false);
    for (BaseOBObject obj : createdObjects) {
      BaseOBObject objToDelete = OBDal.getInstance().get(obj.getClass(), obj.getId());
      if (objToDelete != null) {
        OBDal.getInstance().remove(objToDelete);
      }
    }
    OBDal.getInstance().commitAndClose();
  }
}
