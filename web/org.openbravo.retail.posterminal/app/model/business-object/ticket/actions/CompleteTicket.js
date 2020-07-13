/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket global action that completes a ticket and moves it to a message in the state
 */

/* eslint-disable no-use-before-define */

(() => {
  OB.App.StateAPI.Global.registerAction(
    'completeTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      let newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];

      // Set complete ticket properties
      newTicket.completeTicket = true;
      newTicket = OB.App.State.Ticket.Utils.completeTicket(newTicket, payload);

      // FIXME: Move to calculateTotals?
      newTicket = OB.App.State.Ticket.Utils.updateTicketType(
        newTicket,
        payload
      );

      // Complete ticket payment
      newTicket = OB.App.State.Ticket.Utils.completePayment(newTicket, payload);

      // Document number generation
      ({
        ticket: newTicket,
        documentSequence: newDocumentSequence
      } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
        newTicket,
        newDocumentSequence,
        payload
      ));

      // Delivery generation
      newTicket = OB.App.State.Ticket.Utils.generateDelivery(
        newTicket,
        payload
      );

      // Invoice generation
      newTicket = OB.App.State.Ticket.Utils.generateInvoice(newTicket, payload);
      if (newTicket.calculatedInvoice) {
        ({
          ticket: newTicket.calculatedInvoice,
          documentSequence: newDocumentSequence
        } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
          newTicket.calculatedInvoice,
          newDocumentSequence,
          payload
        ));
      }

      // Cashup update
      ({
        ticket: newTicket,
        cashup: newCashup
      } = OB.App.State.Cashup.Utils.updateCashupFromTicket(
        newTicket,
        newCashup,
        payload
      ));

      // Ticket synchronization message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createNewMessage(
          'Order',
          'org.openbravo.retail.posterminal.OrderLoader',
          newTicket
        )
      ];

      // Ticket print message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createPrintTicketMessage(newTicket)
      ];
      if (newTicket.calculatedInvoice) {
        newMessages = [
          ...newMessages,
          OB.App.State.Messages.Utils.createPrintTicketMessage(
            newTicket.calculatedInvoice
          )
        ];
      }

      newGlobalState.Ticket = newTicket;
      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;

      return newGlobalState;
    }
  );

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      let newPayload = { ...payload };

      newPayload = await checkAnonymousReturn(globalState.Ticket, newPayload);
      newPayload = await checkNegativePayments(globalState.Ticket, newPayload);
      newPayload = await checkExtraPayments(globalState.Ticket, newPayload);
      newPayload = await checkPrePayments(globalState.Ticket, newPayload);
      newPayload = await checkOverPayments(globalState.Ticket, newPayload);
      newPayload = await checkTicketUpdated(globalState.Ticket, newPayload);

      return newPayload;
    },
    async (globalState, payload) => payload,
    100
  );

  const checkAnonymousReturn = async (ticket, payload) => {
    if (
      !payload.terminal.returnsAnonymousCustomer &&
      ticket.businessPartner.id === payload.terminal.businessPartner &&
      ticket.lines.some(line => line.qty < 0 && !line.originalDocumentNo)
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_returnServicesWithAnonimousCust'
      });
    }

    return payload;
  };

  const checkNegativePayments = async (ticket, payload) => {
    if (
      ticket.payments
        .filter(payment => payment.isReturnOrder !== undefined)
        .some(payment => payment.isReturnOrder !== ticket.isNegative)
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: ticket.isNegative
          ? 'OBPOS_PaymentOnReturnReceipt'
          : 'OBPOS_NegativePaymentOnReceipt'
      });
    }

    return payload;
  };

  const checkExtraPayments = async (ticket, payload) => {
    ticket.payments.reduce((total, payment) => {
      if (total >= OB.DEC.abs(ticket.grossAmount) && !payment.paymentRounding) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_UnnecessaryPaymentAdded'
        });
      }

      if (
        payment.isReversePayment ||
        payment.isReversed ||
        payment.isPrePayment
      ) {
        return total;
      }

      return OB.DEC.add(total, payment.origAmount);
    }, OB.DEC.Zero);

    return payload;
  };

  const checkPrePayments = async (ticket, payload) => {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );

    if (
      !payload.terminal.calculatePrepayments ||
      ticket.orderType === 1 ||
      ticket.orderType === 3 ||
      ticket.obposPrepaymentlimitamt === OB.DEC.Zero ||
      paymentStatus.totalAmt <= OB.DEC.Zero ||
      OB.DEC.sub(
        OB.DEC.add(ticket.obposPrepaymentlimitamt, paymentStatus.pendingAmt),
        paymentStatus.totalAmt
      ) <= OB.DEC.Zero
    ) {
      return payload;
    }

    if (!OB.App.Security.hasPermission('OBPOS_AllowPrepaymentUnderLimit')) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_PrepaymentUnderLimit_NotAllowed',
        messageParams: [ticket.obposPrepaymentlimitamt]
      });
    }

    if (
      ticket.approvals.some(
        approval =>
          approval.approvalType === 'OBPOS_approval.prepaymentUnderLimit'
      )
    ) {
      return payload;
    }

    const newPayload = await OB.App.Security.requestApprovalForAction(
      'OBPOS_approval.prepaymentUnderLimit',
      payload
    );
    return newPayload;
  };

  const checkOverPayments = async (ticket, payload) => {
    return OB.App.State.Ticket.Utils.checkOverPayments(ticket, payload);
  };

  const checkTicketUpdated = async (ticket, payload) => {
    return OB.App.State.Ticket.Utils.checkTicketUpdated(ticket, payload);
  };
})();
