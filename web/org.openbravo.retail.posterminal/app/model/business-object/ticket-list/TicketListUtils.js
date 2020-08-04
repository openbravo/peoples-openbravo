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
    removeCurrentTicket(ticketList, ticket, payload) {
      const shouldCreateNewTicket =
        (payload.preferences &&
          payload.preferences.alwaysCreateNewReceiptAfterPayReceipt) ||
        !ticketList.some(t => t.session === payload.session);

      if (shouldCreateNewTicket) {
        let newTicket = { ...ticket };
        if (payload.businessPartner) {
          newTicket = OB.App.State.Ticket.Utils.newTicket(
            payload.businessPartner,
            payload.terminal,
            payload.session,
            payload.orgUserId,
            payload.pricelist,
            payload.contextUser
          );
        }
        return { ticketList, ticket: newTicket };
      }

      const newTicket = ticketList.find(t => t.session === payload.session);
      const newTicketList = ticketList.filter(t => t.id !== newTicket.id);

      return { ticketList: newTicketList, ticket: newTicket };
    }
  });
})();
