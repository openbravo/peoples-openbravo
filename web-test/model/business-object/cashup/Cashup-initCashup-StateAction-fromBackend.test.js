/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupCashup');
const deepfreeze = require('deepfreeze');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/actions/InitCashup');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
require('./SetupCashupUtils');

const terminalPayments = require('./test-data/terminalPayments');
deepfreeze(terminalPayments);
const cleanCashup = require('./test-data/cleanCashup');
deepfreeze(cleanCashup);
const unInitializeCashup = require('./test-data/unInitializeCashup');
deepfreeze(unInitializeCashup);
const requestCashupFromBackend = require('./test-data/requestCashupFromBackend');
deepfreeze(requestCashupFromBackend);
const cleanCacashupAfterLoadCashupFromBackendshup = require('./test-data/cashupAfterLoadCashupFromBackend');
deepfreeze(cleanCacashupAfterLoadCashupFromBackendshup);

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
deepfreeze(payloadForInitCashupActionPreparation);

const payloadInitFromBackend = {
  ...payloadForInitCashupActionPreparation,
  initCashupFrom: 'backend',
  currentCashupFromBackend: {
    ...requestCashupFromBackend.response.data[0],
    totalStartings: OB.DEC.Zero
  }
};
deepfreeze(payloadInitFromBackend);

describe('Cashup - init cashup State Action - from backend', () => {
  let initCashup;

  beforeAll(() => {
    initCashup = OB.App.StateAPI.Global.initCashup;
  });

  it('initialize cashup from backend', () => {
    const initialState = { Cashup: unInitializeCashup };
    const expectedState = {
      Cashup: cleanCacashupAfterLoadCashupFromBackendshup
    };
    deepfreeze(initialState);
    deepfreeze(payloadInitFromBackend);
    const result = initCashup(initialState, payloadInitFromBackend);
    expect(result).toEqual(expectedState);
  });
});
