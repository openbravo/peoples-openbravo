/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/CancelTicket');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Cancel ticket action', () => {
  it('should Cancel Ticket, print it and synchronize it', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({
      isLayaway: true,
      documentNo: 'O/00011',
      grossAmount: 100,
      netAmount: 80,
      payment: 10,
      lines: [
        {
          grossUnitAmount: 200,
          netUnitAmount: 160,
          qty: 10,
          product: { productType: 'I' },
          taxes: {}
        }
      ],
      isEditable: false,
      payments: [{ origAmount: 100 }],
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

    const newState = OB.App.StateAPI.Global.cancelTicket(state, payload);

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
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 0 }
      },
      Cashup: {
        grossSales: 0,
        netSales: 0,
        grossReturns: 0,
        netReturns: 0
      },
      Messages: [
        {
          modelName: 'OBPOS_Order',
          service: 'org.openbravo.retail.posterminal.CancelLayawayLoader',
          type: 'backend',
          messageObj: {
            data: [
              {
                documentNo: 'O/00011',
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
