/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */

(function SplitLineDefinition() {
  function recalculateManualPromotions(
    editingLine,
    manualPromotions,
    splitLines
  ) {
    const newPromos = manualPromotions.flatMap(promo => {
      if (!promo.linesToApply.includes(editingLine.id)) {
        return promo;
      }
      const originalAmt = promo.obdiscAmt;
      return splitLines.map(l => {
        const amt = OB.DEC.toNumber(
          OB.BIGDEC.mul(OB.BIGDEC.div(originalAmt, editingLine.qty), l.qty)
        );
        return { ...promo, obdiscAmt: amt, linesToApply: [l.id] };
      });
    });
    return newPromos;
  }

  OB.App.StateAPI.Ticket.registerAction('splitLine', (state, payload) => {
    const ticket = { ...state };
    const { lineId, quantities } = payload;

    const editingLine = ticket.lines.find(l => l.id === lineId);

    // keep 1st quantity and generate new lines for the rest
    const [qtyToKeep] = quantities.splice(0, 1);

    ticket.lines = ticket.lines.map(l => {
      if (l.id !== lineId) {
        return l;
      }
      return { ...editingLine, qty: qtyToKeep };
    });

    const newLines = quantities.map(qty => {
      const newLine = lodash.cloneDeep(editingLine);
      newLine.qty = qty;
      newLine.id = OB.App.UUID.generate();
      return newLine;
    });

    const lineIdx = ticket.lines.map(l => l.id).indexOf(lineId);
    ticket.lines.splice(lineIdx - 1, 0, ...newLines);

    if (ticket.discountsFromUser && ticket.discountsFromUser.manualPromotions) {
      ticket.discountsFromUser = {
        ...ticket.discountsFromUser,
        manualPromotions: recalculateManualPromotions(
          editingLine,
          ticket.discountsFromUser.manualPromotions,
          [ticket.lines.find(l => l.id === lineId), ...newLines]
        )
      };
    }
    return ticket;
  });
})();
