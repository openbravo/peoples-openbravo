/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Mark as checked the selected tickets in pay open tickets window.
 * This is used for the logic to mantain the seleted tickets, after press F5 in the pay open tickets window
 */
// eslint-disable-next-line no-unused-vars
OB.App.StateAPI.Global.registerAction(
  'checkTicketForPayOpenTickets',
  (state, payload) => {
    const newState = { ...state };
    if (state.Ticket.id === payload.ticketId) {
      newState.Ticket = { ...newState.Ticket, checked: true };
    } else {
      newState.TicketList = { ...newState.TicketList };
      newState.TicketList.tickets = newState.TicketList.tickets.map(ticket => {
        if (ticket.id === payload.ticketId) {
          return { ...ticket, checked: true };
        }
        return ticket;
      });
    }
    return newState;
  }
);
