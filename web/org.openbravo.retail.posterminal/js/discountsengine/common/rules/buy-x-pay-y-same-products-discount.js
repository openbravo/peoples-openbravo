/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function() {
  class BuyXPayYSameProducts extends OB.Discounts.Discount {
    constructor(ticket, discountImpl, discounts) {
      super(ticket, discountImpl, discounts);
    }

    getAvailableQty(lines) {
      let qtyAvailable = 0;
      lines.forEach(line => (qtyAvailable += OB.DEC.toNumber(line.qty)));
      return qtyAvailable;
    }

    canApplyDiscount(lines) {
      let qtyAvailable = this.getAvailableQty(lines);
      return qtyAvailable >= this.discountImpl.oBDISCX;
    }

    executeRule(lines) {
      let totalQty = this.getAvailableQty(lines),
        oBDISCX = this.discountImpl.oBDISCX,
        oBDISCY = this.discountImpl.oBDISCY,
        totalChunks = Math.floor(totalQty / oBDISCX),
        totalToGift = (oBDISCX - oBDISCY) * totalChunks,
        totalToPay = oBDISCY * totalChunks;

      lines.forEach(line => {
        let price = line.price,
          qty = line.qty;
        if (totalToGift > 0) {
          if (totalToGift - qty >= 0) {
            this.addDiscount(line, OB.DEC.toNumber(price * qty), {
              qtyOffer: qty,
              hidden: false,
              chunks: totalChunks
            });
            totalToGift -= qty;
          } else {
            this.addDiscount(line, OB.DEC.toNumber(price * totalToGift), {
              qtyOffer: totalToGift,
              hidden: false,
              chunks: totalChunks
            });

            if (totalToPay - (qty - totalToGift) >= 0) {
              this.addDiscount(line, OB.DEC.toNumber(0), {
                qtyOffer: qty - totalToGift,
                hidden: true,
                chunks: totalChunks
              });

              totalToPay -= qty - totalToGift;
            } else {
              this.addDiscount(line, OB.DEC.toNumber(0), {
                qtyOffer: totalToPay,
                hidden: true,
                chunks: totalChunks
              });
              totalToPay = 0;
            }
            totalToGift = 0;
          }
        } else if (totalToPay > 0) {
          if (totalToPay - qty >= 0) {
            this.addDiscount(line, OB.DEC.toNumber(0), {
              qtyOffer: qty,
              hidden: true,
              chunks: totalChunks
            });
            totalToPay -= qty;
          } else {
            this.addDiscount(line, OB.DEC.toNumber(0), {
              qtyOffer: totalToPay,
              hidden: true,
              chunks: totalChunks
            });
            totalToPay = 0;
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

  OB.Discounts.discountRules[
    'E08EE3C23EBA49358A881EF06C139D63'
  ] = BuyXPayYSameProducts;
})();
