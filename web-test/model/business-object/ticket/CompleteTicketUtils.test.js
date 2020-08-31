/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupTicket');
require('./SetupTicketUtils');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/exception/TranslatableError');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
const deepfreeze = require('deepfreeze');

OB.App.TerminalProperty = { get: jest.fn() };
OB.App.Locale = { formatAmount: jest.fn() };

OB.App.TerminalProperty.get.mockImplementation(property => {
  if (property === 'terminal') {
    return {
      businessPartner: 'BP',
      returns_anonymouscustomer: false,
      terminalType: {
        calculateprepayments: true
      }
    };
  }
  return {};
});

OB.App.Security = {};
OB.App.Security.hasPermission = jest.fn();
OB.App.Security.requestApprovalForAction = jest.fn();
OB.App.Request = {};
OB.App.Request.mobileServiceRequest = jest.fn();
OB.App.View = {
  DialogUIHandler: {
    askConfirmation: jest.fn(),
    askConfirmationWithCancel: jest.fn()
  }
};

const line = {
  id: 'l1',
  qty: 1,
  grossUnitAmount: 100,
  netUnitAmount: 80,
  product: { productType: 'I' }
};

const negativeline = {
  id: 'nl1',
  qty: -1,
  grossUnitAmount: 100,
  netUnitAmount: 80,
  product: { productType: 'I' }
};

describe('Check Anonymous Return', () => {
  it('check with negative line', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [negativeline]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkAnonymousReturn(
        deepfreeze(ticket),
        payload
      )
    ).rejects.toThrow('ActionCanceled');
  });

  it('check with positive line', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkAnonymousReturn(
        deepfreeze(ticket),
        payload
      )
    ).resolves.toMatchObject(payload);
  });
});

describe('Check Anonymous Layaway', () => {
  it('check with orderType 2', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      orderType: 2
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkAnonymousLayaway(
        deepfreeze(ticket),
        payload
      )
    ).rejects.toThrow('ActionCanceled');
  });

  it('check without orderType 2', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      orderType: 0
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkAnonymousLayaway(
        deepfreeze(ticket),
        payload
      )
    ).resolves.toMatchObject(payload);
  });
});

describe('Check Unprocessed Payments', () => {
  it('check without prePayment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      payments: [{ amount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkUnprocessedPayments(
        deepfreeze(ticket),
        payload
      )
    ).rejects.toThrow('ActionCanceled');
  });

  it('check with prePayment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      payments: [{ amount: 100, isPrePayment: true }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkUnprocessedPayments(
        deepfreeze(ticket),
        payload
      )
    ).resolves.toMatchObject(payload);
  });
});

describe('Check Negative Payments', () => {
  it('check with Negative ticket without ReturnOrder Payment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      isNegative: true,
      payments: [{ amount: 100, isReturnOrder: false }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkNegativePayments(
        deepfreeze(ticket),
        payload
      )
    ).rejects.toThrow('ActionCanceled');
  });

  it('check with ticket with ReturnOrder Payment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      isNegative: false,
      payments: [{ amount: 100, isReturnOrder: true }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkNegativePayments(
        deepfreeze(ticket),
        payload
      )
    ).rejects.toThrow('ActionCanceled');
  });

  it('check with return ticket with ReturnOrder Payment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      isNegative: true,
      payments: [{ amount: 100, isReturnOrder: true }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkNegativePayments(
        deepfreeze(ticket),
        payload
      )
    ).resolves.toMatchObject(payload);
  });
});

describe('Check Extra Payments', () => {
  it('check in ticket with Unnecessary Payment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      payments: [{ origAmount: 100 }, { origAmount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkExtraPayments(deepfreeze(ticket), payload)
    ).rejects.toThrow('ActionCanceled');
  });

  it('check in ticket without Unnecessary Payment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      payments: [
        { origAmount: 100, isPrePayment: true },
        { origAmount: 100, isReversePayment: true },
        { origAmount: 100, isReversed: true },
        { origAmount: 100 }
      ]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkExtraPayments(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(payload);
  });

  it('check in multiticket with Unnecessary Payment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      total: 100,
      payments: [{ origAmount: 100 }, { origAmount: 100 }]
    };
    const payload = { multiTicketList: true, businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkExtraPayments(deepfreeze(ticket), payload)
    ).rejects.toThrow('ActionCanceled');
  });

  it('check in multiticket without Unnecessary Payment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      total: 100,
      payments: [
        { origAmount: 100, isPrePayment: true },
        { origAmount: 100, isReversePayment: true },
        { origAmount: 100, isReversed: true },
        { origAmount: 100 }
      ]
    };
    const payload = { multiTicketList: true, businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkExtraPayments(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(payload);
  });
});

