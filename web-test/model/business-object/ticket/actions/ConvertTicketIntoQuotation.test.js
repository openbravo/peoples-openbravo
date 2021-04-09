/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../ticket/SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ConvertTicketIntoQuotation');
require('../../ticket/SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('Convert Ticket Into Quotation', () => {
  it('should convert an existing Ticket into a Sales Quotation', () => {
    const ticket = deepfreeze({
      isQuotation: false,
      grossAmount: 100,
      netAmount: 80,
      payment: 0,
      lines: [
        {
          grossUnitAmount: 100,
          netUnitAmount: 80,
          qty: 10,
          product: { productType: 'I' },
          taxes: {}
        }
      ]
    });
    const payload = deepfreeze({
      terminal: {
        terminalType: {
          documentTypeForQuotations: 'Quotation'
        }
      }
    });

    const newTicket = OB.App.StateAPI.Ticket.convertTicketIntoQuotation(
      ticket,
      payload
    );

    expect(newTicket).toMatchObject({
      isQuotation: true,
      fullInvoice: false,
      generateInvoice: false,
      orderType: 0,
      documentType: 'Quotation',
      grossAmount: 100,
      netAmount: 80,
      payment: 0,
      lines: [
        {
          grossUnitAmount: 100,
          netUnitAmount: 80,
          qty: 10,
          product: { productType: 'I' },
          taxes: {}
        }
      ]
    });
  });
});
