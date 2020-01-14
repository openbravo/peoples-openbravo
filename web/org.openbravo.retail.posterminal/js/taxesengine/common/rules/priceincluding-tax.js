/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class PriceIncludingTax extends OB.Taxes.Tax {
    constructor(ticket, rules) {
      super(ticket, rules);
    }

    /* @Override */
    getLineTaxes(line, rules) {
      // Calculate line net amount and line tax amount with same formula as in C_GET_NET_AMOUNT_FROM_GROSS:
      // lineNetAmount = (lineGrossAmount * lineGrossAmount) / (lineGrossAmount + (lineGrossAmount * taxRate / 100))
      // lineTaxAmount = lineNetAmount * taxRate / 100
      const tax = rules[0];
      const taxRate = new BigDecimal(String(tax.rate)).divide(
        new BigDecimal('100'),
        20,
        BigDecimal.prototype.ROUND_HALF_UP
      );
      const lineGrossAmount = new BigDecimal(String(line.amount));
      const lineNetAmount = OB.DEC.toNumber(
        lineGrossAmount
          .multiply(lineGrossAmount)
          .divide(
            lineGrossAmount.add(lineGrossAmount.multiply(taxRate)),
            20,
            BigDecimal.prototype.ROUND_HALF_UP
          )
      );
      let lineTaxAmount = OB.DEC.mul(lineNetAmount, taxRate);

      // If line gross amount <> line net amount + line tax amount, we need to adjust the line tax amount
      lineTaxAmount = OB.DEC.add(
        lineTaxAmount,
        OB.DEC.sub(lineGrossAmount, OB.DEC.add(lineNetAmount, lineTaxAmount))
      );

      return {
        id: line.id,
        gross: line.amount,
        net: lineNetAmount,
        tax: tax.id,
        taxes: [
          {
            base: lineNetAmount,
            amount: lineTaxAmount,
            tax: tax
          }
        ]
      };
    }

    /* @Override */
    getHeaderTaxes(lines) {
      return lines.reduce(
        (line1, line2) => ({
          gross: line1.gross + line2.gross,
          net: line1.net + line2.net,
          taxes: [
            line1.taxes.concat(line2.taxes).reduce(
              (lineTax1, lineTax2) => ({
                base: lineTax1.base + lineTax2.base,
                amount: lineTax1.amount + lineTax2.amount,
                tax: lineTax2.tax
              }),
              { base: 0, amount: 0, tax: {} }
            )
          ]
        }),
        { gross: 0, net: 0, taxes: [] }
      );
    }
  }

  OB.Taxes.PriceIncludingTax = PriceIncludingTax;
})();
