/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/CompleteMultiTicketUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/CompleteMultiCreditTicket');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Complete Multi ticket action', () => {
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
      preferences: {},
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
        payment: 0,
        gross: 300,
        total: 300,
        payments: [],
        changePayments: []
      },
      multiTicketList: [
        {
          businessPartner: { id: 'BP', creditUsed: 0 },
          grossAmount: 100,
          netAmount: 90,
          payment: 0,
          paymentWithSign: 0,
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
          businessPartner: { id: 'BP', creditUsed: 0 },
          grossAmount: 200,
          netAmount: 180,
          payment: 0,
          paymentWithSign: 0,
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

    const newState = OB.App.StateAPI.Global.completeMultiCreditTicket(
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
          modelName: 'Order',
          service: 'org.openbravo.retail.posterminal.OrderLoader',
          type: 'backend',
          messageObj: {
            data: [
              {
                payOnCredit: true,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 1,
                documentNo: 'O/00001',
                payment: 0,
                ...ticket
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
          modelName: 'Order',
          service: 'org.openbravo.retail.posterminal.OrderLoader',
          type: 'backend',
          messageObj: {
            data: [
              {
                payOnCredit: true,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 2,
                documentNo: 'O/00002',
                payment: 0,
                ...ticket
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
