package org.openbravo.test.taxes.data;

import java.math.BigDecimal;
import java.util.HashMap;

public abstract class TaxesTestData {
  private String testNumber;
  private String testDescription;
  private String productId;
  private BigDecimal quantity;
  private BigDecimal price;
  private BigDecimal lineNet;
  private BigDecimal quantityUpdated;
  private BigDecimal priceUpdated;
  private BigDecimal lineNetUpdated;
  private String bpartnerId;
  private String taxid;
  private HashMap<String, String[]> linetaxes;
  private HashMap<String, String[]> doctaxes;
  private boolean isSalesTest;
  private boolean isPriceIncludingTaxes;

  public TaxesTestData() {
    initialize();
  }

  public String getTestNumber() {
    return testNumber;
  }

  public String getTestDescription() {
    return testDescription;
  }

  public String getProductId() {
    return productId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getLineNet() {
    return lineNet;
  }

  public BigDecimal getQuantityUpdated() {
    return quantityUpdated;
  }

  public BigDecimal getPriceUpdated() {
    return priceUpdated;
  }

  public BigDecimal getLineNetUpdated() {
    return lineNetUpdated;
  }

  public String getBpartnerId() {
    return bpartnerId;
  }

  public String getTaxid() {
    return taxid;
  }

  public HashMap<String, String[]> getLinetaxes() {
    return linetaxes;
  }

  public HashMap<String, String[]> getDoctaxes() {
    return doctaxes;
  }

  public boolean isSalesTest() {
    return isSalesTest;
  }

  public boolean isPriceIncludingTaxes() {
    return isPriceIncludingTaxes;
  }

  public void setTestNumber(String testNumber) {
    this.testNumber = testNumber;
  }

  public void setTestDescription(String testDescription) {
    this.testDescription = testDescription;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public void setLineNet(BigDecimal lineNet) {
    this.lineNet = lineNet;
  }

  public void setQuantityUpdated(BigDecimal quantityUpdated) {
    this.quantityUpdated = quantityUpdated;
  }

  public void setPriceUpdated(BigDecimal priceUpdated) {
    this.priceUpdated = priceUpdated;
  }

  public void setLineNetUpdated(BigDecimal lineNetUpdated) {
    this.lineNetUpdated = lineNetUpdated;
  }

  public void setBpartnerId(String bpartnerId) {
    this.bpartnerId = bpartnerId;
  }

  public void setTaxid(String taxid) {
    this.taxid = taxid;
  }

  public void setLinetaxes(HashMap<String, String[]> linetaxes) {
    this.linetaxes = linetaxes;
  }

  public void setDoctaxes(HashMap<String, String[]> doctaxes) {
    this.doctaxes = doctaxes;
  }

  public void setSalesTest(boolean isSalesTest) {
    this.isSalesTest = isSalesTest;
  }

  public void setPriceIncludingTaxes(boolean isPriceIncludingTaxes) {
    this.isPriceIncludingTaxes = isPriceIncludingTaxes;
  }

  public abstract void initialize();

}