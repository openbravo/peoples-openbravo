/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
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

const terminalPaymentsWhenLoadingCashupFromBackend = require('./test-data/terminalPaymentsWhenLoadingCashupFromBackend');
deepfreeze(terminalPaymentsWhenLoadingCashupFromBackend);
const cleanCashup = require('./test-data/cleanCashup');
deepfreeze(cleanCashup);
const unInitializeCashup = require('./test-data/unInitializeCashup');
deepfreeze(unInitializeCashup);
const requestCashupFromBackend = require('./test-data/requestCashupFromBackend');
deepfreeze(requestCashupFromBackend);
const cashupAfterLoadCashupFromBackend = require('./test-data/cashupAfterLoadCashupFromBackend');
deepfreeze(cashupAfterLoadCashupFromBackend);
const cashupAfterLoadCashupFromBackendWithoutPayments = require('./test-data/cashupAfterLoadCashupFromBackendWithoutPayments');
deepfreeze(cashupAfterLoadCashupFromBackendWithoutPayments);

const payloadForInitCashupActionPreparation = {
  currentDate: new Date('2020-06-25T17:15:04.299Z'),
  userId: '3073EDF96A3C42CC86C7069E379522D2',
  terminalId: '9104513C2D0741D4850AE8493998A7C8',
  terminalIsSlave: false,
  terminalIsMaster: false,
  terminalPayments: terminalPaymentsWhenLoadingCashupFromBackend,
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
    deepfreeze(initialState);
    const expectedState = {
      Cashup: cashupAfterLoadCashupFromBackend
    };
    deepfreeze(expectedState);
    const result = initCashup(initialState, payloadInitFromBackend);
    expect(result).toEqual(expectedState);
  });

  it('initialize cashup from backend - without cashPaymentMethodInfo', () => {
    const initialState = { Cashup: unInitializeCashup };
    deepfreeze(initialState);
    const payloadWithoutCashPaymentMethodInfo = { ...payloadInitFromBackend };
    payloadWithoutCashPaymentMethodInfo.currentCashupFromBackend = {
      ...payloadWithoutCashPaymentMethodInfo.currentCashupFromBackend,
      cashPaymentMethodInfo: []
    };
    deepfreeze(payloadWithoutCashPaymentMethodInfo);
    const expectedState = {
      Cashup: cashupAfterLoadCashupFromBackendWithoutPayments
    };
    deepfreeze(expectedState);
    OB.App.UUID = {};
    OB.App.UUID.generate = jest
      .fn()
      .mockReturnValueOnce('8819C1397B056247DAE6C3151BEA12E6')
      .mockReturnValueOnce('9C9B00E0AF528CF677903DA413252EFF')
      .mockReturnValueOnce('67BCD015B8732AD381492A9BBE0D2DE7')
      .mockReturnValueOnce('8157C9E1C0A9748CA9A4F1A4D58D562F');

    const result = initCashup(
      initialState,
      payloadWithoutCashPaymentMethodInfo
    );
    expect(result).toEqual(expectedState);
  });
});
