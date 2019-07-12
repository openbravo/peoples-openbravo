/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function() {
  class Pack extends OB.Discounts.Discount {
    constructor(ticket, discountImpl, discounts) {
      super(ticket, discountImpl, discounts);
    }

    getDiscountProductUnits() {
      let discountProducts = this.discountImpl.products,
        productsToFullfill = [];
      discountProducts.forEach(productInfo => {
        let product = {
          id: productInfo.product.id,
          qty: OB.DEC.toNumber(productInfo.product.oBDISCQty)
        };
        productsToFullfill.push(product);
      });
      return productsToFullfill;
    }

    getNumberOfChunksToBeApplied(discProducts, lines) {
      let chunks;
      discProducts.forEach(discProduct => {
        let line, applyNtimes, qty;

        if (chunks === 0) {
          // already decided not to apply
          return chunks;
        }
        line = lines.find(ln => ln.product.id === discProduct.id);

        if (!line) {
          // cannot apply the rule
          return (chunks = 0);
        } else {
          // [TODO] this must be a loop to go through all lines as they can be splitted
          qty = discProduct.qty;

          applyNtimes = Math.floor(line.qty / (qty || 1));

          if (!chunks || applyNtimes < chunks) {
            chunks = applyNtimes;
          }
        }
      });
      return chunks;
    }

    getTotalPrice(lines) {
      let totalPrice = 0;
      lines.forEach(
        line => (totalPrice += OB.DEC.toNumber(line.price * line.qty))
      );
      return totalPrice;
    }

    /* @Override */
    getDiscountTypeName() {
      return 'Pack';
    }

    /* @Override */
    canApplyDiscount(lines) {
      let productsToFullfill = this.getDiscountProductUnits(),
        chunks = this.getNumberOfChunksToBeApplied(productsToFullfill, lines);
      if (chunks > 0) {
        return lines;
      } else {
        return [];
      }
    }

    /* @Override */
    executeDiscountCalculation(lines) {
      let discPrice = OB.DEC.toNumber(this.discountImpl.oBDISCPrice),
        discProducts = this.getDiscountProductUnits(),
        chunks = this.getNumberOfChunksToBeApplied(discProducts, lines),
        totalAmt = this.getTotalPrice(lines),
        finalAmt = discPrice * chunks,
        promotionAmt = totalAmt - finalAmt,
        accumulativeAmt = 0;

      // first loop calculated the total discount, now let's apply it
      discProducts.forEach((discProduct, index) => {
        let line = lines.find(ln => ln.product.id === discProduct.id),
          price = line.price,
          qty = line.qty,
          amt;

        if (index < discProducts.length - 1) {
          amt = OB.DEC.toNumber((price * qty * promotionAmt) / totalAmt);
          accumulativeAmt += amt;
        } else {
          // last line with discount: calculate discount based on pending amt to be discounted
          amt = OB.DEC.toNumber(promotionAmt - accumulativeAmt);
        }

        this.addDiscount(line, OB.DEC.toNumber(amt), {
          qtyOffer: OB.DEC.toNumber(discProduct.qty * chunks),
          pack: true,
          chunks: chunks
        });
      });
    }
  }

  OB.Discounts.discountRules['BE5D42E554644B6AA262CCB097753951'] = Pack;
})();
