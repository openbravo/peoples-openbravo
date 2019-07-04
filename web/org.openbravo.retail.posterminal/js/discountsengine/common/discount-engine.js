/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB = OB || {};
  OB.Discounts = OB.Discounts || {};
  OB.Discounts.applyDiscounts = function(ticket, rules) {
    OB.debug('applying discounts for ', rules.length);

    let discounts = [];
    rules.forEach(discountImpl => {
      OB.debug(discountImpl.discountType);
      let DiscountRule = OB.Discounts.discountRules[discountImpl.discountType];
      if (DiscountRule) {
        let r = new DiscountRule(ticket, discountImpl, discounts);
        r.calculateDiscounts();
      }
    });
    return discounts;
  };
})();
