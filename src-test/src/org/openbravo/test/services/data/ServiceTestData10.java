package org.openbravo.test.services.data;

import java.math.BigDecimal;

public class ServiceTestData10 extends ServiceTestData {

  @Override
  public void initialize() {
    setTestNumber("BACK-501");
    setTestDescription("Services missing configuration data. Missing Price Rule Range");
    setErrorMessage("@ServicePriceRuleRangeNotFound@. @ServicePriceRule@: Ranges, @AmountUpTo@: 10000.00");
    setBpartnerId(BP_CUSTOMER_A);
    setOrderDate("01-06-2015");
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
    // ProductId, quantity, price, amount
    { PRODUCT_DISTRIBUTION_GOOD_A, "1000", "10", "10" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("250.00"));
    setPricelistId(PRICELIST_SALES);
  }
}
