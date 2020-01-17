/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  class Tax {
    constructor(ticket, rules) {
      this.ticket = ticket;
      this.rules = rules;
    }

    calculateTaxes() {
      const rulesFilteredByTicket = this.filterTaxRulesByTicket(
        this.ticket,
        this.rules
      );
      const taxes = {};

      taxes.lines = [];
      this.ticket.lines.forEach(line => {
        const rulesFilteredByLine = this.filterTaxRulesByLine(
          line,
          rulesFilteredByTicket
        );
        const lineTaxes = this.getLineTaxes(line, rulesFilteredByLine);
        taxes.lines.push(lineTaxes);
      });

      const headerTaxes = this.getHeaderTaxes(taxes.lines);
      taxes.header = headerTaxes;

      return taxes;
    }

    // getLineTaxes(line, rules) {
    //   // line
    //   throw 'getLineTaxes method not implemented';
    // }

    // getHeaderTaxes(lineTaxes) {
    //   throw 'getHeaderTaxes method not implemented';
    // }

    filterTaxRulesByTicket() {
      const checkValidFromDate = rule => {
        return new Date(rule.validFromDate) <= new Date(this.ticket.date);
      };
      const checkIsCashVAT = rule => {
        return (
          rule.isCashVAT === this.ticket.isCashVat ||
          (!rule.isCashVAT && (rule.withholdingTax || rule.rate === 0))
        );
      };
      const checkCountry = rule => {
        const businessPartnerCountry = this.ticket.businessPartner.country;
        return (
          rule.destinationCountry === businessPartnerCountry ||
          rule.zoneDestinationCountry === businessPartnerCountry ||
          (!rule.destinationCountry && !rule.zoneDestinationCountry)
        );
      };
      const checkRegion = rule => {
        const businessPartnerRegion = this.ticket.businessPartner.region;
        return (
          rule.destinationRegion === businessPartnerRegion ||
          rule.zoneDestinationRegion === businessPartnerRegion ||
          (!rule.destinationRegion && !rule.zoneDestinationRegion)
        );
      };
      const sortByCountryFrom = (rule1, rule2) => {
        const checkCountryFrom = rule => {
          return rule.country || rule.zoneCountry;
        };
        if (checkCountryFrom(rule1) && !checkCountryFrom(rule2)) {
          return -1;
        }
        if (!checkCountryFrom(rule1) && checkCountryFrom(rule2)) {
          return 1;
        }
        return 0;
      };
      const sortByCountryTo = (rule1, rule2) => {
        const checkCountryTo = rule => {
          const businessPartnerCountry = this.ticket.businessPartner.country;
          return (
            rule.destinationCountry === businessPartnerCountry ||
            rule.zoneDestinationCountry === businessPartnerCountry
          );
        };
        if (checkCountryTo(rule1) && !checkCountryTo(rule2)) {
          return -1;
        }
        if (!checkCountryTo(rule1) && checkCountryTo(rule2)) {
          return 1;
        }
        return 0;
      };
      const sortByRegionFrom = (rule1, rule2) => {
        const checkRegionFrom = rule => {
          return rule.region || rule.zoneRegion;
        };
        if (checkRegionFrom(rule1) && !checkRegionFrom(rule2)) {
          return -1;
        }
        if (!checkRegionFrom(rule1) && checkRegionFrom(rule2)) {
          return 1;
        }
        return 0;
      };
      const sortByRegionTo = (rule1, rule2) => {
        const checkRegionTo = rule => {
          const businessPartnerRegion = this.ticket.businessPartner.region;
          return (
            rule.destinationRegion === businessPartnerRegion ||
            rule.zoneDstinationRegion === businessPartnerRegion
          );
        };
        if (checkRegionTo(rule1) && !checkRegionTo(rule2)) {
          return -1;
        }
        if (!checkRegionTo(rule1) && checkRegionTo(rule2)) {
          return 1;
        }
        return 0;
      };
      const sortByDate = (rule1, rule2) => {
        return new Date(rule2.validFromDate) - new Date(rule1.validFromDate);
      };
      const sortByDefault = (rule1, rule2) => {
        return rule2.default - rule1.default;
      };

      return this.rules
        .filter(
          rule =>
            checkValidFromDate(rule) &&
            checkIsCashVAT(rule) &&
            checkCountry(rule) &&
            checkRegion(rule)
        )
        .sort(
          (rule1, rule2) =>
            sortByRegionTo(rule1, rule2) ||
            sortByRegionFrom(rule1, rule2) ||
            sortByCountryTo(rule1, rule2) ||
            sortByCountryFrom(rule1, rule2) ||
            sortByDate(rule1, rule2) ||
            sortByDefault(rule1, rule2)
        )
        .map(rule => {
          const updatedRule = rule;
          updatedRule.country =
            rule.country === this.ticket.country
              ? rule.country
              : rule.zoneCountry;
          updatedRule.region =
            rule.region === this.ticket.region ? rule.region : rule.zoneRegion;
          updatedRule.destinationCountry =
            rule.destinationCountry === this.ticket.businessPartner.country
              ? rule.destinationCountry
              : rule.zoneDestinationCountry;
          updatedRule.destinationRegion =
            rule.destinationRegion === this.ticket.businessPartner.region
              ? rule.destinationRegion
              : rule.zoneDestinationRegion;
          return updatedRule;
        });
    }

    filterTaxRulesByLine(line, rulesFilteredByTicket) {
      const filterTaxRulesByTaxCategory = rules => {
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
      };

      const filterTaxRulesByLocation = rules => {
        return rules.filter(
          taxRate =>
            taxRate.destinationCountry === rules[0].destinationCountry &&
            taxRate.destinationRegion === rules[0].destinationRegion &&
            taxRate.country === rules[0].country &&
            taxRate.region === rules[0].region &&
            taxRate.validFromDate === rules[0].validFromDate
        );
      };

      const rulesFilteredByLine = filterTaxRulesByTaxCategory(
        rulesFilteredByTicket
      );
      return filterTaxRulesByLocation(rulesFilteredByLine);
    }

    // taxRate = rate / 100
    static getTaxRate(rate) {
      return new BigDecimal(String(rate)).divide(
        new BigDecimal('100'),
        20,
        BigDecimal.prototype.ROUND_HALF_UP
      );
    }

    // taxAmount = netAmount * taxRate
    static calculateTaxAmount(netAmount, taxRate) {
      return OB.DEC.mul(netAmount, taxRate);
    }
  }

  OB.Taxes.Tax = Tax;
})();
