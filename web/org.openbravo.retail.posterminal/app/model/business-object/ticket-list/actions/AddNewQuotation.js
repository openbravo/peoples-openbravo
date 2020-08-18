/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares a global action that creates a new quotation ticket in the state.Ticket object
 * and stores the existing one into state.TicketList
 */
// eslint-disable-next-line no-unused-vars
OB.App.StateAPI.Global.registerAction('addNewQuotation', (state, payload) => {
  const newState = { ...state };
  newState.Ticket = { ...state.Ticket };
  newState.TicketList = [...state.TicketList];

  newState.TicketList.unshift(newState.Ticket);
  let ticket = OB.App.State.Ticket.Utils.newTicket(payload);
  ticket.isQuotation = true;
  ticket.orderType = '0';
  ticket = OB.App.State.Ticket.Utils.setFullInvoice(
    ticket,
    payload.terminal,
    false
  );
  ticket.documentType = OB.App.TerminalProperty.get(
    'terminal'
  ).terminalType.documentTypeForQuotations;
  newState.Ticket = ticket;
  return newState;
});
