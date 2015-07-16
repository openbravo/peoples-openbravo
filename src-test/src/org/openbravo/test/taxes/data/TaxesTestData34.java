package org.openbravo.test.taxes.data;

import java.math.BigDecimal;
import java.util.HashMap;

public class TaxesTestData34 extends TaxesTestData {

  @Override
  public void initialize() {
    setTestNumber("34");
    setTestDescription("Price including taxes 34: Sales BOM+Exempt positive");
    setSalesTest(true);
    setPriceIncludingTaxes(true);
    // This info will be set in header
    setBpartnerId(BPartnerDataConstants.CUSTOMER_A);
    // This info will be used in line
    setProductId(ProductDataConstants.DISTRIBUTION_GOOD_E);
    setTaxid(TaxDataConstants.TAX_AS_PER_BOM);
    // This info is used for inserting the line
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("3"));
    setLineNet(new BigDecimal("2.86"));
    // This info is used to update the line
    setQuantityUpdated(new BigDecimal("2"));
    setPriceUpdated(new BigDecimal("3"));
    setLineNetUpdated(new BigDecimal("5.72"));

    // These are the expected results
    // Each line contains the taxID - {taxableAmtAfterInsert, taxAmtAfterInsert,
    // taxableAmtAfterUpdate, taxAmtAfterUpdate}
    // Exempt tax positive amount
    HashMap<String, String[]> lineTaxes = new HashMap<String, String[]>();
    lineTaxes.put(TaxDataConstants.TAX_VAT_10, new String[] { "1.36", "0.14", "2.72", "0.28" });
    lineTaxes.put(TaxDataConstants.TAX_EXEMPT, new String[] { "1.5", "0.00", "3", "0.00" });
    // Both taxes for line level and for document level are provided
    setLinetaxes(lineTaxes);
    setDoctaxes(lineTaxes);

  }

}
