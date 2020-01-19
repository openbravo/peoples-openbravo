/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  class PriceExcludingTax extends OB.Taxes.Tax {
    /* @Override */
    getLineTaxes(line, rules) {
      OB.debug(
        `PriceExcludingTax: calculating line taxes for ticket with id: ${this.ticket.id} and line with id: ${line.id}`
      );
      const currentTax = rules[0];
      const taxRate = OB.Taxes.Tax.getTaxRate(currentTax.rate);
      const lineNetAmount = line.amount;
      const lineTaxAmount = OB.Taxes.Tax.calculateTaxAmount(
        lineNetAmount,
        taxRate
      );
      const lineGrossAmount = OB.Taxes.PriceExcludingTax.calculateGrossAmountFromNetAmount(
        lineNetAmount,
        lineTaxAmount
      );

      return {
        id: line.id,
        gross: lineGrossAmount,
        net: lineNetAmount,
        tax: currentTax.id,
        taxes: [
          {
            base: lineNetAmount,
            amount: lineTaxAmount,
            tax: currentTax
          }
        ]
      };
    }

    /* @Override */
    getHeaderTaxes(lineTaxes) {
      OB.debug(
        `PriceExcludingTax: calculating header taxes for ticket with id: ${this.ticket.id}`
      );
      const linesByTax = OB.App.ArrayUtils.groupBy(lineTaxes, 'tax');
      const headerTaxes = Object.keys(linesByTax).map(tax => {
        const lines = linesByTax[tax];
        const taxRate = OB.Taxes.Tax.getTaxRate(lines[0].taxes[0].tax.rate);

        const netAmount = lines.reduce((line1, line2) => line1 + line2.net, 0);
        const taxAmount = OB.Taxes.Tax.calculateTaxAmount(netAmount, taxRate);
        const grossAmount = OB.Taxes.PriceExcludingTax.calculateGrossAmountFromNetAmount(
          netAmount,
          taxAmount
        );

        // If the header gross amount is different than the sum of line gross amounts, we need to adjust the highest line gross amount
        const adjustment = OB.DEC.sub(
          grossAmount,
          lines.reduce((line1, line2) => line1 + line2.gross, 0)
        );
        if (OB.DEC.compare(adjustment) !== 0) {
          const line = lines.sort(
            (line1, line2) => OB.DEC.abs(line2.gross) - OB.DEC.abs(line1.gross)
          )[0];
          line.gross = OB.DEC.add(line.gross, adjustment);
        }

        return {
          base: netAmount,
          amount: taxAmount,
          tax: lines[0].taxes[0].tax
        };
      });

      const grossAmount = headerTaxes.reduce(
        (lineTax1, lineTax2) => lineTax1 + lineTax2.base + lineTax2.amount,
        0
      );
      const netAmount = lineTaxes.reduce(
        (line1, line2) => line1 + line2.net,
        0
      );

      return {
        gross: grossAmount,
        net: netAmount,
        taxes: headerTaxes
      };
    }

    // grossAmount = netAmount + taxAmount
    static calculateGrossAmountFromNetAmount(netAmount, taxAmount) {
      return OB.DEC.add(netAmount, taxAmount);
    }
  }

  OB.Taxes.PriceExcludingTax = PriceExcludingTax;
})();
