/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ReturnTicket');
const deepfreeze = require('deepfreeze');

describe('returnTicket', () => {
  it('should not return lines when empty ticket', () => {
    const ticket = deepfreeze({ lines: [] });
    const newTicket = OB.App.StateAPI.Ticket.returnTicket(ticket);
    expect(newTicket).toStrictEqual({ lines: [] });
  });

  it('should return lines', () => {
    const ticket = deepfreeze({
      lines: [{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }]
    });
    const newTicket = OB.App.StateAPI.Ticket.returnTicket(ticket);
    expect(newTicket).toStrictEqual({
      lines: [{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }]
    });
  });

  it('should return negative lines', () => {
    const ticket = deepfreeze({
      lines: [{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }]
    });
    const newTicket = OB.App.StateAPI.Ticket.returnTicket(ticket);
    expect(newTicket).toStrictEqual({
      lines: [{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }]
    });
  });
});
