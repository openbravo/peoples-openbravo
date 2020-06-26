/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.App.StateAPI.Global.registerAction('removeTicket', (state, payload) => {
  const newState = { ...state };
  const ticketToRemoveId = payload.id;
  const forceNewTicket =
    payload.forceNewTicket || newState.TicketList.length === 0;

  if (forceNewTicket) {
    newState.Ticket = OB.App.State.Ticket.Utils.newTicket(
      payload.businessPartner,
      payload.terminal,
      payload.session,
      payload.orgUserId,
      payload.pricelist,
      payload.contextUser
    );

    return newState;
  }

  if (!ticketToRemoveId || ticketToRemoveId === newState.Ticket.id) {
    newState.TicketList = [...state.TicketList];
    newState.Ticket = newState.TicketList.shift();

    return newState;
  }

  newState.TicketList = state.TicketList.filter(
    ticket => ticket.id !== ticketToRemoveId
  );

  return newState;
});

OB.App.StateAPI.Global.removeTicket.addActionPreparation(
  async (state, payload) => {
    const newPayload = { ...payload };
    if (newPayload.forceNewTicket) {
      newPayload.businessPartner = JSON.parse(
        JSON.stringify(OB.MobileApp.model.get('businessPartner'))
      );
      newPayload.terminal = OB.MobileApp.model.get('terminal');
      newPayload.session = OB.MobileApp.model.get('session');
      newPayload.orgUserId = OB.MobileApp.model.get('orgUserId');
      newPayload.pricelist = OB.MobileApp.model.get('pricelist');
      newPayload.contextUser = OB.MobileApp.model.get('context').user;
    }

    return newPayload;
  }
);
