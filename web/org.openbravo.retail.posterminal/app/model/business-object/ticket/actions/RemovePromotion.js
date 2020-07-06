/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global */

(function RemovePromotionDefinition() {
  OB.App.StateAPI.Ticket.registerAction(
    'removePromotion',
    (ticket, payload) => {
      const newTicket = { ...ticket };
      const { rule, lineId } = payload;
      const oldLine = ticket.lines.find(line => line.id === lineId);
      if (!oldLine) {
        throw new OB.App.Class.ActionCanceled("Line doesn't exist");
      }

      let newLine = {
        ...oldLine,
        promotions: oldLine.promotions.filter(
          promotion =>
            promotion.ruleId !== rule.id ||
            promotion.discountinstance !== rule.discountinstance
        )
      };

      if (newLine.promotions.length !== oldLine.promotions.length) {
        newLine = OB.App.State.Ticket.Utils.calculateDiscountedLinePrice(
          newLine
        );
        newTicket.lines = ticket.lines.map(line =>
          line.id !== lineId ? line : newLine
        );
      }
      return newTicket;
    }
  );
})();
