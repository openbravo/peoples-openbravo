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

    const ticketsToDelete = ticketList.tickets
      .filter(payload.removeFilter)
      .map(ticket => ticket.id);

    const newTicketList = { ...ticketList };
    newTicketList.tickets = newTicketList.tickets.filter(
      ticket => !ticketsToDelete.includes(ticket.id)
    );
    newTicketList.addedIds = newTicketList.addedIds.filter(
      ticketId => !ticketsToDelete.includes(ticketId)
    );
    return newTicketList;
  }
);

OB.App.StateAPI.TicketList.removeTickets.addActionPreparation(
  async (ticketList, payload) => {
    if (!payload.removeFilter || typeof payload.removeFilter !== 'function') {
      throw new OB.App.Class.ActionCanceled('Missing remove filter function');
    }
    return payload;
  }
);
