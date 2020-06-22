/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupCashup');
const {
  executeActionPreparations
} = require('../../../../../org.openbravo.mobile.core/web-test/base/state-utils');
const deepfreeze = require('deepfreeze');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/actions/InitCashup');
require('./SetupCashupUtils');

const terminalPayments = require('./test-data/terminalPayments');
const currentCashup = require('./test-data/cleanCashup');

describe('init cashup Action Preparation', () => {
  it('initialize from local', async () => {
    const currentState = {
      Cashup: currentCashup
    };

    const payloadForInitCashupActionPreparation = {
      currentDate:
        'Mon Jun 22 2020 17:35:33 GMT+0200 (Central European Summer Time)',
      userId: '3073EDF96A3C42CC86C7069E379522D2',
      terminalId: '9104513C2D0741D4850AE8493998A7C8',
      terminalIsSlave: false,
      terminalIsMaster: false,
      terminalPayments: terminalPayments,
      terminalName: 'VBS-1',
      cacheSessionId: 'B0C3C343D9104FA29E805F5424CE2BE8'
    };

    const expectedPayload = {
      ...payloadForInitCashupActionPreparation,
      initCashupFrom: 'local'
    };

    deepfreeze(currentState);
    deepfreeze(payloadForInitCashupActionPreparation);
    const result = await executeActionPreparations(
      OB.App.StateAPI.Global.initCashup,
      currentState,
      payloadForInitCashupActionPreparation
    );

    expect(result).toEqual(expectedPayload);
  });
});
