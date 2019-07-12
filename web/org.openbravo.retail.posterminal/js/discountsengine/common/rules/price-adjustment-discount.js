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

    /* @Override */
    getDiscountTypeName() {
      return 'Price Adjustment';
    }

    /* @Override */
    canApplyDiscount(lines) {
      const minQty = this.discountImpl.minQuantity,
        maxQty = this.discountImpl.maxQuantity,
        isMultiple = this.discountImpl.ismultiple,
        multipleQty = this.discountImpl.multiple;
      let discountableLines = [];

      lines.forEach(line => {
        let qty = line.qty;
        if (isMultiple) {
          if (qty < multipleQty) {
            return;
          }
        } else if ((minQty && qty < minQty) || (maxQty && qty > maxQty)) {
          return;
        }

        let linePrice = this.getUnitPrice(line);

        if (linePrice < this.discountImpl.fixedPrice) {
          return;
        }

        discountableLines.push(line);
      });

      return discountableLines;
    }

    /* @Override */
    executeDiscountCalculation(lines) {
      let discountAmount = this.discountImpl.discountAmount,
        discountPercentage = this.discountImpl.discountPercentage,
        fixedUnitPrice = this.discountImpl.discountFixedUnitPrice;

      lines.forEach(line => {
        let qty = line.qty,
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
      });
    }
  }

  OB.Discounts.discountRules[
    '5D4BAF6BB86D4D2C9ED3D5A6FC051579'
  ] = PriceAdjustment;
})();
