/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares an action that inserts the Ticket into the state.TicketList
 */
OB.App.StateAPI.TicketList.registerAction(
  'saveTicket',
  (ticketList, payload) => {
    const newTicketList = [...ticketList];
    newTicketList.unshift({ ...payload });
    return newTicketList;
  }
);
