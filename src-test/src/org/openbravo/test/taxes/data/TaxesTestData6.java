package org.openbravo.test.taxes.data;

import java.math.BigDecimal;
import java.util.HashMap;

public class TaxesTestData6 extends TaxesTestData {

  @Override
  public void initialize() {
    setTestNumber("6");
    setTestDescription("Regular pricelist 6: Purchase 3% + Charge negative");
    setSalesTest(false);
    setPriceIncludingTaxes(false);
    // This info will be set in header
    setBpartnerId(BPartnerDataConstants.VENDOR_A);
    // This info will be used in line
    setProductId(ProductDataConstants.RAW_MATERIAL_A);
    setTaxid(TaxDataConstants.TAX_VAT_CHARGE);
    // This info is used for inserting the line
    setQuantity(BigDecimal.ONE.negate());
    setPrice(new BigDecimal("3"));
    setLineNet(BigDecimal.ZERO);
    // This info is used to update the line
    setQuantityUpdated(new BigDecimal("-2"));
    setPriceUpdated(new BigDecimal("3"));
    setLineNetUpdated(BigDecimal.ZERO);

    // These are the expected results
    // Each line contains the taxID - {taxableAmtAfterInsert, taxAmtAfterInsert,
    // taxableAmtAfterUpdate, taxAmtAfterUpdate}
    // Exempt tax positive amount
    HashMap<String, String[]> lineTaxes = new HashMap<String, String[]>();
    lineTaxes.put(TaxDataConstants.TAX_VAT_3_Child, new String[] { "-3", "-0.09", "-6", "-0.18" });
    lineTaxes.put(TaxDataConstants.TAX_CHARGE, new String[] { "-3", "-0.02", "-6", "-0.03" });
    // Both taxes for line level and for document level are provided
    setLinetaxes(lineTaxes);
    setDoctaxes(lineTaxes);

  }

}
