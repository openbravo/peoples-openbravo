/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function() {
  class BuyXPayYDifferentProducts extends OB.Discounts.Discount {
    constructor(ticket, discountImpl, discounts) {
      super(ticket, discountImpl, discounts);
    }

    getAvailableQty(lines) {
      let qtyAvailable = 0;
      lines.forEach(line => (qtyAvailable += OB.DEC.toNumber(line.qty)));
      return qtyAvailable;
    }

    getTotalPrice(lines) {
      let totalPrice = 0;
      lines.forEach(line => (totalPrice += OB.DEC.toNumber(line.price)));
      return totalPrice;
    }

    canApplyDiscount(lines) {
      let qtyAvailable = this.getAvailableQty(lines);
      return qtyAvailable >= this.discountImpl.oBDISCX;
    }

    sortArray(lines) {
      let newArrayLines = [...lines];
      return newArrayLines.sort((lineA, lineB) => {
        if (
          this.discountImpl.oBDISCSubtype === 'CHEAPEST' ||
          this.discountImpl.oBDISCSubtype === 'DISTRIBUTED'
        ) {
          return -(lineA.price - lineB.price);
        } else {
          return lineA.price - lineB.price;
        }
      });
    }

    executeRule(lines) {
      const oBDISCX = this.discountImpl.oBDISCX,
        oBDISCY = this.discountImpl.oBDISCY,
        chunks = Math.floor(this.getAvailableQty(lines) / oBDISCX),
        unitsToPay = chunks * oBDISCY,
        unitsToGift = chunks * (oBDISCX - oBDISCY);

      // [TODO] Do an API change a to transform flag distibuted to a new subtype
      let oBDISCSubtype = this.discountImpl.oBDISCSubtype || 'MOSTEXPENSIVE';
      oBDISCSubtype = this.discountImpl.oBDISCDistribute
        ? 'DISTRIBUTED'
        : oBDISCSubtype;

      // Start calculating discounts
      if (oBDISCSubtype === 'CHEAPEST' || oBDISCSubtype === 'MOSTEXPENSIVE') {
        // For discounts not distributed between lines
        let unitsToCheck,
          groupToPay = [],
          groupToGift = [];

        // Do the group with the products to pay
        unitsToCheck = unitsToPay;
        this.sortArray(lines).forEach(ln => {
          let discountedQty = 0,
            qty = ln.qty;
          if (unitsToCheck > 0) {
            if (qty >= unitsToCheck) {
              discountedQty = unitsToCheck;
            } else {
              discountedQty = qty;
            }
            unitsToCheck -= discountedQty;

            ln.qtyToApplyDisc = discountedQty;
            groupToPay.push(ln);
          }
        });
        // Do the group with the products to gift
        unitsToCheck = unitsToGift;
        this.sortArray(lines)
          .reverse()
          .forEach(ln => {
            let discountedQty = 0,
              qty = ln.qtyToApplyDisc ? ln.qty - ln.qtyToApplyDisc : ln.qty;
            if (unitsToCheck > 0) {
              if (qty >= unitsToCheck) {
                discountedQty = unitsToCheck;
              } else {
                discountedQty = qty;
              }
              unitsToCheck -= discountedQty;
              ln.qtyToApplyDisc = discountedQty;
              groupToGift.push(ln);
            }
          });

        // First Step: Search products that the client must pay
        groupToPay.forEach(line => {
          this.addDiscount(line, OB.DEC.toNumber(0), {
            qtyOffer: line.qtyToApplyDisc,
            hidden: true,
            chunks: chunks
          });
        });
        // Second Step: Search products to gift to the client
        groupToGift.forEach(line => {
          let price = line.price;
          this.addDiscount(line, OB.DEC.toNumber(price * line.qtyToApplyDisc), {
            qtyOffer: line.qtyToApplyDisc,
            hidden: false,
            chunks: chunks
          });
        });
      } else if (oBDISCSubtype === 'AVG' || oBDISCSubtype === 'DISTRIBUTED') {
        // For discounts distributed beetween lines
        let unitsToCheck,
          groupAverage = [],
          totalPrice = 0,
          totalDisc = 0;

        // Do the group with the products to pay
        unitsToCheck = chunks * oBDISCX;
        this.sortArray(lines)
          .reverse()
          .forEach(ln => {
            let discountedQty = 0,
              qty = ln.qty;
            if (unitsToCheck > 0) {
              if (qty >= unitsToCheck) {
                discountedQty = unitsToCheck;
              } else {
                discountedQty = qty;
              }
              unitsToCheck -= discountedQty;

              ln.qtyToApplyDisc = discountedQty;
              groupAverage.push(ln);

              totalPrice += ln.price * discountedQty;
            }
          });

        // Calculate price to discount
        unitsToCheck = unitsToGift;
        groupAverage.forEach(ln => {
          let price = ln.price,
            discountedQty = 0;
          if (unitsToCheck > 0) {
            discountedQty = ln.qtyToApplyDisc;
            if (discountedQty >= unitsToCheck) {
              totalDisc += price * unitsToCheck;
              unitsToCheck = 0;
            } else {
              totalDisc += price * discountedQty;
              unitsToCheck -= discountedQty;
            }
          }
        });

        // Apply promotions for all lines
        groupAverage.forEach(line => {
          let price = line.price,
            qtyToApplyDisc = line.qtyToApplyDisc,
            discountAmt = (totalDisc * price * qtyToApplyDisc) / totalPrice;

          this.addDiscount(line, OB.DEC.toNumber(discountAmt), {
            qtyOffer: qtyToApplyDisc,
            chunks: chunks
          });
        });
      }
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
    '312D41071ED34BA18B748607CA679F44'
  ] = BuyXPayYDifferentProducts;
})();
