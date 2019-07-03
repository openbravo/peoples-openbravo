/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  function FixedPercentage(ticket, discountImpl, discounts) {
    OB.Discounts.Discount.call(this, ticket, discountImpl, discounts);
  }
  FixedPercentage.prototype = Object.create(OB.Discounts.Discount.prototype);

  FixedPercentage.prototype.calculateDiscounts = function() {
    let me = this; // due to partial ES6 support in nashorn
    this.getApplicableLines().forEach(line => {
      let disc = OB.DEC.mul(
        me.getPrice(line),
        OB.DEC.div(me.discountImpl.discount, 100)
      );
      me.addDiscount(line, disc);
    });
  };

  OB.Discounts.discountRules[
    '697A7AB9FD9C4EE0A3E891D3D3CCA0A7'
  ] = FixedPercentage;
})();
