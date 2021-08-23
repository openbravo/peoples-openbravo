/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global global*/

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddPaymentRounding');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

const saleTicket = {
  grossAmount: 150.53,
  lines: [
    {
      id: '1',
      qty: 1,
      product: { id: 'p1' },
      baseGrossUnitPrice: 150.53
    }
  ]
};

const returnTicket = {
  grossAmount: -150.53,
  isNegative: true,
  lines: [
    {
      id: '1',
      qty: -1,
      product: { id: 'p1' },
      baseGrossUnitPrice: 150.53
    }
  ]
};

const rounding = {
  payment: {
    _identifier: 'Rounding',
    searchKey: 'OBPOS_payment.rounding',
    commercialName: 'Rounding'
  },
  paymentMethod: {
    allowOpenDrawer: true,
    iscash: false,
    openDrawer: false,
    printtwice: false
  },
  obposPosprecision: 2,
  mulrate: '1.000000000000'
};

const cash = {
  payment: {
    _identifier: 'Cash',
    searchKey: 'OBPOS_payment.cash',
    commercialName: 'Cash'
  },
  obposPosprecision: 2
};

const cashWithDownRounding = {
  ...cash,
  paymentRounding: {
    paymentRoundingType: 'OBPOS_payment.rounding',
    saleRounding: true,
    saleRoundingMode: 'DR',
    saleRoundingMultiple: 0.05,
    returnRounding: true,
    returnRoundingMode: 'DR',
    returnRoundingMultiple: 0.05
  }
};

const cashWithUpRounding = {
  ...cash,
  paymentRounding: {
    paymentRoundingType: 'OBPOS_payment.rounding',
    saleRounding: true,
    saleRoundingMode: 'UR',
    saleRoundingMultiple: 0.05,
    returnRounding: true,
    returnRoundingMode: 'UR',
    returnRoundingMultiple: 0.05
  }
};

function getCashWithFullRounding(roundingMultiple, roundingLimit) {
  return {
    ...cash,
    paymentRounding: {
      paymentRoundingType: 'OBPOS_payment.rounding',
      saleRounding: true,
      saleRoundingMode: 'FR',
      saleRoundingMultiple: roundingMultiple,
      saleFullRoundingLimit: roundingLimit,
      returnRounding: true,
      returnRoundingMode: 'FR',
      returnRoundingMultiple: roundingMultiple,
      returnFullRoundingLimit: roundingLimit
    }
  };
}

const terminal = { id: '9104513C2D0741D4850AE8493998A7C8' };

function getCashPayment(amount) {
  return {
    allowOpenDrawer: true,
    amount,
    date: null,
    isCash: true,
    isocode: 'EUR',
    kind: 'OBPOS_payment.cash',
    mulrate: '1.000000000000',
    name: 'Cash',
    openDrawer: false,
    origAmount: 0,
    paid: 0,
    printtwice: false,
    rate: 1
  };
}

function withCashPayment(ticket, amount) {
  return {
    ...ticket,
    payment: amount,
    payments: [
      {
        allowOpenDrawer: true,
        amount,
        date: '2021-08-05T11:48:59.382Z',
        id: 'DE4D24197B1CEB19ED989EA187489978',
        isCash: true,
        isPaid: false,
        isReturnOrder: ticket.isNegative === false,
        isocode: 'EUR',
        kind: 'OBPOS_payment.cash',
        mulrate: '1.000000000000',
        name: 'Cash',
        oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
        openDrawer: false,
        orderGross: ticket.grossAmount,
        origAmount: amount,
        paid: amount,
        precision: 2,
        printtwice: false,
        rate: '1'
      }
    ]
  };
}

