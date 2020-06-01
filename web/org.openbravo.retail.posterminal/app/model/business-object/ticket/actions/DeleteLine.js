/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* eslint-disable no-use-before-define */

(function DeleteLineDefinition() {
  window.newDeleteLine = true; // TODO: remove this testing code

  OB.App.StateAPI.Ticket.registerAction('deleteLine', (ticket, payload) => {
    const { lineIds } = payload;

    const newTicket = {
      ...ticket,
      lines: ticket.lines.filter(l => !lineIds.includes(l.id))
    };

    return newTicket;
  });

  OB.App.StateAPI.Ticket.deleteLine.addActionPreparation(
    async (state, payload) => {
      const ticket = state.Ticket;
      let newPayload = { ...payload };

      newPayload = removeRelatedServices(ticket, newPayload);
      return newPayload;
    }
  );

  function removeRelatedServices(ticket, payload) {
    if (!ticket.hasServices) {
      // TODO: is this check necessary?
      return payload;
    }

    const newPayload = { ...payload };

    // TODO: Check if it is necessary to restore the tax category of related products

    newPayload.lineIds = newPayload.lineIds.concat(
      ticket.lines
        .filter(
          l =>
            l.relatedLines &&
            l.relatedLines.some(rl => payload.lineIds.includes(rl.orderlineId))
        )
        .map(l => l.id)
    );
    // TODO: handle lines related to serveral

    return newPayload;
  }
})();
