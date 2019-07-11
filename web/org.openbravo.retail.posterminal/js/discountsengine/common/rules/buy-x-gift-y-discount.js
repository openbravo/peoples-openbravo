/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function() {
  class BuyXGiftY extends OB.Discounts.Discount {
    constructor(ticket, discountImpl, discounts) {
      super(ticket, discountImpl, discounts);
    }

    getDiscountProductUnits() {
      let discountProducts = this.discountImpl.products,
        productsToFullfill = [];
      discountProducts.forEach(productInfo => {
        let product = {
          id: productInfo.product.id,
          isGift: productInfo.product.oBDISCIsGift,
          qty: productInfo.product.oBDISCIsGift
            ? OB.DEC.toNumber(productInfo.product.oBDISCGifQty)
            : OB.DEC.toNumber(productInfo.product.oBDISCQty)
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

    canApplyDiscount(lines) {
      let productsToFullfill = this.getDiscountProductUnits(),
        chunks = this.getNumberOfChunksToBeApplied(productsToFullfill, lines);
      if (chunks > 0) {
        return true;
      } else {
        return false;
      }
    }

    getPromotionAmt(discProducts, lines) {
      let promotionAmt = 0,
        chunks = this.getNumberOfChunksToBeApplied(discProducts, lines);

      discProducts.forEach(discProduct => {
        if (discProduct.isGift) {
          let giftQty = discProduct.qty,
            line = lines.find(ln => ln.product.id === discProduct.id),
            price = line.price;

          promotionAmt += OB.DEC.toNumber(price * chunks * giftQty);
        }
      });
      return promotionAmt;
    }

    getTotalPrice(lines) {
      let totalPrice = 0;
      lines.forEach(
        line => (totalPrice += OB.DEC.toNumber(line.price * line.qty))
      );
      return totalPrice;
    }

    executeRule(lines) {
      const discProducts = this.getDiscountProductUnits(),
        chunks = this.getNumberOfChunksToBeApplied(discProducts, lines);

      // [TODO] Do an API change a to transform flag distibuted to a new subtype
      let oBDISCSubtype = this.discountImpl.oBDISCSubtype || 'STANDARD';
      oBDISCSubtype = this.discountImpl.oBDISCDistribute
        ? 'DISTRIBUTED'
        : oBDISCSubtype;

      // first loop calculated the total discount, now let's apply it
      discProducts.forEach(discProduct => {
        let l = lines.find(ln => ln.product.id === discProduct.id),
          linePrice = l.price,
          lineQty = l.qty,
          giftQty,
          payQty;

        if (oBDISCSubtype === 'DISTRIBUTED') {
          let totalAmt = this.getTotalPrice(lines),
            promotionAmt = this.getPromotionAmt(discProducts, lines),
            actualAmt = (lineQty * linePrice * promotionAmt) / totalAmt;
          if (discProduct.isGift) {
            giftQty = discProduct.qty;
            // gift products are shown to user as free
            this.addDiscount(l, actualAmt, {
              qtyOffer: giftQty,
              chunks: chunks
            });
          } else {
            payQty = discProduct.qty;
            this.addDiscount(l, actualAmt, {
              qtyOffer: OB.DEC.toNumber(payQty * chunks),
              chunks: chunks
            });
          }
        } else {
          // not distributed
          if (discProduct.isGift) {
            giftQty = discProduct.qty;
            // apply just to free products N chunks
            this.addDiscount(l, OB.DEC.toNumber(linePrice * chunks * giftQty), {
              qtyOffer: OB.DEC.toNumber(giftQty * chunks),
              chunks: chunks
            });
          } else {
            // create a fake discount to prevent cascade
            payQty = discProduct.qty;
            this.addDiscount(l, OB.DEC.toNumber(0), {
              qtyOffer: OB.DEC.toNumber(payQty * chunks),
              hidden: true,
              chunks: chunks
            });
          }
        }
      });
    }

    /* @Override */
    calculateDiscounts() {
      let applicableLines = this.getApplicableLines();
      if (this.canApplyDiscount(applicableLines)) {
        this.executeRule(applicableLines);
      }
    }
  }

  OB.Discounts.discountRules['94AEA884F5AD4EABB72322832B9C5172'] = BuyXGiftY;
})();
