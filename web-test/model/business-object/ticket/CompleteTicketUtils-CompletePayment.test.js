/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupTicket');
require('./SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('Ticket Utils completePayment function', () => {
  it('should keep payment if positive ticket', () => {
    const ticket = deepfreeze({
      isNegative: false,
      payments: [
        {
          orderGross: 100,
          amount: 100,
          origAmount: 100,
          paid: 100,
          precision: 2
        }
      ]
    });
    const newTicket = OB.App.State.Ticket.Utils.completePayment(ticket);
    expect(newTicket).toMatchObject({
      isNegative: false,
      payments: [
        {
          orderGross: 100,
          amount: 100,
          origAmount: 100,
          paid: 100,
          precision: 2
        }
      ]
    });
  });

  it('should update payment if negative ticket', () => {
    const ticket = deepfreeze({
      isNegative: true,
      payments: [
        {
          orderGross: 100,
          amount: 100,
          origAmount: 100,
          paid: 100,
          precision: 2
        }
      ]
    });
    const newTicket = OB.App.State.Ticket.Utils.completePayment(ticket);
    expect(newTicket).toMatchObject({
      isNegative: true,
      payments: [
        {
          orderGross: 100,
          amount: -100,
          origAmount: -100,
          paid: -100,
          precision: 2
        }
      ]
    });
  });

  it('should generate payment if ticket with change', () => {
    const ticket = deepfreeze({
      grossAmount: 100,
      payments: [
        {
          kind: 'A',
          orderGross: 100,
          amount: 100,
          origAmount: 100,
          paid: 100
        }
      ],
      changePayments: [
        {
          key: 'B',
          amount: 20,
          origAmount: 20
        }
      ]
    });
    const newTicket = OB.App.State.Ticket.Utils.completePayment(ticket, {
      terminal: {},
      preferences: {},
      payments: [
        {
          payment: { searchKey: 'B' },
          paymentMethod: {}
        }
      ]
    });
    expect(newTicket).toMatchObject({
      grossAmount: 100,
      payments: [
        {
          kind: 'A',
          orderGross: 100,
          amount: 100,
          origAmount: 100,
          paid: 100
        },
        {
          kind: 'B',
          orderGross: 100,
          amount: -20,
          origAmount: -20,
          paid: -20,
          amountRounded: 0,
          origAmountRounded: 0,
          changePayment: true
        }
      ]
    });
  });
});
