package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData7 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("BACK-400");
    setTestDescription("Modify Ordered Quantity of related Product");
    setBpartnerId(BP_CUSTOMER_A);
    setProductId(PRODUCT_DISTRIBUTION_GOOD_A);
    setQuantity(new BigDecimal("15.00"));
    setPrice(new BigDecimal("10.00"));
    setProductChangedQty(new BigDecimal("10.00"));
    setServices(new String[][] {
    // ProductId, quantity, price, amount
    { SERVICE_WARRANTY, "1", "200.00", "2000.00" }
    });
    setServicesResults(new String[][] {
    // ProductId, quantity, price, amount
    { SERVICE_WARRANTY, "10", "202.00", "2020.00" }
    });
    setPricelistId(PRICELIST_SALES);
  }
}
