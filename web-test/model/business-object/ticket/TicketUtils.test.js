/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
global.OB = {
  App: {
    Class: {},
    TerminalProperty: { get: jest.fn() },
    UUID: { generate: jest.fn() }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');

// set Ticket model utility functions
OB.App.State = { Ticket: { Utils: {} } };
OB.App.StateAPI.Ticket.utilities.forEach(
  util => (OB.App.State.Ticket.Utils[util.functionName] = util.implementation)
);

OB.App.TerminalProperty.get.mockImplementation(property => {
  if (property === 'warehouses') {
    return [
      {
        priority: 10,
        warehouseid: 'A154EC30A296479BB078B0AFFD74CA22',
        warehousename: 'Vall Blanca Store Warehouse'
      }
    ];
  } else if (property === 'terminal') {
    return {
      organization: 'D270A5AC50874F8BA67A88EE977F8E3B',
      organization$_identifier: 'Vall Blanca Store',
      country: '106',
      region: 'AF310D01B53B461283EB40DB21DCA6B5'
    };
  }
  return {};
});

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
    const { newTicket, newLine } = OB.App.State.Ticket.Utils.createLine(
      deepfreeze(ticket),
      productB,
      23
    );
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
    ${{}}                                   | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [] }}                        | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [{ qty: 0 }] }}              | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [{ qty: 1 }] }}              | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [{ qty: -1 }] }}             | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${false}
    ${{ lines: [{ qty: 0 }, { qty: 1 }] }}  | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [{ qty: 0 }, { qty: -1 }] }} | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [{ qty: 1 }, { qty: -1 }] }} | ${{ preferences: { salesWithOneLineNegativeAsReturns: false } }} | ${true}
    ${{ lines: [{ qty: 1 }, { qty: -1 }] }} | ${{ preferences: { salesWithOneLineNegativeAsReturns: true } }}  | ${false}
  `("Ticket '$ticket' is a sale", async ({ ticket, payload, expected }) => {
    expect(OB.App.State.Ticket.Utils.isSale(ticket, payload)).toBe(expected);
  });
});
