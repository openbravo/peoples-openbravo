/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/CompleteCreditTicket');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Complete Credit ticket action', () => {
  it('should complete ticket with credit, print it and synchronize it', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({
      businessPartner: { id: 'BP', creditUsed: 0 },
      grossAmount: 100,
      netAmount: 80,
      payment: 0,
      paymentWithSign: 0,
      lines: [
        {
          grossUnitAmount: 100,
          netUnitAmount: 80,
          qty: 10,
          product: { productType: 'I' },
          taxes: {}
        }
      ],
      payments: [],
      approvals: []
    });
    const documentSequence = deepfreeze({
      lastassignednum: { sequencePrefix: 'O', sequenceNumber: 0 }
    });
    const cashup = deepfreeze({
      grossSales: 0,
      netSales: 0,
      grossReturns: 0,
      netReturns: 0,
      cashPaymentMethodInfo: []
    });
    const messages = deepfreeze([]);
    const state = deepfreeze({
      TicketList: ticketList,
      Ticket: ticket,
      DocumentSequence: documentSequence,
      Cashup: cashup,
      Messages: messages
    });
    const payload = deepfreeze({
      completeTicket: true,
      terminal: {
        id: '0',
        documentnoPadding: 5,
        terminalType: { documentType: 'Sale' }
      },
      businessPartner: 'BP',
      documentNumberSeparator: '/',
      pricelist: {},
      context: { user: {} },
      preferences: {},
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.completeCreditTicket(
      state,
      payload
    );

    expect(newState).toMatchObject({
      TicketList: [],
      Ticket: {
        documentNo: '',
        businessPartner: 'BP',
        grossAmount: 0,
        netAmount: 0,
        payment: 0,
        lines: [],
        payments: [],
        approvals: []
      },
      DocumentSequence: {
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 1 }
      },
      Cashup: {
        grossSales: 100,
        netSales: 80,
        grossReturns: 0,
        netReturns: 0
      },
      Messages: [
        {
          modelName: 'Order',
          service: 'org.openbravo.retail.posterminal.OrderLoader',
          type: 'backend',
          messageObj: {
            data: [
              {
                ...ticket,
                payOnCredit: true,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 1,
                documentNo: 'O/00001',
                businessPartner: {
                  creditUsed: 100,
                  id: 'BP'
                }
              }
            ]
          }
        },
        {
          modelName: '',
          service: '',
          type: 'printTicket',
          messageObj: {
            data: {}
          }
        },
        {
          modelName: '',
          service: '',
          type: 'printWelcome',
          messageObj: { data: {} }
        }
      ]
    });
  });
});