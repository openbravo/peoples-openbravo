package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData2 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("BACK-202");
    setTestDescription("Service with three related products and regular pricelist");
    setBpartnerId(BP_CUSTOMER_A);
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
        // ProductId, quantity, price, amount
        { PRODUCT_DISTRIBUTION_GOOD_A, "1", "10", "10" },
        { PRODUCT_DISTRIBUTION_GOOD_B, "1", "5", "5" },
        { PRODUCT_DISTRIBUTION_GOOD_C, "1", "7", "7" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("200.00"));
    setServicePriceResult(new BigDecimal("201.47"));
    setServiceAmountResult(new BigDecimal("604.41"));
    setServiceQtyResult(new BigDecimal("3"));
    setPricelistId(PRICELIST_SALES);
  }
}
