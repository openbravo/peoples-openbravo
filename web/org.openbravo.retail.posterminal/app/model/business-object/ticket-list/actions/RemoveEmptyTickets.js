/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares an action that removes the empty tickets from the ticket list
 */
OB.App.StateAPI.TicketList.registerAction('removeEmptyTickets', ticketList => {
  return ticketList.filter(ticket => ticket.lines.length > 0);
});
