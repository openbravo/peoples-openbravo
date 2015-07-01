package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData3 extends ServiceTestData {

  @Override
  public void initialize() {
    setBpartnerId(BP_CUSTOMER_A);
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
    // ProductId, quantity, price, amount
    { PRODUCT_DISTRIBUTION_GOOD_A, "1", "100", "10" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(BigDecimal.ZERO);
    setServicePriceResult(new BigDecimal("10.00"));
    setServiceAmountResult(new BigDecimal("10.00"));
    setPricelistId(PRICELIST_CUSTOMER_A_INCL_TAX);
  }
}
