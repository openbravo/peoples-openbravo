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

      // FIXME: send terminal and preference objects in settings?
      const {
        terminal,
        paymentNames,
        documentNumberSeparator,
        salesWithOneLineNegativeAsReturns,
        splitChange,
        discountRules,
        bpSets,
        taxRules
      } = payload;
      const settings = {
        discountRules,
        bpSets,
        taxRules,
        terminalId: terminal.id,
        terminalOrganization: terminal.organization,
        documentTypeForSales: terminal.terminalType.documentType,
        documentTypeForReturns: terminal.terminalType.documentTypeForReturns,
        returnSequencePrefix: terminal.returnDocNoPrefix,
        quotationSequencePrefix: terminal.quotationDocNoPrefix,
        fullReturnInvoiceSequencePrefix: terminal.fullReturnInvoiceDocNoPrefix,
        simplifiedReturnInvoiceSequencePrefix:
          terminal.simplifiedReturnInvoiceDocNoPrefix,
        documentNumberSeparator,
        documentNumberPadding: terminal.documentnoPadding,
        paymentNames,
        multiChange: terminal.multiChange,
        salesWithOneLineNegativeAsReturns,
        splitChange
      };

      newTicket.created = new Date().getTime();
      newTicket.completeTicket = true;
      newTicket.hasbeenpaid = 'Y';
      newTicket.obposAppCashup = terminal.cashUpId;
      newTicket = OB.App.State.Ticket.Utils.updateTicketType(
        newTicket,
        settings
      );

      // Complete ticket payment
      newTicket = OB.App.State.Ticket.Utils.completePayment(
        newTicket,
        settings
      );

      // Document number generation
      ({
        ticket: newTicket,
        documentSequence: newDocumentSequence
      } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
        newTicket,
        newDocumentSequence,
        settings
      ));

      // Shipment generation
      newTicket = OB.App.State.Ticket.Utils.generateShipment(
        newTicket,
        settings
      );

      // Invoice generation
      newTicket = OB.App.State.Ticket.Utils.generateInvoice(
        newTicket,
        settings
      );
      if (newTicket.calculatedInvoice) {
        ({
          ticket: newTicket.calculatedInvoice,
          documentSequence: newDocumentSequence
        } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
          newTicket.calculatedInvoice,
          newDocumentSequence,
          settings
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
