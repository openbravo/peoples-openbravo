/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class FixedPercentage extends OB.Discounts.Discount {
    constructor(ticket, discountImpl, discounts) {
      super(ticket, discountImpl, discounts);
    }

    /* @Override */
    calculateDiscounts() {
      this.getApplicableLines().forEach(line => {
        let disc = OB.DEC.mul(
          this.getPrice(line),
          OB.DEC.div(this.discountImpl.discount, 100)
        );
        this.addDiscount(line, disc);
      });
    }
  }

  OB.Discounts.discountRules[
    '697A7AB9FD9C4EE0A3E891D3D3CCA0A7'
  ] = FixedPercentage;
})();
