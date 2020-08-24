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
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/DeleteLine');

let Ticket = {
  empty: {
    lines: [],
    businessPartner: { id: '1' },
    orderType: 0
  }
};

Ticket = deepfreeze({
  basic: {
    ...Ticket.empty,
    lines: [
      {
        id: '1',
        qty: 1,
        product: { id: 'p1' }
      },
      {
        id: '2',
        qty: 2,
        grossAmount: 20,
        product: { id: 'p2' },
        taxes: { t1: { net: 2, amount: 20, rate: 10 } },
        promotions: ['pr1']
      },
      {
        id: '3',
        qty: 3,
        product: { id: 'p3' }
      }
    ]
  },

  services: {
    ...Ticket.empty,
    hasServices: true,
    lines: [
      { id: 'l1', product: { id: 'p1' } },
      {
        id: 's1',
        product: { id: 'sp1' },
        relatedLines: [{ orderlineId: 'l1' }]
      },
      { id: 'l2', product: { id: 'p2' } },
      {
        id: 's2',
        product: { id: 'sp2' },
        relatedLines: [{ orderlineId: 'l2' }]
      },
      {
        id: 's2.1',
        product: { id: 'sp3' },
        relatedLines: [{ orderlineId: 's2' }]
      },
      {
        id: 's2.1.1',
        product: { id: 'sp4' },
        relatedLines: [{ orderlineId: 's2.1' }]
      }
    ]
  }
});

describe('Ticket.deleteLine action', () => {
  describe('basics', () => {
    it('deletes lines', () => {
      const newTicket = OB.App.StateAPI.Ticket.deleteLine(Ticket.basic, {
        lineIds: ['1', '3']
      });

      expect(newTicket.lines).toMatchObject([
        { id: '2', product: { id: 'p2' } }
      ]);
    });
  });

  describe('save removal', () => {
    it('does not track deleted lines if save removal not configured', () => {
      const newTicket = OB.App.StateAPI.Ticket.deleteLine(Ticket.basic, {
        lineIds: ['2']
      });

      expect(newTicket).not.toHaveProperty('deletedLines');
    });

    it('tracks deleted lines if save removal configured', () => {
      const newTicket = OB.App.StateAPI.Ticket.deleteLine(Ticket.basic, {
        lineIds: ['2'],
        config: { saveRemoval: true }
      });

      expect(newTicket.deletedLines).toBeInstanceOf(Array);
      expect(newTicket.deletedLines).toHaveLength(1);
      expect(newTicket.deletedLines).toMatchObject([
        {
          obposQtyDeleted: 2,
          qty: 0,
          netUnitPrice: 0,
          grossUnitPrice: 0,
          netUnitAmount: 0,
          grossUnitAmount: 0,
          product: { id: 'p2' },
          taxes: { t1: { net: 0, amount: 0, rate: 10 } },
          promotions: []
        }
      ]);
    });

    it('tracks multiple deletions at once', () => {
      const newTicket = OB.App.StateAPI.Ticket.deleteLine(Ticket.basic, {
        lineIds: ['1', '2'],
        config: { saveRemoval: true }
      });

      expect(newTicket.deletedLines).toHaveLength(2);
    });

    it('tracks multiple consecutive deletions', () => {
      let newTicket = OB.App.StateAPI.Ticket.deleteLine(Ticket.basic, {
        lineIds: ['1'],
        config: { saveRemoval: true }
      });

      newTicket = OB.App.StateAPI.Ticket.deleteLine(newTicket, {
        lineIds: ['2'],
        config: { saveRemoval: true }
      });

      expect(newTicket.deletedLines).toHaveLength(2);
    });
  });

  function deletedLineIds(oldTicket, newTicket) {
    return global.lodash.difference(
      oldTicket.lines.map(l => l.id),
      newTicket.lines.map(l => l.id)
    );
  }

  describe('related services lines', () => {
    it('deletes 1 level', () => {
      const newTicket = OB.App.StateAPI.Ticket.deleteLine(Ticket.services, {
        lineIds: ['l1']
      });

      expect(deletedLineIds(Ticket.services, newTicket)).toEqual(
        expect.arrayContaining(['l1', 's1'])
      );
    });

    it('deletes multi level', () => {
      const newTicket = OB.App.StateAPI.Ticket.deleteLine(Ticket.services, {
        lineIds: ['l2']
      });

      expect(deletedLineIds(Ticket.services, newTicket)).toEqual(
        expect.arrayContaining(['l2', 's2', 's2.1', 's2.1.1'])
      );
    });
  });
});
