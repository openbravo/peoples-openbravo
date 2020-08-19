/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to create a message to print a ticket.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

OB.App.StateAPI.Global.registerAction('printTicket', (state, payload) => {
  const newState = { ...state };

  const printTicketMsg = OB.App.State.Messages.Utils.createPrintTicketMessage(
    payload.ticket || { ...newState.Ticket },
    payload.printSettings
  );

  newState.Messages = [...newState.Messages, printTicketMsg];

  return newState;
});
