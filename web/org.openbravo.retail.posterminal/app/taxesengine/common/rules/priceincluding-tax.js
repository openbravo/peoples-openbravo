/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  class PriceIncludingTax extends OB.Taxes.Tax {
    /* @Override */
    getLineTaxes(line, rules) {
      OB.debug(
        `PriceIncludingTax: calculating line taxes for ticket with id: ${this.ticket.id} and line with id: ${line.id}`
      );

      const parentTaxId = OB.Taxes.Tax.getParentTaxId(rules[0]);
      const lineGrossAmount = line.amount;
      const lineGrossPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        lineGrossAmount,
        line.quantity
      );
      const lineNetAmount = OB.Taxes.PriceIncludingTax.calculateNetAmountFromGrossAmount(
        lineGrossAmount,
        rules
      );
      const lineNetPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        lineNetAmount,
        line.quantity
      );
      const lineTaxes = OB.Taxes.PriceIncludingTax.calculateTaxes(
        lineGrossAmount,
        lineNetAmount,
        rules
      );

      return {
        id: line.id,
        grossAmount: lineGrossAmount,
        netAmount: lineNetAmount,
        grossPrice: lineGrossPrice,
        netPrice: lineNetPrice,
        tax: parentTaxId,
        taxes: lineTaxes
      };
    }

    /* @Override */
    getHeaderTaxes(lines) {
      OB.debug(
        `PriceIncludingTax: calculating header taxes for ticket with id: ${this.ticket.id}`
      );

      const linesByParentTaxId = OB.App.ArrayUtils.groupBy(lines, 'tax');
      const groupTaxes = Object.values(linesByParentTaxId).map(groupLines => {
        const rules = groupLines[0].taxes.map(lineTax => lineTax.tax);
        const groupGrossAmount = groupLines.reduce(
          (total, line) => OB.DEC.add(total, line.grossAmount),
          OB.DEC.Zero
        );
        const groupNetAmount = OB.Taxes.PriceIncludingTax.calculateNetAmountFromGrossAmount(
          groupGrossAmount,
          rules
        );

        OB.Taxes.PriceIncludingTax.adjustLineNetAmount(
          groupNetAmount,
          groupLines
        );

        return OB.Taxes.PriceIncludingTax.calculateTaxes(
          groupGrossAmount,
          groupNetAmount,
          rules
        );
      });

      const headerGrossAmount = lines.reduce(
        (total, line) => OB.DEC.add(total, line.grossAmount),
        OB.DEC.Zero
      );
      const headerNetAmount = lines.reduce(
        (total, line) => OB.DEC.add(total, line.netAmount),
        OB.DEC.Zero
      );
      const headerTaxes = groupTaxes.flat();

      return {
        grossAmount: headerGrossAmount,
        netAmount: headerNetAmount,
        taxes: headerTaxes
      };
    }

    static calculateTaxes(grossAmount, netAmount, rules) {
      let taxBase = netAmount;
      const taxes = rules.map(rule => {
        const taxRate = OB.Taxes.Tax.getTaxRate(rule.rate);
        const taxAmount = OB.Taxes.Tax.calculateTaxAmount(taxBase, taxRate);
        const tax = {
          base: taxBase,
          amount: taxAmount,
          tax: rule
        };
        taxBase = OB.DEC.add(taxBase, taxAmount);
        return tax;
      });

      OB.Taxes.PriceIncludingTax.adjustTaxAmount(grossAmount, netAmount, taxes);
      return taxes;
    }

    /**
     * If gross amount <> net amount + tax amount, we need to adjust the highest tax amount
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
        const tax = taxes.sort(
          (tax1, tax2) => OB.DEC.abs(tax2.amount) - OB.DEC.abs(tax1.amount)
        )[0];
        tax.amount = OB.DEC.add(tax.amount, adjustment);
      }
    }

    /**
     * If the header net amount is different than the sum of line net amounts, we need to adjust the highest line net amount
     */
    static adjustLineNetAmount(netAmount, lines) {
      const adjustment = OB.DEC.sub(
        netAmount,
        lines.reduce(
          (total, line) => OB.DEC.add(total, line.netAmount),
          OB.DEC.Zero
        )
      );
      if (OB.DEC.compare(adjustment) !== 0) {
        const line = lines.sort(
          (line1, line2) =>
            OB.DEC.abs(line2.netAmount) - OB.DEC.abs(line1.netAmount)
        )[0];
        line.netAmount = OB.DEC.add(line.netAmount, adjustment);
        line.taxes[0].base = OB.DEC.add(line.taxes[0].base, adjustment);
      }
    }

    /**
     * netAmount = (grossAmount * grossAmount) / (grossAmount + (grossAmount * taxRate))
     */
    static calculateNetAmountFromGrossAmount(grossAmount, rules) {
      if (OB.DEC.compare(grossAmount) === 0) {
        return OB.DEC.Zero;
      }

      const amount = new BigDecimal(String(grossAmount));
      const taxAmount = rules.reduce((total, rule) => {
        const taxRate = OB.Taxes.Tax.getTaxRate(rule.rate);
        return total.add(total.add(amount).multiply(taxRate));
      }, BigDecimal.prototype.ZERO);

      return OB.DEC.toNumber(
        amount
          .multiply(amount)
          .divide(amount.add(taxAmount), 20, BigDecimal.prototype.ROUND_HALF_UP)
      );
    }
  }

  OB.Taxes.PriceIncludingTax = PriceIncludingTax;
})();
