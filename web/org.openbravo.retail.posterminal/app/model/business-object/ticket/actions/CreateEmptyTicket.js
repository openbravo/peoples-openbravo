/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines an action that creates an empty ticket using the utility function
 */
OB.App.StateAPI.Global.registerAction('createEmptyTicket', (state, payload) => {
  const newState = { ...state };

  if (newState.Ticket.hasbeenpaid === 'N') {
    // current ticket is in pending status, add to the list to avoid losing it
    newState.TicketList = [...newState.TicketList];
    newState.TicketList.unshift({ ...newState.Ticket });
  }

  newState.Ticket = OB.App.State.Ticket.Utils.newTicket(
    payload.businessPartner,
    payload.terminal,
    payload.session,
    payload.orgUserId,
    payload.pricelist,
    payload.contextUser
  );

  return newState;
});

OB.App.StateAPI.Global.createEmptyTicket.addActionPreparation(
  async (ticket, payload) => {
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
