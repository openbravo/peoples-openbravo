/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to print a document.
 */

OB.App.StateAPI.Global.registerAction('printDocument', (state, payload) => {
  const newState = { ...state };
  const message = OB.App.State.Messages.Utils.createNewMessage(
    '',
    '',
    payload,
    {
      type: 'printDocument',
      consumeOffline: true
    }
  );
  newState.Messages = [...newState.Messages, message];
  return newState;
});
