/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ConvertTicketIntoQuotation');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

OB.App = {
  ...OB.App,
  TerminalProperty: {
    get: () => {
      return {
        terminalType: {
          documentTypeForQuotations: 'Quotation'
        }
      };
    }
  }
};

describe('Convert Ticket Into Quotation', () => {
  it('should convert an existing Ticket into a Sales Quotation', () => {
    const ticketList = deepfreeze([]);
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
    const state = deepfreeze({
      TicketList: ticketList,
      Ticket: ticket
    });
    const payload = deepfreeze({});

    const newState = OB.App.StateAPI.Global.convertTicketIntoQuotation(
      state,
      payload
    );

    expect(newState).toMatchObject({
      TicketList: [],
      Ticket: {
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
      }
    });
  });
});
