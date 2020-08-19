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

global.OB = {
  App: { StateBackwardCompatibility: { setProperties: jest.fn() }, Class: {} },
  UTIL: { HookManager: { registerHook: jest.fn() } }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SetLinePrice');

describe('Ticket.setPrice action', () => {
  const basicTicket = deepfreeze({
    priceIncludesTax: true,
    lines: [
      {
        id: '1',
        qty: 1,
        baseGrossUnitPrice: 10,
        grossListPrice: 10,
        product: { listPrice: 10 }
      },
      {
        id: '2',
        qty: 1,
        baseGrossUnitPrice: 20,
        grossListPrice: 20,
        product: { listPrice: 20 }
      },
      {
        id: '3',
        qty: 1,
        baseGrossUnitPrice: 30,
        grossListPrice: 30,
        product: { listPrice: 30 }
      }
    ]
  });

  it('sets price to a single line', () => {
    const newTicket = OB.App.StateAPI.Ticket.setLinePrice(basicTicket, {
      lineIds: ['1'],
      price: 5
    });

    const modifiedLine = newTicket.lines.filter(l => l.id === '1')[0];
    expect(modifiedLine).toMatchObject({
      id: '1',
      qty: 1,
      baseGrossUnitPrice: 5,
      grossListPrice: 10,
      discountPercentage: 50,
      product: { listPrice: 10 }
    });
  });

  it('sets price to multiple lines', () => {
    const lineIds = ['1', '2'];
    const newTicket = OB.App.StateAPI.Ticket.setLinePrice(basicTicket, {
      lineIds,
      price: 5
    });

    const modifiedLine = newTicket.lines.filter(l => lineIds.includes(l.id));
    expect(modifiedLine).toMatchObject([
      {
        id: '1',
        qty: 1,
        baseGrossUnitPrice: 5,
        grossListPrice: 10,
        discountPercentage: 50,
        product: { listPrice: 10 }
      },
      {
        id: '2',
        qty: 1,
        baseGrossUnitPrice: 5,
        grossListPrice: 20,
        discountPercentage: 75,
        product: { listPrice: 20 }
      }
    ]);
  });

  it('keeps other lines untouched', () => {
    const newTicket = OB.App.StateAPI.Ticket.setLinePrice(basicTicket, {
      lineIds: ['1'],
      price: 5
    });

    const unmodifiedLines = newTicket.lines.filter(l => l.id !== '1');
    expect(unmodifiedLines).toMatchObject(
      basicTicket.lines.filter(l => l.id !== '1')
    );
  });

  it('can define a reson', () => {
    const newTicket = OB.App.StateAPI.Ticket.setLinePrice(basicTicket, {
      lineIds: ['1'],
      price: 5,
      reason: 'reasonId'
    });

    expect(newTicket.lines[0]).toMatchObject({
      oBPOSPriceModificationReason: 'reasonId'
    });
  });

  it('resets reson', () => {
    const ticket1 = OB.App.StateAPI.Ticket.setLinePrice(basicTicket, {
      lineIds: ['1'],
      price: 5,
      reason: 'reasonId'
    });

    const newTicket = OB.App.StateAPI.Ticket.setLinePrice(deepfreeze(ticket1), {
      lineIds: ['1'],
      price: 5
    });

    expect(
      newTicket.lines[0].hasOwnProperty('oBPOSPriceModificationReason')
    ).toBe(false);
  });

  it('does not mutate lines', () => {
    const newTicket = OB.App.StateAPI.Ticket.setLinePrice(basicTicket, {
      lineIds: ['1'],
      price: 5
    });

    expect(newTicket.lines[0]).not.toBe(basicTicket.lines[0]);
  });

  it('sets prices with not included taxes', () => {
    const newTicket = OB.App.StateAPI.Ticket.setLinePrice(
      {
        ...basicTicket,
        priceIncludesTax: false,
        lines: basicTicket.lines.map(line => {
          const newLine = { ...line };
          newLine.baseNetUnitPrice = newLine.baseGrossUnitPrice;
          newLine.netListPrice = newLine.grossListPrice;
          delete newLine.baseGrossUnitPrice;
          delete newLine.grossListPrice;
          return newLine;
        })
      },
      {
        lineIds: ['1'],
        price: 5
      }
    );

    expect(newTicket.lines[0]).toMatchObject({
      id: '1',
      baseNetUnitPrice: 5,
      discountPercentage: 50
    });
  });

  describe('Payment delivery', () => {
    const pdTicket = deepfreeze({
      priceIncludesTax: true,
      deliveryPaymentMode: 'PD',
      lines: [
        {
          id: '1',
          qty: 1,
          baseGrossUnitPrice: 10,
          priceList: 10,
          baseAmountToPayInDeliver: 5,
          product: { listPrice: 10, obrdmIsdeliveryservice: true }
        },
        {
          id: '2',
          qty: 1,
          baseGrossUnitPrice: 30,
          priceList: 30,
          product: { listPrice: 30 }
        }
      ]
    });

    it('delivery services reset price to 0 setting it in amt to pay in delivery', () => {
      const newTicket = OB.App.StateAPI.Ticket.setLinePrice(pdTicket, {
        lineIds: ['1'],
        price: 500
      });

      expect(newTicket.lines[0]).toMatchObject({
        baseGrossUnitPrice: 0,
        obrdmAmttopayindelivery: 500
      });
    });

    it('standard products do not get affected', () => {
      const newTicket = OB.App.StateAPI.Ticket.setLinePrice(pdTicket, {
        lineIds: ['2'],
        price: 500
      });

      expect(newTicket.lines[1]).toMatchObject({ baseGrossUnitPrice: 500 });
    });
  });
});
