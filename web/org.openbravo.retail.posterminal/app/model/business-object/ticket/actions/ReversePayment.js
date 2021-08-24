/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to reverse a payment to the ticket
 */

(function ReversePaymentDefinition() {
  function addPayment(ticket, payload) {
    let newTicket = { ...ticket };
    let newPayload = { ...payload };
    ({
      ticket: newTicket,
      payload: newPayload
    } = OB.App.State.Ticket.Utils.addPaymentRounding(newTicket, newPayload));
    // Set values defined in actionPreparations
    if (newPayload.extraInfo) {
      newTicket = { ...newTicket, ...newPayload.extraInfo.ticket };
    }
    newTicket = OB.App.State.Ticket.Utils.addPayment(newTicket, newPayload);
    return newTicket;
  }
  OB.App.StateAPI.Ticket.registerAction('reversePayment', (ticket, payload) => {
    let newTicket = { ...ticket };
    newTicket = addPayment(newTicket, payload);
    if (payload.payment.paymentRoundingLine) {
      const newPayloadRounding = { ...payload };
      newPayloadRounding.payment = payload.payment.paymentRoundingLine;
      newTicket = addPayment(newTicket, newPayloadRounding);
    }
    return newTicket;
  });

  OB.App.StateAPI.Ticket.reversePayment.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };

      newPayload = await OB.App.State.Ticket.Utils.notReversablePaymentValidation(
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.moreThanOnePaymentMethodValidation(
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.createReversalPaymentAndRounding(
        ticket,
        newPayload
      );
      return newPayload;
    },
    async (ticket, payload) => payload,
    10
  );

  OB.App.StateAPI.Ticket.reversePayment.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };

      newPayload = await OB.App.State.Ticket.Utils.fillPayment(
        ticket,
        newPayload
      );
      if (newPayload.payment.paymentRoundingLine) {
        const newPayloadRounding = { ...payload };
        newPayloadRounding.payment = newPayload.payment.paymentRoundingLine;
        const paymentRounding = await OB.App.State.Ticket.Utils.fillPayment(
          ticket,
          newPayloadRounding
        );
        newPayload.payment.paymentRoundingLine = paymentRounding.payment;
      }
      return newPayload;
    },
    async (ticket, payload) => payload,
    0
  );

  OB.App.StateAPI.Ticket.reversePayment.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };

      newPayload = await OB.App.State.Ticket.Utils.checkAlreadyPaid(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkNotNumberAmount(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkStopAddingPayments(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkExactPaid(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkVoidLayaway(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.managePrePaymentChange(
        ticket,
        newPayload
      );

      return newPayload;
    }
  );
})();
