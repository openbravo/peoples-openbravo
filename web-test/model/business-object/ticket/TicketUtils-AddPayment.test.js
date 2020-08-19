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

describe('Ticket Utils addPayment function', () => {
  it('should create payment if ticket without payments', () => {
    const ticket = deepfreeze({
      grossAmount: 100,
      isPaid: true,
      isNegative: false,
      payments: []
    });
    const newTicket = OB.App.State.Ticket.Utils.addPayment(ticket, {
      terminal: { id: '0' },
      payment: { amount: 100, origAmount: 100 },
      payments: []
    });
    expect(newTicket).toMatchObject({
      payment: 100,
      paymentWithSign: 100,
      payments: [
        {
          isPaid: true,
          isReturnOrder: false,
          oBPOSPOSTerminal: '0',
          orderGross: 100,
          amount: 100,
          origAmount: 100,
          paid: 100,
          precision: 2
        }
      ]
    });
  });

  it('should create payment if ticket with payment of different kind', () => {
    const ticket = deepfreeze({
      grossAmount: 120,
      isPaid: true,
      isNegative: false,
      payments: [
        {
          kind: 'A',
          isPaid: true,
          isReturnOrder: false,
          oBPOSPOSTerminal: '0',
          orderGross: 120,
          amount: 100,
          origAmount: 100,
          paid: 100,
          precision: 2
        }
      ]
    });
    const newTicket = OB.App.State.Ticket.Utils.addPayment(ticket, {
      terminal: { id: '0' },
      payment: { kind: 'B', amount: 20, origAmount: 20 },
      payments: []
    });
    expect(newTicket).toMatchObject({
      payment: 120,
      paymentWithSign: 120,
      payments: [
        {
          kind: 'A',
          isPaid: true,
          isReturnOrder: false,
          oBPOSPOSTerminal: '0',
          orderGross: 120,
          amount: 100,
          origAmount: 100,
          paid: 100,
          precision: 2
        },
        {
          kind: 'B',
          isPaid: true,
          isReturnOrder: false,
          oBPOSPOSTerminal: '0',
          orderGross: 120,
          amount: 20,
          origAmount: 20,
          paid: 20,
          precision: 2
        }
      ]
    });
  });

  it('should update payment if ticket with payment of same kind', () => {
    const ticket = deepfreeze({
      grossAmount: 120,
      isPaid: true,
      isNegative: false,
      payments: [
        {
          kind: 'A',
          isPaid: true,
          isReturnOrder: false,
          oBPOSPOSTerminal: '0',
          orderGross: 120,
          amount: 100,
          origAmount: 100,
          paid: 100,
          precision: 2
        }
      ]
    });
    const newTicket = OB.App.State.Ticket.Utils.addPayment(ticket, {
      terminal: { id: '0' },
      payment: { kind: 'A', amount: 20, origAmount: 20 },
      payments: []
    });
    expect(newTicket).toMatchObject({
      payment: 120,
      paymentWithSign: 120,
      payments: [
        {
          kind: 'A',
          isPaid: true,
          isReturnOrder: false,
          oBPOSPOSTerminal: '0',
          orderGross: 120,
          amount: 120,
          origAmount: 120,
          paid: 120,
          precision: 2
        }
      ]
    });
  });
});
