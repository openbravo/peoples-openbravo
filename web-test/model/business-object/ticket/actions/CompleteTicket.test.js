/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/CompleteTicket');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Complete ticket action', () => {
  const ticket = deepfreeze({
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
    preferences: {
      autoPrintReceipts: true
    },
    constants: {
      fieldSeparator: '-',
      identifierSuffix: '_id'
    }
  });

  const cashup = deepfreeze({
    grossSales: 0,
    netSales: 0,
    grossReturns: 0,
    netReturns: 0,
    cashPaymentMethodInfo: []
  });

  it('should complete ticket, print it and synchronize it', () => {
    const ticketList = deepfreeze([]);
    const documentSequence = deepfreeze({
      lastassignednum: { sequencePrefix: 'O', sequenceNumber: 0 }
    });
    const messages = deepfreeze([]);
    const state = deepfreeze({
      TicketList: ticketList,
      Ticket: ticket,
      DocumentSequence: documentSequence,
      Cashup: cashup,
      Messages: messages
    });

    const newState = OB.App.StateAPI.Global.completeTicket(state, payload);

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

  it('should update in the cashup the countPerAmount property of the payment methods that support it', () => {
    const ticketWithPayments = {
      ...ticket,
      payments: [
        {
          kind: 'OBPOS_payment.voucher',
          amount: 100,
          countPerAmount: { '50.00': 1, '10.00': 5 }
        }
      ]
    };
    const documentSequence = {
      lastassignednum: { sequencePrefix: 'O', sequenceNumber: 0 }
    };
    const cashupWithPaymentMethodInfo = {
      ...cashup,
      cashPaymentMethodInfo: [
        {
          id: '1',
          searchKey: 'OBPOS_payment.voucher',
          totalSales: 50,
          countPerAmount: { '50.00': 1 },
          usedInCurrentTrx: false
        }
      ]
    };
    const messages = [];
    const state = {
      TicketList: [],
      Ticket: ticketWithPayments,
      DocumentSequence: documentSequence,
      Cashup: cashupWithPaymentMethodInfo,
      Messages: messages
    };
    const payloadWithPayment = {
      ...payload,
      payments: [
        {
          payment: { searchKey: 'OBPOS_payment.voucher' },
          obposPosprecision: 2
        }
      ]
    };

    const newState = OB.App.StateAPI.Global.completeTicket(
      state,
      payloadWithPayment
    );
    expect(newState).toMatchObject({
      Cashup: {
        cashPaymentMethodInfo: [{ countPerAmount: { '50.00': 2, '10.00': 5 } }]
      }
    });
  });
});
