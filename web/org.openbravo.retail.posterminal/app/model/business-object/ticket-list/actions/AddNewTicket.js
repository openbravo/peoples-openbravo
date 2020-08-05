/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares a global action that creates a new ticket in the state.Ticket object
 * and stores the existing one into state.TicketList
 */
// eslint-disable-next-line no-unused-vars
OB.App.StateAPI.Global.registerAction('addNewTicket', (state, payload) => {
  const newState = { ...state };
  newState.Ticket = { ...state.Ticket };
  newState.TicketList = [...state.TicketList];

  newState.TicketList.unshift(newState.Ticket);
  newState.Ticket = OB.App.State.Ticket.Utils.newTicket(payload);

  return newState;
});

OB.App.StateAPI.Global.addNewTicket.addActionPreparation(
  async (state, payload) => {
    const newPayload = { ...payload };
    newPayload.businessPartner = JSON.parse(
      JSON.stringify(OB.App.TerminalProperty.get('businessPartner'))
    );
    newPayload.terminal = OB.App.TerminalProperty.get('terminal');
    newPayload.session = OB.App.TerminalProperty.get('session');
    newPayload.orgUserId = OB.App.TerminalProperty.get('orgUserId');
    newPayload.pricelist = OB.App.TerminalProperty.get('pricelist');
    newPayload.contextUser = OB.App.TerminalProperty.get('context').user;

    return newPayload;
  }
);
