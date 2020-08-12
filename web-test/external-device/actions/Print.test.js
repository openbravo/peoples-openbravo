/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
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
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/Messages');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/MessagesUtils');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/actions/Print');

OB.App.State = { Messages: { Utils: {} } };
OB.App.StateAPI.Messages.utilities.forEach(util => {
  OB.App.State.Messages.Utils[util.functionName] = util.implementation;
});

describe('Print action', () => {
  it('Expected state after printing state ticket', () => {
    const state = { Messages: [], Ticket: { dummy: 'state' } };
    const payload = {
      printSettings: {
        offline: true
      }
    };
    deepfreeze(state);
    const newState = OB.App.StateAPI.Global.printTicket(state, payload);
    const expectedState = {
      Messages: [
        {
          id: expect.stringMatching(/^[0-9A-F]{32}$/),
          messageObj: {
            data: {
              ticket: { dummy: 'state' },
              printSettings: {
                offline: true
              }
            }
          },
          modelName: '',
          service: '',
          time: expect.any(Number),
          type: 'printTicket',
          consumeOffline: true
        }
      ],
      Ticket: { dummy: 'state' }
    };
    expect(newState).toMatchObject(expectedState);
  });

  it('Expected state after printing payload ticket', () => {
    const state = { Messages: [], Ticket: { dummy: 'state' } };
    const payload = {
      ticket: { dummy: 'payload' },
      printSettings: {
        offline: true
      }
    };
    deepfreeze(state);
    const newState = OB.App.StateAPI.Global.printTicket(state, payload);
    const expectedState = {
      Messages: [
        {
          id: expect.stringMatching(/^[0-9A-F]{32}$/),
          messageObj: {
            data: {
              ticket: { dummy: 'payload' },
              printSettings: {
                offline: true
              }
            }
          },
          modelName: '',
          service: '',
          time: expect.any(Number),
          type: 'printTicket',
          consumeOffline: true
        }
      ],
      Ticket: { dummy: 'state' }
    };
    expect(newState).toMatchObject(expectedState);
  });
});
