/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class PriceExcludingTax extends OB.Taxes.Tax {
    constructor(ticket, rules) {
      super(ticket, rules);
    }

    /* @Override */
    getLineTaxes(line, rules) {
      return;
    }

    /* @Override */
    getHeaderTaxes(lines) {
      return;
    }
  }

  OB.Taxes.PriceExcludingTax = PriceExcludingTax;
})();
