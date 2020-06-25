/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function markIgnoreCheckIfItsActiveToPendingTickets() {
  const updateIgnoreCheckFlagInNonPaidTicket = ticket => {
    if (ticket.hasbeenpaid === 'N') {
      const newTicket = { ...ticket };
      newTicket.ignoreCheckIfIsActiveOrder = true;
      return newTicket;
    }
    return ticket;
  };

  OB.App.StateAPI.Global.registerAction(
    'markIgnoreCheckIfIsActiveOrderToPendingTickets',
    state => {
      const newState = { ...state };

      newState.Ticket = updateIgnoreCheckFlagInNonPaidTicket(newState.Ticket);
      newState.TicketList = newState.TicketList.map(ticket => {
        return updateIgnoreCheckFlagInNonPaidTicket(ticket);
      });

      return newState;
    }
  );
})();
