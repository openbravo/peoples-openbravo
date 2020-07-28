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
    return ticketList.filter(ticket => !payload.removeFilter(ticket));
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
