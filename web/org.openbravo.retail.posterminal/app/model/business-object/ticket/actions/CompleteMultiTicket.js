/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
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
    'completeMultiTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      let newTicketList = [...newGlobalState.TicketList];
      let newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];

      let checkedTicketList = [];
      payload.ticketsIdToClose.forEach(ticketCloseId => {
        if (ticketCloseId === newTicket.id) {
          checkedTicketList.push(newTicket);
        } else {
          checkedTicketList.push(
            newTicketList.find(
              ticketOfFind => ticketOfFind.id === ticketCloseId
            )
          );
        }
      });
      checkedTicketList = OB.App.State.Ticket.Utils.setPaymentsToReceipts(
        checkedTicketList,
        payload
      );

      checkedTicketList.forEach(checkedTicket => {
        let currentTicket = { ...checkedTicket };

        // Set complete ticket properties
        currentTicket.completeTicket = !currentTicket.amountToLayaway;
        currentTicket = OB.App.State.Ticket.Utils.completeTicket(
          currentTicket,
          payload
        );

        // FIXME: Move to calculateTotals?
        currentTicket = OB.App.State.Ticket.Utils.updateTicketType(
          currentTicket,
          payload
        );

        // Complete ticket payment
        currentTicket = OB.App.State.Ticket.Utils.completePayment(
          currentTicket,
          payload
        );

        // Document number generation
        ({
          ticket: currentTicket,
          documentSequence: newDocumentSequence
        } = OB.App.State.DocumentSequence.Utils.generateDocumentNumber(
          currentTicket,
          newDocumentSequence,
          payload
        ));

        // Delivery generation
        currentTicket = OB.App.State.Ticket.Utils.generateDelivery(
          currentTicket,
          payload
        );

        // Invoice generation
        currentTicket = OB.App.State.Ticket.Utils.generateInvoice(
          currentTicket,
          payload
        );
        if (currentTicket.calculatedInvoice) {
          ({
            ticket: currentTicket.calculatedInvoice,
            documentSequence: newDocumentSequence
          } = OB.App.State.DocumentSequence.Utils.generateDocumentNumber(
            currentTicket.calculatedInvoice,
            newDocumentSequence,
            payload
          ));
        }

        // Cashup update
        ({
          ticket: currentTicket,
          cashup: newCashup
        } = OB.App.State.Cashup.Utils.updateCashupFromTicket(
          currentTicket,
          newCashup,
          payload
        ));

        // Ticket synchronization message
        newMessages = [
          ...newMessages,
          OB.App.State.Messages.Utils.createNewMessage(
            'Order',
            'org.openbravo.retail.posterminal.OrderLoader',
            [currentTicket],
            payload.extraProperties
          )
        ];

        // Ticket print message
        newMessages = [
          ...newMessages,
          OB.App.State.Messages.Utils.createPrintTicketMessage(currentTicket)
        ];
        if (currentTicket.calculatedInvoice) {
          newMessages = [
            ...newMessages,
            OB.App.State.Messages.Utils.createPrintTicketMessage(
              currentTicket.calculatedInvoice
            )
          ];
        }

        // TicketList update
        ({
          ticketList: newTicketList,
          ticket: newTicket
        } = OB.App.State.TicketList.Utils.removeCurrentTicket(
          newTicketList,
          currentTicket,
          payload
        ));
      });

      newGlobalState.Ticket = newTicket;
      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;
      newGlobalState.TicketList = newTicketList;

      return newGlobalState;
    }
  );

  OB.App.StateAPI.Global.completeMultiTicket.addActionPreparation(
    async (globalState, payload) => {
      let newPayload = { ...payload };

      newPayload.ticketsIdToClose = newPayload.multiTickets.multiOrdersList.map(
        ticket => ticket.id
      );

      newPayload.isMultiTicket = true;

      // eslint-disable-next-line no-restricted-syntax
      for (const ticketCloseId of newPayload.ticketsIdToClose) {
        let ticket;
        if (ticketCloseId === globalState.Ticket.id) {
          ticket = globalState.Ticket;
        } else {
          [ticket] = globalState.TicketList.filter(
            ticketOfFilter => ticketOfFilter.id === ticketCloseId
          );
        }

        // eslint-disable-next-line no-await-in-loop
        newPayload = await OB.App.State.Ticket.Utils.checkAnonymousReturn(
          ticket,
          newPayload
        );
        // eslint-disable-next-line no-await-in-loop
        newPayload = await OB.App.State.Ticket.Utils.checkAnonymousLayaway(
          ticket,
          newPayload
        );
      }

      newPayload = await OB.App.State.Ticket.Utils.checkNegativePayments(
        newPayload.multiTickets,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkExtraPayments(
        newPayload.multiTickets,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkPrePayments(
        newPayload.multiTickets,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkOverPayments(
        newPayload.multiTickets,
        newPayload
      );

      // eslint-disable-next-line no-restricted-syntax
      for (const ticketCloseId of newPayload.ticketsIdToClose) {
        let ticket;
        if (ticketCloseId === globalState.Ticket.id) {
          ticket = globalState.Ticket;
        } else {
          [ticket] = globalState.TicketList.filter(
            ticketOfFilter => ticketOfFilter.id === ticketCloseId
          );
        }

        // eslint-disable-next-line no-await-in-loop
        newPayload = await OB.App.State.Ticket.Utils.checkTicketUpdated(
          ticket,
          newPayload
        );
      }

      return newPayload;
    }
  );
})();
