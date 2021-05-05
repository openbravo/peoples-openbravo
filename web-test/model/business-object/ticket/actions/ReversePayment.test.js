/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ReversePayment');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');
OB.App.Locale = { formatAmount: number => number.toFixed(2) };

const basicTicket = deepfreeze({
  lines: [
    {
      id: '1',
      qty: 1,
      product: { id: 'p1' },
      baseGrossUnitPrice: 3
    }
  ],
  payments: [
    {
      amount: 3,
      date:
        'Fri Apr 30 2021 02:00:00 GMT+0200 (Central European Summer Time) {}',
      isPaid: true,
      isPrePayment: true,
      isReversed: true,
      isocode: 'EUR',
      kind: 'OBPOS_payment.cash',
      mulrate: '1.000000000000',
      name: 'Cash',
      oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
      oBPOSPOSTerminalSearchKey: 'VBS-1',
      obposAppCashup: '3B39E21A2DD9D8B74A5C3FCB316DA1FA',
      openDrawer: false,
      orderGross: 3,
      origAmount: 3,
      paid: 3,
      paymentAmount: 3,
      paymentData: {
        mergeable: true,
        properties: {},
        provider: { _identifier: 'Cash', provider: 'OBPOS2_PaymentProvider' }
      },
      paymentDate: '2021-04-30T00:00:00.000Z',
      paymentId: '187C74691C3587B90742BC2247EFC790',
      precision: 2,
      rate: '1'
    }
  ]
});

const terminalPayments = deepfreeze([
  {
    payment: {
      _identifier: 'Cash',
      searchKey: 'OBPOS_payment.cash'
    },
    obposPosprecision: 2
  },
  {
    payment: {
      _identifier: 'Card',
      searchKey: 'OBPOS_payment.card'
    },
    obposPosprecision: 2
  },
  {
    payment: {
      _identifier: 'Voucher',
      searchKey: 'OBPOS_payment.voucher'
    },
    paymentMethod: {
      countPerAmount: true
    },
    obposPosprecision: 2
  }
]);

const reversePayment = deepfreeze({
  allowOpenDrawer: true,
  amount: -3,
  date: undefined,
  index: 0,
  isCash: true,
  isPaid: undefined,
  isPrePayment: undefined,
  isReversePayment: true,
  isocode: 'EUR',
  kind: 'OBPOS_payment.cash',
  mulrate: '1.000000000000',
  name: 'Cash',
  oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
  oBPOSPOSTerminalSearchKey: 'VBS-1',
  obposAppCashup: '3B39E21A2DD9D8B74A5C3FCB316DA1FA',
  openDrawer: false,
  orderGross: 3,
  origAmount: -3,
  paid: -3,
  paymentAmount: undefined,
  paymentData: {
    mergeable: true,
    properties: {},
    provider: { _identifier: 'Cash', provider: 'OBPOS2_PaymentProvider' }
  },
  paymentDate: undefined,
  paymentId: undefined,
  paymentRoundingLine: undefined,
  precision: 2,
  printtwice: false,
  rate: '1',
  reversedPayment: {
    allowOpenDrawer: true,
    amount: 3,
    date: '2021-04-30T00:00:00.000Z',
    isCash: true,
    isPaid: true,
    isPrePayment: true,
    isReversed: true,
    isocode: 'EUR',
    kind: 'OBPOS_payment.cash',
    mulrate: '1.000000000000',
    name: 'Cash',
    oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
    oBPOSPOSTerminalSearchKey: 'VBS-1',
    obposAppCashup: '3B39E21A2DD9D8B74A5C3FCB316DA1FA',
    openDrawer: false,
    orderGross: 3,
    origAmount: 3,
    paid: 3,
    paymentAmount: 3,
    paymentData: {
      mergeable: true,
      properties: {},
      provider: { _identifier: 'Cash', provider: 'OBPOS2_PaymentProvider' }
    },
    paymentDate: '2021-04-30T00:00:00.000Z',
    paymentId: '187C74691C3587B90742BC2247EFC790',
    precision: 2,
    printtwice: false,
    rate: '1'
  },
  reversedPaymentId: '187C74691C3587B90742BC2247EFC790'
});

const terminal = { id: '9104513C2D0741D4850AE8493998A7C8' };
describe('Ticket.reversePayment action', () => {
  it('reverse a payment', () => {
    const newTicket = OB.App.StateAPI.Ticket.reversePayment(basicTicket, {
      payments: terminalPayments,
      terminal,
      payment: reversePayment
    });
    expect(newTicket.payments).toHaveLength(2);
  });
});
