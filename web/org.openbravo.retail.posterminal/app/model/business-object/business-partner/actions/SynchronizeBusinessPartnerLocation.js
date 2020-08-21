/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action that fires the synchronization of a new business partner location
 */

OB.App.StateAPI.Global.registerAction(
  'synchronizeBusinessPartnerLocation',
  (state, payload) => {
    const newState = { ...state };
    const data = {
      modelName: 'BusinessPartnerLocation',
      service: 'org.openbravo.retail.posterminal.CustomerAddrLoader',
      newObject: payload
    };

    const messages = OB.App.State.Messages.Utils.createMasterdataMessages(data);
    newState.Messages = [...newState.Messages, ...messages];

    return newState;
  }
);
