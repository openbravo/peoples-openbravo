/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to create a message to print a ticket line.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */
OB.App.StateAPI.Global.registerAction('printTicketLine', (state, payload) => {
  const newState = { ...state };
  const data = { line: payload.line };

  const printTicketMsg = OB.App.State.Messages.Utils.createNewMessage(
    '',
    '',
    data,
    { type: 'printTicketLine', consumeOffline: true }
  );

  newState.Messages = [...newState.Messages, printTicketMsg];

  return newState;
});
