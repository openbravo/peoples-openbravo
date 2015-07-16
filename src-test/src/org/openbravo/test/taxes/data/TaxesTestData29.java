package org.openbravo.test.taxes.data;

import java.math.BigDecimal;
import java.util.HashMap;

public class TaxesTestData29 extends TaxesTestData {

  @Override
  public void initialize() {
    setTestNumber("29");
    setTestDescription("Price including taxes 29: Purchase BOM Taxes negative");
    setSalesTest(false);
    setPriceIncludingTaxes(true);
    // This info will be set in header
    setBpartnerId(BPartnerDataConstants.VENDOR_A);
    // This info will be used in line
    setProductId(ProductDataConstants.DISTRIBUTION_GOOD_D);
    setTaxid(TaxDataConstants.TAX_AS_PER_BOM);
    // This info is used for inserting the line
    setQuantity(BigDecimal.ONE.negate());
    setPrice(new BigDecimal("3"));
    setLineNet(new BigDecimal("-2.85"));
    // This info is used to update the line
    setQuantityUpdated(new BigDecimal("-2"));
    setPriceUpdated(new BigDecimal("3"));
    setLineNetUpdated(new BigDecimal("-5.68"));

    // These are the expected results
    // Each line contains the taxID - {taxableAmtAfterInsert, taxAmtAfterInsert,
    // taxableAmtAfterUpdate, taxAmtAfterUpdate}
    // Exempt tax positive amount
    HashMap<String, String[]> lineTaxes = new HashMap<String, String[]>();
    lineTaxes.put(TaxDataConstants.TAX_VAT_10, new String[] { "-1.03", "-0.10", "-2.04", "-0.21" });
    lineTaxes.put(TaxDataConstants.TAX_VAT_3, new String[] { "-1.82", "-0.05", "-3.64", "-0.11" });
    // Both taxes for line level and for document level are provided
    setLinetaxes(lineTaxes);
    setDoctaxes(lineTaxes);

  }

}
