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
      // Calculate taxes for each ticket line
      const lineTaxes = this.ticket.lines.map(line =>
        this.applyLineTaxes(line)
      );

      // Calculate taxes for ticket header
      const headerTaxes = this.applyHeaderTaxes(lineTaxes);

      return { ...headerTaxes, lines: lineTaxes };
    }

    applyLineTaxes(line) {
      const newLine = { ...line };
      newLine.amount = this.ticket.priceIncludesTax
        ? line.grossUnitAmount
        : line.netUnitAmount;

      if (newLine.product.productBOM) {
        newLine.product.productBOM = line.product.productBOM.map(bomLine => {
          const newBomLine = { ...bomLine };
          newBomLine.amount = this.ticket.priceIncludesTax
            ? bomLine.grossUnitAmount
            : bomLine.netUnitAmount;
          return newBomLine;
        });
        return this.calculateLineBOMTaxes(newLine);
      }

      return this.calculateLineTaxes(newLine);
    }

    calculateLineTaxes(line) {
      const rulesFilteredByLine = OB.Taxes.filterRulesByTicketLine(
        this.ticket,
        line,
        this.rules
      );

      if (rulesFilteredByLine.length === 0) {
        throw new OB.App.Class.TaxEngineError('OBPOS_NoTaxFoundForProduct', [
          // eslint-disable-next-line no-underscore-dangle
          line.product._identifier
        ]);
      }

      return this.getLineTaxes(line, rulesFilteredByLine);
    }

    calculateLineBOMTaxes(line) {
      const bomTotalAmount = line.product.productBOM.reduce(
        (total, bomLine) => OB.DEC.add(total, bomLine.amount),
        OB.DEC.Zero
      );

      const bomGroups = line.product.productBOM
        .reduce((result, bomLine) => {
          const bomGroup = result.find(
            group => group.product.taxCategory === bomLine.product.taxCategory
          );
          if (bomGroup) {
            bomGroup.amount = OB.DEC.add(bomGroup.amount, bomLine.amount);
          } else {
            const updatedLine = { ...line };
            updatedLine.id = bomLine.id;
            updatedLine.amount = bomLine.amount;
            updatedLine.quantity = bomLine.quantity;
            updatedLine.product = bomLine.product;
            result.push(updatedLine);
          }
          return result;
        }, [])
        .map(bomGroup => {
          const bomLine = bomGroup;
          bomLine.amount = OB.DEC.div(
            OB.DEC.mul(bomGroup.amount, line.amount),
            bomTotalAmount
          );
          return bomLine;
        })
        .sort((a, b) => OB.DEC.abs(a.amount) - OB.DEC.abs(b.amount));

      const adjustment = OB.DEC.sub(
        line.amount,
        bomGroups.reduce(
          (total, bomGroup) => OB.DEC.add(total, bomGroup.amount),
          OB.DEC.Zero
        )
      );
      if (OB.DEC.compare(adjustment) !== 0) {
        const bomGroup = bomGroups.reduce((bomGroup1, bomGroup2) => {
          return OB.DEC.abs(bomGroup1.amount) > OB.DEC.abs(bomGroup2.amount)
            ? bomGroup1
            : bomGroup2;
        });
        bomGroup.amount = OB.DEC.add(bomGroup.amount);
      }

      const lineTaxes = this.calculateLineTaxes(line);
      const lineBomTaxes = bomGroups.flatMap(bomGroup => {
        return this.calculateLineTaxes(bomGroup);
      });

      lineTaxes.taxes = lineBomTaxes.flatMap(lineBomTax => lineBomTax.taxes);
      lineTaxes.bomLines = lineBomTaxes;
      lineTaxes.grossUnitAmount = lineBomTaxes.reduce(
        (total, bomLine) => OB.DEC.add(total, bomLine.grossUnitAmount),
        OB.DEC.Zero
      );
      lineTaxes.netUnitAmount = lineBomTaxes.reduce(
        (total, bomLine) => OB.DEC.add(total, bomLine.netUnitAmount),
        OB.DEC.Zero
      );
      lineTaxes.grossUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        lineTaxes.grossUnitAmount,
        lineTaxes.qty
      );
      lineTaxes.netUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        lineTaxes.netUnitAmount,
        lineTaxes.qty
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
      throw new OB.App.Class.TaxEngineError(
        `${this.constructor.name} does not implement getLineTaxes()`
      );
    }

    // eslint-disable-next-line no-unused-vars
    getHeaderTaxes(lines) {
      throw new OB.App.Class.TaxEngineError(
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
     * The tax amount with the highest variance will be the one with the lowest abs(rounded tax amount + adjustment - exact tax amount)
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
              .add(new BigDecimal(String(adjustment)))
              .subtract(OB.Taxes.Tax.calculateTaxAmount(tax.base, tax.tax))
              .abs();
          };
          return calculateDifference(tax1).compareTo(
            calculateDifference(tax2)
          ) <= 0
            ? tax1
            : tax2;
        });
        adjustedTax.amount = OB.DEC.add(adjustedTax.amount, adjustment);
        taxes
          .filter(
            childTax =>
              childTax.tax.id !== adjustedTax.tax.id &&
              (childTax.tax.cascade ||
                childTax.tax.taxBase === adjustedTax.tax.id)
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
     * price = amount / qty
     */
    static calculatePriceFromAmount(amount, qty) {
      if (OB.DEC.compare(qty) === 0) {
        return OB.DEC.Zero;
      }

      return OB.DEC.div(amount, qty);
    }

    /**
     * lineTaxRate = 1 + mul(rate / 100) for each rule
     */
    static calculateLineTaxRate(taxes) {
      return OB.DEC.toNumber(
        taxes.reduce(
          (total, tax) =>
            total.multiply(
              BigDecimal.prototype.ONE.add(
                OB.Taxes.Tax.getTaxRate(tax.tax.rate)
              )
            ),
          BigDecimal.prototype.ONE
        )
      );
    }

    /**
     * Group line taxes by taxId in order to compute them at header level
     */
    static groupLinesByTax(lines) {
      const result = [];
      return lines.reduce((object, item) => {
        (result[item.tax] = object[item.tax] || []).push(item);
        return result;
      }, {});
    }
  }

  OB.Taxes.Tax = Tax;
})();