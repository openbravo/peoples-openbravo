/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* global global */

global.OB = { App: { Class: {} } };

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/UUID');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/Messages');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/MessagesUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/business-partner/actions/SynchronizeBusinessPartnerLocation');

OB.App.State = { Messages: { Utils: {} } };
OB.App.StateAPI.Messages.utilities.forEach(util => {
  OB.App.State.Messages.Utils[util.functionName] = util.implementation;
});

describe('SynchronizeBusinessPartner action', () => {
  it('Expected state after Synchronize BusinessPartnerLocation', () => {
    const customerLocation = { id: '1', name: 'test' };
    const state = { Messages: [] };

    deepfreeze(state);
    const newState = OB.App.StateAPI.Global.synchronizeBusinessPartnerLocation(
      state,
      customerLocation
    );
    const expectedState = {
      Messages: [
        {
          id: expect.stringMatching(/^[0-9A-F]{32}$/),
          messageObj: {
            data: {
              id: '1',
              name: 'test'
            }
          },
          modelName: 'BusinessPartnerLocation',
          service: '',
          time: expect.any(Number),
          type: 'masterdata'
        },
        {
          id: expect.stringMatching(/^[0-9A-F]{32}$/),
          messageObj: {
            data: {
              id: '1',
              name: 'test'
            }
          },
          modelName: 'OBPOS_BusinessPartnerLocation',
          service: 'org.openbravo.retail.posterminal.CustomerAddrLoader',
          time: expect.any(Number),
          type: 'backend'
        }
      ]
    };
    expect(newState).toMatchObject(expectedState);
  });
});