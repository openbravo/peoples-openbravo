/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/RemovePromotion');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

const basicTicket = deepfreeze({
  lines: [
    {
      id: '1',
      qty: 100,
      product: { id: 'p1' },
      baseGrossUnitPrice: 5,
      promotions: [
        { ruleId: '1', discountinstance: '1', amt: 1 },
        { ruleId: '2', discountinstance: '2', amt: 1 }
      ]
    }
  ],
  discountsFromUser: {}
});

const rule2 = { id: '2', discountinstance: '2' };
const rule3 = { id: '3', discountinstance: '2' };

describe('Ticket.removePromotion action', () => {
  it('remove simple promotion', () => {
    const { lines } = OB.App.StateAPI.Ticket.removePromotion(basicTicket, {
      rule: rule2,
      lineId: '1'
    });
    expect(lines[0].promotions).toHaveLength(1);
  });

  it('call remove for non existing line', () => {
    let error;
    try {
      OB.App.StateAPI.Ticket.removePromotion(basicTicket, {
        rule: rule2,
        lineId: '3'
      });
    } catch (e) {
      error = e;
    }
    expect(error).toBeDefined();
  });
  it('call remove for non existing promotion', () => {
    const { lines } = OB.App.StateAPI.Ticket.removePromotion(basicTicket, {
      rule: rule3,
      lineId: '1'
    });

    expect(lines[0].promotions).toHaveLength(2);
  });
});
