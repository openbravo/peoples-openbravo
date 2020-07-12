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
      let newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];

      // Set complete ticket properties
      const currentDate = new Date();
      const creationDate = newTicket.creationDate
        ? new Date(newTicket.creationDate)
        : currentDate;
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

      newGlobalState.Ticket = newTicket;
      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;

      return newGlobalState;
    }
  );
})();
