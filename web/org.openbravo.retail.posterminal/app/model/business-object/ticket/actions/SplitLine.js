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
    const { lineId, quantities } = payload;

    const editingLine = ticket.lines.find(l => l.id === lineId);

    const qtyToKeep = editingLine.qty - quantities.reduce((q0, q) => q0 + q);

    ticket.lines = ticket.lines.map(l => {
      if (l.id !== lineId) {
        return l;
      }
      return { ...editingLine, qty: qtyToKeep };
    });

    const newLines = quantities.map(qty => {
      const newLine = lodash.cloneDeep(editingLine);
      newLine.qty = qty;
      return newLine;
    });

    ticket.lines = ticket.lines.concat(newLines);

    return ticket;
  });
})();
