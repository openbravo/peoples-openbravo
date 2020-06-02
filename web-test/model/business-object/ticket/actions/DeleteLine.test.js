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

const basicTicket = deepfreeze({
  lines: [
    {
      id: '1',
      qty: 1,
      product: { id: 'p1' }
    },
    {
      id: '2',
      qty: 2,
      product: { id: 'p2' }
    },
    {
      id: '3',
      qty: 3,
      product: { id: 'p3' }
    }
  ]
});

describe('Ticket.deleteLine action', () => {
  it('deletes lines', () => {
    const newTicket = OB.App.StateAPI.Ticket.deleteLine(basicTicket, {
      lineIds: ['1', '3']
    });

    expect(newTicket.lines).toMatchObject([{ id: '2', product: { id: 'p2' } }]);
  });

  it('does not track deleted lines if save removal not configured', () => {
    const newTicket = OB.App.StateAPI.Ticket.deleteLine(basicTicket, {
      lineIds: ['2']
    });

    expect(newTicket).not.toHaveProperty('deletedLines');
  });

  it('tracks deleted lines if save removal configured', () => {
    const newTicket = OB.App.StateAPI.Ticket.deleteLine(basicTicket, {
      lineIds: ['2'],
      config: { saveRemoval: true }
    });

    expect(newTicket.deletedLines).toBeInstanceOf(Array);
    expect(newTicket.deletedLines).toHaveLength(1);
    expect(newTicket.deletedLines).toMatchObject([
      {
        obposQtyDeleted: 2,
        qty: 0,
        grossAmount: 0,
        netAmount: 0,
        product: { id: 'p2' }
      }
    ]);
  });
});
