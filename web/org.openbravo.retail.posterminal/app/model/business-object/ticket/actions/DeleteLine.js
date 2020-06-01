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
})();
