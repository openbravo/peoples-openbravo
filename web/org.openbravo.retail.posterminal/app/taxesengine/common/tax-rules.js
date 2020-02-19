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

    applyTaxes() {
      const taxes = {};
      taxes.header = {};
      taxes.lines = [];

      // Calculate taxes for each ticket line
      taxes.lines = this.ticket.lines.map(line => this.applyLineTaxes(line));

      // Calculate taxes for ticket header in case there was no error calculating taxes for ticket lines
      const lineWithError = taxes.lines.find(line => line.error);
      if (!lineWithError) {
        taxes.header = this.applyHeaderTaxes(taxes.lines);
      }

      return taxes;
    }

    applyLineTaxes(line) {
      if (line.bomLines) {
        return this.calculateLineBOMTaxes(line);
      }

      return this.calculateLineTaxes(line);
    }

    calculateLineTaxes(line) {
      const rulesFilteredByLine = OB.Taxes.filterRulesByTicketLine(
        this.ticket,
        line,
        this.rules
      );

      if (rulesFilteredByLine.length === 0) {
        return {
          id: line.id,
          error: 'No tax found'
        };
      }

      return this.getLineTaxes(line, rulesFilteredByLine);
    }

    calculateLineBOMTaxes(line) {
      const bomGroups = line.bomLines
        .reduce((result, bomLine) => {
          const bomGroup = result.find(
            group => group.product.taxCategory === bomLine.product.taxCategory
          );
          if (bomGroup) {
            bomGroup.amount = OB.DEC.add(bomGroup.amount, bomLine.amount);
          } else {
            result.push(bomLine);
          }
          return result;
        }, [])
        .sort((a, b) => a.amount - b.amount);

      // FIXME: adjust the bigger amount if necessary
      const bomTotalAmount = bomGroups.reduce(
        (total, bomGroup) => OB.DEC.add(total, bomGroup.amount),
        OB.DEC.Zero
      );

      const lineTaxes = this.calculateLineTaxes(line);
      const lineBomTaxes = bomGroups.flatMap(bomGroup => {
        const bomLine = bomGroup;
        bomLine.amount = OB.DEC.div(
          OB.DEC.mul(bomGroup.amount, line.amount),
          bomTotalAmount
        );
        return this.calculateLineTaxes(bomLine);
      });

      const bomLineWithError = lineBomTaxes.find(bomLine => bomLine.error);
      lineTaxes.error =
        lineTaxes.error || bomLineWithError ? bomLineWithError.error : null;
      if (lineTaxes.error) {
        return lineTaxes;
      }

      lineTaxes.taxes = lineBomTaxes.flatMap(lineBomTax => lineBomTax.taxes);
      lineTaxes.bomLines = lineBomTaxes;
      lineTaxes.grossAmount = lineBomTaxes.reduce(
        (total, bomLine) => OB.DEC.add(total, bomLine.grossAmount),
        OB.DEC.Zero
      );
      lineTaxes.netAmount = lineBomTaxes.reduce(
        (total, bomLine) => OB.DEC.add(total, bomLine.netAmount),
        OB.DEC.Zero
      );

      return lineTaxes;
    }

    applyHeaderTaxes(lines) {
      // Lines with bom taxes are split by each different tax category
      const lineTaxes = lines.flatMap(line =>
        line.bomLines ? line.bomLines : line
      );

      return this.getHeaderTaxes(lineTaxes);
    }

    // eslint-disable-next-line no-unused-vars
    getLineTaxes(line, rules) {
      throw new Error(
        `${this.constructor.name} does not implement getLineTaxes()`
      );
    }

    // eslint-disable-next-line no-unused-vars
    getHeaderTaxes(lines) {
      throw new Error(
        `${this.constructor.name} does not implement getHeaderTaxes()`
      );
    }

    /**
     * Return parent rule id if given rule has a parent, otherwise return given rule id
     */
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

      OB.Taxes.Tax.adjustTaxAmount(grossAmount, netAmount, taxes);
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
      if (rule.cascade || (rule.taxBase && rule.baseAmount === 'LNATAX')) {
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
          .filter(childTax => childTax.tax.taxBase === adjustedTax.tax.id)
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
