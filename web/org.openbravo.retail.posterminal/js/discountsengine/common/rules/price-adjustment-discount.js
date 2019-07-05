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
