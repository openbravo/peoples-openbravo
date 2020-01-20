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

    filterTaxRulesByTicket() {
      const checkValidFromDate = rule => {
        return new Date(rule.validFromDate) <= new Date(this.ticket.date);
      };
      const checkIsCashVAT = rule => {
        return (
          OB.Taxes.Tax.equals(rule.isCashVAT, this.ticket.isCashVat) ||
          (!rule.isCashVAT &&
            (rule.withholdingTax || OB.Taxes.Tax.equals(rule.rate, 0)))
        );
      };
      const checkCountry = rule => {
        const businessPartnerCountry = this.ticket.businessPartner.country;
        return (
          OB.Taxes.Tax.equals(
            rule.destinationCountry,
            businessPartnerCountry
          ) ||
          OB.Taxes.Tax.equals(
            rule.zoneDestinationCountry,
            businessPartnerCountry
          ) ||
          (!rule.destinationCountry && !rule.zoneDestinationCountry)
        );
      };
      const checkRegion = rule => {
        const businessPartnerRegion = this.ticket.businessPartner.region;
        return (
          OB.Taxes.Tax.equals(rule.destinationRegion, businessPartnerRegion) ||
          OB.Taxes.Tax.equals(
            rule.zoneDestinationRegion,
            businessPartnerRegion
          ) ||
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
            OB.Taxes.Tax.equals(
              rule.destinationCountry,
              businessPartnerCountry
            ) ||
            OB.Taxes.Tax.equals(
              rule.zoneDestinationCountry,
              businessPartnerCountry
            )
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
            OB.Taxes.Tax.equals(
              rule.destinationRegion,
              businessPartnerRegion
            ) ||
            OB.Taxes.Tax.equals(
              rule.zoneDstinationRegion,
              businessPartnerRegion
            )
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
          updatedRule.country = OB.Taxes.Tax.equals(
            rule.country,
            this.ticket.country
          )
            ? rule.country
            : rule.zoneCountry;
          updatedRule.region = OB.Taxes.Tax.equals(
            rule.region,
            this.ticket.region
          )
            ? rule.region
            : rule.zoneRegion;
          updatedRule.destinationCountry = OB.Taxes.Tax.equals(
            rule.destinationCountry,
            this.ticket.businessPartner.country
          )
            ? rule.destinationCountry
            : rule.zoneDestinationCountry;
          updatedRule.destinationRegion = OB.Taxes.Tax.equals(
            rule.destinationRegion,
            this.ticket.businessPartner.region
          )
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
              ? OB.Taxes.Tax.equals(rule.taxExempt, isTaxExempt)
              : OB.Taxes.Tax.equals(
                  rule.businessPartnerTaxCategory,
                  this.ticket.businessPartner.taxCategory
                )) &&
            OB.Taxes.Tax.equals(rule.taxCategory, line.product.taxCategory)
          );
        });
      };

      const filterTaxRulesByLocation = rules => {
        return rules.filter(
          taxRate =>
            OB.Taxes.Tax.equals(
              taxRate.destinationCountry,
              rules[0].destinationCountry
            ) &&
            OB.Taxes.Tax.equals(
              taxRate.destinationRegion,
              rules[0].destinationRegion
            ) &&
            OB.Taxes.Tax.equals(taxRate.country, rules[0].country) &&
            OB.Taxes.Tax.equals(taxRate.region, rules[0].region) &&
            OB.Taxes.Tax.equals(taxRate.validFromDate, rules[0].validFromDate)
        );
      };

      const rulesFilteredByLine = filterTaxRulesByTaxCategory(
        rulesFilteredByTicket
      );
      return filterTaxRulesByLocation(rulesFilteredByLine);
    }

    static equals(value1, value2) {
      if (
        OB.UTIL.isNullOrUndefined(value1) &&
        OB.UTIL.isNullOrUndefined(value2)
      ) {
        return true;
      }
      return value1 === value2;
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

    static calculatePriceFromAmount(amount, quantity) {
      if (OB.DEC.compare(quantity) === 0) {
        return OB.DEC.Zero;
      }

      return OB.DEC.div(amount, quantity);
    }
  }

  OB.Taxes.Tax = Tax;
})();
