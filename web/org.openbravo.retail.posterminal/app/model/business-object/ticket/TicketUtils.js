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
   * Computes the totals of a given ticket which include: discounts, taxes and other calculated fields.
   *
   * @param {object} ticket - The ticket whose totals will be calculated
   * @param {object} settings - The calculation settings, which include:
   *             * discountRules - The discount rules to be considered
   *             * taxRules - The tax rules to be considered
   *             * bpSets - The businessPartner sets
   *             * qtyScale - The scale of the ticket quantity (qty)
   * @returns The ticket with the result of the totals calculation
   */
  calculateTotals(ticket, settings) {
    const newTicket = { ...ticket };
    const { priceIncludesTax } = newTicket;

    // calculate discounts
    const discountsResult = OB.Discounts.Pos.applyDiscounts(
      newTicket,
      settings.discountRules,
      settings.bpSets
    );

    // set initial line amounts and prices and applies the discount calculation result into the ticket
    newTicket.lines = newTicket.lines.map(line => {
      const hasDiscounts = line.promotions && line.promotions.length > 0;
      if (line.skipApplyPromotions && hasDiscounts) {
        return { ...line };
      }
      const discounts = line.skipApplyPromotions
        ? undefined
        : discountsResult.lines.find(l => l.id === line.id);
      const newLine = {
        ...line,
        promotions: discounts ? discounts.discounts : []
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
    newTicket.grossAmount = taxesResult.grossAmount;
    newTicket.netAmount = taxesResult.netAmount;
    newTicket.taxes = taxesResult.taxes;
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

    // set the total quantity
    newTicket.qty = newTicket.lines
      .map(l => l.qty)
      .reduce(
        (total, qty) =>
          qty > 0 ? OB.DEC.add(total, qty, settings.qtyScale) : total,
        OB.DEC.Zero
      );

    return newTicket;
  },

  getCurrentDiscountedLinePrice(line, ignoreExecutedAtTheEndPromo) {
    let currentDiscountedLinePrice;
    let allDiscountedAmt = OB.DEC.Zero;
    let i = 0;
    if (line.promotions) {
      for (i = 0; i < line.promotions.length; i += 1) {
        if (!line.promotions[i].hidden) {
          if (
            !ignoreExecutedAtTheEndPromo ||
            !line.promotions[i].executedAtTheEndPromo
          ) {
            allDiscountedAmt = OB.DEC.add(
              allDiscountedAmt,
              line.promotions[i].amt
            );
          }
        }
      }
    }
    if (allDiscountedAmt > 0 && line.qty > 0) {
      currentDiscountedLinePrice = OB.DEC.sub(
        line.baseGrossUnitPrice,
        OB.DEC.div(allDiscountedAmt, line.qty, OB.DEC.getRoundingMode()),
        OB.DEC.getRoundingMode()
      );
    } else {
      currentDiscountedLinePrice = line.baseGrossUnitPrice;
    }

    return currentDiscountedLinePrice;
  },

  calculateDiscountedLinePrice(line) {
    const newLine = { ...line };
    if (line.qty === 0) {
      delete newLine.discountedLinePrice;
    } else {
      newLine.discountedLinePrice = OB.App.State.Ticket.Utils.getCurrentDiscountedLinePrice(
        newLine,
        false
      );
    }
    return newLine;
  }
});
