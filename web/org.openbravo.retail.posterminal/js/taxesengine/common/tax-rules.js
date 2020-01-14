/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class Tax {
    constructor(ticket, rules) {
      this.ticket = ticket;
      this.rules = rules;
    }

    calculateTaxes() {
      const taxes = {};

      taxes.lines = [];
      this.ticket.lines.forEach(line => {
        const rules = this.filterTaxesByTaxCategoryAndLocation(line);
        const lineTaxes = this.getLineTaxes(line, rules);
        taxes.lines.push(lineTaxes);
      });

      const headerTaxes = this.getHeaderTaxes(taxes.lines);
      taxes.header = headerTaxes;

      return taxes;
    }

    getLineTaxes(line, rules) {
      throw 'getLineTaxes method not implemented';
    }

    getHeaderTaxes(lines) {
      throw 'getHeaderTaxes method not implemented';
    }

    filterTaxesByTaxCategoryAndLocation(line) {
      return this.filterTaxesByLocation(
        this.filterTaxesByTaxCategory(this.rules, line)
      );
    }

    filterTaxesByTaxCategory(rules, line) {
      return rules.filter(rule => {
        const isTaxExempt =
          line.taxExempt || this.ticket.businessPartner.taxExempt;
        return (
          (isTaxExempt
            ? rule.taxExempt === isTaxExempt
            : rule.businessPartnerTaxCategory ===
              this.ticket.businessPartner.taxCategory) &&
          rule.taxCategory === line.product.taxCategory
        );
      });
    }

    filterTaxesByLocation(rules) {
      return rules.filter(
        taxRate =>
          taxRate.destinationCountry === rules[0].destinationCountry &&
          taxRate.destinationRegion === rules[0].destinationRegion &&
          taxRate.country === rules[0].country &&
          taxRate.region === rules[0].region &&
          taxRate.validFromDate === rules[0].validFromDate
      );
    }
  }

  OB.Taxes.Tax = Tax;
})();
