/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket global action that deletes a ticket and moves it to a message in the state
 */

(() => {
  OB.App.StateAPI.Global.registerAction(
    'deleteTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      let newTicketList = [...newGlobalState.TicketList];
      let newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];

      payload.multiTicketList.forEach(multiTicket => {
        let newMultiTicket = { ...multiTicket };

        if (
          payload.preferences.removeTicket &&
          newMultiTicket.isEditable &&
          (newMultiTicket.lines.length || newMultiTicket.deletedLines)
        ) {
          newMultiTicket = OB.App.State.Ticket.Utils.updateTicketType(
            newMultiTicket,
            payload
          );

          newMultiTicket = OB.App.State.Ticket.Utils.processTicket(
            newMultiTicket,
            payload
          );

          // Document number generation
          ({
            ticket: newMultiTicket,
            documentSequence: newDocumentSequence
          } = OB.App.State.DocumentSequence.Utils.generateDocumentNumber(
            newMultiTicket,
            newDocumentSequence,
            payload
          ));

          // Set complete ticket properties
          newMultiTicket.obposIsDeleted = true;
          newTicket.organization = payload.terminal.organization;
          newMultiTicket.grossAmount = 0;
          newMultiTicket.netAmount = 0;
          newMultiTicket.lines = newMultiTicket.lines.map(line => {
            return {
              ...line,
              obposIsDeleted: true,
              obposQtyDeleted: line.qty,
              grossUnitAmount: 0,
              netUnitAmount: 0,
              qty: 0,
              taxes: Object.keys(line.taxes).reduce((taxes, tax) => {
                const result = { ...taxes };
                result[tax] = { ...line.taxes[tax], net: 0, amount: 0 };
                return result;
              }, {})
            };
          });
          newMultiTicket.taxes = newMultiTicket.lines
            .concat(newMultiTicket.deletedLines || [])
            .flatMap(line => Object.values(line.taxes))
            .reduce((taxes, tax) => {
              const result = { ...taxes };
              result[tax.id] = result[tax.id] || { ...tax };
              return result;
            }, {});

          // Cashup update
          ({
            ticket: newMultiTicket,
            cashup: newCashup
          } = OB.App.State.Cashup.Utils.updateCashupFromTicket(
            newMultiTicket,
            newCashup,
            payload
          ));

          // Ticket synchronization message
          newMessages = [
            ...newMessages,
            OB.App.State.Messages.Utils.createNewMessage(
              'OBPOS_Order',
              'org.openbravo.retail.posterminal.OrderLoader',
              [OB.App.State.Ticket.Utils.cleanTicket(newMultiTicket)],
              {
                ...payload.extraProperties,
                name: 'OBPOS_Order'
              }
            )
          ];
        }
      });

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

  OB.App.StateAPI.Global.deleteTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      if (newPayload.ticketIds) {
        newPayload.multiTicketList = newPayload.ticketIds.map(ticketId =>
          globalState.Ticket.id === ticketId
            ? globalState.Ticket
            : globalState.TicketList.find(ticket => ticket.id === ticketId)
        );
      } else {
        newPayload.multiTicketList = [globalState.Ticket];
      }

      return newPayload;
    }
  );
})();
