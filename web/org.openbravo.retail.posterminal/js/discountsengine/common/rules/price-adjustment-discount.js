/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class PriceAdjustment extends OB.Discounts.Discount {
    constructor(ticket, discountImpl, discounts) {
      super(ticket, discountImpl, discounts);
    }

    canApplyDiscount(line) {
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
    }

    discountForLine(line) {
      let discountAmount = this.discountImpl.discountAmount,
        discountPercentage = this.discountImpl.discountPercentage,
        fixedUnitPrice = this.discountImpl.discountFixedUnitPrice,
        qty = line.qty,
        discountedAmt;

      if (fixedUnitPrice) {
        discountedAmt = line.price - fixedUnitPrice * qty;
      } else if (discountPercentage) {
        discountedAmt = line.price * (discountPercentage / 100);
      } else if (discountAmount) {
        discountedAmt = discountAmount;
      }

      if (discountedAmt) {
        this.addDiscount(line, OB.DEC.toNumber(discountedAmt));
      }
    }

    /* @Override */
    calculateDiscounts() {
      this.getApplicableLines()
        .filter(line => this.canApplyDiscount(line))
        .forEach(line => this.discountForLine(line));
    }
  }

  OB.Discounts.discountRules[
    '5D4BAF6BB86D4D2C9ED3D5A6FC051579'
  ] = PriceAdjustment;
})();
