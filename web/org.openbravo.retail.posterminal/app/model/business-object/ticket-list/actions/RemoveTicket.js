/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Register the remove Ticket action. If no payload is used, it will remove the currently
 * active ticket and replace it with the first item in the TicketList, or a new Ticket if no other is available.
 *
 * If a id property is provided in the payload, the ticket with the provided id will be removed from the TicketList if
 * found. If the id corresponds to the active Ticket, it will be removed and replaced with the first in TicketList or
 * a new Ticket if the list is empty.
 *
 * Finally, the payload can contain alwaysCreateNewReceiptAfterPayReceipt preference that if set to true, will replace the current ticket
 * with a new one, regardless the content of the TicketList.
 */
OB.App.StateAPI.Global.registerAction(
  'removeTicket',
  (globalState, payload) => {
    const newGlobalState = { ...globalState };
    let newTicketList = [...newGlobalState.TicketList];
    let newTicket = { ...newGlobalState.Ticket };

    if (!payload.id || payload.id === newTicket.id) {
      ({
        ticketList: newTicketList,
        ticket: newTicket
      } = OB.App.State.TicketList.Utils.removeCurrentTicket(
        newTicketList,
        newTicket,
        payload
      ));
    } else {
      newTicketList = newTicketList.filter(ticket => ticket.id !== payload.id);
    }

    newGlobalState.TicketList = newTicketList;
    newGlobalState.Ticket = newTicket;

    return newGlobalState;
  }
);

OB.App.StateAPI.Global.removeTicket.addActionPreparation(
  async (state, payload) => {
    const newPayload = { ...payload };

    const terminal = OB.App.TerminalProperty.get('terminal');
    if (!terminal) {
      throw new OB.App.Class.ActionCanceled(`Missing terminal information`);
    }
    newPayload.terminal = terminal;
    newPayload.businessPartner = OB.App.TerminalProperty.get('businessPartner')
      ? JSON.parse(
          JSON.stringify(OB.App.TerminalProperty.get('businessPartner'))
        )
      : undefined;
    newPayload.session = OB.App.TerminalProperty.get('session');
    newPayload.orgUserId = OB.App.TerminalProperty.get('orgUserId');
    newPayload.pricelist = OB.App.TerminalProperty.get('pricelist');
    newPayload.contextUser = OB.App.TerminalProperty.get('context').user;

    return newPayload;
  }
);
