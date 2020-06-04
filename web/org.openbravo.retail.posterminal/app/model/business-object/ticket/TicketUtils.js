/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Ticket Model
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * Applies the discounts and taxes to the given ticket
   *
   * @param {object} ticket - The ticket whose discounts and taxes will be calculated
   * @param {object} settings - The calculation settings, which include:
   *             * discountRules - The discount rules to be considered
   *             * taxRules - The tax rules to be considered
   *             * bpSets - The businessPartner sets
   * @returns The ticket with the result of the discounts and taxes calculation
   */
  applyDiscountsAndTaxes(ticket, settings) {
    const newTicket = { ...ticket };
    const { priceIncludesTax } = newTicket;

    // calculate discounts
    const discountsResult = OB.Discounts.Pos.applyDiscounts(
      newTicket,
      settings.discountRules,
      settings.bpSets
    );

    // set the discount calculation result into the ticket
    newTicket.lines = newTicket.lines.map(line => {
      const discounts =
        discountsResult.lines[line.id] &&
        discountsResult.lines[line.id].discounts;
      const newLine = {
        ...line,
        promotions: discounts ? discounts.promotions : []
      };
      if (priceIncludesTax) {
        newLine.grossUnitPrice = discounts
          ? discounts.grossUnitPrice
          : line.baseGrossUnitPrice;
        newLine.grossUnitAmount = discounts
          ? discounts.grossUnitAmount
          : OB.DEC.mul(line.qty, line.baseGrossUnitPrice);
      } else {
        newLine.netUnitPrice = discounts
          ? discounts.netUnitPrice
          : line.baseNetUnitPrice;
        newLine.netUnitAmount = discounts
          ? discounts.netUnitAmount
          : OB.DEC.mul(line.qty, line.baseNetUnitPrice);
      }
      return newLine;
    });

    // recalculate taxes
    const taxesResult = OB.Taxes.Pos.applyTaxes(newTicket, settings.taxRules);

    // set the tax calculation result into the ticket
    newTicket.grossAmount = taxesResult.header.grossAmount;
    newTicket.netAmount = taxesResult.header.netAmount;
    newTicket.taxes = taxesResult.header.taxes;
    taxesResult.lines.forEach(taxLine => {
      const line = newTicket.lines.find(l => l.id === taxLine.id);
      line.grossUnitAmount = taxLine.grossUnitAmount;
      line.netUnitAmount = taxLine.netUnitAmount;
      line.grossUnitPrice = taxLine.grossUnitPrice;
      line.netUnitPrice = taxLine.netUnitPrice;
      line.taxRate = taxLine.taxRate;
      line.tax = taxLine.tax;
      line.taxes = taxLine.taxes;
    });

    return newTicket;
  }
});
