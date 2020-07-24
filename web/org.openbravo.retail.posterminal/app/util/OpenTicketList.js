/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.App.OpenTicketList = {
  getAllTickets(sortByRecent = true) {
    const currentTicket = OB.App.State.getState().Ticket;

    const { addedIds } = OB.App.State.getState().TicketList;
    const allTickets = addedIds
      .map(ticketId => {
        if (currentTicket && ticketId === currentTicket.id) {
          return { ...currentTicket };
        }
        return OB.App.State.getState().TicketList.tickets.find(
          t => t.id === ticketId
        );
      })
      .filter(ticket => ticket !== undefined);

    if (
      currentTicket &&
      Object.keys(currentTicket).length !== 0 &&
      !allTickets.some(ticket => ticket.id === currentTicket.id)
    ) {
      // current ticket has not been in ticket list yet
      allTickets.push({ ...currentTicket });
    }

    return sortByRecent ? allTickets.reverse() : allTickets;
  }
};
