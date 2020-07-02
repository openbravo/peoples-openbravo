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
const deepfreeze = require('deepfreeze');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddByTotalPromotion');

const basicTicket = deepfreeze({
  lines: [{ id: '1', qty: 100, product: { id: 'p1' } }],
  discountsFromUser: {}
});

const basicDiscount = {
  rule: { id: '1' },
  discountRule: { id: '1' }
};

const multipleDiscount = {
  rule: { id: '2', obdiscAllowmultipleinstan: true },
  discountRule: { id: '2', obdiscAllowmultipleinstan: true }
};

const noMultipleDiscount = {
  rule: { id: '3', obdiscAllowmultipleinstan: false },
  discountRule: { id: '3' }
};

const overrideUserPerc = {
  rule: { id: '4', userAmt: 23, obdiscAllowmultipleinstan: true },
  discountRule: { id: '4', disctTotalpercdisc: 10 }
};
const overrideUserAmt = {
  rule: { id: '5', userAmt: 23, obdiscAllowmultipleinstan: true },
  discountRule: { id: '5', disctTotalamountdisc: 10 }
};

describe('Ticket.addByTotalPromotion action', () => {
  it('add simple manual promotion to list', () => {
    const { discountsFromUser } = OB.App.StateAPI.Ticket.addByTotalPromotion(
      basicTicket,
      { discount: basicDiscount }
    );
    expect(discountsFromUser.bytotalManualPromotions).toHaveLength(1);
    expect(
      discountsFromUser.bytotalManualPromotions.map(discount => discount.id)
    ).toMatchObject(['1']);
  });

  it('allow multiple instance', () => {
    const newTicket = OB.App.StateAPI.Ticket.addByTotalPromotion(basicTicket, {
      discount: multipleDiscount
    });
    const { discountsFromUser } = OB.App.StateAPI.Ticket.addByTotalPromotion(
      newTicket,
      {
        discount: multipleDiscount
      }
    );

    expect(discountsFromUser.bytotalManualPromotions).toHaveLength(2);

    expect(
      discountsFromUser.bytotalManualPromotions.map(discount => discount.id)
    ).toMatchObject(['2', '2']);
  });

  it('do not allow multiple instance, replace', () => {
    const newTicket = OB.App.StateAPI.Ticket.addByTotalPromotion(basicTicket, {
      discount: noMultipleDiscount
    });
    const { discountsFromUser } = OB.App.StateAPI.Ticket.addByTotalPromotion(
      newTicket,
      {
        discount: noMultipleDiscount
      }
    );

    expect(discountsFromUser.bytotalManualPromotions).toHaveLength(1);

    expect(
      discountsFromUser.bytotalManualPromotions.map(discount => discount.id)
    ).toMatchObject(['3']);
  });

  it('override percentage with user amount', () => {
    const { discountsFromUser } = OB.App.StateAPI.Ticket.addByTotalPromotion(
      basicTicket,
      { discount: overrideUserPerc }
    );

    expect(discountsFromUser.bytotalManualPromotions).toHaveLength(1);

    expect(
      discountsFromUser.bytotalManualPromotions[0].disctTotalpercdisc
    ).toBe(23);
  });

  it('override amount with user amount', () => {
    const { discountsFromUser } = OB.App.StateAPI.Ticket.addByTotalPromotion(
      basicTicket,
      { discount: overrideUserAmt }
    );

    expect(discountsFromUser.bytotalManualPromotions).toHaveLength(1);

    expect(
      discountsFromUser.bytotalManualPromotions[0].disctTotalamountdisc
    ).toBe(23);
  });
});
