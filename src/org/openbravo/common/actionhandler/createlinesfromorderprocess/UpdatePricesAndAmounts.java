/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.createlinesfromorderprocess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.enterprise.context.Dependent;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.ProductPrice;

@Dependent
@Qualifier(CreateLinesFromOrderProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
class UpdatePricesAndAmounts implements CreateLinesFromOrderProcessImplementationInterface {
  private InvoiceLine invoiceLine;
  private Invoice processingInvoice;
  private OrderLine orderLine;

  @Override
  public int getOrder() {
    return -20;
  }

  /**
   * Updates prices and amounts. If the product has a product price in the invoice price list then
   * all prices and amounts will be recalculated using currency precisions and taking into account
   * if the price list includes taxes or not.
   * 
   */
  @Override
  public void exec(final Invoice currentInvoice, final OrderLine copiedOrderLine,
      InvoiceLine newInvoiceLine) {
    this.invoiceLine = newInvoiceLine;
    this.orderLine = copiedOrderLine;
    this.processingInvoice = currentInvoice;

    ProductPrice productPrice = getProductPriceInPriceList(orderLine.getProduct(),
        processingInvoice.getPriceList());
    if (productPrice != null) {
      setPricesBasedOnPriceList(productPrice);
    } else {
      setPricesToZero();
    }
  }

  private void setPricesBasedOnPriceList(final ProductPrice productPrice) {
    PriceInformation priceInformation = new PriceInformation();
    BigDecimal qtyOrdered = orderLine.getOrderedQuantity();

    // Standard and Price precision
    Currency invoiceCurrency = processingInvoice.getCurrency();
    int stdPrecision = invoiceCurrency.getStandardPrecision().intValue();
    int pricePrecision = invoiceCurrency.getPricePrecision().intValue();

    // Price List, Price Standard and discount
    BigDecimal priceActual = productPrice.getStandardPrice().setScale(pricePrecision,
        RoundingMode.HALF_UP);
    BigDecimal priceList = productPrice.getListPrice().setScale(pricePrecision,
        RoundingMode.HALF_UP);
    BigDecimal priceLimit = productPrice.getPriceLimit().setScale(pricePrecision,
        RoundingMode.HALF_UP);

    BigDecimal discount = BigDecimal.ZERO;
    if (productPrice.getListPrice().compareTo(BigDecimal.ZERO) != 0) {
      // Discount = ((PL-PA)/PL)*100
      discount = priceList.subtract(priceActual).multiply(new BigDecimal("100"))
          .divide(priceList, stdPrecision, RoundingMode.HALF_UP);
    }
    BigDecimal lineNetAmount = qtyOrdered.multiply(priceActual).setScale(stdPrecision,
        RoundingMode.HALF_UP);

    // Processing for Prices Including Taxes
    if (processingInvoice.getPriceList().isPriceIncludesTax()) {
      BigDecimal grossUnitPrice = priceActual;
      BigDecimal grossAmount = qtyOrdered.multiply(grossUnitPrice).setScale(stdPrecision,
          RoundingMode.HALF_UP);

      // Set gross price information
      priceInformation.setGrossUnitPrice(grossUnitPrice);
      priceInformation.setGrossBaseUnitPrice(grossUnitPrice);
      priceInformation.setGrossListPrice(priceList);
      priceInformation.setLineGrossAmount(grossAmount);

      // Update Net Prices to 0
      priceActual = BigDecimal.ZERO;
      priceList = BigDecimal.ZERO;
      priceLimit = BigDecimal.ZERO;
    }

    priceInformation.setUnitPrice(priceActual);
    priceInformation.setStandardPrice(priceActual);
    priceInformation.setListPrice(priceList);
    priceInformation.setLineNetAmount(lineNetAmount);

    priceInformation.setDiscount(discount);
    priceInformation.setPriceLimit(priceLimit);

    setPrices(priceInformation);
  }

  private void setPricesToZero() {
    PriceInformation zeroPrices = new PriceInformation();
    setPrices(zeroPrices);
  }

  private void setPrices(final PriceInformation priceInformation) {
    // Net Prices
    invoiceLine.setUnitPrice(priceInformation.getUnitPrice());
    invoiceLine.setListPrice(priceInformation.getListPrice());
    invoiceLine.setStandardPrice(priceInformation.getStandardPrice());
    // Gross Prices
    invoiceLine.setGrossUnitPrice(priceInformation.getGrossUnitPrice());
    invoiceLine.setGrossListPrice(priceInformation.getGrossListPrice());
    invoiceLine.setBaseGrossUnitPrice(priceInformation.getGrossBaseUnitPrice());
    invoiceLine.setGrossAmount(priceInformation.getLineGrossAmount());
    // Price Limit
    invoiceLine.setPriceLimit(priceInformation.getPriceLimit());
    invoiceLine.setLineNetAmount(priceInformation.getLineNetAmount());
  }

  /**
   * Return the defined product price in a selected pricelist or null if the product doesn't has any
   * price defined on the price list
   * 
   * @param product
   *          The product where the price is searched.
   * @param priceList
   *          The price list where the product price is searched.
   * @return The product price defined for the product in the price list or NULL if any.
   */
  private ProductPrice getProductPriceInPriceList(final Product product, final PriceList priceList) {
    StringBuilder obq = new StringBuilder("");
    obq.append(" as pp ");
    obq.append(" join pp.priceListVersion plv ");
    obq.append(" where pp.product.id = :productID");
    obq.append(" and plv.priceList.id = :priceListID");
    obq.append(" and plv.active = true");
    obq.append(" and (plv.validFromDate is null or plv.validFromDate <= :validFromDate)");
    obq.append(" order by plv.validFromDate desc");

    OBQuery<ProductPrice> obQuery = OBDal.getInstance().createQuery(ProductPrice.class,
        obq.toString());
    obQuery.setNamedParameter("productID", product.getId());
    obQuery.setNamedParameter("priceListID", priceList.getId());
    obQuery.setNamedParameter("validFromDate", new Date());
    obQuery.setMaxResult(1);
    return obQuery.uniqueResult();
  }

  private static class PriceInformation {
    // Net Prices
    BigDecimal unitPrice;
    BigDecimal standardPrice;
    BigDecimal listPrice;
    private BigDecimal lineNetAmount;
    // Gross Prices
    BigDecimal grossUnitPrice;
    BigDecimal grossBaseUnitPrice;
    BigDecimal grossListPrice;
    BigDecimal lineGrossAmount;
    BigDecimal priceLimit;

    private PriceInformation() {
      this.priceLimit = BigDecimal.ZERO;
      this.unitPrice = BigDecimal.ZERO;
      this.standardPrice = BigDecimal.ZERO;
      this.listPrice = BigDecimal.ZERO;
      this.setLineNetAmount(BigDecimal.ZERO);
      this.grossUnitPrice = BigDecimal.ZERO;
      this.grossBaseUnitPrice = BigDecimal.ZERO;
      this.grossListPrice = BigDecimal.ZERO;
      this.lineGrossAmount = BigDecimal.ZERO;
    }

    private void setDiscount(final BigDecimal discount) {
    }

    private BigDecimal getPriceLimit() {
      return priceLimit;
    }

    private void setPriceLimit(final BigDecimal priceLimit) {
      this.priceLimit = priceLimit;
    }

    private BigDecimal getUnitPrice() {
      return unitPrice;
    }

    private void setUnitPrice(final BigDecimal unitPrice) {
      this.unitPrice = unitPrice;
    }

    private BigDecimal getStandardPrice() {
      return standardPrice;
    }

    private void setStandardPrice(final BigDecimal priceStandard) {
      this.standardPrice = priceStandard;
    }

    private BigDecimal getListPrice() {
      return listPrice;
    }

    private void setListPrice(final BigDecimal listPrice) {
      this.listPrice = listPrice;
    }

    private BigDecimal getGrossUnitPrice() {
      return grossUnitPrice;
    }

    private void setGrossUnitPrice(final BigDecimal grossUnitPrice) {
      this.grossUnitPrice = grossUnitPrice;
    }

    private BigDecimal getGrossBaseUnitPrice() {
      return grossBaseUnitPrice;
    }

    private void setGrossBaseUnitPrice(final BigDecimal grossBaseUnitPrice) {
      this.grossBaseUnitPrice = grossBaseUnitPrice;
    }

    private BigDecimal getGrossListPrice() {
      return grossListPrice;
    }

    private void setGrossListPrice(final BigDecimal grossListPrice) {
      this.grossListPrice = grossListPrice;
    }

    private BigDecimal getLineGrossAmount() {
      return lineGrossAmount;
    }

    private void setLineGrossAmount(final BigDecimal lineGrossAmount) {
      this.lineGrossAmount = lineGrossAmount;
    }

    private BigDecimal getLineNetAmount() {
      return lineNetAmount;
    }

    private void setLineNetAmount(BigDecimal lineNetAmount) {
      this.lineNetAmount = lineNetAmount;
    }
  }

}
