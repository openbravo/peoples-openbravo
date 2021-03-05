/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function ReturnTicketDefinition() {
  OB.App.StateAPI.Ticket.registerAction('returnTicket', (ticket, payload) => {
    let newTicket = { ...ticket };

    newTicket.lines = newTicket.lines.map(line => {
      const newLine = { ...line };
      newLine.qty = -newLine.qty;
      return newLine;
    });
    newTicket = OB.App.State.Ticket.Utils.updateTicketType(newTicket, payload);

    return newTicket;
  });
})();
