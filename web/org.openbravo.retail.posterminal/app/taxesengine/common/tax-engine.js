/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  if (window) {
    // For browser
    window.OB = window.OB || {};
  } else {
    // For graal/webservice
    OB = OB || {};
  }

  OB.Taxes = OB.Taxes || {};

  OB.Taxes.calculateTaxes = (ticket, rules) => {
    const rule = ticket.priceIncludesTax
      ? new OB.Taxes.PriceIncludingTax(ticket, rules)
      : new OB.Taxes.PriceExcludingTax(ticket, rules);
    return rule.calculateTaxes();
  };
})();
