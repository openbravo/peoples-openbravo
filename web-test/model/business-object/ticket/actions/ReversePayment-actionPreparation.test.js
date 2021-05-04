/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
/* eslint-disable jest/expect-expect */

/**
 * @fileoverview performs tests on setQuantity action preparation
 * @see SetQuantity.test for unit tests on setQuantity action
 **/

global.OB = {
  App: {
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() }
  },
  UTIL: {
    HookManager: { registerHook: jest.fn() }
  }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
const {
  executeActionPreparations
} = require('../../../../../../org.openbravo.mobile.core/web-test/base/state-utils');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/exception/TranslatableError');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionSilentlyCanceled');

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/AddPaymentUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/ReversePaymentUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ReversePayment');

// set Ticket model utility functions
OB.App.State = { Ticket: { Utils: {} } };
OB.App.StateAPI.Ticket.utilities.forEach(
  util => (OB.App.State.Ticket.Utils[util.functionName] = util.implementation)
);
OB.DEC = OB.DEC || {};
OB.DEC.Zero = 0;
OB.DEC.One = 1;
OB.DEC.sub = jest.fn((a, b) => a - b);
OB.DEC.add = jest.fn((a, b) => a + b);
OB.DEC.isNumber = jest.fn(a => true);
OB.DEC.abs = jest.fn(a => a);
OB.DEC.compare = jest.fn(a => a);

const basicTicket = {
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
  ],
  prepaymentChangeMode: false
};

const terminalPayments = [
  {
    payment: {
      _identifier: 'Cash',
      searchKey: 'OBPOS_payment.cash'
    },
    obposPosprecision: 2,
    paymentMethod: {
      countPerAmount: true,
      _identifier: 'Cash',
      isreversable: true
    }
  },
  {
    payment: {
      _identifier: 'Card',
      searchKey: 'OBPOS_payment.card'
    },
    obposPosprecision: 2,
    paymentMethod: {
      countPerAmount: true,
      _identifier: 'Card',
      isreversable: true
    }
  },
  {
    payment: {
      _identifier: 'Voucher',
      searchKey: 'OBPOS_payment.voucher'
    },
    paymentMethod: {
      countPerAmount: true,
      _identifier: 'Voucher',
      isreversable: false
    },
    obposPosprecision: 2
  }
];

const payment = {
  allowOpenDrawer: true,
  amount: 3,
  date: '2021-04-30T00:00:00.000Z',
  isCash: true,
  isPaid: true,
  isPrePayment: true,
  isReversed: false,
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
  printtwice: false
};
const terminal = {
  id: '9104513C2D0741D4850AE8493998A7C8',
  terminalType: {
    calculateprepayments: false
  }
};

const reversePayment = {
  allowOpenDrawer: true,
  amount: -3,
  date: undefined,
  index: 0,
  isCash: true,
  isPaid: undefined,
  isPrePayment: undefined,
  isReversePayment: true,
  isReversed: false,
  isocode: 'EUR',
  kind: 'OBPOS_payment.cash',
  mulrate: '1.000000000000',
  name: 'Cash',
  oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
  oBPOSPOSTerminalSearchKey: 'VBS-1',
  obposAppCashup: '3B39E21A2DD9D8B74A5C3FCB316DA1FA',
  openDrawer: undefined,
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
  printtwice: undefined,
  rate: undefined,
  reversedPayment: {
    allowOpenDrawer: true,
    amount: 3,
    date: '2021-04-30T00:00:00.000Z',
    isCash: true,
    isPaid: true,
    isPrePayment: true,
    isReversed: false,
    isocode: 'EUR',
    kind: 'OBPOS_payment.cash',
    mulrate: '1.000000000000',
    name: 'Cash',
    oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
    oBPOSPOSTerminalSearchKey: 'VBS-1',
    obposAppCashup: '3B39E21A2DD9D8B74A5C3FCB316DA1FA',
    openDrawer: undefined,
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
    printtwice: undefined,
    rate: undefined
  },
  reversedPaymentId: '187C74691C3587B90742BC2247EFC790'
};

