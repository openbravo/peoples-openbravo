/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to create a message to display the ticket total.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

OB.App.StateAPI.Global.registerAction('displayTotal', state => {
  const newState = { ...state };
  const data = {
    ticket: { ...newState.Ticket }
  };

  const displayTotalMsg = OB.App.State.Messages.Utils.createNewMessage(
    '',
    '',
    data,
    { type: 'displayTotal', consumeOffline: true }
  );

  newState.Messages = [...newState.Messages, displayTotalMsg];

  return newState;
});
