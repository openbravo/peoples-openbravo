/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('Ticket Utils getPaymentStatus function', () => {
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
