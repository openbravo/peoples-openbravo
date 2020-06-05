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
  const AMT_DISCOUNTS = {
    '7B49D8CC4E084A75B7CB4D85A6A3A578': 'Fixed',
    D1D193305A6443B09B299259493B272A: 'User defined',
    F3B0FB45297844549D9E6B5F03B23A82: 'Line amt'
  };

  function recalculateManualPromotions(
    editingLine,
    manualPromotions,
    splitLines
  ) {
    const newPromos = manualPromotions.flatMap(promo => {
      if (!promo.linesToApply.includes(editingLine.id)) {
        return promo;
      }

      let accumDisc = 0;

      const amtDiscountType = AMT_DISCOUNTS[promo.discountType];

      return splitLines.map((l, i) => {
        let amt;

        const newPromo = { ...promo, linesToApply: [l.id] };

        if (amtDiscountType) {
          // take into account propportional split for discounts setting amounts
          // percentual discounts are already correctly set
          let discProperty;
          if (amtDiscountType === 'Line amt') {
            discProperty = 'obdiscLineFinalgross';
          } else {
            discProperty = 'obdiscAmt';
          }

          const originalAmt = promo[discProperty];
          if (i === splitLines.length - 1) {
            amt = originalAmt - accumDisc;
          } else {
            amt = OB.DEC.toNumber(
              OB.BIGDEC.mul(OB.BIGDEC.div(originalAmt, editingLine.qty), l.qty)
            );
          }

          newPromo[discProperty] = amt;

          accumDisc = OB.DEC.add(accumDisc, amt);
        }
        return newPromo;
      });
    });
    return newPromos;
  }

  OB.App.StateAPI.Ticket.registerAction('splitLine', (ticket, payload) => {
    const newTicket = { ...ticket };
    const { lineId, quantities } = payload;

    const editingLine = newTicket.lines.find(l => l.id === lineId);

    // keep 1st quantity and generate new lines for the rest
    const [qtyToKeep] = quantities.splice(0, 1);

    newTicket.lines = newTicket.lines.map(l => {
      if (l.id !== lineId) {
        return l;
      }
      return { ...editingLine, qty: qtyToKeep, splitline: true };
    });

    const newLines = quantities.map(qty => {
      const newLine = lodash.cloneDeep(editingLine);
      newLine.id = OB.App.UUID.generate();
      newLine.qty = qty;
      newLine.splitline = true;
      return newLine;
    });

    const lineIdx = newTicket.lines.map(l => l.id).indexOf(lineId);
    newTicket.lines.splice(lineIdx + 1, 0, ...newLines);

    if (
      newTicket.discountsFromUser &&
      newTicket.discountsFromUser.manualPromotions
    ) {
      newTicket.discountsFromUser = {
        ...newTicket.discountsFromUser,
        manualPromotions: recalculateManualPromotions(
          editingLine,
          newTicket.discountsFromUser.manualPromotions,
          [newTicket.lines.find(l => l.id === lineId), ...newLines]
        )
      };
    }
    return newTicket;
  });

  OB.App.StateAPI.Ticket.splitLine.addActionPreparation(
    async (ticket, payload) => {
      const { lineId, quantities } = payload;

      if (lineId === undefined) {
        throw new Error('lineId parameter is mandatory');
      }

      const line = ticket.lines.find(l => l.id === lineId);

      if (!line) {
        throw new Error(`lineId ${lineId} not found in ticket`);
      }

      if (quantities === undefined) {
        throw new Error('quantities parameter is mandatory');
      }

      if (
        !(quantities instanceof Array) ||
        quantities.some(q => typeof q !== 'number')
      ) {
        throw new Error('quantities must be an array of numbers');
      }

      const qtySum = quantities.reduce((a, b) => a + b, 0);
      if (qtySum !== line.qty) {
        throw new Error(
          `quantities must sum ${line.qty} but they are ${qtySum}`
        );
      }
      return payload;
    }
  );
})();
