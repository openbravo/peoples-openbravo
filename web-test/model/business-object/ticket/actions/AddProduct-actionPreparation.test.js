/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
/* eslint-disable jest/expect-expect */

OB = {
  App: {
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() }
  },
  UTIL: {
    HookManager: { registerHook: jest.fn() }
  }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
const {
  executeActionPreparations
} = require('../../../../../../org.openbravo.mobile.core/web-test/base/state-utils');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionSilentlyCanceled');

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddProduct');

const emptyTicket = { priceIncludesTax: true, lines: [] };

const scaleProduct = {
  id: 'scaleProduct',
  obposScale: true,
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
};

const prepareAction = async (payload, ticket = emptyTicket) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.addProduct,
    deepfreeze({ Ticket: ticket }),
    deepfreeze(payload)
  );
  return newPayload;
};

describe('addProduct preparation', () => {
  it('does not allow multiple scale products', async () => {
    await expect(
      prepareAction({
        products: [{ product: scaleProduct }, { product: scaleProduct }]
      })
    ).rejects.toThrow('Cannot handle more than one scale product');
  });
});
