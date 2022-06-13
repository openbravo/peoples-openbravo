/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupCashup');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/Messages.js');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/actions/InitCashup');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
require('./SetupGlobalUtils');

const terminalPaymentsWhenLoadingCashupFromBackend = require('./test-data/terminalPaymentsWhenLoadingCashupFromBackend');
deepfreeze(terminalPaymentsWhenLoadingCashupFromBackend);
const unInitializeCashup = require('./test-data/unInitializeCashup');
deepfreeze(unInitializeCashup);
const cashupCreatedFromScratch = require('./test-data/cashupCreatedFromScratch');
deepfreeze(cashupCreatedFromScratch);
const cashupCreatedFromScratchWithoutLastCashupPayments = require('./test-data/cashupCreatedFromScratchWithoutLastCashupPayments');
deepfreeze(cashupCreatedFromScratchWithoutLastCashupPayments);
const requestCashupFromBackendProcessed = require('./test-data/requestCashupFromBackendProcessed');
deepfreeze(requestCashupFromBackendProcessed);
const cashupMessageSharedPayments = require('./test-data/cashupMessageSharedPayments');
deepfreeze(cashupMessageSharedPayments);

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

const payloadInitFromScratch = {
  ...payloadForInitCashupActionPreparation,
  initCashupFrom: 'scratch',
  lastCashUpPayments:
    requestCashupFromBackendProcessed.response.data[0].cashPaymentMethodInfo
};
deepfreeze(payloadInitFromScratch);

describe('Cashup - init cashup State Action - from scratch', () => {
  let initCashup;

  beforeAll(() => {
    initCashup = OB.App.StateAPI.Global.initCashup;
  });

  it('initialize cashup from scratch', () => {
    const initialState = { Cashup: unInitializeCashup };
    deepfreeze(initialState);
    const expectedState = {
      Cashup: cashupCreatedFromScratch
    };
    deepfreeze(expectedState);
    OB.App.UUID = {};
    OB.App.UUID.generate = jest
      .fn()
      .mockReturnValueOnce('568949F93EE9F6EE7020CA0933B9C185')
      .mockReturnValueOnce('C070C2E43D83CDEC145AB0572E7FFDDF')
      .mockReturnValueOnce('07EBF759AE5254264E54F3E676F5B5A1')
      .mockReturnValueOnce('9A39ECD75EF05115DD81CCB5DD177B0A')
      .mockReturnValueOnce('B333F171E7F747657C6EA36C2169DE63');
    const result = initCashup(initialState, payloadInitFromScratch);
    expect(result).toEqual(expectedState);
  });

  it('initialize cashup from scratch - without lastCashUpPayments', () => {
    const initialState = { Cashup: unInitializeCashup };
    deepfreeze(initialState);
    const payloadFromScratchWithoutlastCashUpPayments = {
      ...payloadInitFromScratch,
      lastCashUpPayments: null
    };
    deepfreeze(payloadFromScratchWithoutlastCashUpPayments);
    const expectedState = {
      Cashup: cashupCreatedFromScratchWithoutLastCashupPayments
    };
    deepfreeze(expectedState);
    OB.App.UUID = {};
    OB.App.UUID.generate = jest
      .fn()
      .mockReturnValueOnce('568949F93EE9F6EE7020CA0933B9C185')
      .mockReturnValueOnce('C070C2E43D83CDEC145AB0572E7FFDDF')
      .mockReturnValueOnce('07EBF759AE5254264E54F3E676F5B5A1')
      .mockReturnValueOnce('9A39ECD75EF05115DD81CCB5DD177B0A')
      .mockReturnValueOnce('B333F171E7F747657C6EA36C2169DE63');
    const result = initCashup(
      initialState,
      payloadFromScratchWithoutlastCashUpPayments
    );
    expect(result).toEqual(expectedState);
  });

  it('initialize cashup from scratch - shared payments', () => {
    const initialState = { Cashup: unInitializeCashup, Messages: [] };
    deepfreeze(initialState);
    const payloadFromScratchSharedPayments = {
      ...payloadInitFromScratch,
      terminalIsSlave: true
    };
    deepfreeze(payloadFromScratchSharedPayments);
    const expectedState = {
      Cashup: cashupCreatedFromScratch,
      Messages: cashupMessageSharedPayments
    };
    deepfreeze(expectedState);
    OB.App.UUID = {};
    OB.App.UUID.generate = jest
      .fn()
      .mockReturnValueOnce('568949F93EE9F6EE7020CA0933B9C185')
      .mockReturnValueOnce('C070C2E43D83CDEC145AB0572E7FFDDF')
      .mockReturnValueOnce('07EBF759AE5254264E54F3E676F5B5A1')
      .mockReturnValueOnce('9A39ECD75EF05115DD81CCB5DD177B0A')
      .mockReturnValueOnce('B333F171E7F747657C6EA36C2169DE63')
      .mockReturnValueOnce('25B643330792E8785B10AAB95FC8BB66')
      .mockReturnValueOnce('45C13AA2C79EAD7D766C99ECAAAB6D89');

    jest
      .useFakeTimers('modern')
      .setSystemTime(new Date(1593117791040).getTime());
    const result = initCashup(initialState, payloadFromScratchSharedPayments);
    jest.useRealTimers();
    expect(result).toEqual(expectedState);
  });
});
