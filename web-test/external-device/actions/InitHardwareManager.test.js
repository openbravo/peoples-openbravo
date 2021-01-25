/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* global global */

global.OB = {
  App: {
    Class: {}
  }
};

global.lodash = require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/UUID');
require('.../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/Messages');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/MessagesUtils');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/actions/InitHardwareManager');

OB.App.State = { Messages: { Utils: {} } };
OB.App.StateAPI.Messages.utilities.forEach(util => {
  OB.App.State.Messages.Utils[util.functionName] = util.implementation;
});

describe('InitHardwareManager action', () => {
  it('Expected state after InitHardwareManager action', () => {
    const state = {
      Messages: []
    };
    const payload = {};
    deepfreeze(state);
    const newState = OB.App.StateAPI.Global.initHardwareManager(state, payload);
    const expectedState = {
      Messages: [
        {
          id: expect.stringMatching(/^[0-9A-F]{32}$/),
          messageObj: {
            data: {}
          },
          modelName: '',
          service: '',
          time: expect.any(Number),
          type: 'initHardwareManager',
          consumeOffline: true
        }
      ]
    };
    expect(newState).toMatchObject(expectedState);
  });
});
