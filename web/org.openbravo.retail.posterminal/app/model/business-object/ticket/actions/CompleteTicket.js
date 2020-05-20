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
      let ticket = { ...newGlobalState.Ticket };
      let documentSequence = { ...newGlobalState.DocumentSequence };

      const {
        organization,
        cashUpId,
        returnSequencePrefix,
        quotationSequencePrefix,
        fullReturnInvoiceSequencePrefix,
        simplifiedReturnInvoiceSequencePrefix,
        documentNumberSeperator,
        documentNumberPadding,
        salesWithOneLineNegativeAsReturns
      } = payload;

      ticket.created = new Date().getTime();
      ticket.completeTicket = true;
      // FIXME: set cashup info once Cashup is migrated to state
      // ticket.obposAppCashup = cashUpId;

      // Document number generation
      ({
        ticket,
        documentSequence
      } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
        ticket,
        documentSequence,
        returnSequencePrefix,
        quotationSequencePrefix,
        null,
        null,
        documentNumberSeperator,
        documentNumberPadding,
        salesWithOneLineNegativeAsReturns
      ));

      // Shipment generation
      ({ ticket } = OB.App.State.Ticket.Utils.generateShipment(
        ticket,
        organization
      ));

      // Invoice generation
      ({ ticket, documentSequence } = OB.App.State.Ticket.Utils.generateInvoice(
        ticket,
        documentSequence,
        fullReturnInvoiceSequencePrefix,
        simplifiedReturnInvoiceSequencePrefix,
        documentNumberSeperator,
        documentNumberPadding,
        salesWithOneLineNegativeAsReturns
      ));

      // FIXME: Remove once properties are mapped
      ticket.bp = ticket.businessPartner;
      ticket.gross = ticket.grossAmount;
      ticket.net = ticket.netAmount;
      ticket.lines = ticket.lines.map(line => {
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
        ticket
      );

      newGlobalState.DocumentSequence = documentSequence;
      newGlobalState.Messages = [...newGlobalState.Messages, newMessage];

      return newGlobalState;
    }
  );
})();
