/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares actions that brings tickets to a session
 */

/**
 * Declares an action that brings tickets present in the list to a session
 */
OB.App.StateAPI.TicketList.registerAction(
  'bringTicketToSession',
  (ticketList, payload) => {
    return OB.App.State.TicketList.Utils.bringTicketListToSession(
      ticketList,
      payload
    );
  }
);

OB.App.StateAPI.TicketList.bringTicketToSession.addActionPreparation(
  async (ticketList, payload) => {
    const newPayload = { ...payload };

    if (!newPayload.session) {
      throw new Error('session parameter is mandatory');
    }

    if (!newPayload.ticketIds) {
      throw new Error('ticketIds parameter is mandatory');
    }

    if (!newPayload.userId) {
      throw new Error('userId parameter is mandatory');
    }

    if (!(newPayload.ticketIds instanceof Array)) {
      newPayload.ticketIds = [newPayload.ticketIds];
    }

    return newPayload;
  }
);

/**
 * Declares an action that brings all pending tickets to a session
 */
OB.App.StateAPI.Global.registerAction(
  'bringAllTicketsToSession',
  (state, payload) => {
    if (!state.Ticket || !state.TicketList) {
      return state;
    }

    const currentTicketId = state.Ticket.id;

    let allTickets = [{ ...state.Ticket }, ...state.TicketList];

    // Check pending tickets in other session
    const pendingTicketsInOtherSessions = allTickets.filter(
      ticket =>
        ticket.session &&
        ticket.session !== payload.session &&
        ticket.hasbeenpaid === 'N'
    );

    if (pendingTicketsInOtherSessions.length > 0) {
      const newPayload = {
        ...payload,
        ticketIds: pendingTicketsInOtherSessions.map(m => m.id)
      };

      allTickets = OB.App.State.TicketList.Utils.bringTicketListToSession(
        allTickets,
        newPayload
      );
      const newState = { ...state };

      if (currentTicketId) {
        newState.Ticket = allTickets.find(
          ticket => ticket.id === currentTicketId
        );
      }
      newState.TicketList = allTickets.filter(
        ticket => ticket.id !== currentTicketId
      );
      return newState;
    }
    return state;
  }
);

OB.App.StateAPI.Global.bringAllTicketsToSession.addActionPreparation(
  async (state, payload) => {
    if (!payload.session) {
      throw new Error('session parameter is mandatory');
    }

    if (!payload.userId) {
      throw new Error('userId parameter is mandatory');
    }

    return payload;
  }
);
