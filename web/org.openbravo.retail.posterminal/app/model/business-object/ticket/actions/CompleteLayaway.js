/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket global action that completes a layaway and moves it to a message in the state
 */

/* eslint-disable no-use-before-define */

(() => {
  OB.App.StateAPI.Global.registerAction(
    'completeLayaway',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      let newTicketList = [...newGlobalState.TicketList];
      let newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];

      // Set complete ticket properties
      newTicket = OB.App.State.Ticket.Utils.processTicket(newTicket, payload);

      // Document number generation
      ({
        ticket: newTicket,
        documentSequence: newDocumentSequence
      } = OB.App.State.DocumentSequence.Utils.generateDocumentNumber(
        newTicket,
        newDocumentSequence,
        payload
      ));

      // Delivery generation
      newTicket = OB.App.State.Ticket.Utils.generateDelivery(
        newTicket,
        payload
      );

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
          [OB.App.State.Ticket.Utils.cleanTicket(newTicket)],
          payload.extraProperties
        )
      ];

      // Ticket print message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createPrintTicketMessage(newTicket)
      ];

      // Welcome message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createPrintWelcomeMessage()
      ];

      // TicketList update
      ({
        ticketList: newTicketList,
        ticket: newTicket
      } = OB.App.State.TicketList.Utils.removeTicket(
        newTicketList,
        newTicket,
        payload
      ));

      newGlobalState.TicketList = newTicketList;
      newGlobalState.Ticket = newTicket;
      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;

      return newGlobalState;
    }
  );

  OB.App.StateAPI.Global.completeLayaway.addActionPreparation(
    async (globalState, payload) => {
      let newPayload = { ...payload };

      newPayload = await OB.App.State.Ticket.Utils.checkAnonymousLayaway(
        globalState.Ticket,
        newPayload
      );
      newPayload = await checkReturnLayaway(globalState.Ticket, newPayload);
      newPayload = await checkPrePayments(globalState.Ticket, newPayload);
      newPayload = await checkInvoiceLayaway(globalState.Ticket, newPayload);
      newPayload = await OB.App.State.Ticket.Utils.checkTicketUpdated(
        globalState.Ticket,
        newPayload
      );

      return newPayload;
    }
  );

  const checkReturnLayaway = async (ticket, payload) => {
    const negativeLines = ticket.lines.some(line => line.qty < 0);
    if (
      negativeLines &&
      !OB.App.Security.hasPermission('OBPOS_AllowLayawaysNegativeLines')
    ) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_layawaysOrdersWithReturnsNotAllowed'
      });
    }

    if (negativeLines && ticket.payment > 0) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_partiallyLayawaysWithNegLinesNotAllowed'
      });
    }

    return payload;
  };

  const checkPrePayments = async (ticket, payload) => {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );

    if (
      !OB.App.TerminalProperty.get('terminal').terminalType
        .calculateprepayments ||
      ticket.obposPrepaymentlaylimitamt === OB.DEC.Zero ||
      paymentStatus.totalAmt <= OB.DEC.Zero ||
      OB.DEC.sub(
        OB.DEC.add(ticket.obposPrepaymentlaylimitamt, paymentStatus.pendingAmt),
        paymentStatus.totalAmt
      ) <= OB.DEC.Zero ||
      !paymentStatus.payments.length
    ) {
      return payload;
    }

    if (
      !OB.App.Security.hasPermission('OBPOS_AllowPrepaymentUnderLimitLayaway')
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_PrepaymentUnderLimit_NotAllowed',
        messageParams: [ticket.obposPrepaymentlaylimitamt]
      });
    }

    if (
      ticket.approvals.some(
        approval =>
          approval.approvalType === 'OBPOS_approval.prepaymentUnderLimitLayaway'
      )
    ) {
      return payload;
    }

    const newPayload = await OB.App.Security.requestApprovalForAction(
      'OBPOS_approval.prepaymentUnderLimitLayaway',
      payload
    );
    return newPayload;
  };

  const checkInvoiceLayaway = async (ticket, payload) => {
    if (ticket.generateInvoice) {
      OB.App.UserNotifier.notifyWarning({
        message: 'OBPOS_noInvoiceIfLayaway'
      });
    }

    return payload;
  };
})();
