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
    // eslint-disable-next-line class-methods-use-this
    getLineTaxes(line, rules) {
      const tax = OB.Taxes.Tax.getParentTaxId(rules[0]);
      const netUnitAmount = line.amount;
      const netUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        netUnitAmount,
        line.qty
      );
      const grossUnitAmount = OB.Taxes.PriceExcludingTax.calculateGrossAmountFromNetAmount(
        netUnitAmount,
        rules
      );
      const grossUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
        grossUnitAmount,
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

        const groupNetAmount = groupLines.reduce(
          (total, line) => OB.DEC.add(total, line.netUnitAmount),
          OB.DEC.Zero
        );
        const groupGrossAmount = OB.Taxes.PriceExcludingTax.calculateGrossAmountFromNetAmount(
          groupNetAmount,
          rules
        );

        const groupTaxes = OB.Taxes.Tax.calculateTaxes(
          groupGrossAmount,
          groupNetAmount,
          rules
        );

        OB.Taxes.PriceExcludingTax.adjustLineGrossAmount(
          groupGrossAmount,
          groupLines
        );

        return groupTaxes;
      });

      const netAmount = lines.reduce(
        (total, line) => OB.DEC.add(total, line.netUnitAmount),
        OB.DEC.Zero
      );
      const grossAmount = taxes.reduce(
        (total, headerTax) => OB.DEC.add(total, headerTax.amount),
        netAmount
      );

      return {
        id: this.ticket.id,
        grossAmount,
        netAmount,
        taxes
      };
    }

    /**
     * If the header gross amount is different than the sum of line gross amounts, we need to adjust the highest line gross amount
     * Last tax amount of this line will be adjusted as well.
     */
    static adjustLineGrossAmount(grossAmount, lines) {
      const adjustment = OB.DEC.sub(
        grossAmount,
        lines.reduce(
          (line1, line2) => OB.DEC.add(line1, line2.grossUnitAmount),
          OB.DEC.Zero
        )
      );

      if (OB.DEC.compare(adjustment) !== 0) {
        const line = lines.reduce((line1, line2) => {
          return OB.DEC.abs(line1.grossUnitAmount) >
            OB.DEC.abs(line2.grossUnitAmount)
            ? line1
            : line2;
        });
        line.grossUnitAmount = OB.DEC.add(line.grossUnitAmount, adjustment);
        line.grossUnitPrice = OB.Taxes.Tax.calculatePriceFromAmount(
          line.grossUnitAmount,
          line.qty
        );
        const adjustedTax = line.taxes.reduce((tax1, tax2) => {
          return OB.DEC.abs(tax1.base) <= OB.DEC.abs(tax2.base) ? tax2 : tax1;
        });
        adjustedTax.amount = OB.DEC.add(adjustedTax.amount, adjustment);
      }
    }

    /**
     * grossAmount = netAmount + taxAmount
     */
    static calculateGrossAmountFromNetAmount(netAmount, rules) {
      return OB.DEC.add(
        netAmount,
        OB.Taxes.Tax.calculateTotalTaxAmount(netAmount, rules)
      );
    }
  }

  OB.Taxes.PriceExcludingTax = PriceExcludingTax;
})();
