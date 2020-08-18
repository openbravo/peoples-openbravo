/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function TicketListUtilsDefinition() {
  OB.App.StateAPI.TicketList.registerUtilityFunctions({
    removeTicket(ticketList, ticket, payload) {
      const multiTicketListIds = (payload.multiTicketList || []).map(t => t.id);
      let newTicket = { ...ticket };
      let newTicketList = ticketList.filter(
        t => !multiTicketListIds.includes(t.id)
      );

      const shouldReplaceCurrentTicket =
        !payload.multiTicketList || multiTicketListIds.includes(newTicket.id);
      const shouldCreateNewTicket =
        (payload.preferences &&
          payload.preferences.alwaysCreateNewReceiptAfterPayReceipt) ||
        (shouldReplaceCurrentTicket &&
          !newTicketList.some(t => t.session === payload.session));

      if (shouldCreateNewTicket) {
        if (payload.businessPartner) {
          newTicket = OB.App.State.Ticket.Utils.newTicket(payload);
        }
        return { ticketList: newTicketList, ticket: newTicket };
      }

      newTicket = shouldReplaceCurrentTicket
        ? newTicketList.find(t => t.session === payload.session)
        : newTicket;
      newTicketList = newTicketList.filter(t => t.id !== newTicket.id);

      return { ticketList: newTicketList, ticket: newTicket };
    },

    getAllTickets() {
      const currentTicket = OB.App.State.getState().Ticket;
      if (currentTicket && Object.keys(currentTicket).length !== 0) {
        return [{ ...currentTicket }, ...OB.App.State.getState().TicketList];
      }
      return [...OB.App.State.getState().TicketList];
    },

    getSessionTickets(session) {
      return OB.App.State.TicketList.Utils.getAllTickets().filter(
        ticket => ticket.session === session
      );
    },

    getOtherSessionsTickets(session) {
      return OB.App.State.TicketList.Utils.getAllTickets().filter(
        ticket => ticket.session !== session
      );
    }
  });
})();
