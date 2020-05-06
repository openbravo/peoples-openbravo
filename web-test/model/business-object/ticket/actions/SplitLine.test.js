/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

OB = {
  App: {
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    UUID: { generate: jest.fn() }
  },
  UTIL: { HookManager: { registerHook: jest.fn() } }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/deepfreeze-2.0.0');

require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SplitLine');

const basicTicket = deepfreeze({
  lines: [
    { id: '1', qty: 100, product: { id: 'p1' } },
    { id: '2', qty: 100, product: { id: 'p2' } }
  ]
});

describe('Ticket.splitLine action', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('basics', () => {
    it('generates lines with correct quantities', () => {
      const { lines } = OB.App.StateAPI.Ticket.splitLine(basicTicket, {
        lineId: '1',
        quantities: [70, 10, 20]
      });

      expect(lines).toHaveLength(4);

      const p1Quantities = lines
        .filter(l => l.product.id === 'p1')
        .map(l => l.qty);
      expect(p1Quantities).toMatchObject([70, 10, 20]);
    });

    it('new lines have different ids', () => {
      const { lines } = OB.App.StateAPI.Ticket.splitLine(basicTicket, {
        lineId: '1',
        quantities: [70, 10, 20]
      });

      expect(lines).toHaveLength(4);
      expect(OB.App.UUID.generate).toHaveBeenCalledTimes(2);
    });

    it('inserts new lines after the original one', () => {
      const { lines } = OB.App.StateAPI.Ticket.splitLine(basicTicket, {
        lineId: '1',
        quantities: [50, 50]
      });

      expect(lines).toMatchObject([
        { product: { id: 'p1' } },
        { product: { id: 'p1' } },
        { product: { id: 'p2' } }
      ]);
    });
  });

  describe('manual promotions', () => {
    const basicDiscountTicket = deepfreeze({
      ...basicTicket,
      discountsFromUser: {
        manualPromotions: [
          {
            discountType: '7B49D8CC4E084A75B7CB4D85A6A3A578', // fixed
            obdiscAmt: 10,
            linesToApply: ['1']
          }
        ]
      }
    });

    const basicDiscountTicket2 = deepfreeze({
      lines: [{ id: '1', qty: 3, product: { id: 'p1' } }],
      discountsFromUser: {
        manualPromotions: [
          {
            discountType: 'D1D193305A6443B09B299259493B272A', // user defined
            obdiscAmt: 10,
            linesToApply: ['1']
          }
        ]
      }
    });

    const setLineAmtTicket = deepfreeze({
      lines: [{ id: '1', qty: 2, product: { id: 'p1' } }],
      discountsFromUser: {
        manualPromotions: [
          {
            discountType: 'F3B0FB45297844549D9E6B5F03B23A82', // set line amount
            obdiscLineFinalgross: 100,
            linesToApply: ['1']
          }
        ]
      }
    });

    const percentageDiscountTicket = deepfreeze({
      lines: [{ id: '1', qty: 2, product: { id: 'p1' } }],
      discountsFromUser: {
        manualPromotions: [
          {
            discountType: '20E4EC27397344309A2185097392D964', // percentage
            obdiscPercentage: 10,
            linesToApply: ['1']
          }
        ]
      }
    });

    it('proportionally distributes amount discounts', () => {
      const { discountsFromUser } = OB.App.StateAPI.Ticket.splitLine(
        basicDiscountTicket,
        {
          lineId: '1',
          quantities: [70, 10, 20]
        }
      );

      expect(discountsFromUser.manualPromotions).toMatchObject([
        { obdiscAmt: 7 },
        { obdiscAmt: 1 },
        { obdiscAmt: 2 }
      ]);
    });

    it('last line includes pending amount', () => {
      const { discountsFromUser } = OB.App.StateAPI.Ticket.splitLine(
        basicDiscountTicket2,
        {
          lineId: '1',
          quantities: [1, 1, 1]
        }
      );

      expect(discountsFromUser.manualPromotions).toMatchObject([
        { obdiscAmt: 3.33 },
        { obdiscAmt: 3.33 },
        { obdiscAmt: 3.34 }
      ]);
    });

    it('splits amount of set line amount discounts', () => {
      const { discountsFromUser } = OB.App.StateAPI.Ticket.splitLine(
        setLineAmtTicket,
        {
          lineId: '1',
          quantities: [1, 1]
        }
      );

      expect(discountsFromUser.manualPromotions).toMatchObject([
        { obdiscLineFinalgross: 50 },
        { obdiscLineFinalgross: 50 }
      ]);
    });

    it('keeps percentage in percental discounts', () => {
      const { discountsFromUser } = OB.App.StateAPI.Ticket.splitLine(
        percentageDiscountTicket,
        {
          lineId: '1',
          quantities: [1, 1]
        }
      );

      expect(discountsFromUser.manualPromotions).toMatchObject([
        { obdiscPercentage: 10 },
        { obdiscPercentage: 10 }
      ]);
    });
  });
});
