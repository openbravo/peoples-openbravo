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
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/initCashup');
require('./SetupCashupUtils');

describe('Cashup - init cashup State Action', () => {
  let initCashup;

  beforeAll(() => {
    initCashup = OB.App.StateAPI.Cashup.initCashup;
  });

  it('initialize cashup from local', () => {
    const initialState = {
      id: 'D208D4D868EC1E5C9316006606A90911',
      cashPaymentMethodInfo: []
    };

    const expectedState = {
      id: 'D208D4D868EC1E5C9316006606A90911',
      cashPaymentMethodInfo: []
    };

    deepfreeze(initialState);
    const result = initCashup(initialState, {
      initCashupFrom: 'local',
      terminalPayments: []
    });

    expect(result).toEqual(expectedState);
  });
});
