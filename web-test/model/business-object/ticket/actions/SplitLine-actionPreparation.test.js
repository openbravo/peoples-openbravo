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
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SplitLine');

const prepareAction = async (payload, ticket = basicTicket) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.splitLine,
    deepfreeze(ticket),
    payload
  );
  return newPayload;
};

const basicTicket = deepfreeze({
  lines: [
    { id: '1', qty: 100, product: { id: 'p1' } },
    { id: '2', qty: 100, product: { id: 'p2' } }
  ]
});

describe('Ticket.splitLine action preparation', () => {
  it('checks line id parameter is present', async () => {
    await expect(prepareAction({ quantities: [50, 50] })).rejects.toThrow(
      'lineId parameter is mandatory'
    );
  });

  it('checks line id is present in ticket', async () => {
    await expect(
      prepareAction({ lineId: '0', quantities: [50, 50] })
    ).rejects.toThrow('lineId 0 not found in ticket');
  });

  it('checks quantities parameter is present', async () => {
    await expect(prepareAction({ lineId: '1' })).rejects.toThrow(
      'quantities parameter is mandatory'
    );
  });

  it('checks quantities parameter is an array', async () => {
    await expect(prepareAction({ lineId: '1', quantities: 1 })).rejects.toThrow(
      'quantities must be an array'
    );
  });

  it('checks quantities is an array of numbers', async () => {
    await expect(
      prepareAction({ lineId: '1', quantities: ['1', 2] })
    ).rejects.toThrow('quantities must be an array of numbers');
  });

  it('checks quantities sum correctly', async () => {
    await expect(
      prepareAction({ lineId: '1', quantities: [50, 40] })
    ).rejects.toThrow('quantities must sum 100 but they are 90');
  });

  it('empty quantities array is correctly handled', async () => {
    await expect(
      prepareAction({ lineId: '1', quantities: [] })
    ).rejects.toThrow('quantities must sum 100 but they are 0');
  });

  it('passes validations with correct params', async () => {
    await expect(
      prepareAction({ lineId: '1', quantities: [50, 30, 20] })
    ).resolves.not.toThrow();
  });
});
