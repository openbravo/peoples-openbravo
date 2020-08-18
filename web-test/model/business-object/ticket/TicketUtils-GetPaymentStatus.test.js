/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
global.OB = {
  App: {
    Class: {},
    TerminalProperty: { get: jest.fn() },
    UUID: { generate: jest.fn() }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');

// set Ticket model utility functions
OB.App.State = { Ticket: { Utils: {} } };
OB.App.StateAPI.Ticket.utilities.forEach(
  util => (OB.App.State.Ticket.Utils[util.functionName] = util.implementation)
);

describe('Ticket Utils getPaymentStatus method', () => {
  it('should return initial status for empty ticket', () => {
    const ticket = deepfreeze({
      grossAmount: 0,
      change: 0,
      isNegative: false,
      lines: [],
      payments: []
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: false,
      isNegative: false,
      isReturn: false,
      isReversal: false,
      overpayment: 0,
      pending: 0,
      pendingAmt: 0,
      total: 0,
      totalAmt: 0,
      payments: []
    });
  });

  it('should return initial status for positive ticket without payments', () => {
    const ticket = deepfreeze({
      grossAmount: 100,
      change: 0,
      isNegative: false,
      lines: [{ qty: 1 }],
      payments: []
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: false,
      isNegative: false,
      isReturn: false,
      isReversal: false,
      overpayment: 0,
      pending: 100,
      pendingAmt: 100,
      total: 100,
      totalAmt: 100,
      payments: []
    });
  });

  it('should return initial status for negative ticket without payments', () => {
    const ticket = deepfreeze({
      grossAmount: -100,
      change: 0,
      isNegative: true,
      lines: [{ qty: -1 }],
      payments: []
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: false,
      isNegative: true,
      isReturn: true,
      isReversal: false,
      overpayment: 0,
      pending: 100,
      pendingAmt: 100,
      total: -100,
      totalAmt: -100,
      payments: []
    });
  });

  it('should return initial status for multi ticket without payments', () => {
    const ticket = deepfreeze({
      gross: 100,
      total: 100,
      payment: 0,
      change: 0,
      payments: []
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket, {
      multiTicketList: true
    });
    expect(newTicket).toEqual({
      isNegative: false,
      isReturn: false,
      overpayment: 0,
      pending: 100,
      pendingAmt: 100,
      total: 100,
      totalAmt: 100,
      payments: []
    });
  });

  it('should return complete status for positive ticket with payments', () => {
    const ticket = deepfreeze({
      grossAmount: 100,
      change: 0,
      isNegative: false,
      lines: [{ qty: 1 }],
      payments: [{ origAmount: 100 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: true,
      isNegative: false,
      isReturn: false,
      isReversal: false,
      overpayment: 0,
      pending: 0,
      pendingAmt: 0,
      total: 100,
      totalAmt: 100,
      payments: [{ origAmount: 100 }]
    });
  });

  it('should return complete status for negative ticket with payments', () => {
    const ticket = deepfreeze({
      grossAmount: -100,
      change: 0,
      isNegative: true,
      lines: [{ qty: -1 }],
      payments: [{ origAmount: 100 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: true,
      isNegative: true,
      isReturn: true,
      isReversal: false,
      overpayment: 0,
      pending: 0,
      pendingAmt: 0,
      total: -100,
      totalAmt: -100,
      payments: [{ origAmount: 100 }]
    });
  });

  it('should return complete status for multi ticket with payments', () => {
    const ticket = deepfreeze({
      gross: 100,
      total: 100,
      payment: 100,
      change: 0,
      payments: [{ origAmount: 100 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket, {
      multiTicketList: true
    });
    expect(newTicket).toEqual({
      isNegative: false,
      isReturn: false,
      overpayment: 0,
      pending: 0,
      pendingAmt: 0,
      total: 100,
      totalAmt: 100,
      payments: [{ origAmount: 100 }]
    });
  });

  it('should return initial status for positive ticket with prepayment', () => {
    const ticket = deepfreeze({
      grossAmount: 100,
      change: 0,
      isNegative: false,
      lines: [{ qty: 1 }],
      payments: [{ origAmount: 20 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: false,
      isNegative: false,
      isReturn: false,
      isReversal: false,
      overpayment: 0,
      pending: 80,
      pendingAmt: 80,
      total: 100,
      totalAmt: 100,
      payments: [{ origAmount: 20 }]
    });
  });

  it('should return initial status for negative ticket with prepayment', () => {
    const ticket = deepfreeze({
      grossAmount: -100,
      change: 0,
      isNegative: true,
      lines: [{ qty: -1 }],
      payments: [{ origAmount: 20 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: false,
      isNegative: true,
      isReturn: true,
      isReversal: false,
      overpayment: 0,
      pending: 80,
      pendingAmt: 80,
      total: -100,
      totalAmt: -100,
      payments: [{ origAmount: 20 }]
    });
  });

  it('should return initial status for multi ticket with prepayment', () => {
    const ticket = deepfreeze({
      gross: 100,
      total: 100,
      payment: 20,
      change: 0,
      payments: [{ origAmount: 20 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket, {
      multiTicketList: true
    });
    expect(newTicket).toEqual({
      isNegative: false,
      isReturn: false,
      overpayment: 0,
      pending: 80,
      pendingAmt: 80,
      total: 100,
      totalAmt: 100,
      payments: [{ origAmount: 20 }]
    });
  });

  it('should return complete status for positive ticket with overpayment', () => {
    const ticket = deepfreeze({
      grossAmount: 100,
      change: 0,
      isNegative: false,
      lines: [{ qty: 1 }],
      payments: [{ origAmount: 100 }, { origAmount: 20 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: true,
      isNegative: false,
      isReturn: false,
      isReversal: false,
      overpayment: 20,
      pending: 0,
      pendingAmt: 0,
      total: 100,
      totalAmt: 100,
      payments: [{ origAmount: 100 }, { origAmount: 20 }]
    });
  });

  it('should return complete status for negative ticket with overpayment', () => {
    const ticket = deepfreeze({
      grossAmount: -100,
      change: 0,
      isNegative: true,
      lines: [{ qty: -1 }],
      payments: [{ origAmount: 100 }, { origAmount: 20 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
    expect(newTicket).toEqual({
      done: true,
      isNegative: true,
      isReturn: true,
      isReversal: false,
      overpayment: 20,
      pending: 0,
      pendingAmt: 0,
      total: -100,
      totalAmt: -100,
      payments: [{ origAmount: 100 }, { origAmount: 20 }]
    });
  });

  it('should return complete status for multi ticket with overpayment', () => {
    const ticket = deepfreeze({
      gross: 100,
      total: 100,
      payment: 120,
      change: 0,
      payments: [{ origAmount: 100 }, { origAmount: 20 }]
    });
    const newTicket = OB.App.State.Ticket.Utils.getPaymentStatus(ticket, {
      multiTicketList: true
    });
    expect(newTicket).toEqual({
      isNegative: false,
      isReturn: false,
      overpayment: 20,
      pending: 0,
      pendingAmt: 0,
      total: 100,
      totalAmt: 100,
      payments: [{ origAmount: 100 }, { origAmount: 20 }]
    });
  });
});
