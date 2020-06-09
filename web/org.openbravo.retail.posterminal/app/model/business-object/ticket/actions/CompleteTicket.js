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

      newTicket.created = new Date().getTime();
      newTicket.completeTicket = true;
      newTicket.hasbeenpaid = 'Y';
      newTicket.obposAppCashup = payload.terminal.cashupId;
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

      // Shipment generation
      newTicket = OB.App.State.Ticket.Utils.generateShipment(
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
