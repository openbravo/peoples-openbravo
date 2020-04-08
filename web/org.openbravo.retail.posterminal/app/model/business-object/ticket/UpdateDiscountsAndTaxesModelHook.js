/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

const processResultManualPromotions = (result, lineId, ticketManualPromos) => {
  const newResult = { ...result };

  const discountInfoForLine =
    newResult.lines[lineId] && newResult.lines[lineId].discounts.promotions;
  if (!discountInfoForLine || discountInfoForLine.length === 0) {
    return newResult;
  }

  // Create new instances of original definitions for manual promotions
  newResult.lines[lineId].discounts.promotions = discountInfoForLine.map(
    promotion => {
      const newPromotion = { ...promotion };

      if (newPromotion.manual) {
        const promotionRuleId = newPromotion.ruleId;
        const promotionDiscountInstance = newPromotion.discountinstance;
        const promotionNoOrder = newPromotion.noOrder;
        const promotionSplitAmt = newPromotion.splitAmt;

        const discountInstance = ticketManualPromos.find(ticketManualPromo => {
          return (
            ticketManualPromo.ruleId === promotionRuleId &&
            ticketManualPromo.discountinstance === promotionDiscountInstance &&
            ticketManualPromo.noOrder === promotionNoOrder &&
            ticketManualPromo.splitAmt === promotionSplitAmt
          );
        });

        const newPromoInstance = {};

        Object.keys(discountInstance).forEach(key => {
          newPromoInstance[key] = discountInstance[key];
        });

        Object.keys(newPromotion).forEach(key => {
          newPromoInstance[key] = newPromotion[key];
        });

        delete newPromoInstance.linesToApply;

        Object.keys(newPromoInstance).forEach(key => {
          newPromotion[key] = newPromoInstance[key];
        });
      }
      newPromotion.calculatedOnDiscountEngine = true;
      newPromotion.obdiscQtyoffer = promotion.qtyOffer;
      newPromotion.displayedTotalAmount = promotion.amt;
      newPromotion.fullAmt = promotion.amt;
      newPromotion.actualAmt = promotion.amt;

      return newPromotion;
    }
  );

  return newResult;
};

const processReceivedManualPromotions = (ticket, result) => {
  let processedResult = { ...result };
  const ticketManualPromos = ticket.discountsFromUser.manualPromotions
    ? ticket.discountsFromUser.manualPromotions
    : [];

  ticket.lines.forEach(line => {
    processedResult = processResultManualPromotions(
      processedResult,
      line.id,
      ticketManualPromos
    );
  });

  return processedResult;
};

const applyDiscounts = (ticket, result) => {
  const newTicket = { ...ticket };
  newTicket.lines = newTicket.lines.map(line => {
    const mappedLine = { ...line };
    const discountInfoForLine =
      result.lines[mappedLine.id] &&
      result.lines[mappedLine.id].discounts.promotions;
    const excludedFromEnginePromotions = mappedLine.promotions
      ? mappedLine.promotions.filter(promo => {
          return !promo.calculatedOnDiscountEngine;
        })
      : [];
    if (!discountInfoForLine) {
      // No discounts for this line, we keep existing discounts if they exist, and move to the next
      mappedLine.promotions = excludedFromEnginePromotions;
      return mappedLine;
    }

    // Concatenate new promotions and excluded promotions in line
    mappedLine.promotions = [
      ...excludedFromEnginePromotions,
      ...discountInfoForLine
    ];

    return mappedLine;
  });

  return newTicket;
};

OB.App.StateAPI.Ticket.addModelHook({
  generatePayload: () => {
    return {
      discountsData: [...OB.Discounts.Pos.ruleImpls]
    };
  },

  hook: (ticket, payload) => {
    const result = OB.Discounts.Pos.calculateLocal(
      ticket,
      payload.discountsData
    );

    const processedResult = processReceivedManualPromotions(ticket, result);
    return applyDiscounts(ticket, processedResult);
  }
});
