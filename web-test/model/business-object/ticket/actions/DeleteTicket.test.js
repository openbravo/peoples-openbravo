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

describe('Delete ticket action', () => {
  it('should delete ticket', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({
      id: 'A',
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

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

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

  it('should delete ticket with removeTicket preference', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({
      id: 'A',
      isEditable: true,
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
      taxes: {},
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
      preferences: {
        removeTicket: true
      },
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

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
        grossSales: 0,
        netSales: 0,
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
                obposIsDeleted: true,
                grossAmount: 0,
                netAmount: 0,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 1,
                documentNo: 'O/00001',
                lines: [
                  {
                    grossUnitAmount: 0,
                    netUnitAmount: 0,
                    obposIsDeleted: true,
                    obposQtyDeleted: 10,
                    qty: 0,
                    product: { productType: 'I' },
                    taxes: {}
                  }
                ]
              }
            ]
          }
        }
      ]
    });
  });

  it('should delete ticket from ticketList', () => {
    const ticketList = deepfreeze([
      {
        id: 'B',
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

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

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

  it('should delete ticket from ticketList with removeTicket preference', () => {
    const ticketList = deepfreeze([
      {
        id: 'B',
        isEditable: true,
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
        taxes: {},
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
      preferences: {
        removeTicket: true
      },
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

    expect(newState).toMatchObject({
      TicketList: [{ id: 'C' }],
      Ticket: { id: 'A' },
      DocumentSequence: {
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 1 }
      },
      Cashup: {
        grossSales: 0,
        netSales: 0,
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
                ...ticketList[0],
                obposIsDeleted: true,
                grossAmount: 0,
                netAmount: 0,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 1,
                documentNo: 'O/00001',
                lines: [
                  {
                    grossUnitAmount: 0,
                    netUnitAmount: 0,
                    obposIsDeleted: true,
                    obposQtyDeleted: 10,
                    qty: 0,
                    product: { productType: 'I' },
                    taxes: {}
                  }
                ]
              }
            ]
          }
        }
      ]
    });
  });

  it('should delete several tickets', () => {
    const ticketList = deepfreeze([
      {
        id: 'B',
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

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

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

  it('should delete several tickets with removeTicket preference', () => {
    const ticketList = deepfreeze([
      {
        id: 'B',
        isEditable: true,
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
        taxes: {},
        payments: [],
        approvals: []
      },
      {
        id: 'C'
      }
    ]);
    const ticket = deepfreeze({
      id: 'A',
      isEditable: true,
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
      taxes: {},
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
      preferences: {
        removeTicket: true
      },
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

    expect(newState).toMatchObject({
      TicketList: [],
      Ticket: { id: 'C' },
      DocumentSequence: {
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 2 }
      },
      Cashup: {
        grossSales: 0,
        netSales: 0,
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
                obposIsDeleted: true,
                grossAmount: 0,
                netAmount: 0,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 1,
                documentNo: 'O/00001',
                lines: [
                  {
                    grossUnitAmount: 0,
                    netUnitAmount: 0,
                    obposIsDeleted: true,
                    obposQtyDeleted: 10,
                    qty: 0,
                    product: { productType: 'I' },
                    taxes: {}
                  }
                ]
              }
            ]
          }
        },
        {
          modelName: 'Order',
          service: 'org.openbravo.retail.posterminal.OrderLoader',
          type: 'backend',
          messageObj: {
            data: [
              {
                ...ticketList[0],
                obposIsDeleted: true,
                grossAmount: 0,
                netAmount: 0,
                obposSequencename: 'lastassignednum',
                obposSequencenumber: 2,
                documentNo: 'O/00002',
                lines: [
                  {
                    grossUnitAmount: 0,
                    netUnitAmount: 0,
                    obposIsDeleted: true,
                    obposQtyDeleted: 10,
                    qty: 0,
                    product: { productType: 'I' },
                    taxes: {}
                  }
                ]
              }
            ]
          }
        }
      ]
    });
  });

  it('should not synchronize ticket not editable', () => {
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
      preferences: {},
      constants: {
        fieldSeparator: '-',
        identifierSuffix: '_id'
      }
    });

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

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

  it('should not synchronize ticket without lines', () => {
    const ticketList = deepfreeze([]);
    const ticket = deepfreeze({
      id: 'A',
      isEditable: true,
      lines: []
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

    const newState = OB.App.StateAPI.Global.deleteTicket(state, payload);

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
