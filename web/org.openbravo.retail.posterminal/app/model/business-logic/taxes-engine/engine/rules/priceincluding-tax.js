/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  class PriceIncludingTax extends OB.Taxes.Tax {
    /* @Override */
    getLineTaxes(line, rules) {
      const tax = OB.Taxes.Tax.getParentTaxId(rules[0]);
      const grossUnitAmount = line.amount;
      const grossUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        grossUnitAmount,
        line.qty
      );
      const netUnitAmount = OB.Taxes.PriceIncludingTax.calculateNetAmountFromGrossAmount(
        grossUnitAmount,
        rules
      );
      const netUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        netUnitAmount,
        line.qty
      );
      const taxes = OB.Taxes.Tax.calculateTaxes(
        grossUnitAmount,
        netUnitAmount,
        rules
      );
      const taxRate = OB.Taxes.Tax.calculateLineTaxRate(taxes);

      return {
        id: line.id,
        grossUnitAmount,
        netUnitAmount,
        grossUnitPrice,
        netUnitPrice,
        qty: line.qty,
        tax,
        taxRate,
        taxes
      };
    }

    /* @Override */
    getHeaderTaxes(lines) {
      const linesByParentTaxId = OB.Taxes.Tax.groupLinesByTax(lines);
      const taxes = Object.values(linesByParentTaxId).flatMap(groupLines => {
        const rules = groupLines[0].taxes.map(lineTax => lineTax.tax);
        if (rules[0].docTaxAmount !== 'D') {
          return OB.Taxes.Tax.calculateTaxesFromLinesTaxes(groupLines, rules);
        }

        const groupGrossAmount = groupLines.reduce(
          (total, line) => OB.DEC.add(total, line.grossUnitAmount),
          OB.DEC.Zero
        );
        const groupNetAmount = OB.Taxes.PriceIncludingTax.calculateNetAmountFromGrossAmount(
          groupGrossAmount,
          rules
        );

        const groupTaxes = OB.Taxes.Tax.calculateTaxes(
          groupGrossAmount,
          groupNetAmount,
          rules
        );

        OB.Taxes.PriceIncludingTax.adjustLineNetAmount(
          groupNetAmount,
          groupLines
        );

        return groupTaxes;
      });

      const grossAmount = lines.reduce(
        (total, line) => OB.DEC.add(total, line.grossUnitAmount),
        OB.DEC.Zero
      );
      const netAmount = taxes.reduce(
        (total, headerTax) => OB.DEC.sub(total, headerTax.amount),
        grossAmount
      );

      return {
        id: this.ticket.id,
        grossAmount,
        netAmount,
        taxes
      };
    }

    /**
     * If the header net amount is different than the sum of line net amounts, we need to adjust the highest line net amount
     * First tax base and amount of this line will be adjusted as well.
     */
    static adjustLineNetAmount(netAmount, lines) {
      const adjustment = OB.DEC.sub(
        netAmount,
        lines.reduce(
          (total, line) => OB.DEC.add(total, line.netUnitAmount),
          OB.DEC.Zero
        )
      );

      if (OB.DEC.compare(adjustment) === 0) {
        return;
      }

      const linesToAdjust = [];
      lines.forEach(line => {
        const roundingLineTax = [];
        line.taxes.forEach(tax => {
          if (tax.tax.summaryLevel || OB.DEC.compare(tax.base) === 0) {
            return;
          }
          roundingLineTax.push(
            OB.DEC.sub(
              OB.DEC.mul(
                OB.DEC.add(tax.base, adjustment, 20),
                OB.DEC.div(tax.tax.rate, 100, 20),
                20
              ),
              OB.DEC.sub(tax.amount, adjustment, 20),
              20
            )
          );
        });
        if (roundingLineTax.length > 0) {
          linesToAdjust.push({
            line: line.lineTax ? line.lineTax : line, // Get Line if it is bomLine
            roundingAmt: Math.abs(
              OB.DEC.div(
                roundingLineTax.reduce((a, b) => OB.DEC.add(a, b, 20), 0),
                roundingLineTax.length,
                20
              )
            )
          });
        }
      });
      if (linesToAdjust.length > 0) {
        const lineToAdjust = linesToAdjust.reduce((line1, line2) => {
          return line1.roundingAmt > line2.roundingAmt ? line1 : line2;
        }).line;
        lineToAdjust.netUnitAmount = OB.DEC.add(
          lineToAdjust.netUnitAmount,
          adjustment
        );
        lineToAdjust.netUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
          lineToAdjust.netUnitAmount,
          lineToAdjust.qty
        );

        const adjustedTax = lineToAdjust.taxes.reduce((tax1, tax2) => {
          return OB.DEC.abs(tax1.amount) > OB.DEC.abs(tax2.amount)
            ? tax1
            : tax2;
        });
        adjustedTax.base = OB.DEC.add(adjustedTax.base, adjustment);
        adjustedTax.amount = OB.DEC.sub(adjustedTax.amount, adjustment);
      }
    }

    /**
     * netAmount = (grossAmount * grossAmount) / (grossAmount + taxAmount)
     */
    static calculateNetAmountFromGrossAmount(grossAmount, rules) {
      if (OB.DEC.compare(grossAmount) === 0) {
        return OB.DEC.Zero;
      }

      const amount = new BigDecimal(String(grossAmount));
      const taxAmount = OB.Taxes.Tax.calculateTotalTaxAmount(
        grossAmount,
        rules
      );

      return OB.DEC.toNumber(
        amount
          .multiply(amount)
          .divide(amount.add(taxAmount), 20, BigDecimal.prototype.ROUND_HALF_UP)
      );
    }
  }

  OB.Taxes.PriceIncludingTax = PriceIncludingTax;
})();
