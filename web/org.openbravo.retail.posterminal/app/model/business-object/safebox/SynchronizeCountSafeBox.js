/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action that fires the synchronization of safebox
 */

OB.App.StateAPI.Global.registerAction(
  'synchronizeCountSafeBox',
  (state, payload) => {
    const newState = { ...state };
    // set the new object's ID property if not already present
    const newObject = { id: OB.App.UUID.generate(), ...payload };
    const data = {
      modelName: 'CountSafeBox',
      service: 'org.openbravo.retail.posterminal.ProcessCountSafeBox',
      newObject: payload
    };
    const backendMsg = OB.App.State.Messages.Utils.createNewMessage(
      data.modelName,
      data.service,
      newObject,
      { type: 'backend' }
    );

    newState.Messages = [...newState.Messages, backendMsg];

    return newState;
  }
);
