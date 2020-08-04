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
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
require('./SetupCashupUtils');

const terminalPayments = require('./test-data/terminalPayments');
deepfreeze(terminalPayments);
const currentCashup = require('./test-data/cleanCashup');
deepfreeze(currentCashup);
const unInitializeCashup = require('./test-data/unInitializeCashup');
deepfreeze(unInitializeCashup);
const requestCashupFromBackend = require('./test-data/requestCashupFromBackend');
deepfreeze(requestCashupFromBackend);
const requestCashupFromBackendNoData = require('./test-data/requestCashupFromBackendNoData');
deepfreeze(requestCashupFromBackendNoData);
const requestCashupFromBackendProcessed = require('./test-data/requestCashupFromBackendProcessed');
deepfreeze(requestCashupFromBackendProcessed);

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

describe('init cashup Action Preparation', () => {
  it('initialize from local', async () => {
    // normal case
    const currentState = {
      Cashup: currentCashup
    };
    deepfreeze(currentState);
    const expectedPayload = {
      ...payloadForInitCashupActionPreparation,
      initCashupFrom: 'local'
    };
    deepfreeze(expectedPayload);
    const result = await executeActionPreparations(
      OB.App.StateAPI.Global.initCashup,
      currentState,
      payloadForInitCashupActionPreparation
    );
    expect(result).toEqual(expectedPayload);
  });

  it('initialize from backend', async () => {
    // clear cache after a ticket or cash management
    const currentState = {
      Cashup: unInitializeCashup
    };
    deepfreeze(currentState);

    const expectedPayload = {
      ...payloadForInitCashupActionPreparation,
      initCashupFrom: 'backend',
      currentCashupFromBackend: {
        ...requestCashupFromBackend.response.data[0],
        totalStartings: OB.DEC.Zero
      }
    };
    deepfreeze(expectedPayload);
    OB.App.Request = {};
    OB.App.Request.mobileServiceRequest = jest.fn();
    OB.App.Request.mobileServiceRequest.mockReturnValue(
      requestCashupFromBackend
    );

    const result = await executeActionPreparations(
      OB.App.StateAPI.Global.initCashup,
      currentState,
      payloadForInitCashupActionPreparation
    );
    expect(result).toEqual(expectedPayload);
  });

  it('initialize from scratch', async () => {
    // clear cache just after a cashup
    const currentState = {
      Cashup: unInitializeCashup
    };
    deepfreeze(currentState);

    const expectedPayload = {
      ...payloadForInitCashupActionPreparation,
      initCashupFrom: 'scratch',
      lastCashUpPayments:
        requestCashupFromBackendProcessed.response.data[0].cashPaymentMethodInfo
    };
    deepfreeze(expectedPayload);
    OB.App.Request = {};
    OB.App.Request.mobileServiceRequest = jest
      .fn()
      .mockImplementation((model, params) => {
        if (params.isprocessed === 'Y') {
          return requestCashupFromBackendProcessed;
        } else {
          return requestCashupFromBackendNoData;
        }
      });

    const result = await executeActionPreparations(
      OB.App.StateAPI.Global.initCashup,
      currentState,
      payloadForInitCashupActionPreparation
    );
    expect(result).toEqual(expectedPayload);
  });
});
