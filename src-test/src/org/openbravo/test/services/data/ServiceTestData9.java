package org.openbravo.test.services.data;

import java.math.BigDecimal;

import org.openbravo.erpCommon.utility.OBDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceTestData9 extends ServiceTestData {

  final static private Logger log = LoggerFactory.getLogger(ServiceTestData9.class);

  @Override
  public void initialize() {
    setTestNumber("BACK-500");
    setTestDescription("Services missing configuration data. Missing Price Rule Version");
    try {
      setErrorMessage("@ServicePriceRuleVersionNotFound@ Warranty, @Date@: "
          + OBDateUtils.formatDate(OBDateUtils.getDate("01-04-2015")));
    } catch (Exception ex) {
      log.error("Error when executing ServiceTestData9: " + ex);
    }
    setBpartnerId(BP_CUSTOMER_A);
    setOrderDate("01-04-2015");
    setServiceId(SERVICE_WARRANTY);
    setProducts(new String[][] {//
    // ProductId, quantity, price, amount
    { PRODUCT_DISTRIBUTION_GOOD_A, "1", "10", "10" } //
    });
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("250.00"));
    setPricelistId(PRICELIST_SALES);
  }
}
