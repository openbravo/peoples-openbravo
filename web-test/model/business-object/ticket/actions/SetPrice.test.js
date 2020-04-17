/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

/**
 * @fileoverview performs unit tests on setPrice action
 * @see SetQuantity-actionPreparation.test for action preparation
 **/

OB = {
  App: { StateBackwardCompatibility: { setProperties: jest.fn() }, Class: {} },
  UTIL: { HookManager: { registerHook: jest.fn() } }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SetPrice');

describe('Ticket.setPrice action', () => {
  const basicTicket = {
    lines: [
      { id: '1', qty: 1, price: 10, priceList: 10, product: { listPrice: 10 } },
      { id: '2', qty: 1, price: 20, priceList: 20, product: { listPrice: 20 } },
      { id: '3', qty: 1, price: 30, priceList: 30, product: { listPrice: 30 } }
    ]
  };

  it('sets price to a single line', () => {
    const newTicket = OB.App.StateAPI.Ticket.setPrice(basicTicket, {
      lineIds: ['1'],
      price: 5
    });

    const modifiedLine = newTicket.lines.filter(l => l.id === '1')[0];
    expect(modifiedLine).toMatchObject({
      id: '1',
      qty: 1,
      price: 5,
      priceList: 10,
      product: { listPrice: 10 }
    });
  });

  it('sets price to multiple lines', () => {
    const lineIds = ['1', '2'];
    const newTicket = OB.App.StateAPI.Ticket.setPrice(basicTicket, {
      lineIds,
      price: 5
    });

    const modifiedLine = newTicket.lines.filter(l => lineIds.includes(l.id));
    expect(modifiedLine).toMatchObject([
      { id: '1', qty: 1, price: 5, priceList: 10, product: { listPrice: 10 } },
      { id: '2', qty: 1, price: 5, priceList: 20, product: { listPrice: 20 } }
    ]);
  });

  it('keeps other lines untouched', () => {
    const newTicket = OB.App.StateAPI.Ticket.setPrice(basicTicket, {
      lineIds: ['1'],
      price: 5
    });

    const unmodifiedLines = newTicket.lines.filter(l => l.id !== '1');
    expect(unmodifiedLines).toMatchObject(
      basicTicket.lines.filter(l => l.id !== '1')
    );
  });

  it('does not mutate lines', () => {
    const newTicket = OB.App.StateAPI.Ticket.setPrice(basicTicket, {
      lineIds: ['1'],
      price: 5
    });

    expect(newTicket.lines[0]).not.toBe(basicTicket.lines[0]);
  });
});
