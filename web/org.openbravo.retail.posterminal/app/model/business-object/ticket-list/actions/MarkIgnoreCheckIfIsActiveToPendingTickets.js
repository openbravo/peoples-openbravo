/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function markIgnoreCheckIfItsActiveToPendingTickets() {
  const updateIgnoreCheckFlagInNonPaidTicket = (ticket, session) => {
    if (
      ticket.hasbeenpaid === 'N' &&
      (!session || (session && ticket.session === session))
    ) {
      const newTicket = { ...ticket };
      newTicket.ignoreCheckIfIsActiveOrder = true;
      return newTicket;
    }
    return ticket;
  };

  OB.App.StateAPI.Global.registerAction(
    'markIgnoreCheckIfIsActiveOrderToPendingTickets',
    (state, payload = {}) => {
      const newState = { ...state };
      const { session } = payload;

      newState.Ticket = updateIgnoreCheckFlagInNonPaidTicket(
        newState.Ticket,
        session
      );
      newState.TicketList = newState.TicketList.map(ticket => {
        return updateIgnoreCheckFlagInNonPaidTicket(ticket, session);
      });

      return newState;
    }
  );
})();
