package org.openbravo.test.taxes.data;

import java.math.BigDecimal;
import java.util.HashMap;

public class TaxesTestData21 extends TaxesTestData {

  @Override
  public void initialize() {
    setTestNumber("21");
    setTestDescription("Price including taxes 21: Purchase 3% + charge positive");
    setSalesTest(false);
    setPriceIncludingTaxes(true);
    // This info will be set in header
    setBpartnerId(BPartnerDataConstants.VENDOR_A);
    // This info will be used in line
    setProductId(ProductDataConstants.RAW_MATERIAL_A);
    setTaxid(TaxDataConstants.TAX_VAT_CHARGE);
    // This info is used for inserting the line
    setQuantity(BigDecimal.ONE);
    setPrice(new BigDecimal("3"));
    setLineNet(new BigDecimal("2.90"));
    // This info is used to update the line
    setQuantityUpdated(new BigDecimal("2"));
    setPriceUpdated(new BigDecimal("3"));
    setLineNetUpdated(new BigDecimal("5.80"));

    // These are the expected results
    // Each line contains the taxID - {taxableAmtAfterInsert, taxAmtAfterInsert,
    // taxableAmtAfterUpdate, taxAmtAfterUpdate}
    // Exempt tax positive amount
    HashMap<String, String[]> lineTaxes = new HashMap<String, String[]>();
    lineTaxes
        .put(TaxDataConstants.TAX_VAT_3_Child, new String[] { "2.90", "0.09", "5.80", "0.17" });
    lineTaxes.put(TaxDataConstants.TAX_CHARGE, new String[] { "2.90", "0.01", "5.80", "0.03" });
    // Both taxes for line level and for document level are provided
    setLinetaxes(lineTaxes);
    setDoctaxes(lineTaxes);

  }

}
