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
  OB.App.StateAPI.Ticket.registerAction('splitLine', (state, payload) => {
    const ticket = { ...state };
    const { lineId, qtyToKeep } = payload;

    const editingLine = ticket.lines.find(l => l.id === lineId);

    const originalQty = editingLine.qty;

    if (originalQty === qtyToKeep) {
      return ticket;
    }

    ticket.lines = ticket.lines.map(l => {
      if (l.id !== lineId) {
        return l;
      }
      return { ...editingLine, qty: qtyToKeep };
    });

    const newLine = lodash.cloneDeep(editingLine);
    newLine.qty = originalQty - qtyToKeep;

    ticket.lines.push(newLine);
    return ticket;
  });
})();
