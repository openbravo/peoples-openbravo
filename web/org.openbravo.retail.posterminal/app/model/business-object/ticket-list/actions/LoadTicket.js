/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares a global action that loads the Ticket passed as payload as the current active ticket
 * and enqueues the active ticket into the list
 */
(function LoadTicketActions() {
  function loadTicketAndEnqueueCurrentById(
    state,
    ticketToLoadId,
    enqueueCurrent
  ) {
    if (ticketToLoadId && state.Ticket.id === ticketToLoadId) {
      return state;
    }

    const newState = { ...state };
    const newCurrentTicket = newState.TicketList.find(
      ticket => ticket.id === ticketToLoadId
    );
    if (!newCurrentTicket) {
      throw new OB.App.Class.ActionCanceled('Ticket to load not found', {
        ticketToLoadId
      });
    }

    newState.TicketList = newState.TicketList.filter(
      ticket => ticket.id !== ticketToLoadId
    );
    if (enqueueCurrent) {
      newState.TicketList.unshift({ ...newState.Ticket });
    }
    newState.Ticket = newCurrentTicket;

    return newState;
  }

  OB.App.StateAPI.Global.registerAction('loadTicket', (state, payload) => {
    const ticketToLoadId = payload.ticket.id;
    const enqueueCurrent =
      payload.enqueueCurrent != null ? payload.enqueueCurrent : true;
    return loadTicketAndEnqueueCurrentById(
      state,
      ticketToLoadId,
      enqueueCurrent
    );
  });

  OB.App.StateAPI.Global.registerAction('loadTicketById', (state, payload) => {
    const ticketToLoadId = payload.id;
    const enqueueCurrent =
      payload.enqueueCurrent != null ? payload.enqueueCurrent : true;
    return loadTicketAndEnqueueCurrentById(
      state,
      ticketToLoadId,
      enqueueCurrent
    );
  });
})();
