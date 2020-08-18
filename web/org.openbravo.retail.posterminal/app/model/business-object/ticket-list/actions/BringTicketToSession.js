/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares an action that brings tickets present in the list to a session
 */
OB.App.StateAPI.TicketList.registerAction(
  'bringTicketToSession',
  (ticketList, payload) => {
    return ticketList.map(ticket => {
      if (payload.ticketIds.some(id => id === ticket.id)) {
        return {
          ...ticket,
          session: payload.session,
          createdBy: payload.userId,
          updatedBy: payload.userId
        };
      }
      return ticket;
    });
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
