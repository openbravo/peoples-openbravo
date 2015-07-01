package org.openbravo.test.services.data;

import java.math.BigDecimal;

public abstract class ServiceTestData {

  /*
   * CONSTANTS:
   */
  public final String BP_CUSTOMER_A = "4028E6C72959682B01295F40C3CB02EC";
  public final String SERVICE_WARRANTY = "D67EF9E66FF447E88176DF0C054A9D3F";
  public final String PRODUCT_DISTRIBUTION_GOOD_A = "4028E6C72959682B01295ADC211E0237";
  public final String PRODUCT_DISTRIBUTION_GOOD_B = "4028E6C72959682B01295ADC21C90239";
  public final String PRODUCT_DISTRIBUTION_GOOD_C = "4028E6C72959682B01295ADC2285023B";
  public final String PRICELIST_SALES = "4028E6C72959682B01295ADC1D55022B";
  public final String PRICELIST_CUSTOMER_A_INCL_TAX = "6C69F63AE6C34DD48329368AFE29C91D";

  private String serviceId;
  private String[][] products;
  private BigDecimal quantity;
  private BigDecimal price;
  private String bpartnerId;
  private String pricelistId;
  private BigDecimal servicePriceResult;
  private BigDecimal serviceAmountResult;

  public BigDecimal getServiceAmountResult() {
    return serviceAmountResult;
  }

  public void setServiceAmountResult(BigDecimal serviceNetAmountResult) {
    this.serviceAmountResult = serviceNetAmountResult;
  }

  public BigDecimal getServicePriceResult() {
    return servicePriceResult;
  }

  public void setServicePriceResult(BigDecimal servicePriceResult) {
    this.servicePriceResult = servicePriceResult;
  }

  public String getPricelistId() {
    return pricelistId;
  }

  public void setPricelistId(String pricelistId) {
    this.pricelistId = pricelistId;
  }

  public String getBpartnerId() {
    return bpartnerId;
  }

  public void setBpartnerId(String bpartnerId) {
    this.bpartnerId = bpartnerId;
  }

  public ServiceTestData() {
    initialize();
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public String getServiceId() {
    return this.serviceId;
  }

  public String[][] getProducts() {
    return this.products;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public void setProducts(String[][] products) {
    this.products = products;
  }

  public abstract void initialize();

}
