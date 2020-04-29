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

    ticket.lines = ticket.lines.concat(newLines);

    return ticket;
  });
})();
