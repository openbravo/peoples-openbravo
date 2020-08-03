/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function TicketListUtilsDefinition() {
  const replaceCurrentTicketWithANewOne = (state, newTicketData) => {
    const newState = { ...state };
    if (newTicketData.businessPartner) {
      newState.Ticket = OB.App.State.Ticket.Utils.newTicket(
        newTicketData.businessPartner,
        newTicketData.terminal,
        newTicketData.session,
        newTicketData.orgUserId,
        newTicketData.pricelist,
        newTicketData.contextUser
      );
    }
    return newState;
  };

  OB.App.StateAPI.TicketList.registerUtilityFunctions({
    removeCurrentTicketAndForceCreateNew(state, newTicketData) {
      return replaceCurrentTicketWithANewOne(state, newTicketData);
    },

    removeCurrentTicket(state, newTicketData) {
      const newState = { ...state };
      const shouldCreateNewTicket = !newState.TicketList.some(
        t => t.session === newTicketData.session
      );

      if (!shouldCreateNewTicket) {
        newState.Ticket = state.TicketList.find(
          t => t.session === newTicketData.session
        );
        newState.TicketList = state.TicketList.filter(
          ticket => ticket.id !== newState.Ticket.id
        );

        return newState;
      }

      return replaceCurrentTicketWithANewOne(state, newTicketData);
    }
  });
})();
