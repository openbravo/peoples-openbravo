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
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('returnTicket', () => {
  test.each`
    lines                                         | result
    ${[]}                                         | ${{ orderType: 1, documentType: 'R', lines: [] }}
    ${[{ id: '1', qty: 1 }]}                      | ${{ orderType: 1, documentType: 'R', lines: [{ id: '1', qty: -1 }] }}
    ${[{ id: '1', qty: 1 }, { id: '2', qty: 2 }]} | ${{ orderType: 1, documentType: 'R', lines: [{ id: '1', qty: -1 }, { id: '2', qty: -2 }] }}
  `('should return ticket lines', ({ lines, result }) => {
    const ticket = deepfreeze({ lines });
    const payload = deepfreeze({
      terminal: {
        terminalType: { documentType: 'O', documentTypeForReturns: 'R' }
      }
    });
    const newTicket = OB.App.StateAPI.Ticket.returnTicket(ticket, payload);
    expect(newTicket).toStrictEqual(result);
  });
});
