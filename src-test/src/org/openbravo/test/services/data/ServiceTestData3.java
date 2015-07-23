package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData3 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("03");
    setTestDescription("Service with one related product and price including taxes");
    setBpartnerId(BP_CUSTOMER_A);
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
    // ProductId, quantity, price, amount
    { PRODUCT_DISTRIBUTION_GOOD_A, "1", "10", "10" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("250.00"));
    setServicePriceResult(new BigDecimal("252.00"));
    setServiceAmountResult(new BigDecimal("252.00"));
    setPricelistId(PRICELIST_CUSTOMER_A_INCL_TAX);
  }
}
