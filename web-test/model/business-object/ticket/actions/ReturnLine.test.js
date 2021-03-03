/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ReturnLine');
const deepfreeze = require('deepfreeze');

describe('returnLine', () => {
  it('should not return lines when empty ticket', () => {
    const ticket = deepfreeze({ lines: [] });
    const payload = deepfreeze({ lineIds: [] });
    const newTicket = OB.App.StateAPI.Ticket.returnLine(ticket, payload);
    expect(newTicket).toStrictEqual({ lines: [] });
  });

  it('should not return lines when empty payload', () => {
    const ticket = deepfreeze({ lines: [{ id: '1', qty: 1 }] });
    const payload = deepfreeze({ lineIds: [] });
    const newTicket = OB.App.StateAPI.Ticket.returnLine(ticket, payload);
    expect(newTicket).toStrictEqual({ lines: [{ id: '1', qty: 1 }] });
  });

  it('should return line when defined in payload', () => {
    const ticket = deepfreeze({
      lines: [{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }]
    });
    const payload = deepfreeze({ lineIds: ['2'] });
    const newTicket = OB.App.StateAPI.Ticket.returnLine(ticket, payload);
    expect(newTicket).toStrictEqual({
      lines: [{ id: '1', qty: 1 }, { id: '2', qty: -2 }, { id: '3', qty: 3 }]
    });
  });

  it('should return negative line when defined in payload', () => {
    const ticket = deepfreeze({
      lines: [{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }]
    });
    const payload = deepfreeze({ lineIds: ['2'] });
    const newTicket = OB.App.StateAPI.Ticket.returnLine(ticket, payload);
    expect(newTicket).toStrictEqual({
      lines: [{ id: '1', qty: -1 }, { id: '2', qty: 2 }, { id: '3', qty: -3 }]
    });
  });

  it('should return lines when defined in payload', () => {
    const ticket = deepfreeze({
      lines: [{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }]
    });
    const payload = deepfreeze({ lineIds: ['1', '3'] });
    const newTicket = OB.App.StateAPI.Ticket.returnLine(ticket, payload);
    expect(newTicket).toStrictEqual({
      lines: [{ id: '1', qty: -1 }, { id: '2', qty: 2 }, { id: '3', qty: -3 }]
    });
  });

  it('should return negative lines when defined in payload', () => {
    const ticket = deepfreeze({
      lines: [{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }]
    });
    const payload = deepfreeze({ lineIds: ['1', '3'] });
    const newTicket = OB.App.StateAPI.Ticket.returnLine(ticket, payload);
    expect(newTicket).toStrictEqual({
      lines: [{ id: '1', qty: 1 }, { id: '2', qty: -2 }, { id: '3', qty: 3 }]
    });
  });
});
