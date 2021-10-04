/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupTicket');
require('./SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('Ticket Utils updateTicketType function', () => {
  it('should set sale type for positive ticket', () => {
    const ticket = deepfreeze({ organization: 'A', lines: [{ qty: 1 }] });
    const newTicket = OB.App.State.Ticket.Utils.updateTicketType(ticket, {
      terminal: {
        organization: 'A',
        terminalType: { documentType: 'Sale', documentTypeForReturns: 'Return' }
      }
    });
    expect(newTicket).toEqual({
      organization: 'A',
      orderType: 0,
      documentType: 'Sale',
      lines: [{ qty: 1 }]
    });
  });

  it('should set return type for negative ticket', () => {
    const ticket = deepfreeze({ organization: 'A', lines: [{ qty: -1 }] });
    const newTicket = OB.App.State.Ticket.Utils.updateTicketType(ticket, {
      terminal: {
        organization: 'A',
        terminalType: { documentType: 'Sale', documentTypeForReturns: 'Return' }
      }
    });
    expect(newTicket).toEqual({
      organization: 'A',
      orderType: 1,
      documentType: 'Return',
      lines: [{ qty: -1 }]
    });
  });

  it('should change order type for cross store ticket', () => {
    const ticket = deepfreeze({ organization: 'A', lines: [{ qty: -1 }] });
    const newTicket = OB.App.State.Ticket.Utils.updateTicketType(ticket, {
      terminal: {
        organization: 'B',
        terminalType: { documentType: 'Sale', documentTypeForReturns: 'Return' }
      }
    });
    expect(newTicket).toEqual({
      orderType: 1,
      organization: 'A',
      lines: [{ qty: -1 }]
    });
  });
});
