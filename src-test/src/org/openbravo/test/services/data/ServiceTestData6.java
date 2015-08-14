package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData6 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("BACK-301");
    setTestDescription("Service of Quantity Rule = 'Unique Quantity'");
    setBpartnerId(BP_CUSTOMER_A);
    setServiceId(SERVICE_TRANSPORTATION);
    setProducts(new String[][] {//
    // ProductId, quantity, price, amount
    { PRODUCT_DISTRIBUTION_GOOD_A, "15", "10", "150" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("250.00"));
    setServicePriceResult(new BigDecimal("250.00"));
    setServiceAmountResult(new BigDecimal("250.00"));
    setServiceQtyResult(BigDecimal.ONE);
    setPricelistId(PRICELIST_SALES);
  }
}
