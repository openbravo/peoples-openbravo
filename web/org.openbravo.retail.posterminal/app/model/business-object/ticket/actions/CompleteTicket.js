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

      const {
        terminal,
        documentNumberSeparator,
        salesWithOneLineNegativeAsReturns,
        discountRules,
        bpSets,
        taxRules
      } = payload;
      const settings = {
        discountRules,
        bpSets,
        taxRules,
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
        salesWithOneLineNegativeAsReturns
      };

      newTicket.created = new Date().getTime();
      newTicket.completeTicket = true;
      newTicket.obposAppCashup = terminal.cashUpId;
      newTicket = OB.App.State.Ticket.Utils.updateTicketType(
        newTicket,
        settings
      );

      // FIXME: Move to add payment action?
      if (OB.App.State.Ticket.Utils.isReturnTicket(newTicket, settings)) {
        newTicket.payments = newTicket.payments.map(payment => {
          const newPayment = { ...payment };

          if (
            !payment.isPrePayment &&
            !payment.reversedPaymentId &&
            !newTicket.isPaid
          ) {
            newPayment.amount = -payment.amount;
            if (payment.amountRounded) {
              newPayment.amountRounded = -payment.amountRounded;
            }
            newPayment.origAmount = -payment.origAmount;
            if (payment.origAmountRounded) {
              newPayment.origAmountRounded = -payment.origAmountRounded;
            }
            newPayment.paid = -payment.paid;
          } else {
            newPayment.paid = payment.amount;
          }

          return newPayment;
        });
      }

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

      // FIXME: Remove once properties are mapped
      newTicket.gross = newTicket.grossAmount;
      newTicket.net = newTicket.netAmount;
      newTicket.lines = newTicket.lines.map(line => {
        return {
          ...line,
          gross: line.gross || line.grossAmount,
          net: line.net || line.netAmount,
          taxLines: line.taxLines || line.taxes
        };
      });

      const newMessage = OB.App.State.Messages.Utils.createNewMessage(
        'Order',
        'org.openbravo.retail.posterminal.OrderLoader',
        newTicket
      );

      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Messages = [...newGlobalState.Messages, newMessage];

      return newGlobalState;
    }
  );
})();
