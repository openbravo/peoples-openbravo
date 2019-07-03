/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  function PriceAdjustment(ticket, discountImpl, discounts) {
    OB.Discounts.Discount.call(this, ticket, discountImpl, discounts);
  }

  PriceAdjustment.prototype = Object.create(OB.Discounts.Discount.prototype);

  PriceAdjustment.prototype.calculateDiscounts = function() {
    let me = this; // due to partial ES6 support in nashorn
    this.getApplicableLines()
      .filter(line => me.canApplyDiscount(line))
      .forEach(line => me.discountForLine(line));
  };

  PriceAdjustment.prototype.canApplyDiscount = function(line) {
    let minQty = this.discountImpl.minQuantity,
      maxQty = this.discountImpl.maxQuantity,
      isMultiple = this.discountImpl.ismultiple,
      multipleQty = this.discountImpl.multiple,
      qty = line.qty;

    if (isMultiple) {
      if (qty < multipleQty) {
        return false;
      }
    } else if ((minQty && qty < minQty) || (maxQty && qty > maxQty)) {
      return false;
    }

    let linePrice = this.getUnitPrice(line);

    if (linePrice < this.discountImpl.fixedPrice) {
      return false;
    }

    return true;
  };

  PriceAdjustment.prototype.discountForLine = function(line) {
    let discountAmount = this.discountImpl.discountAmount,
      qty = line.qty,
      fixedPrice = this.discountImpl.fixedPrice,
      discountedAmt,
      discountedLinePrice,
      linePrice = this.getUnitPrice(line);

    // TODO: wtf are chunks?

    if (fixedPrice) {
      discountedLinePrice = fixedPrice;
    } else {
      discountedLinePrice =
        (linePrice - discountAmount) * (1 - this.discountImpl.discount / 100);
    }
    discountedAmt = OB.DEC.toNumber(
      (linePrice -
        OB.DEC.toNumber(OB.DEC.toBigDecimal(String(discountedLinePrice)))) *
        qty
    );
    this.addDiscount(line, discountedAmt);
  };

  OB.Discounts.discountRules[
    '5D4BAF6BB86D4D2C9ED3D5A6FC051579'
  ] = PriceAdjustment;
})();