describe('Ticket.addPaymentRounding action', () => {
  const mockDate = new Date(1628164162079);
  const spy = jest.spyOn(global, 'Date').mockImplementation(() => mockDate);

  afterAll(() => {
    spy.mockRestore();
  });

  it.each`
    description                              | paid   | payments                            | isReversePayment
    ${'rounding amount > rounding multiple'} | ${100} | ${[cashWithDownRounding, rounding]} | ${false}
    ${'reverse payment'}                     | ${150} | ${[cashWithDownRounding, rounding]} | ${true}
    ${'no payment with rounding configured'} | ${150} | ${[cash, rounding]}                 | ${false}
  `(
    'no rounding payment added: $description',
    ({ paid, payments, isReversePayment }) => {
      const ticket = deepfreeze(withCashPayment(saleTicket, paid));
      const newTicket = OB.App.StateAPI.Ticket.addPaymentRounding(ticket, {
        payments,
        terminal,
        payment: { ...getCashPayment(0.5), isReversePayment }
      });

      expect(newTicket.payments).toHaveLength(1);
      expect(newTicket.payments).toMatchObject([
        {
          kind: 'OBPOS_payment.cash',
          name: 'Cash',
          allowOpenDrawer: true,
          amount: paid,
          date: '2021-08-05T11:48:59.382Z',
          id: 'DE4D24197B1CEB19ED989EA187489978',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 150.53,
          origAmount: paid,
          paid,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ]);
    }
  );

  it('add expected payment rounding', () => {
    const ticket = deepfreeze(withCashPayment(saleTicket, 150));
    const newTicket = OB.App.StateAPI.Ticket.addPaymentRounding(ticket, {
      payments: [cashWithDownRounding, rounding],
      terminal,
      payment: getCashPayment(0.5)
    });

    expect(newTicket.payments).toHaveLength(2);
    expect(newTicket.payments).toMatchObject([
      {
        kind: 'OBPOS_payment.cash',
        name: 'Cash',
        allowOpenDrawer: true,
        amount: 150,
        date: '2021-08-05T11:48:59.382Z',
        id: 'DE4D24197B1CEB19ED989EA187489978',
        isCash: true,
        isPaid: false,
        isReturnOrder: false,
        isocode: 'EUR',
        mulrate: '1.000000000000',
        oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
        openDrawer: false,
        orderGross: 150.53,
        origAmount: 150,
        paid: 150,
        precision: 2,
        printtwice: false,
        rate: '1'
      },
      {
        kind: 'OBPOS_payment.rounding',
        name: 'Rounding',
        allowOpenDrawer: true,
        amount: 0.03,
        date: mockDate,
        id: undefined,
        isCash: false,
        isPaid: undefined,
        isReturnOrder: undefined,
        isocode: undefined,
        mulrate: '1.000000000000',
        oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
        openDrawer: false,
        orderGross: 150.53,
        origAmount: 0.03,
        paid: 0.03,
        paymentRounding: true,
        precision: 2,
        printtwice: false,
        rate: undefined,
        roundedPaymentId: undefined
      }
    ]);
  });

  it.each`
    description                                | baseTicket      | payments                                           | roundingAmount
    ${'sale rounding: down rounding'}          | ${saleTicket}   | ${[cashWithDownRounding, rounding]}                | ${0.03}
    ${'sale rounding: up rounding'}            | ${saleTicket}   | ${[cashWithUpRounding, rounding]}                  | ${-0.02}
    ${'sale rounding: full rounding (down)'}   | ${saleTicket}   | ${[getCashWithFullRounding(0.05, 0.04), rounding]} | ${0.03}
    ${'sale rounding: full rounding (up)'}     | ${saleTicket}   | ${[getCashWithFullRounding(0.05, 0.03), rounding]} | ${-0.02}
    ${'return rounding: down rounding'}        | ${returnTicket} | ${[cashWithDownRounding, rounding]}                | ${0.03}
    ${'return rounding: up rounding'}          | ${returnTicket} | ${[cashWithUpRounding, rounding]}                  | ${-0.02}
    ${'return rounding: full rounding (down)'} | ${returnTicket} | ${[getCashWithFullRounding(0.05, 0.04), rounding]} | ${0.03}
    ${'return rounding: full rounding (up)'}   | ${returnTicket} | ${[getCashWithFullRounding(0.05, 0.03), rounding]} | ${-0.02}
  `('$description', ({ baseTicket, payments, roundingAmount }) => {
    const ticket = deepfreeze(withCashPayment(baseTicket, 150));
    const newTicket = OB.App.StateAPI.Ticket.addPaymentRounding(ticket, {
      payments,
      terminal,
      payment: getCashPayment(0.5)
    });

    expect(newTicket.payments).toHaveLength(2);
    expect(newTicket.payments[1]).toMatchObject({
      kind: 'OBPOS_payment.rounding',
      amount: roundingAmount,
      origAmount: roundingAmount,
      paid: roundingAmount
    });
  });

  it.each`
    description                          | grossAmount | payments                                       | roundingAmount
    ${'pay total: full rounding (down)'} | ${150.5}    | ${[getCashWithFullRounding(1, 0.5), rounding]} | ${-0.5}
    ${'pay total: full rounding (up)'}   | ${150.49}   | ${[getCashWithFullRounding(1, 0.5), rounding]} | ${0.49}
  `('$description', ({ grossAmount, payments, roundingAmount }) => {
    const ticket = deepfreeze({
      grossAmount,
      lines: [
        {
          id: '1',
          qty: 1,
          product: { id: 'p1' },
          baseGrossUnitPrice: grossAmount
        }
      ],
      payments: []
    });
    const newTicket = OB.App.StateAPI.Ticket.addPaymentRounding(ticket, {
      payments,
      terminal,
      payment: getCashPayment(grossAmount)
    });

    expect(newTicket.payments[0]).toMatchObject({
      kind: 'OBPOS_payment.rounding',
      amount: roundingAmount,
      origAmount: roundingAmount,
      paid: roundingAmount
    });
  });
});
