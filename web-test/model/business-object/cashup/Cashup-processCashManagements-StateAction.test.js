/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupCashup');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/business-object/messages/Messages.js');

const deepfreeze = require('deepfreeze');

require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/actions/ProcessCashManagements');
require('./SetupGlobalUtils');

require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/UUID.js');

const currentCashup = require('./test-data/cleanCashup');
const terminalPayments = require('./test-data/terminalPayments');

describe('Cashup - process Cash Management State Action', () => {
  function addCashManagement(cashup, cashManagement) {
    const paymentMethod = cashup.cashPaymentMethodInfo.find(
      payment => payment.paymentMethodId === cashManagement.paymentMethodId
    );
    paymentMethod.cashManagements.push(cashManagement);
  }
  it('process all cash managements', () => {
    const initialState = { Cashup: currentCashup, Messages: [] };

    const payload = {
      parameters: {
        terminalPayments: terminalPayments,
        terminalName: 'VBS-1',
        cacheSessionId: 'B0C3C343D9104FA29E805F5424CE2BE8'
      }
    };

    const addedCashManagement = {
      id: '539AE3F2670FFFD7D8DC1A81D07B399A',
      description: 'Cash - Backoffice transfer to VBS - Yosemite Store',
      amount: 1,
      origAmount: 1,
      type: 'deposit',
      paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
      cashup_id: '0274FA0A8DC4E59354D4A71ED56F486D',
      isDraft: true
    };
    addCashManagement(initialState.Cashup, addedCashManagement);
    deepfreeze(initialState);
    const result = OB.App.StateAPI.Global.processCashManagements(
      initialState,
      payload
    );

    const paymentInfo = result.Cashup.cashPaymentMethodInfo.find(
      paymentMethod => {
        return (
          paymentMethod.paymentMethodId === addedCashManagement.paymentMethodId
        );
      }
    );
    const processedCashManagement = { ...addedCashManagement };
    delete processedCashManagement.isDraft;
    delete paymentInfo.cashManagements[0].cashUpReportInformation;
    expect(paymentInfo.cashManagements).toEqual([processedCashManagement]);
    expect(paymentInfo.totalDeposits).toEqual(1);
    expect(paymentInfo.totalDrops).toEqual(0);
    expect(result.Messages).toHaveLength(1);
    const message = result.Messages[0];
    expect(message.modelName).toEqual('Cash Management');
    const messageData = message.messageObj.data[0];
    expect(messageData.amount).toEqual(1);
    expect(messageData.paymentMethodId).toEqual(
      addedCashManagement.paymentMethodId
    );
    expect(messageData.type).toEqual(addedCashManagement.type);
  });
});
