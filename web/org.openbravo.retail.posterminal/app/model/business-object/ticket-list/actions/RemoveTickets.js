/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares an action that removes tickets from the ticket list
 */
OB.App.StateAPI.TicketList.registerAction(
  'removeTickets',
  (ticketList, payload) => {
    if (ticketList.tickets.length === 0) {
      return { ...ticketList, addedIds: [] };
    }

    let ticketsToDelete = ticketList.tickets;
    if (payload.removeFilter) {
      ticketsToDelete = ticketList.tickets.filter(payload.removeFilter);
    }
    const idsToDelete = ticketsToDelete.map(ticket => ticket.id);

    const newTicketList = { ...ticketList };
    newTicketList.tickets = newTicketList.tickets.filter(
      ticket => !idsToDelete.includes(ticket.id)
    );
    newTicketList.addedIds = newTicketList.addedIds.filter(
      ticketId => !idsToDelete.includes(ticketId)
    );
    return newTicketList;
  }
);
