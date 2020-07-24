/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares an action that removes the empty tickets from the ticket list
 */
OB.App.StateAPI.TicketList.registerAction('removeEmptyTickets', ticketList => {
  if (ticketList.tickets.length === 0) {
    return { ...ticketList, addedIds: [] };
  }

  const emptyTickets = ticketList.tickets
    .filter(ticket => ticket.lines.length === 0)
    .map(ticket => ticket.id);

  const newTicketList = { ...ticketList };
  newTicketList.tickets = newTicketList.tickets.filter(
    ticket => !emptyTickets.includes(ticket.id)
  );
  newTicketList.addedIds = newTicketList.addedIds.filter(
    ticketId => !emptyTickets.includes(ticketId)
  );
  return newTicketList;
});
