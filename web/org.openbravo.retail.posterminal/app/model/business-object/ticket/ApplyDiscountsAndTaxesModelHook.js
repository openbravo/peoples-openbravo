/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines a hook for the Ticket model in charge of checking the ticket discounts
 * and recalculating the taxes in order to maintain all the ticket data consistent.
 */

OB.App.StateAPI.Ticket.addModelHook({
  generatePayload: () => {
    return {
      discountRules: [...OB.Discounts.Pos.ruleImpls],
      taxRules: [...OB.Taxes.Pos.ruleImpls],
      bpSets: { ...OB.Discounts.Pos.bpSets }
    };
  },

  hook: (ticket, payload) => {
    const newTicket = { ...ticket };
    const { priceIncludesTax } = newTicket;

    // calculate discounts
    const discountsResult = OB.Discounts.applyDiscounts(
      newTicket,
      payload.discountRules,
      payload.bpSets
    );

    // set the discount calculation result into the ticket
    newTicket.lines = newTicket.lines.map(line => {
      const discounts =
        discountsResult.lines[line.id] &&
        discountsResult.lines[line.id].discounts;
      const newLine = {
        ...line,
        gross: line.qty * line.price,
        discountedGrossAmount: line.qty * line.price,
        net: line.qty * line.pricenet,
        discountedNetAmount: line.qty * line.pricenet
      };
      if (discounts) {
        if (priceIncludesTax) {
          newLine.discountedGrossAmount = discounts.finalLinePrice;
        } else {
          newLine.discountedNetAmount = discounts.finalLinePrice;
        }
        newLine.promotions = discounts.promotions.map(promotion => ({
          ...promotion,
          calculatedOnDiscountEngine: true
        }));
      }
      return newLine;
    });

    // recalculate taxes
    const taxesResult = OB.Taxes.Pos.applyTaxes(newTicket, payload.taxRules);

    // set the tax calculation result into the ticket
    newTicket.grossAmount = taxesResult.header.grossAmount;
    newTicket.netAmount = taxesResult.header.netAmount;
    newTicket.taxes = taxesResult.header.taxes;
    taxesResult.lines.forEach(taxLine => {
      const line = newTicket.lines.find(l => l.id === taxLine.id);
      if (priceIncludesTax) {
        line.netAmount = taxLine.netAmount;
        line.discountedNetAmount = taxLine.netAmount;
        line.netPrice = taxLine.netPrice;
      } else {
        line.grossAmount = taxLine.grossAmount;
        line.discountedGrossAmount = taxLine.grossAmount;
        line.grossPrice = taxLine.grossPrice;
      }
      line.taxRate = taxLine.taxRate;
      line.tax = taxLine.tax;
      line.taxes = taxLine.taxes;
    });

    return newTicket;
  }
});
