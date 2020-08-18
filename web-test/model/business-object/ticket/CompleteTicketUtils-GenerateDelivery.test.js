/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('Ticket Utils generateDelivery function', () => {
  it('should generate delivery when completing a ticket with item product', () => {
    const ticket = deepfreeze({
      completeTicket: true,
      grossAmount: 100,
      payment: 100,
      lines: [{ qty: 10, product: { productType: 'I' } }]
    });
    const newTicket = OB.App.State.Ticket.Utils.generateDelivery(ticket);
    expect(newTicket).toMatchObject({
      generateShipment: true,
      deliver: true,
      lines: [{ qty: 10, obposQtytodeliver: 10 }]
    });
  });
});
