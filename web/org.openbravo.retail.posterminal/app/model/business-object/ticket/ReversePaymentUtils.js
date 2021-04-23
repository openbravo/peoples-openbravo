/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Add Payment action
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * create ReversePayment
   */
  createReversePayment(ticket, payload) {
    const originalPayment = payload.payment;
    const reversePayment = { ...originalPayment };
    const { payments } = ticket;

    // Remove the cloned properties that must not be in the payment
    reversePayment.date = undefined;
    reversePayment.isPaid = undefined;
    reversePayment.isPrePayment = undefined;
    reversePayment.paymentAmount = undefined;
    reversePayment.paymentDate = undefined;
    reversePayment.paymentId = OB.App.UUID.generate();
    reversePayment.paymentRoundingLine = undefined;

    // Modify other properties for the reverse payment
    reversePayment.amount = OB.DEC.sub(OB.DEC.Zero, originalPayment.amount);
    reversePayment.origAmount = OB.DEC.sub(
      OB.DEC.Zero,
      originalPayment.origAmount
    );
    reversePayment.paid = OB.DEC.sub(OB.DEC.Zero, originalPayment.paid);
    if (originalPayment.overpayment) {
      reversePayment.overpayment = OB.DEC.sub(
        OB.DEC.Zero,
        originalPayment.overpayment
      );
    }
    reversePayment.reversedPaymentId = originalPayment.paymentId;
    reversePayment.reversedPayment = originalPayment;
    reversePayment.index = OB.DEC.add(
      OB.DEC.One,
      originalPayment.paymentRounding && originalPayment.paymentRounding
        ? payments.indexOf(originalPayment) + OB.DEC.One
        : payments.indexOf(originalPayment)
    );
    // reversePayment.set('reverseCallback', reverseCallback);
    reversePayment.isReversePayment = true;
    reversePayment.paymentData = originalPayment.paymentData
      ? originalPayment.paymentData
      : null;
    reversePayment.oBPOSPOSTerminal = originalPayment.oBPOSPOSTerminal
      ? originalPayment.oBPOSPOSTerminal
      : null;
    return reversePayment;
  },

  async createReversalPaymentAndRounding(ticket, payload) {
    const newPayload = { ...payload };
    const reversalPayment = this.createReversePayment(ticket, payload);
    const paymentRounding = payload.payment.paymentRoundingLine
      ? payload.payment.paymentRoundingLine
      : null;
    let reversalPaymentRounding;
    if (!(paymentRounding === null || paymentRounding === undefined)) {
      reversalPaymentRounding = this.createReversePayment(
        !paymentRounding.get
          ? new OB.Model.PaymentLine(paymentRounding)
          : paymentRounding
      );
    }
    newPayload.payment = reversalPayment;
    newPayload.paymentRounding = reversalPaymentRounding;
    return newPayload;
  },

  getUsedPayment(payload) {
    return payload.payments.filter(
      p => p.payment.searchKey === payload.payment.kind
    );
  },

  async notReversablePaymentValidation(payload) {
    const usedPayment = this.getUsedPayment(payload);
    const usedPaymentMethod = usedPayment[0].paymentMethod;
    if (usedPayment.length < 1 || !usedPaymentMethod.isreversable) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_NotReversablePayment',
        messageParams: [payload.payment.name]
      });
    }
    return payload;
  },

  async moreThanOnePaymentMethodValidation(payload) {
    const usedPayment = this.getUsedPayment(payload);
    if (usedPayment.length > 1) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_MoreThanOnePaymentMethod',
        messageParams: [payload.payment.name]
      });
    }
    return payload;
  }
});
