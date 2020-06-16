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
      let newMessages = [...newGlobalState.Messages];

      // Set complete ticket properties
      newTicket.completeTicket = true;
      newTicket.hasbeenpaid = 'Y';
      newTicket.obposAppCashup = payload.terminal.cashupId;
      newTicket.creationDate = newTicket.creationDate || new Date();
      newTicket.timezoneOffset = newTicket.creationDate.getTimezoneOffset();
      newTicket.created = newTicket.creationDate.getTime();
      newTicket.orderDate = new Date().toISOString();
      newTicket.movementDate = new Date().toISOString();
      newTicket.accountingDate = new Date().toISOString();
      newTicket.creationDate = newTicket.creationDate.toISOString();
      newTicket.obposCreatedabsolute = newTicket.creationDate;
      newTicket.posTerminal = payload.terminal.id;
      newTicket.undo = null;
      newTicket.multipleUndo = null;
      newTicket.paymentMethodKind =
        newTicket.payments.length === 1 &&
        OB.App.State.Ticket.Utils.isFullyPaid(newTicket)
          ? newTicket.payments[0].kind
          : null;
      newTicket.approvals = [...newTicket.approvals, ...payload.approvals];

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
      newGlobalState.Messages = newMessages;

      return newGlobalState;
    }
  );

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = await checkPrepaymentApproval(
        globalState.Ticket,
        payload
      );
      return newPayload;
    },
    async (globalState, payload) => payload,
    100
  );

  const checkPrepaymentApproval = async (ticket, payload) => {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(ticket);
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
})();
