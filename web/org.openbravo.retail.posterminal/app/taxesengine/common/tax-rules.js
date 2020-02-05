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
      const taxes = {};
      taxes.header = {};
      taxes.lines = [];

      const rulesFilteredByTicket = this.filterTaxRulesByTicket(
        this.ticket,
        this.rules
      );

      this.ticket.lines.forEach(line => {
        const rulesFilteredByLine = this.filterTaxRulesByLine(
          line,
          rulesFilteredByTicket
        );

        if (rulesFilteredByLine.length === 0) {
          taxes.lines.push({
            id: line.id,
            error: 'No tax found'
          });
        } else {
          taxes.lines.push(this.getLineTaxes(line, rulesFilteredByLine));
        }
      });

      if (!taxes.lines.find(line => line.error)) {
        taxes.header = this.getHeaderTaxes(taxes.lines);
      }

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
        const businessPartnerCountry = this.ticket.businessPartner.address
          .country;
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
        const businessPartnerRegion = this.ticket.businessPartner.address
          .region;
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
          const businessPartnerCountry = this.ticket.businessPartner.address
            .country;
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
          const businessPartnerRegion = this.ticket.businessPartner.address
            .region;
          return (
            OB.Taxes.Tax.equals(
              rule.destinationRegion,
              businessPartnerRegion
            ) ||
            OB.Taxes.Tax.equals(
              rule.zoneDestinationRegion,
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
            this.ticket.businessPartner.address.country
          )
            ? rule.destinationCountry
            : rule.zoneDestinationCountry;
          updatedRule.destinationRegion = OB.Taxes.Tax.equals(
            rule.destinationRegion,
            this.ticket.businessPartner.address.region
          )
            ? rule.destinationRegion
            : rule.zoneDestinationRegion;
          return updatedRule;
        });
    }

    filterTaxRulesByLine(line, rulesFilteredByTicket) {
      const checkTaxCategory = rule => {
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
      };
      const checkLocation = (rule, rulesFilteredByLine) => {
        return (
          OB.Taxes.Tax.equals(
            rule.destinationCountry,
            rulesFilteredByLine[0].destinationCountry
          ) &&
          OB.Taxes.Tax.equals(
            rule.destinationRegion,
            rulesFilteredByLine[0].destinationRegion
          ) &&
          OB.Taxes.Tax.equals(rule.country, rulesFilteredByLine[0].country) &&
          OB.Taxes.Tax.equals(rule.region, rulesFilteredByLine[0].region) &&
          OB.Taxes.Tax.equals(
            rule.validFromDate,
            rulesFilteredByLine[0].validFromDate
          )
        );
      };
      const sortByLineno = (rule1, rule2) => {
        return rule1.lineNo - rule2.lineNo;
      };
      const sortByTaxBase = (rule1, rule2) => {
        const checkTaxBase = rule => {
          return rule.taxBase;
        };
        if (!checkTaxBase(rule1) && checkTaxBase(rule2)) {
          return -1;
        }
        if (checkTaxBase(rule1) && !checkTaxBase(rule2)) {
          return 1;
        }
        return 0;
      };

      return rulesFilteredByTicket
        .filter(rule => checkTaxCategory(rule))
        .filter((rule, index, rulesFilteredByLine) =>
          checkLocation(rule, rulesFilteredByLine)
        )
        .sort(
          (rule1, rule2) =>
            sortByLineno(rule1, rule2) || sortByTaxBase(rule1, rule2)
        );
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

    static getParentTaxId(rule) {
      if (rule.parentTax) {
        return rule.parentTax;
      }

      return rule.id;
    }

    /**
     * Calculate tax base and amount for each rule
     */
    static calculateTaxes(grossAmount, netAmount, rules) {
      let accumulatedTaxBase = netAmount;
      const taxes = rules.map(rule => {
        const ruleTaxBase = OB.Taxes.Tax.calculateTaxBase(
          netAmount,
          accumulatedTaxBase,
          rule
        );
        const ruleTaxAmount = OB.DEC.toNumber(
          OB.Taxes.Tax.calculateTaxAmount(ruleTaxBase, rule)
        );
        const tax = {
          base: ruleTaxBase,
          amount: ruleTaxAmount,
          tax: rule
        };
        accumulatedTaxBase = OB.DEC.add(accumulatedTaxBase, ruleTaxAmount);
        return tax;
      });

      OB.Taxes.PriceIncludingTax.adjustTaxAmount(grossAmount, netAmount, taxes);
      return taxes;
    }

    /**
     * Calculate tax base and amount for each rule
     */
    static calculateTaxesFromLinesTaxes(lines, rules) {
      return rules.map(rule => {
        return lines
          .flatMap(line => line.taxes)
          .filter(lineTaxes => lineTaxes.tax.id === rule.id)
          .reduce(
            (total, line) => {
              return {
                base: OB.DEC.add(total.base, line.base),
                amount: OB.DEC.add(total.amount, line.amount),
                tax: total.tax
              };
            },
            { base: OB.DEC.Zero, amount: OB.DEC.Zero, tax: rule }
          );
      });
    }

    /**
     * If rule is cascade or dependant, we take as tax base the tax base of the previous tax,
     * if not, we take as tax base the net amount
     */
    static calculateTaxBase(netAmount, accumulatedTaxBase, rule) {
      if (rule.taxBase && rule.baseAmount === 'LNATAX') {
        return accumulatedTaxBase;
      }
      return netAmount;
    }

    /**
     * totalTaxAmount = sum(taxBase * taxRate) for each rule
     */
    static calculateTotalTaxAmount(taxBase, rules) {
      return rules.reduce((total, rule) => {
        const ruleTaxBase = OB.Taxes.Tax.calculateTaxBase(
          taxBase,
          OB.DEC.add(total, taxBase),
          rule
        );
        return total.add(OB.Taxes.Tax.calculateTaxAmount(ruleTaxBase, rule));
      }, BigDecimal.prototype.ZERO);
    }

    /**
     * taxAmount = taxBase * taxRate
     */
    static calculateTaxAmount(taxBase, rule) {
      const amount = new BigDecimal(String(taxBase));
      const taxRate = OB.Taxes.Tax.getTaxRate(rule.rate);
      return amount.multiply(taxRate);
    }

    /**
     * Adjust the tax amount with the highest variance in case gross amount <> net amount + tax amount
     * The tax amount with the highest variance will be the lowest abs(rounded tax amount - (exact tax amount + adjustment))
     * The tax base of dependant taxes will be adjusted as well
     */
    static adjustTaxAmount(grossAmount, netAmount, taxes) {
      const taxAmount = taxes.reduce(
        (total, tax) => OB.DEC.add(total, tax.amount),
        OB.DEC.Zero
      );
      const adjustment = OB.DEC.sub(
        grossAmount,
        OB.DEC.add(netAmount, taxAmount)
      );
      if (OB.DEC.compare(adjustment) !== 0) {
        const adjustedTax = taxes.reduce((tax1, tax2) => {
          const calculateDifference = tax => {
            return new BigDecimal(String(tax.amount))
              .subtract(
                OB.Taxes.Tax.calculateTaxAmount(tax.base, tax.tax).add(
                  new BigDecimal(String(adjustment))
                )
              )
              .abs();
          };
          return calculateDifference(tax1).compareTo(
            calculateDifference(tax2)
          ) > 0
            ? tax1
            : tax2;
        });
        adjustedTax.amount = OB.DEC.add(adjustedTax.amount, adjustment);
        taxes
          .filter(childTax =>
            OB.Taxes.Tax.equals(childTax.tax.taxBase, adjustedTax.tax.id)
          )
          .map(childTax => {
            const updatedChildTax = childTax;
            updatedChildTax.base = OB.DEC.add(childTax.base, adjustment);
            return updatedChildTax;
          });
      }
    }

    /**
     * taxRate = rate / 100
     */
    static getTaxRate(rate) {
      return new BigDecimal(String(rate)).divide(
        new BigDecimal('100'),
        20,
        BigDecimal.prototype.ROUND_HALF_UP
      );
    }

    /**
     * price = amount / quantity
     */
    static calculatePriceFromAmount(amount, quantity) {
      if (OB.DEC.compare(quantity) === 0) {
        return OB.DEC.Zero;
      }

      return OB.DEC.div(amount, quantity);
    }
  }

  OB.Taxes.Tax = Tax;
})();
