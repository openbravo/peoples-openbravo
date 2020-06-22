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

describe('Cashup - create Cash Management State Action', () => {
  it('create a cash management', () => {
    const initialState = {
      id: 'C74BF4B09DD827502AABA8DF4882DEDC',
      cashPaymentMethodInfo: [
        {
          id: '8F7C3BE0AF285B5AD75A20C82704B58F',
          name: 'Card',
          paymentMethodId: '1297F940A627474F935F7D14DBF35460',
          searchKey: 'OBPOS_payment.card',
          cashManagements: []
        },
        {
          id: '3AEFBCC53A3C987A9BC353CB1374203C',
          name: 'Cash',
          paymentMethodId: '676B26243EA547A1A52AEB302BFFA2BA',
          searchKey: 'OBPOS_payment.cash',
          cashManagements: []
        }
      ]
    };

    const payload = {
      cashManagement: {
        id: '539AE3F2670FFFD7D8DC1A81D07B399A',
        description: 'Cash - Backoffice transfer to VBS - Yosemite Store',
        amount: 1,
        origAmount: 1,
        type: 'deposit',
        paymentMethodId: '676B26243EA547A1A52AEB302BFFA2BA',
        cashup_id: 'C74BF4B09DD827502AABA8DF4882DEDC'
      }
    };

    const expectedState = {
      id: 'C74BF4B09DD827502AABA8DF4882DEDC',
      cashPaymentMethodInfo: [
        {
          id: '8F7C3BE0AF285B5AD75A20C82704B58F',
          name: 'Card',
          paymentMethodId: '1297F940A627474F935F7D14DBF35460',
          searchKey: 'OBPOS_payment.card',
          cashManagements: []
        },
        {
          id: '3AEFBCC53A3C987A9BC353CB1374203C',
          name: 'Cash',
          paymentMethodId: '676B26243EA547A1A52AEB302BFFA2BA',
          searchKey: 'OBPOS_payment.cash',
          cashManagements: [
            {
              id: '539AE3F2670FFFD7D8DC1A81D07B399A',
              isDraft: true,
              description: 'Cash - Backoffice transfer to VBS - Yosemite Store',
              amount: 1,
              origAmount: 1,
              type: 'deposit',
              paymentMethodId: '676B26243EA547A1A52AEB302BFFA2BA',
              cashup_id: 'C74BF4B09DD827502AABA8DF4882DEDC'
            }
          ]
        }
      ]
    };

    deepfreeze(initialState);
    const result = OB.App.StateAPI.Cashup.createCashManagement(
      initialState,
      payload
    );

    expect(result).toEqual(expectedState);
  });
});
