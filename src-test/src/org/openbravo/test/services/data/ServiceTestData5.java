package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData5 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("BACK-300");
    setTestDescription("Service of Quantity Rule = 'As per product'");
    setBpartnerId(BP_CUSTOMER_A);
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
    // ProductId, quantity, price, amount
    { PRODUCT_DISTRIBUTION_GOOD_A, "15", "10", "150" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("200.00"));
    setServicePriceResult(new BigDecimal("202.00"));
    setServiceAmountResult(new BigDecimal("3030.00"));
    setServiceQtyResult(new BigDecimal("15"));
    setPricelistId(PRICELIST_SALES);
  }
}
