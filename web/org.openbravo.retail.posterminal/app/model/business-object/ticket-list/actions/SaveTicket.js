/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares a global action that inserts the Ticket into the state.TicketList
 */
// eslint-disable-next-line no-unused-vars
OB.App.StateAPI.TicketList.registerAction(
  'saveTicket',
  (ticketList, payload) => {
    const newTicketList = [...ticketList];
    newTicketList.unshift({ ...payload });
    return newTicketList;
  }
);
