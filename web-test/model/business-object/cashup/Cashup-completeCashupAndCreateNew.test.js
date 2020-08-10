/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global global*/
require('./SetupCashup');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/Messages.js');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/actions/CompleteCashupAndCreateNew');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
require('./SetupGlobalUtils');

const cashupBeforeComplete = require('./test-data/cashupBeforeComplete');
deepfreeze(cashupBeforeComplete);
const cashupAfterComplete = require('./test-data/cashupAfterComplete');
deepfreeze(cashupAfterComplete);
const messagesAfterCompleteCashup = require('./test-data/messagesAfterCompleteCashup');
deepfreeze(messagesAfterCompleteCashup);
const messagesAfterCompleteCashupSharedPayments = require('./test-data/messagesAfterCompleteCashupSharedPayments');
deepfreeze(messagesAfterCompleteCashupSharedPayments);
const closeCashupInfoForCompleteCashup = require('./test-data/closeCashupInfoForCompleteCashup');
deepfreeze(closeCashupInfoForCompleteCashup);
const terminalPayments = require('./test-data/terminalPayments');
deepfreeze(terminalPayments);

describe('Cashup - complete cashup and create new', () => {
  let compleateCashupAndCreateNew;

  beforeAll(() => {
    compleateCashupAndCreateNew =
      OB.App.StateAPI.Global.completeCashupAndCreateNew;
  });

  it('complete cashup and create new', () => {
    const initialState = { Cashup: cashupBeforeComplete, Messages: [] };
    deepfreeze(initialState);
    const expectedState = {
      Cashup: cashupAfterComplete,
      Messages: messagesAfterCompleteCashup
    };
    const payload = {
      completedCashupParams: {
        closeCashupInfo: closeCashupInfoForCompleteCashup, //JSON.parse(cashUp.at(0).get('objToSend')),
        terminalName: 'VBS-1',
        cacheSessionId: 'DD9EBD1620664C8593FC1AD644F86821'
      },
      newCashupParams: {
        currentDate: new Date('2020-06-26T14:36:53.173Z'),
        userId: '3073EDF96A3C42CC86C7069E379522D2',
        terminalId: '9104513C2D0741D4850AE8493998A7C8',
        terminalIsSlave: false,
        terminalIsMaster: false,
        terminalPayments: terminalPayments
      }
    };
    deepfreeze(payload);
    deepfreeze(expectedState);
    OB.App.UUID = {};
    OB.App.UUID.generate = jest
      .fn()
      .mockReturnValueOnce('83B5C6BE04037E1F85C92404F53433BD')
      .mockReturnValueOnce('CD0EA49B0941ADFD933081E34CF9BEB9')
      .mockReturnValueOnce('FDAB894083D7A22D971A42AF3C63EBF3')
      .mockReturnValueOnce('21A3F0739E4DA84D1F31FEF0281B1C23')
      .mockReturnValueOnce('9CC37F3EDAE5F37D55D37F783A2AE5E1')
      .mockReturnValueOnce('582637FC4A84DBC7F3E3BCBEA385BF30')
      .mockReturnValueOnce('718C1EC40B8A629F4DA732C46DB2709E');

    const mockDate = new Date(1593182307146);
    const spy = jest.spyOn(global, 'Date').mockImplementation(() => mockDate);
    const result = compleateCashupAndCreateNew(initialState, payload);
    spy.mockRestore();
    expect(result).toEqual(expectedState);
  });

  it('complete cashup and create new - shared payments', () => {
    const initialState = { Cashup: cashupBeforeComplete, Messages: [] };
    deepfreeze(initialState);
    const expectedState = {
      Cashup: cashupAfterComplete,
      Messages: messagesAfterCompleteCashupSharedPayments
    };
    deepfreeze(expectedState);
    const payload = {
      completedCashupParams: {
        closeCashupInfo: closeCashupInfoForCompleteCashup, //JSON.parse(cashUp.at(0).get('objToSend')),
        terminalName: 'VBS-1',
        cacheSessionId: 'DD9EBD1620664C8593FC1AD644F86821'
      },
      newCashupParams: {
        currentDate: new Date('2020-06-26T14:36:53.173Z'),
        userId: '3073EDF96A3C42CC86C7069E379522D2',
        terminalId: '9104513C2D0741D4850AE8493998A7C8',
        terminalIsSlave: true,
        terminalIsMaster: false,
        terminalPayments: terminalPayments
      }
    };
    deepfreeze(payload);
    OB.App.UUID = {};
    OB.App.UUID.generate = jest
      .fn()
      .mockReturnValueOnce('83B5C6BE04037E1F85C92404F53433BD')
      .mockReturnValueOnce('CD0EA49B0941ADFD933081E34CF9BEB9')
      .mockReturnValueOnce('FDAB894083D7A22D971A42AF3C63EBF3')
      .mockReturnValueOnce('21A3F0739E4DA84D1F31FEF0281B1C23')
      .mockReturnValueOnce('9CC37F3EDAE5F37D55D37F783A2AE5E1')
      .mockReturnValueOnce('582637FC4A84DBC7F3E3BCBEA385BF30')
      .mockReturnValueOnce('718C1EC40B8A629F4DA732C46DB2709E')
      .mockReturnValueOnce('3CF92A24A63491CFCD674FC032AD9CE5')
      .mockReturnValueOnce('254D0D182905168F76FB9ADCB7C7EC81');

    const mockDate1 = new Date(1593182307146);
    const mockDate2 = new Date(1593184229010);
    const spy = jest
      .spyOn(global, 'Date')
      .mockImplementationOnce(() => mockDate1)
      .mockImplementationOnce(() => mockDate2);
    const result = compleateCashupAndCreateNew(initialState, payload);
    spy.mockRestore();
    expect(result).toEqual(expectedState);
  });
});
