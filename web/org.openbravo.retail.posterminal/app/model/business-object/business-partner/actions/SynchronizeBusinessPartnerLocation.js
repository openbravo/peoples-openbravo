/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
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
      modelName: 'OBPOS_BusinessPartnerLocation',
      masterDataModelName: 'BusinessPartnerLocation',
      service: 'org.openbravo.retail.posterminal.CustomerAddrLoader',
      newObject: payload,
      extraProperties: {
        name: 'OBPOS_LblCustomerAddress'
      }
    };

    const messages = OB.App.State.Messages.Utils.createMasterdataMessages(data);
    newState.Messages = [...newState.Messages, ...messages];

    return newState;
  }
);

OB.App.StateAPI.Global.registerAction(
  'saveBusinessPartnerLocation',
  (state, payload) => {
    const newState = { ...state };

    // set the new object's ID property if not already present
    const newObject = { id: OB.App.UUID.generate(), ...payload };
    const localMsg = OB.App.State.Messages.Utils.createNewMessage(
      'BusinessPartnerLocation',
      '',
      newObject,
      {
        type: 'masterdata',
        forceLocalSave: true
      }
    );

    newState.Messages = [...newState.Messages, localMsg];
    return newState;
  }
);

OB.App.StateAPI.registerIdentifierForMessages(
  'OBPOS_BusinessPartnerLocation',
  message => {
    // eslint-disable-next-line no-underscore-dangle
    return message._identifier;
  }
);
