package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData8 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("BACK-401");
    setTestDescription("Modify Ordered Quantity of a Sales Order Line related to three services");
    setBpartnerId(BP_CUSTOMER_A);
    setProductId(PRODUCT_DISTRIBUTION_GOOD_A);
    setQuantity(new BigDecimal("15.00"));
    setPrice(new BigDecimal("10.00"));
    setProductChangedQty(new BigDecimal("10.00"));
    setServices(new String[][] {
        // ProductId, quantity, price, amount
        { SERVICE_WARRANTY, "1", "200.00", "2000.00" },
        { SERVICE_INSURANCE, "1", "25.00", "250.00" },
        { SERVICE_TRANSPORTATION, "1", "250.00", "250.00" } });
    setServicesResults(new String[][] {
        // ProductId, quantity, price, amount
        { SERVICE_WARRANTY, "10", "202.00", "2020.00" },
        { SERVICE_INSURANCE, "10", "26.50", "265.00" },
        { SERVICE_TRANSPORTATION, "1", "250.00", "250.00" } });
    setPricelistId(PRICELIST_SALES);
  }
}
