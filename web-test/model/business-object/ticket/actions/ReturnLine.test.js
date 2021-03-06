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
  test.each`
    lines                                                                 | lineIds       | result
    ${[]}                                                                 | ${[]}         | ${{ lines: [] }}
    ${[{ id: '1', qty: 1 }]}                                              | ${[]}         | ${{ lines: [{ id: '1', qty: 1 }] }}
    ${[{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }]}    | ${['2']}      | ${{ lines: [{ id: '1', qty: 1 }, { id: '2', qty: -2 }, { id: '3', qty: 3 }] }}
    ${[{ id: '1', qty: -1 }, { id: '2', qty: 2 }, { id: '3', qty: -3 }]}  | ${['2']}      | ${{ lines: [{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }] }}
    ${[{ id: '1', qty: 1 }, { id: '2', qty: -2 }, { id: '3', qty: 3 }]}   | ${['2']}      | ${{ lines: [{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }] }}
    ${[{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }]} | ${['2']}      | ${{ lines: [{ id: '1', qty: -1 }, { id: '2', qty: 2 }, { id: '3', qty: -3 }] }}
    ${[{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }]}    | ${['1', '3']} | ${{ lines: [{ id: '1', qty: -1 }, { id: '2', qty: 2 }, { id: '3', qty: -3 }] }}
    ${[{ id: '1', qty: 1 }, { id: '2', qty: -2 }, { id: '3', qty: 3 }]}   | ${['1', '3']} | ${{ lines: [{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }] }}
    ${[{ id: '1', qty: -1 }, { id: '2', qty: 2 }, { id: '3', qty: -3 }]}  | ${['1', '3']} | ${{ lines: [{ id: '1', qty: 1 }, { id: '2', qty: 2 }, { id: '3', qty: 3 }] }}
    ${[{ id: '1', qty: -1 }, { id: '2', qty: -2 }, { id: '3', qty: -3 }]} | ${['1', '3']} | ${{ lines: [{ id: '1', qty: 1 }, { id: '2', qty: -2 }, { id: '3', qty: 3 }] }}
  `('should return defined lines', ({ lines, lineIds, result }) => {
    const ticket = deepfreeze({ lines });
    const payload = deepfreeze({ lineIds, preferences: {} });
    const newTicket = OB.App.StateAPI.Ticket.returnLine(ticket, payload);
    expect(newTicket).toStrictEqual(result);
  });
});