describe('Check Pre Payments', () => {
  it('check in ticket with Prepayment limit as zero', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      orderType: 0,
      obposPrepaymentlimitamt: OB.DEC.Zero,
      payments: [{ origAmount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    await expect(
      OB.App.State.Ticket.Utils.checkPrePayments(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(payload);
  });

  it('check in ticket without Preference AllowPrepaymentUnderLimit', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      orderType: 0,
      obposPrepaymentlimitamt: 100,
      payments: [{ origAmount: 50 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Security.hasPermission.mockReturnValue(false);
    await expect(
      OB.App.State.Ticket.Utils.checkPrePayments(deepfreeze(ticket), payload)
    ).rejects.toThrow('ActionCanceled');
  });

  it('check in ticket with Preference AllowPrepaymentUnderLimit', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      orderType: 0,
      approvals: [],
      obposPrepaymentlimitamt: 100,
      payments: [{ origAmount: 50 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    const finalPayload = {
      ticket: [{ ticket: ticket }],
      approvals: ['OBPOS_approval.prepaymentUnderLimit']
    };
    OB.App.Security.hasPermission.mockReturnValue(true);
    OB.App.Security.requestApprovalForAction.mockResolvedValue(finalPayload);
    await expect(
      OB.App.State.Ticket.Utils.checkPrePayments(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(finalPayload);
  });
});

describe('Check OverPayments', () => {
  it('check with overpayment', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      total: 100,
      change: 0,
      payment: 200,
      payments: [{ origAmount: 200, amount: 200 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.View.DialogUIHandler.askConfirmation.mockResolvedValue(true);
    OB.App.View.DialogUIHandler.askConfirmationWithCancel.mockResolvedValue(
      true
    );
    await expect(
      OB.App.State.Ticket.Utils.checkOverPayments(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(payload);
  });
});

describe('Check Ticket Updated', () => {
  it('check ticket updated with Success', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      grossAmount: 100,
      payments: [{ origAmount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({});
    OB.App.Security.hasPermission.mockReturnValue(false);
    await expect(
      OB.App.State.Ticket.Utils.checkTicketUpdated(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(payload);
  });

  it('check ticket updated with Error Response', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      grossAmount: 100,
      isPaid: true,
      lines: [line],
      payments: [{ origAmount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({
      response: { data: {} }
    });
    OB.App.Security = {};
    OB.App.Security.hasPermission = jest.fn();
    OB.App.Security.hasPermission.mockReturnValue(false);
    await expect(
      OB.App.State.Ticket.Utils.checkTicketUpdated(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(payload);
  });
});

describe('Check Ticket Cancelled', () => {
  it('check ticket cancelled with Success', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      grossAmount: 100,
      canceledorder: {
        lines: [line]
      },
      payments: [{ origAmount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({
      response: { data: { orderCancelled: false } }
    });
    OB.App.Security.hasPermission.mockReturnValue(false);
    await expect(
      OB.App.State.Ticket.Utils.checkTicketCanceled(deepfreeze(ticket), payload)
    ).resolves.toMatchObject(payload);
  });

  it('check ticket cancelled with Error without Canceled Order data', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      grossAmount: 100,
      isPaid: true,
      lines: [line],
      payments: [{ origAmount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({
      response: { data: { orderCancelled: true } }
    });
    OB.App.Security.hasPermission.mockReturnValue(false);
    await expect(
      OB.App.State.Ticket.Utils.checkTicketCanceled(deepfreeze(ticket), payload)
    ).rejects.toThrow('ActionCanceled');
  });

  it('check ticket cancelled with Error Response', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      grossAmount: 100,
      isPaid: true,
      lines: [line],
      payments: [{ origAmount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({
      response: { data: { orderCancelled: true } }
    });
    OB.App.Security.hasPermission.mockReturnValue(false);
    await expect(
      OB.App.State.Ticket.Utils.checkTicketCanceled(deepfreeze(ticket), payload)
    ).rejects.toThrow('ActionCanceled');
  });
});

describe('Check BusinessPartner Credit', () => {
  it('check for return', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [negativeline],
      grossAmount: -100,
      total: -100,
      change: 0,
      payment: 100,
      payments: [{ origAmount: 100, amount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.View.DialogUIHandler.askConfirmation.mockResolvedValue(true);
    await expect(
      OB.App.State.Ticket.Utils.checkBusinessPartnerCredit(
        deepfreeze(ticket),
        payload
      )
    ).resolves.toMatchObject(payload);
  });

  it('check with enoughCredit', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      total: 100,
      change: 0,
      payment: 100,
      payments: [{ origAmount: 100, amount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({
      response: { data: { enoughCredit: true } }
    });
    await expect(
      OB.App.State.Ticket.Utils.checkBusinessPartnerCredit(
        deepfreeze(ticket),
        payload
      )
    ).resolves.toMatchObject(payload);
  });

  it('check without data in response', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      total: 100,
      change: 0,
      payment: 100,
      payments: [{ origAmount: 100, amount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({
      response: {}
    });
    await expect(
      OB.App.State.Ticket.Utils.checkBusinessPartnerCredit(
        deepfreeze(ticket),
        payload
      )
    ).rejects.toThrow('ActionCanceled');
  });

  it('check without enoughCredit in response', async () => {
    const ticket = {
      businessPartner: { id: 'BP' },
      lines: [line],
      grossAmount: 100,
      total: 100,
      change: 0,
      payment: 100,
      payments: [{ origAmount: 100, amount: 100 }]
    };
    const payload = { businessPartner: { id: 'BP' } };
    OB.App.Request.mobileServiceRequest.mockReturnValue({
      response: { data: { enoughCredit: false } }
    });
    await expect(
      OB.App.State.Ticket.Utils.checkBusinessPartnerCredit(
        deepfreeze(ticket),
        payload
      )
    ).rejects.toThrow('ActionCanceled');
  });
});
