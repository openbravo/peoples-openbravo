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
  UTIL: { HookManager: { registerHook: jest.fn() } },
  MobileApp: { model: { get: jest.fn(() => jest.fn()) } }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddProduct');

const emptyTicket = deepfreeze({ priceIncludesTax: true, lines: [] });

const productA = deepfreeze({
  id: 'pA',
  uOMstandardPrecision: 2,
  standardPrice: 5,
  listPrice: 5
});

const productB = deepfreeze({
  id: 'pB',
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
});

describe('addProduct', () => {
  it('adds new lines', () => {
    const newTicket = OB.App.StateAPI.Ticket.addProduct(emptyTicket, {
      products: [{ product: productA, qty: 1 }, { product: productB, qty: 2 }]
    });
    expect(newTicket.lines).toMatchObject([
      {
        qty: 1,
        grossPrice: 5,
        priceList: 5,
        priceIncludesTax: true,
        product: { id: 'pA' }
      },
      {
        qty: 2,
        grossPrice: 10,
        priceList: 11,
        priceIncludesTax: true,
        product: { id: 'pB' }
      }
    ]);
  });
});
