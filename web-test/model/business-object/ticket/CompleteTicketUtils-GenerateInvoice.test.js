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

describe('Ticket Utils generateInvoice function', () => {
  it('should generate invoice when completing an immediate ticket with generateInvoice flag', () => {
    const ticket = deepfreeze({
      id: 'A',
      documentNo: 'O1',
      invoiceTerms: 'I',
      generateInvoice: true,
      grossAmount: 100,
      netAmount: 80,
      lines: [{ id: 'B', qty: 10, product: {} }]
    });
    const newTicket = OB.App.State.Ticket.Utils.generateInvoice(ticket);
    expect(newTicket).toMatchObject({
      calculatedInvoice: {
        isInvoice: true,
        orderId: 'A',
        orderDocumentNo: 'O1',
        documentNo: undefined,
        obposSequencename: undefined,
        obposSequencenumber: undefined,
        invoiceTerms: 'I',
        generateInvoice: true,
        grossAmount: 100,
        netAmount: 80,
        lines: [{ orderLineId: 'B', qty: 10, product: {} }]
      }
    });
  });
});
