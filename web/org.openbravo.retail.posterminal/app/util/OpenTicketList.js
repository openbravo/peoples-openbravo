/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.App.OpenTicketList = {
  getAllTickets() {
    const allReceipts = [...OB.App.State.getState().TicketList];
    const currentTicket = OB.App.State.getState().Ticket;
    if (currentTicket && Object.keys(currentTicket).length !== 0) {
      allReceipts.unshift(currentTicket);
    }

    return allReceipts;
  }
};
