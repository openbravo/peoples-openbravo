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

describe('Ticket Utils processTicket function', () => {
  it('should update needed properties', () => {
    const ticket = deepfreeze({ payments: [], approvals: [] });
    const newTicket = OB.App.State.Ticket.Utils.processTicket(ticket, {
      terminal: { id: '0' }
    });
    expect(newTicket).toMatchObject({
      hasbeenpaid: 'Y',
      posTerminal: '0',
      payments: [],
      approvals: []
    });
  });
});
