/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action that fires the synchronization of a new business partner
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

OB.App.StateAPI.Global.registerAction(
  'synchronizeBusinessPartner',
  (state, payload) => {
    const newState = { ...state };
    const data = {
      modelName: 'BusinessPartner',
      service: 'org.openbravo.retail.posterminal.CustomerLoader',
      newObject: payload
    };

    const messages = OB.App.State.Messages.Utils.createMasterdataMessages(data);
    newState.Messages = [...newState.Messages, ...messages];

    return newState;
  }
);
