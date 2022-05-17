/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
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
  const ticket = payload.ticket || { ...newState.Ticket };

  newState.Messages = OB.App.State.Messages.Utils.generateDeliverTicketMessages(
    newState.Messages,
    ticket,
    payload.printSettings,
    payload.deliverAction,
    payload.deliverService
  );

  return newState;
});
