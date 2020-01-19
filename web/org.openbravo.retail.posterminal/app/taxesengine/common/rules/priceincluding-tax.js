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
      const currentTax = rules[0];
      const taxRate = OB.Taxes.Tax.getTaxRate(currentTax.rate);
      const lineGrossAmount = line.amount;
      const lineNetAmount = OB.Taxes.PriceIncludingTax.calculateNetAmountFromGrossAmount(
        lineGrossAmount,
        taxRate
      );
      let lineTaxAmount = OB.Taxes.Tax.calculateTaxAmount(
        lineNetAmount,
        taxRate
      );

      // If line gross amount <> line net amount + line tax amount, we need to adjust the highest line tax amount
      lineTaxAmount = OB.DEC.add(
        lineTaxAmount,
        OB.DEC.sub(lineGrossAmount, OB.DEC.add(lineNetAmount, lineTaxAmount))
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
        `PriceIncludingTax: calculating header taxes for ticket with id: ${this.ticket.id}`
      );
      const linesByTax = OB.App.ArrayUtils.groupBy(lineTaxes, 'tax');
      const headerTaxes = Object.keys(linesByTax).map(tax => {
        const lines = linesByTax[tax];
        const taxRate = OB.Taxes.Tax.getTaxRate(lines[0].taxes[0].tax.rate);

        const grossAmount = lines.reduce(
          (line1, line2) => line1 + line2.gross,
          0
        );
        const netAmount = OB.Taxes.PriceIncludingTax.calculateNetAmountFromGrossAmount(
          grossAmount,
          taxRate
        );
        let taxAmount = OB.Taxes.Tax.calculateTaxAmount(netAmount, taxRate);

        // If header gross amount <> header net amount + header tax amount, we need to adjust the highest header tax amount
        taxAmount = OB.DEC.add(
          taxAmount,
          OB.DEC.sub(grossAmount, OB.DEC.add(netAmount, taxAmount))
        );

        // If the header net amount is different than the sum of line net amounts, we need to adjust the highest line net amount
        const adjustment = OB.DEC.sub(
          netAmount,
          lines.reduce((line1, line2) => line1 + line2.net, 0)
        );
        if (OB.DEC.compare(adjustment) !== 0) {
          const line = lines.sort(
            (line1, line2) => OB.DEC.abs(line2.net) - OB.DEC.abs(line1.net)
          )[0];
          line.net = OB.DEC.add(line.net, adjustment);
          line.taxes[0].base = OB.DEC.add(line.taxes[0].base, adjustment);
        }

        return {
          base: netAmount,
          amount: taxAmount,
          tax: lines[0].taxes[0].tax
        };
      });

      const grossAmount = lineTaxes.reduce(
        (line1, line2) => line1 + line2.gross,
        0
      );
      const netAmount = headerTaxes.reduce(
        (lineTax1, lineTax2) => lineTax1 + lineTax2.base,
        0
      );

      return {
        gross: grossAmount,
        net: netAmount,
        taxes: headerTaxes
      };
    }

    // netAmount = (grossAmount * grossAmount) / (grossAmount + (grossAmount * taxRate))
    static calculateNetAmountFromGrossAmount(grossAmount, taxRate) {
      const amount = new BigDecimal(String(grossAmount));
      return OB.DEC.toNumber(
        amount
          .multiply(amount)
          .divide(
            amount.add(amount.multiply(taxRate)),
            20,
            BigDecimal.prototype.ROUND_HALF_UP
          )
      );
    }
  }

  OB.Taxes.PriceIncludingTax = PriceIncludingTax;
})();
