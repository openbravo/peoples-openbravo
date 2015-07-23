package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData1 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("01");
    setTestDescription("Service with one related product and regular pricelist");
    setBpartnerId(BP_CUSTOMER_A);
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
    // ProductId, quantity, price, amount
    { PRODUCT_DISTRIBUTION_GOOD_A, "1", "10", "10" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("200.00"));
    setServicePriceResult(new BigDecimal("202.00"));
    setServiceAmountResult(new BigDecimal("202.00"));
    setPricelistId(PRICELIST_SALES);
  }
}
