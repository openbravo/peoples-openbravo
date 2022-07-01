/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../model/business-object/global/SetupGlobal');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/actions/PrintTicket');
require('../../model/business-object/global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

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
          messageObj: {
            data: {
              ticket: { dummy: 'state' },
              printSettings: {
                offline: true
              }
            }
          },
          modelName: 'OBMOBC_PrintTicket',
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
          messageObj: {
            data: {
              ticket: { dummy: 'payload' },
              printSettings: {
                offline: true
              }
            }
          },
          modelName: 'OBMOBC_PrintTicket',
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
