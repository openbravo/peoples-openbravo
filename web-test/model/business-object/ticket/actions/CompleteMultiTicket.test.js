/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/CompleteMultiTicketUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/AddPaymentUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/CompleteMultiTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/CompleteMultiTicket');

require('../../global/SetupGlobalUtils');

const deepfreeze = require('deepfreeze');

describe('Complete Multi ticket action', () => {
  beforeAll(() => {
    OB = OB || {};
    OB.UTIL = OB.UTIL || {};
    OB.UTIL.getDefaultCashPaymentMethod = jest.fn().mockReturnValue({});
  });
  it('should complete nulti ticket, print it and synchronize it', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({});
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
    const payload = {
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
      preferences: {
        autoPrintReceipts: true
      },
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      },
      payments: [
        {
          mulrate: 1,
          payment: { searchKey: 'OBPOS_payment.cash' }
        }
      ],
      multiTickets: {
        payment: 300,
        gross: 300,
        total: 300,
        payments: [
          {
            amount: 300,
            origAmount: 300,
            kind: 'OBPOS_payment.cash'
          }
        ],
        changePayments: []
      },
      multiTicketList: [
        {
          grossAmount: 100,
          netAmount: 90,
          payment: 0,
          lines: [
            {
              grossUnitAmount: 100,
              netUnitAmount: 90,
              qty: 1,
              product: { productType: 'I' },
              taxes: {}
            }
          ],
          payments: [],
          approvals: []
        },
        {
          grossAmount: 200,
          netAmount: 180,
          payment: 0,
          lines: [
            {
              grossUnitAmount: 100,
              netUnitAmount: 90,
              qty: 2,
              product: { productType: 'I' },
              taxes: {}
            }
          ],
          payments: [],
          approvals: []
        }
      ]
    };

    const newState = OB.App.StateAPI.Global.completeMultiTicket(state, payload);

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
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 2 }
      },
      Cashup: {
        grossSales: 200,
        netSales: 180,
        grossReturns: 0,
        netReturns: 0
      },
      Messages: [
        {
          modelName: 'OBPOS_Order',
          service: 'org.openbravo.retail.posterminal.OrderLoader',
          type: 'backend',
          messageObj: {
            data: [
              {
                completeTicket: true,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 1,
                documentNo: 'O/00001',
                payment: 100,
                ...ticket
              }
            ]
          }
        },
        {
          modelName: 'OBMOBC_PrintTicket',
          service: '',
          type: 'printTicket',
          messageObj: {
            data: {}
          }
        },
        {
          modelName: 'OBPOS_Order',
          service: 'org.openbravo.retail.posterminal.OrderLoader',
          type: 'backend',
          messageObj: {
            data: [
              {
                completeTicket: true,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 2,
                documentNo: 'O/00002',
                payment: 200,
                ...ticket
              }
            ]
          }
        },
        {
          modelName: 'OBMOBC_PrintTicket',
          service: '',
          type: 'printTicket',
          messageObj: {
            data: {}
          }
        },
        {
          modelName: 'OBMOBC_PrintWelcome',
          service: '',
          type: 'printWelcome',
          messageObj: { data: {} }
        }
      ]
    });
  });
});
