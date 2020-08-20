/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupTicket');
require('./SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('Ticket Utils cleanTicket function', () => {
  it('should remove needed properties', () => {
    const ticket = deepfreeze({
      lines: [{ product: { id: '0', imgId: 'X', img: 'XXX' } }]
    });
    const newTicket = OB.App.State.Ticket.Utils.cleanTicket(ticket);
    expect(newTicket).toEqual({
      lines: [{ product: { id: '0' } }]
    });
  });

  it('should remove needed properties if saveToReceipt is false', () => {
    const ticket = deepfreeze({
      lines: [
        { product: { id: '0', imgId: 'X', img: 'XXX', saveToReceipt: false } }
      ]
    });
    const newTicket = OB.App.State.Ticket.Utils.cleanTicket(ticket);
    expect(newTicket).toEqual({
      lines: [{ product: { id: '0', saveToReceipt: false } }]
    });
  });

  it('should not remove needed properties if saveToReceipt is true', () => {
    const ticket = deepfreeze({
      lines: [
        { product: { id: '0', imgId: 'X', img: 'XXX', saveToReceipt: true } }
      ]
    });
    const newTicket = OB.App.State.Ticket.Utils.cleanTicket(ticket);
    expect(newTicket).toEqual({
      lines: [
        { product: { id: '0', imgId: 'X', img: 'XXX', saveToReceipt: true } }
      ]
    });
  });
});
