/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
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
      const currentDate = new Date();
      const creationDate = newTicket.creationDate
        ? new Date(newTicket.creationDate)
        : currentDate;
      newTicket.completeTicket = true;
      newTicket.hasbeenpaid = 'Y';
      newTicket.orderDate = currentDate.toISOString();
      newTicket.movementDate = currentDate.toISOString();
      newTicket.accountingDate = currentDate.toISOString();
      newTicket.creationDate = creationDate.toISOString();
      newTicket.obposCreatedabsolute = creationDate.toISOString();
      newTicket.created = creationDate.getTime();
      newTicket.timezoneOffset = creationDate.getTimezoneOffset();
      newTicket.posTerminal = payload.terminal.id;
      newTicket.undo = null;
      newTicket.multipleUndo = null;
      newTicket.paymentMethodKind =
        newTicket.payments.length === 1 &&
        OB.App.State.Ticket.Utils.isFullyPaid(newTicket)
          ? newTicket.payments[0].kind
          : null;
      newTicket.approvals = [
        ...newTicket.approvals,
        ...(payload.approvals || [])
      ];

      // FIXME: Remove once every use of OB.UTIL.Approval.requestApproval() send approvalType as string
      newTicket.approvals = newTicket.approvals.map(approval => {
        const newApproval = { ...approval };
        if (typeof approval.approvalType === 'object') {
          newApproval.approvalType = approval.approvalType.approval;
        }
        return newApproval;
      });

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

      newPayload = await checkPrepayment(globalState.Ticket, newPayload);
      newPayload = await checkOverpayment(globalState.Ticket, newPayload);

      return newPayload;
    },
    async (globalState, payload) => payload,
    100
  );

  const checkPrepayment = async (ticket, payload) => {
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

  const checkOverpayment = async (ticket, payload) => {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );
    if (!paymentStatus.overpayment) {
      return payload;
    }

    const confirmation = await OB.App.View.DialogUIHandler.askConfirmation({
      title: 'OBPOS_OverpaymentWarningTitle',
      message: 'OBPOS_OverpaymentWarningBody',
      messageParams: [
        OB.I18N.formatCurrencyWithSymbol(
          paymentStatus.overpayment,
          payload.terminal.symbol,
          payload.terminal.currencySymbolAtTheRight
        )
      ]
    });
    if (!confirmation) {
      throw new OB.App.Class.ActionCanceled();
    }

    return payload;
  };
})();
