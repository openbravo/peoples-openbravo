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
      const lineTaxes = OB.Taxes.Tax.calculateTaxes(
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
      const headerTaxes = Object.values(linesByParentTaxId).flatMap(
        groupLines => {
          const rules = groupLines[0].taxes.map(lineTax => lineTax.tax);
          if (rules[0].docTaxAmount !== 'D') {
            return OB.Taxes.Tax.calculateTaxesFromLinesTaxes(groupLines, rules);
          }

          const groupGrossAmount = groupLines.reduce(
            (total, line) => OB.DEC.add(total, line.grossAmount),
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
        }
      );

      const headerGrossAmount = lines.reduce(
        (total, line) => OB.DEC.add(total, line.grossAmount),
        OB.DEC.Zero
      );
      const headerNetAmount = headerTaxes.reduce(
        (total, headerTax) => OB.DEC.sub(total, headerTax.amount),
        headerGrossAmount
      );

      return {
        id: this.ticket.id,
        grossAmount: headerGrossAmount,
        netAmount: headerNetAmount,
        taxes: headerTaxes
      };
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
        const line = lines.reduce((line1, line2) => {
          return OB.DEC.abs(line1.netAmount) > OB.DEC.abs(line2.netAmount)
            ? line1
            : line2;
        });
        line.netAmount = OB.DEC.add(line.netAmount, adjustment);
        const adjustedTax = line.taxes.reduce((tax1, tax2) => {
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
          ) < 0
            ? tax1
            : tax2;
        });
        adjustedTax.base = OB.DEC.add(adjustedTax.base, adjustment);
        line.taxes
          .filter(childTax =>
            OB.Taxes.Tax.equals(childTax.tax.taxBase, adjustedTax.tax.id)
          )
          .map(childTax => {
            const updatedChildTax = childTax;
            updatedChildTax.amount = OB.DEC.sub(childTax.amount, adjustment);
            return updatedChildTax;
          });
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
