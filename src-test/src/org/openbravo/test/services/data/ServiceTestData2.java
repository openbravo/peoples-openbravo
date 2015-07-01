package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData2 extends ServiceTestData {

  @Override
  public void initialize() {
    setBpartnerId(BP_CUSTOMER_A);
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
        // ProductId, quantity, price, amount
        { PRODUCT_DISTRIBUTION_GOOD_A, "1", "100", "10" },
        { PRODUCT_DISTRIBUTION_GOOD_B, "1", "50", "5" },
        { PRODUCT_DISTRIBUTION_GOOD_C, "1", "70", "7" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(BigDecimal.ZERO);
    setServicePriceResult(new BigDecimal("22.00"));
    setServiceAmountResult(new BigDecimal("22.00"));
    setPricelistId(PRICELIST_SALES);
  }
}
