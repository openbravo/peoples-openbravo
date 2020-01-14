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
      // lineTaxAmount = lineGrossAmount - lineNetAmount
      const tax = rules[0];
      const lineGrossAmount = new BigDecimal(String(line.amount));
      const lineNetAmount = lineGrossAmount
        .multiply(lineGrossAmount)
        .divide(
          lineGrossAmount.add(
            lineGrossAmount
              .multiply(new BigDecimal(String(tax.rate)))
              .divide(
                new BigDecimal('100'),
                50,
                BigDecimal.prototype.ROUND_HALF_UP
              )
          ),
          50,
          BigDecimal.prototype.ROUND_HALF_UP
        );
      const lineTaxAmount = lineGrossAmount.add(lineNetAmount.negate());

      return {
        id: line.id,
        gross: OB.DEC.toNumber(lineGrossAmount),
        net: OB.DEC.toNumber(lineNetAmount),
        tax: tax.id,
        taxes: [
          {
            base: OB.DEC.toNumber(lineNetAmount),
            amount: OB.DEC.toNumber(lineTaxAmount),
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
