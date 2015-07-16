package org.openbravo.test.taxes.data;

import java.math.BigDecimal;
import java.util.HashMap;

public class TaxesTestData7 extends TaxesTestData {

  @Override
  public void initialize() {
    setTestNumber("7");
    setTestDescription("Regular pricelist 7: Sales Exempt positive");
    setSalesTest(true);
    setPriceIncludingTaxes(false);
    // This info will be set in header
    setBpartnerId(BPartnerDataConstants.VENDOR_A);
    // This info will be used in line
    setProductId(ProductDataConstants.RAW_MATERIAL_A);
    setTaxid(TaxDataConstants.TAX_EXEMPT_10);
    // This info is used for inserting the line
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("3"));
    setLineNet(new BigDecimal("3"));
    // This info is used to update the line
    setQuantityUpdated(new BigDecimal("2"));
    setPriceUpdated(new BigDecimal("3"));
    setLineNetUpdated(new BigDecimal("6"));

    // These are the expected results
    // Each line contains: //
    // taxID - {taxableAmtAfterInsert, taxAmtAfterInsert, taxableAmtAfterUpdate, taxAmtAfterUpdate}
    // Exempt tax positive amount
    HashMap<String, String[]> lineTaxes = new HashMap<String, String[]>();
    lineTaxes.put(TaxDataConstants.TAX_EXEMPT_10, new String[] { "3", "0", "6", "0" });
    // Both taxes for line level and for document level are provided
    setLinetaxes(lineTaxes);
    setDoctaxes(lineTaxes);

  }

}
