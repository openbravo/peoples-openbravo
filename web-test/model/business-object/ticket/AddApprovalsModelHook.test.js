/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
global.OB = {
  App: {
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() }
  },
  UTIL: {
    HookManager: { registerHook: jest.fn() }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.21');

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/AddApprovalsModelHook');

describe('AddApprovalsModelHook', () => {
  const basicTicket = {
    lines: [
      { id: '1', qty: 1, price: 10, priceList: 10, product: { listPrice: 10 } },
      { id: '2', qty: 1, price: 20, priceList: 20, product: { listPrice: 20 } },
      { id: '3', qty: 1, price: 30, priceList: 30, product: { listPrice: 30 } }
    ]
  };

  const hookFn = OB.App.StateAPI.Ticket.modelHooks[0].hook;
  it('payload wihtout approavals does not change ticket', () => {
    const newTicket = hookFn(basicTicket, {});
    expect(newTicket).toBe(basicTicket);
  });

  it('payload with approvals creates aprrovals in ticket without any', () => {
    const newTicket = hookFn(basicTicket, { approvals: [{ supervisor: '1' }] });
    expect(newTicket.approvals).toHaveLength(1);
    expect(newTicket.approvals).toMatchObject([{ supervisor: '1' }]);
  });

  it('payload with approvals adds aprrovals to ticket', () => {
    const newTicket = hookFn(
      { ...basicTicket, approvals: [{ supervisor: '0' }] },
      { approvals: [{ supervisor: '1' }] }
    );
    expect(newTicket.approvals).toHaveLength(2);
    expect(newTicket.approvals).toMatchObject([
      { supervisor: '0' },
      { supervisor: '1' }
    ]);
  });
});
