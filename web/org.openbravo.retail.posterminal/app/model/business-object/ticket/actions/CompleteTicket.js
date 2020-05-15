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
      const newTicket = { ...newGlobalState.Ticket };

      const {
        cashUpId,
        returnSequencePrefix,
        quotationSequencePrefix,
        documentNumberSeperator,
        documentNumberPadding,
        salesWithOneLineNegativeAsReturns
      } = payload;

      newTicket.created = new Date().getTime();
      newTicket.obposAppCashup = cashUpId;

      // Document Sequence calculation
      const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
        newTicket,
        returnSequencePrefix,
        quotationSequencePrefix,
        salesWithOneLineNegativeAsReturns
      );
      const newDocumentSequence = OB.App.State.DocumentSequence.Utils.increaseSequence(
        newGlobalState.DocumentSequence,
        sequenceName
      );
      const { sequencePrefix, sequenceNumber } = newDocumentSequence[
        sequenceName
      ];
      newTicket.sequenceName = sequenceName;
      newTicket.sequenceNumber = sequenceNumber;
      newTicket.documentNo = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
        sequencePrefix,
        documentNumberSeperator,
        documentNumberPadding,
        sequenceNumber
      );

      // FIXME: Remove once properties are mapped
      newTicket.gross = newTicket.grossAmount;
      newTicket.net = newTicket.netAmount;
      newTicket.bp = newTicket.businessPartner;

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
