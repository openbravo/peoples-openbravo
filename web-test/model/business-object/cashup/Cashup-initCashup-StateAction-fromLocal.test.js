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

const terminalPayments = require('./test-data/terminalPayments');
deepfreeze(terminalPayments);
const cleanCashup = require('./test-data/cleanCashup');
deepfreeze(cleanCashup);

const payloadForInitCashupActionPreparation = {
  currentDate: new Date('2020-06-25T17:15:04.299Z'),
  userId: '3073EDF96A3C42CC86C7069E379522D2',
  terminalId: '9104513C2D0741D4850AE8493998A7C8',
  terminalIsSlave: false,
  terminalIsMaster: false,
  terminalPayments: terminalPayments,
  terminalName: 'VBS-1',
  cacheSessionId: 'B0C3C343D9104FA29E805F5424CE2BE8'
};
deepfreeze(payloadForInitCashupActionPreparation);

const payloadInitFromLocal = {
  ...payloadForInitCashupActionPreparation,
  initCashupFrom: 'local'
};
deepfreeze(payloadInitFromLocal);

describe('Cashup - init cashup State Action - from local', () => {
  let initCashup;

  beforeAll(() => {
    initCashup = OB.App.StateAPI.Global.initCashup;
  });

  it('initialize cashup from local', () => {
    const initialState = { Cashup: cleanCashup };
    deepfreeze(initialState);
    const expectedState = { Cashup: cleanCashup };
    deepfreeze(expectedState);
    const result = initCashup(initialState, payloadInitFromLocal);
    expect(result).toEqual(expectedState);
  });

  it('initialize cashup from local - new Payment', () => {
    // remove payment card from the initial state
    const cashupWithoutCard = { ...cleanCashup };
    cashupWithoutCard.cashPaymentMethodInfo = cashupWithoutCard.cashPaymentMethodInfo.filter(
      payment => payment.name !== 'Card'
    );
    deepfreeze(cashupWithoutCard);
    const initialState = { Cashup: cashupWithoutCard };
    deepfreeze(initialState);

    // readd the payment card in the expected state,
    // note: readding instead using the cleanState, because when added the new payment it is added at the end of the array.
    const cashupAddingCard = { ...cashupWithoutCard };
    const cardPayment = cleanCashup.cashPaymentMethodInfo
      .filter(payment => payment.name === 'Card')
      .reduce((a, b) => a.concat(b));
    deepfreeze(cardPayment);
    cashupAddingCard.cashPaymentMethodInfo = [
      ...cashupAddingCard.cashPaymentMethodInfo,
      cardPayment
    ];
    deepfreeze(cashupAddingCard);
    const expectedState = { Cashup: cashupAddingCard };
    deepfreeze(expectedState);

    OB.App.UUID = {};
    OB.App.UUID.generate = jest.fn();
    OB.App.UUID.generate.mockReturnValue('225405573F92976A56776BC5C5BF6595');
    const result = initCashup(initialState, payloadInitFromLocal);
    expect(result).toEqual(expectedState);
  });

  it('initialize cashup from local - payment name changed', () => {
    // change payment name in the payload
    const payloadPaymentNameChanged = { ...payloadInitFromLocal };
    payloadPaymentNameChanged.terminalPayments = [
      ...payloadInitFromLocal.terminalPayments
    ];
    payloadPaymentNameChanged.terminalPayments[0] = {
      ...payloadPaymentNameChanged.terminalPayments[0]
    };
    payloadPaymentNameChanged.terminalPayments[0].paymentMethod = {
      ...payloadPaymentNameChanged.terminalPayments[0].paymentMethod
    };
    payloadPaymentNameChanged.terminalPayments[0].paymentMethod._identifier =
      'Credit card - Modified';
    deepfreeze(payloadPaymentNameChanged);

    // change payment name in the expected state
    const expectedCashupWithPaymentNameModified = { ...cleanCashup };
    expectedCashupWithPaymentNameModified.cashPaymentMethodInfo = cleanCashup.cashPaymentMethodInfo.map(
      payment => {
        if (payment.name === 'Credit Card') {
          return { ...payment, name: 'Credit card - Modified' };
        } else {
          return payment;
        }
      }
    );
    deepfreeze(expectedCashupWithPaymentNameModified);

    const initialState = { Cashup: cleanCashup };
    deepfreeze(initialState);
    const expectedState = { Cashup: expectedCashupWithPaymentNameModified };
    deepfreeze(expectedState);
    const result = initCashup(initialState, payloadInitFromLocal);
    expect(result).toEqual(expectedState);
  });
});
