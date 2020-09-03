/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/AddPaymentUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddPayment');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

const basicTicket = deepfreeze({
  lines: [
    {
      id: '1',
      qty: 100,
      product: { id: 'p1' },
      baseGrossUnitPrice: 5
    }
  ],
  payments: []
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
  }
]);
const cashPayment = deepfreeze({
  id: '1',
  amount: 200,
  origAmount: 200,
  kind: 'OBPOS_payment.cash'
});
const cardPayment = deepfreeze({
  id: '2',
  amount: 100,
  origAmount: 200,
  kind: 'OBPOS_payment.card'
});
const terminal = { id: '9104513C2D0741D4850AE8493998A7C8' };
describe('Ticket.addPayment action', () => {
  it('add simple payment', () => {
    const newTicket = OB.App.StateAPI.Ticket.addPayment(basicTicket, {
      payments: terminalPayments,
      terminal,
      payment: cashPayment
    });
    expect(newTicket.payments).toHaveLength(1);
  });
});

it('add two payments of same paymentType', () => {
  let newTicket = OB.App.StateAPI.Ticket.addPayment(basicTicket, {
    payments: terminalPayments,
    terminal,
    payment: cashPayment
  });
  newTicket = OB.App.StateAPI.Ticket.addPayment(newTicket, {
    payments: terminalPayments,
    terminal,
    payment: cashPayment
  });
  expect(newTicket.payments).toHaveLength(1);
});

it('add two payments of different paymentType', () => {
  let newTicket = OB.App.StateAPI.Ticket.addPayment(basicTicket, {
    payments: terminalPayments,
    terminal,
    payment: cashPayment
  });
  newTicket = OB.App.StateAPI.Ticket.addPayment(newTicket, {
    payments: terminalPayments,
    terminal,
    payment: cardPayment
  });
  expect(newTicket.payments).toHaveLength(2);
});