const ticketVoucher = {
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
      amount: 6,
      date:
        'Fri Apr 30 2021 02:00:00 GMT+0200 (Central European Summer Time) {}',
      isPaid: true,
      isPrePayment: true,
      isReversed: true,
      isocode: 'EUR',
      kind: 'OBPOS_payment.voucher',
      mulrate: '1.000000000000',
      name: 'Voucher',
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
        provider: { _identifier: 'Voucher', provider: 'OBPOS2_PaymentProvider' }
      },
      paymentDate: '2021-04-30T00:00:00.000Z',
      paymentId: '187C74691C3587B90742BC2247EFC790',
      precision: 2,
      rate: '1'
    }
  ],
  prepaymentChangeMode: false
};

const paymentVoucher = {
  allowOpenDrawer: true,
  amount: 6,
  date: 'Tue May 04 2021 02:00:00 GMT+0200 (Central European Summer Time) {}',
  isCash: false,
  isPaid: true,
  isPrePayment: true,
  isocode: 'EUR',
  kind: 'OBPOS_payment.voucher',
  mulrate: '1.000000000000',
  name: 'Voucher',
  oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
  oBPOSPOSTerminalSearchKey: 'VBS-1',
  obposAppCashup: '3B39E21A2DD9D8B74A5C3FCB316DA1FA',
  openDrawer: false,
  orderGross: 6,
  origAmount: 6,
  paid: 6,
  paymentAmount: 6,
  paymentData: {
    mergeable: true,
    properties: {},
    provider: { _identifier: 'Voucher', provider: 'OBPOS2_PaymentProvider' }
  },
  paymentDate: '2021-05-04T00:00:00.000Z',
  paymentId: 'EBDA843AD0BA96A7A983FC45DA9A2323',
  precision: 2,
  printtwice: false,
  rate: '1'
};

const wrongPayments = [
  {
    payment: {
      _identifier: 'Cash',
      searchKey: 'OBPOS_payment.cash'
    },
    obposPosprecision: 2,
    paymentMethod: {
      countPerAmount: true,
      _identifier: 'Cash',
      isreversable: true
    }
  },
  {
    payment: {
      _identifier: 'Cash2',
      searchKey: 'OBPOS_payment.cash'
    },
    obposPosprecision: 2,
    paymentMethod: {
      countPerAmount: true,
      _identifier: 'Cash2',
      isreversable: true
    }
  },
  {
    payment: {
      _identifier: 'Voucher',
      searchKey: 'OBPOS_payment.voucher'
    },
    paymentMethod: {
      countPerAmount: true,
      _identifier: 'Voucher',
      isreversable: false
    },
    obposPosprecision: 2
  }
];

const prepareAction = async (payload, ticket) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.reversePayment,
    deepfreeze(ticket),
    deepfreeze(payload)
  );
  return newPayload;
};

const expectError = async (action, expectedError) => {
  let error;
  try {
    await action();
  } catch (e) {
    error = e;
  }
  expect(error).toMatchObject({ info: expectedError });
};

describe('Reverse payment action preparation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    OB.App.Security.hasPermission.mockReturnValue(true);
  });

  describe('check reverse payment action preparation', () => {
    it('check payment is reversed', async () => {
      const newPayload = await prepareAction(
        {
          payments: terminalPayments,
          terminal,
          payment: payment
        },
        basicTicket
      );

      expect(newPayload.payment).toMatchObject(reversePayment);
    });
    it('check payment not reversable', async () => {
      await expectError(
        () =>
          prepareAction(
            {
              payments: terminalPayments,
              terminal,
              payment: paymentVoucher
            },
            ticketVoucher
          ),
        {
          messageParams: ['Voucher'],
          warningMsg: 'OBPOS_NotReversablePayment'
        }
      );
    });

    it('check more than one payment method validation', async () => {
      await expectError(
        () =>
          prepareAction(
            {
              payments: wrongPayments,
              terminal,
              payment: payment
            },
            basicTicket
          ),
        {
          messageParams: ['Cash'],
          warningMsg: 'OBPOS_MoreThanOnePaymentMethod'
        }
      );
    });
  });
});
