/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/DeleteTicket');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Close ticket action', () => {
  it('should close ticket', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({
      id: 'A',
      isEditable: false,
      grossAmount: 100,
      netAmount: 80,
      payment: 100,
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
      terminal: {
        id: '0',
        documentnoPadding: 5,
        terminalType: { documentType: 'Sale' }
      },
      businessPartner: 'BP',
      multiTicketList: [ticket],
      documentNumberSeparator: '/',
      pricelist: {},
      context: { user: {} },
      preferences: {},
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.closeTicket(state, payload);

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
      Messages: []
    });
  });

  it('should close ticket from ticketList', () => {
    const ticketList = deepfreeze([
      {
        id: 'B',
        isEditable: false,
        grossAmount: 100,
        netAmount: 80,
        payment: 100,
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
      },
      {
        id: 'C'
      }
    ]);
    const ticket = deepfreeze({
      id: 'A'
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
      terminal: {
        id: '0',
        documentnoPadding: 5,
        terminalType: { documentType: 'Sale' }
      },
      businessPartner: 'BP',
      multiTicketList: [ticketList[0]],
      documentNumberSeparator: '/',
      pricelist: {},
      context: { user: {} },
      preferences: {},
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.closeTicket(state, payload);

    expect(newState).toMatchObject({
      TicketList: [{ id: 'C' }],
      Ticket: { id: 'A' },
      DocumentSequence: {
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 0 }
      },
      Cashup: {
        grossSales: 0,
        netSales: 0,
        grossReturns: 0,
        netReturns: 0
      },
      Messages: []
    });
  });

  it('should close several tickets', () => {
    const ticketList = deepfreeze([
      {
        id: 'B',
        isEditable: false,
        grossAmount: 100,
        netAmount: 80,
        payment: 100,
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
      },
      {
        id: 'C'
      }
    ]);
    const ticket = deepfreeze({
      id: 'A',
      isEditable: false,
      grossAmount: 100,
      netAmount: 80,
      payment: 100,
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
      terminal: {
        id: '0',
        documentnoPadding: 5,
        terminalType: { documentType: 'Sale' }
      },
      businessPartner: 'BP',
      multiTicketList: [ticket, ticketList[0]],
      documentNumberSeparator: '/',
      pricelist: {},
      context: { user: {} },
      preferences: {},
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.closeTicket(state, payload);

    expect(newState).toMatchObject({
      TicketList: [],
      Ticket: { id: 'C' },
      DocumentSequence: {
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 0 }
      },
      Cashup: {
        grossSales: 0,
        netSales: 0,
        grossReturns: 0,
        netReturns: 0
      },
      Messages: []
    });
  });

  it('should not synchronize ticket not editable with removeTicket preference', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({
      id: 'A',
      isEditable: false,
      lines: [{}]
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
      terminal: {
        id: '0',
        documentnoPadding: 5,
        terminalType: { documentType: 'Sale' }
      },
      businessPartner: 'BP',
      multiTicketList: [ticket],
      documentNumberSeparator: '/',
      pricelist: {},
      context: { user: {} },
      preferences: {
        removeTicket: true
      },
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.closeTicket(state, payload);

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
      Messages: []
    });
  });
});
