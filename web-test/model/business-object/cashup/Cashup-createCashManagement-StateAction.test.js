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
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/actions/CreateCashManagement');

const currentCashup = require('./test-data/cleanCashup');

describe('Cashup - create Cash Management State Action', () => {
  it('create a cash management', () => {
    const initialState = currentCashup;
    const payload = {
      cashManagement: {
        id: '539AE3F2670FFFD7D8DC1A81D07B399A',
        description: 'Cash - Backoffice transfer to VBS - Yosemite Store',
        amount: 1,
        origAmount: 1,
        type: 'deposit',
        paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
        cashup_id: '0274FA0A8DC4E59354D4A71ED56F486D',
        isDraft: true
      }
    };

    deepfreeze(initialState);

    const result = OB.App.StateAPI.Cashup.createCashManagement(
      initialState,
      payload
    );

    const p = result.cashPaymentMethodInfo.map(paymentMethod => {
      return {
        paymentMethodId: paymentMethod.paymentMethodId,
        cashManagements: paymentMethod.cashManagements
      };
    });

    expect(p).toEqual(
      expect.arrayContaining([
        {
          paymentMethodId: payload.cashManagement.paymentMethodId,
          cashManagements: [payload.cashManagement]
        }
      ])
    );
  });
});
