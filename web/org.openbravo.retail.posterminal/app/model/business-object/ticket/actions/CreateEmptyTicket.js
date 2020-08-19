/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines an action that creates an empty ticket using the utility function
 */
OB.App.StateAPI.Global.registerAction('createEmptyTicket', (state, payload) => {
  const newState = { ...state };

  if (newState.Ticket.hasbeenpaid === 'N') {
    // current ticket is in pending status, add to the list to avoid losing it
    newState.TicketList = [...newState.TicketList];
    newState.TicketList.unshift({ ...newState.Ticket });
  }

  newState.Ticket = OB.App.State.Ticket.Utils.newTicket(payload);

  return newState;
});
