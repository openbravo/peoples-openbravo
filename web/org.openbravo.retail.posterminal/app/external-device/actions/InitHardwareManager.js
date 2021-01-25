/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to create a message that when consumed fires the initial
 *               communication with the Hardware Manager and its related tasks.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

OB.App.StateAPI.Global.registerAction('initHardwareManager', state => {
  const newState = { ...state };

  const newMsg = OB.App.State.Messages.Utils.createNewMessage(
    '',
    '',
    {},
    { type: 'initHardwareManager', consumeOffline: true }
  );

  newState.Messages = [...newState.Messages, newMsg];

  return newState;
});
