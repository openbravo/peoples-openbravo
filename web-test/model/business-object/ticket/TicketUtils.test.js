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

const productA = {
  id: 'A',
  listPrice: 10
};

const productB = {
  id: 'B',
  listPrice: 20
};

describe('TicketUtils', () => {
  it('create line', () => {
    const ticket = {
      lines: [{ product: productA, qty: 1 }]
    };
    const createdData = OB.App.State.Ticket.Utils.createLine(
      deepfreeze(ticket),
      {
        product: productB,
        qty: 23,
        terminal: {
          organization: 'D270A5AC50874F8BA67A88EE977F8E3B',
          organization$_identifier: 'Vall Blanca Store',
          country: '106',
          region: 'AF310D01B53B461283EB40DB21DCA6B5'
        },
        warehouses: [
          {
            priority: 10,
            warehouseid: 'A154EC30A296479BB078B0AFFD74CA22',
            warehousename: 'Vall Blanca Store Warehouse'
          }
        ]
      }
    );
    const newTicket = createdData.ticket;
    const newLine = createdData.line;
    expect(newLine).toMatchObject({ product: productB, qty: 23 });
    expect(newTicket.lines).toMatchObject([
      { product: productA, qty: 1 },
      { product: productB, qty: 23 }
    ]);
  });

  it('delete line', () => {
    const ticket = {
      lines: [
        { id: '1', product: productA, qty: 1 },
        { id: '2', product: productB, qty: 23 }
      ]
    };
    const newTicket = OB.App.State.Ticket.Utils.deleteLine(
      deepfreeze(ticket),
      '1'
    );
    expect(newTicket.lines).toMatchObject([
      { id: '2', product: productB, qty: 23 }
    ]);
  });

  test.each`
    ticket                    | expected
    ${{ isQuotation: false }} | ${false}
    ${{ isQuotation: true }}  | ${true}
  `("Ticket '$ticket' is a quotation", async ({ ticket, expected }) => {
    expect(OB.App.State.Ticket.Utils.isQuotation(ticket)).toBe(expected);
  });

  test.each`
    ticket                 | expected
    ${{ orderType: 0 }}    | ${false}
    ${{ orderType: 2 }}    | ${true}
    ${{ orderType: 3 }}    | ${true}
    ${{ isLayaway: true }} | ${true}
  `("Ticket '$ticket' is a layaway", async ({ ticket, expected }) => {
    expect(OB.App.State.Ticket.Utils.isLayaway(ticket)).toBe(expected);
  });

  test.each`
    ticket              | expected
    ${{ orderType: 0 }} | ${false}
    ${{ orderType: 1 }} | ${true}
    ${{ orderType: 2 }} | ${false}
  `("Ticket '$ticket' is a return", async ({ ticket, expected }) => {
    expect(OB.App.State.Ticket.Utils.isReturn(ticket)).toBe(expected);
  });

  test.each`
    ticket                                  | payload                                                          | expected
    ${{}}                                   | ${undefined}                                                     | ${true}
    ${{ lines: [] }}                        | ${undefined}                                                     | ${true}
    ${{ lines: [{ qty: 0 }] }}              | ${undefined}                                                     | ${true}
    ${{ lines: [{ qty: 1 }] }}              | ${undefined}                                                     | ${true}
    ${{ lines: [{ qty: -1 }] }}             | ${undefined}                                                     | ${false}
    ${{ lines: [{ qty: 0 }, { qty: 1 }] }}  | ${undefined}                                                     | ${true}
    ${{ lines: [{ qty: 0 }, { qty: -1 }] }} | ${undefined}                                                     | ${true}
    ${{ lines: [{ qty: 1 }, { qty: -1 }] }} | ${undefined}                                                     | ${true}
    ${{ lines: [{ qty: 1 }, { qty: -1 }] }} | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [{ qty: 1 }, { qty: -1 }] }} | ${{ preferences: { salesWithOneLineNegativeAsReturns: true } }}  | ${false}
  `("Ticket '$ticket' is a sale", async ({ ticket, payload, expected }) => {
    expect(OB.App.State.Ticket.Utils.isSale(ticket, payload)).toBe(expected);
  });

  test.each`
    ticket                   | payload                                | expected
    ${{}}                    | ${undefined}                           | ${false}
    ${{}}                    | ${{ terminal: { organization: 'A' } }} | ${false}
    ${{ organization: 'A' }} | ${undefined}                           | ${false}
    ${{ organization: 'A' }} | ${{ terminal: { organization: 'A' } }} | ${false}
    ${{ organization: 'A' }} | ${{ terminal: { organization: 'B' } }} | ${true}
  `(
    "Ticket '$ticket' is cross store",
    async ({ ticket, payload, expected }) => {
      expect(OB.App.State.Ticket.Utils.isCrossStore(ticket, payload)).toBe(
        expected
      );
    }
  );

  test.each`
    ticket                             | expected
    ${{ grossAmount: 0, payment: 0 }}  | ${true}
    ${{ grossAmount: 0, payment: 1 }}  | ${true}
    ${{ grossAmount: 1, payment: 0 }}  | ${false}
    ${{ grossAmount: 1, payment: 1 }}  | ${true}
    ${{ grossAmount: -1, payment: 0 }} | ${false}
    ${{ grossAmount: -1, payment: 1 }} | ${true}
  `("Ticket '$ticket' is fully paid", async ({ ticket, expected }) => {
    expect(OB.App.State.Ticket.Utils.isFullyPaid(ticket)).toBe(expected);
  });

  test.each`
    ticket                                                                                        | expected
    ${{ cancelAndReplaceChangePending: true }}                                                    | ${true}
    ${{ isNegative: true }}                                                                       | ${true}
    ${{ isNegative: false }}                                                                      | ${false}
    ${{ isLayaway: true, total: -100 }}                                                           | ${true}
    ${{ isLayaway: true, total: 100 }}                                                            | ${false}
    ${{ isPaid: true, total: -100 }}                                                              | ${true}
    ${{ isPaid: true, total: 100 }}                                                               | ${false}
    ${{ payments: [{ origAmount: 10 }, { origAmount: 200, isPrePayment: true }], total: 100 }}    | ${true}
    ${{ payments: [{ origAmount: 10 }, { origAmount: 100, isPrePayment: true }], total: 100 }}    | ${false}
    ${{ payments: [{ origAmount: 10 }, { origAmount: 10, isPrePayment: true }], total: 100 }}     | ${false}
    ${{ payments: [{ origAmount: 10 }, { origAmount: 200, isPrePayment: false }], total: 100 }}   | ${false}
    ${{ payments: [{ origAmount: 10 }, { origAmount: 200, isPrePayment: true }], total: -100 }}   | ${true}
    ${{ payments: [{ origAmount: 10 }, { origAmount: -100, isPrePayment: true }], total: -100 }}  | ${true}
    ${{ payments: [{ origAmount: 10 }, { origAmount: -110, isPrePayment: true }], total: -100 }}  | ${false}
    ${{ payments: [{ origAmount: 10 }, { origAmount: 10, isPrePayment: true }], total: -100 }}    | ${true}
    ${{ payments: [{ origAmount: 10 }, { origAmount: 200, isPrePayment: false }], total: -100 }}  | ${true}
    ${{ payments: [{ origAmount: 10, isPrePayment: true }], nettingPayment: 100, total: 100 }}    | ${true}
    ${{ payments: [{ origAmount: 10, isPrePayment: true }], nettingPayment: 90, total: 100 }}     | ${false}
    ${{ payments: [{ origAmount: 10, isPrePayment: true }], nettingPayment: 80, total: 100 }}     | ${false}
    ${{ payments: [{ origAmount: -10, isPrePayment: true }], nettingPayment: -100, total: -100 }} | ${false}
    ${{ payments: [{ origAmount: -10, isPrePayment: true }], nettingPayment: -90, total: -100 }}  | ${true}
    ${{ payments: [{ origAmount: -10, isPrePayment: true }], nettingPayment: -80, total: -100 }}  | ${true}
  `("Ticket '$ticket' is negative", async ({ ticket, expected }) => {
    expect(OB.App.State.Ticket.Utils.isNegative(ticket)).toBe(expected);
  });

  test.each`
    ticket                                                                                                    | expected
    ${{ paymentWithSign: 50, grossAmount: 150.5 }}                                                            | ${100.5}
    ${{ prepaymentChangeMode: true, payments: [{ origAmount: 50 }, { origAmount: 60 }], grossAmount: 150.5 }} | ${40.5}
  `(
    "Pending amount for ticket '$ticket' is $expected",
    async ({ ticket, expected }) => {
      expect(OB.App.State.Ticket.Utils.getPendingAmount(ticket)).toBe(expected);
    }
  );
});
