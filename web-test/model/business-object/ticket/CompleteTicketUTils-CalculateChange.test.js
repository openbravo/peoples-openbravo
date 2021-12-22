/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupTicket');
require('./SetupTicketUtils');
const deepfreeze = require('deepfreeze');

OB = OB || {};
OB.I18N = OB.I18N || {};
OB.I18N.formatCurrencyWithSymbol = jest.fn(
  (number, symbol, currencySymbolToTheRight) => {
    return `${number}${symbol}`;
  }
);
OB.I18N.getLabel = jest.fn(label => label);

describe('Ticket Utils calculateChange function', () => {
  it('should be defined', () => {
    expect(OB.App.State.Ticket.Utils.calculateChange).toBeDefined();
  });

  it('should return the original ticket if splitChange preference is disabled', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 50,
          date: '2021-12-16T10:24:45.562Z',
          id: '262DEE5153FE1781ADA91204E6191B6C',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 50,
          paid: 18.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: false
      }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject(ticket);
  });

  it('should return the original ticket if terminal is offline', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 50,
          date: '2021-12-16T10:24:45.562Z',
          id: '262DEE5153FE1781ADA91204E6191B6C',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 50,
          paid: 18.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject(ticket);
  });

  it('should return the original ticket if ticket has no payments', () => {
    const ticket = deepfreeze({
      payments: [],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.card'
          },
          paymentMethod: {
            iscash: false
          },
          rate: '1',
          symbol: '€'
        },
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.voucher'
          },
          paymentMethod: {
            iscash: false
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject(ticket);
  });

  it('should return the original ticket if there are no cash payment methods defined', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 50,
          date: '2021-12-16T10:24:45.562Z',
          id: '262DEE5153FE1781ADA91204E6191B6C',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 50,
          paid: 18.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.card'
          },
          paymentMethod: {
            iscash: false
          },
          rate: '1',
          symbol: '€'
        },
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.voucher'
          },
          paymentMethod: {
            iscash: false
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject(ticket);
  });

  it('should return a new ticket with at least one changePayment if splitChange preference is enabled and ticket has cash payments', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 50,
          date: '2021-12-16T10:24:45.562Z',
          id: '262DEE5153FE1781ADA91204E6191B6C',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 50,
          paid: 18.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.cash'
          },
          paymentMethod: {
            iscash: true
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject({
      ...ticket,
      changePayments: expect.any(Array)
    });
  });

  it('should return a new ticket with one changePayment if single payment is made with cash', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 50,
          date: '2021-12-16T10:24:45.562Z',
          id: '262DEE5153FE1781ADA91204E6191B6C',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 50,
          paid: 18.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.cash'
          },
          paymentMethod: {
            iscash: true
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject({
      ...ticket,
      changePayments: expect.any(Array)
    });
  });

  it('should have one changePayment that is equal to the ticket change if ticket has one cash payment', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 50,
          date: '2021-12-16T10:24:45.562Z',
          id: '262DEE5153FE1781ADA91204E6191B6C',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 50,
          paid: 18.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.cash'
          },
          paymentMethod: {
            iscash: true
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket.changePayments[0].amount).toEqual(ticket.change);
  });

  it('should have multiple changePayments if there are split cash payments', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 10,
          cancelAndReplace: undefined,
          id: 'C3722402F43B84DD704BE6744963DB15',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 10,
          overpayment: 0.55,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        },
        {
          allowOpenDrawer: true,
          amount: 10,
          cancelAndReplace: undefined,
          id: 'C3722402F43B84DD704BE6744963DB15',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 10,
          overpayment: 0.55,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.cash'
          },
          paymentMethod: {
            iscash: true
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket.changePayments).toHaveLength(2);
  });

  it('should have changePayments that are equal to the payment overpayment if ticket has split cash payments', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 10,
          cancelAndReplace: undefined,
          id: 'C3722402F43B84DD704BE6744963DB15',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 10,
          overpayment: 0.55,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        },
        {
          allowOpenDrawer: true,
          amount: 10,
          cancelAndReplace: undefined,
          id: 'C3722402F43B84DD704BE6744963DB15',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 10,
          overpayment: 0.55,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.cash'
          },
          paymentMethod: {
            iscash: true
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket.changePayments[0].amount).toEqual(
      ticket.payments[0].overpayment
    );
  });

  it('should account for exact amounts paid if there are split overpayments and exact payments on the same ticket', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 9.45,
          cancelAndReplace: undefined,
          id: '1E538568BC54029EF36F6B769EE35DC3',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '1C9CB2318D17467BA0A76DB6CF309213',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 9.45,
          overpayment: 0,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        },
        {
          allowOpenDrawer: true,
          amount: 10,
          cancelAndReplace: undefined,
          id: '180B0D6FD5478443CBE70296766867D8',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '1C9CB2318D17467BA0A76DB6CF309213',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 10,
          overpayment: 0.55,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 0.55
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.cash'
          },
          paymentMethod: {
            iscash: true
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket.payments).toEqual([
      {
        allowOpenDrawer: true,
        amount: 9.45,
        cancelAndReplace: undefined,
        id: '1E538568BC54029EF36F6B769EE35DC3',
        isCash: true,
        isPaid: false,
        isReturnOrder: false,
        isocode: 'EUR',
        kind: 'OBPOS_payment.cash',
        mulrate: '1.000000000000',
        name: 'Cash',
        oBPOSPOSTerminal: '1C9CB2318D17467BA0A76DB6CF309213',
        openDrawer: false,
        orderGross: 18.9,
        origAmount: 9.45,
        overpayment: 0,
        paid: 8.9,
        paymentData: {
          amount: 0.55,
          amountRounded: 0.55,
          key: 'OBPOS_payment.cash',
          label: '0.55€',
          origAmount: 0.55
        },
        precision: 2,
        printtwice: false,
        rate: '1'
      },
      {
        allowOpenDrawer: true,
        amount: 10,
        cancelAndReplace: undefined,
        id: '180B0D6FD5478443CBE70296766867D8',
        isCash: true,
        isPaid: false,
        isReturnOrder: false,
        isocode: 'EUR',
        kind: 'OBPOS_payment.cash',
        mulrate: '1.000000000000',
        name: 'Cash',
        oBPOSPOSTerminal: '1C9CB2318D17467BA0A76DB6CF309213',
        openDrawer: false,
        orderGross: 18.9,
        origAmount: 10,
        overpayment: 0,
        paid: 8.9,
        paymentData: {
          amount: 0.55,
          amountRounded: 0.55,
          key: 'OBPOS_payment.cash',
          label: '0.55€',
          origAmount: 0.55
        },
        precision: 2,
        printtwice: false,
        rate: '1'
      }
    ]);
  });

  it('should have changePayments that are well-formed objects with correct and equal properties', () => {
    const ticket = deepfreeze({
      payments: [
        {
          allowOpenDrawer: true,
          amount: 10,
          cancelAndReplace: undefined,
          id: 'C3722402F43B84DD704BE6744963DB15',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 10,
          overpayment: 0.55,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        },
        {
          allowOpenDrawer: true,
          amount: 10,
          cancelAndReplace: undefined,
          id: 'C3722402F43B84DD704BE6744963DB15',
          isCash: true,
          isPaid: false,
          isReturnOrder: false,
          isocode: 'EUR',
          kind: 'OBPOS_payment.cash',
          mulrate: '1.000000000000',
          name: 'Cash',
          oBPOSPOSTerminal: '9104513C2D0741D4850AE8493998A7C8',
          openDrawer: false,
          orderGross: 18.9,
          origAmount: 10,
          overpayment: 0.55,
          paid: 8.9,
          precision: 2,
          printtwice: false,
          rate: '1'
        }
      ],
      change: 31.1
    });
    const payload = deepfreeze({
      preferences: {
        splitChange: true
      },
      payments: [
        {
          currencySymbolAtTheRight: true,
          currentBalance: 448.7,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: {
            searchKey: 'OBPOS_payment.cash'
          },
          paymentMethod: {
            iscash: true
          },
          rate: '1',
          symbol: '€'
        }
      ],
      terminal: { id: '9104513C2D0741D4850AE8493998A7C8' }
    });
    const newTicket = OB.App.State.Ticket.Utils.calculateChange(
      ticket,
      payload
    );
    expect(newTicket.changePayments[0]).toMatchObject({
      key: 'OBPOS_payment.cash',
      amount: 0.55,
      amountRounded: 0.55,
      origAmount: 0.55,
      label: '0.55€'
    });
  });
});
