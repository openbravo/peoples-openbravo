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
      newTicket.obposCreatedabsolute = OB.I18N.formatDateISO(
        newTicket.creationDate
      );
      newTicket.orderDate = OB.I18N.normalizeDate(new Date());
      newTicket.movementDate = OB.I18N.normalizeDate(new Date());
      newTicket.accountingDate = OB.I18N.normalizeDate(new Date());
      newTicket.creationDate = OB.I18N.normalizeDate(newTicket.creationDate);
      newTicket.posTerminal = payload.terminal.id;
      newTicket.undo = null;
      newTicket.multipleUndo = null;
      newTicket.paymentMethodKind =
        newTicket.payments.length === 1 &&
        OB.App.State.Ticket.Utils.isFullyPaid(newTicket)
          ? newTicket.payments[0].kind
          : null;
      newTicket = OB.App.State.Ticket.Utils.updateTicketType(
        newTicket,
        payload
      );

      // FIXME: is it needed with new
      newTicket.approvals = newTicket.approvals.map(approval => {
        const newApproval = { ...approval };
        if (typeof approval.approvalType === 'object') {
          newApproval.approvalType = approval.approvalType.approval;
        }
        return newApproval;
      });

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
})();
