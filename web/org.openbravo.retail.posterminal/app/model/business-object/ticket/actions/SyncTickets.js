/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Provisional action used to create a message for all Tickets in order to be
 * synchronized with the backoffice. This should be removed once the complete ticket process is completed
 */
OB.App.StateAPI.Global.registerAction('syncTickets', state => {
  const newState = { ...state };
  const allTickets = [...state.TicketList.tickets];
  allTickets.unshift(state.Ticket);

  newState.Messages = [...newState.Messages];
  allTickets
    .filter(ticket => ticket.hasbeenpaid === 'Y')
    .forEach(ticket => {
      newState.Messages.push(
        OB.App.State.Messages.Utils.createNewMessage(
          'OB.Model.Order',
          'org.openbravo.retail.posterminal.OrderLoader',
          ticket
        )
      );
    });

  return newState;
});
