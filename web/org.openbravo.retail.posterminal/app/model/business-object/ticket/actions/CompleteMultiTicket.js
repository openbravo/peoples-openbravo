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

/* eslint-disable no-use-before-define */

(() => {
  OB.App.StateAPI.Global.registerAction(
    'completeMultiTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      const newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];
      const newTicketList = [...newGlobalState.TicketList];

      payload.ticketIdToClose.forEach(ticketCloseId => {
        let ticket;
        if (ticketCloseId === newTicket.id) {
          ticket = newTicket;
        } else {
          ticket = newTicketList.filter(
            ticketOfFilter => ticketOfFilter.id === ticketCloseId
          );
        }

        const ticketPayload = payload[ticket.id];

        // Set complete ticket properties
        ticket.completeTicket = true;
        ticket = OB.App.State.Ticket.Utils.completeTicket(
          ticket,
          ticketPayload
        );

        // FIXME: Move to calculateTotals?
        ticket = OB.App.State.Ticket.Utils.updateTicketType(
          ticket,
          ticketPayload
        );

        // Complete ticket payment
        ticket = OB.App.State.Ticket.Utils.completePayment(
          ticket,
          ticketPayload
        );

        // Document number generation
        ({
          ticket,
          documentSequence: newDocumentSequence
        } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
          ticket,
          newDocumentSequence,
          ticketPayload
        ));

        // Delivery generation
        ticket = OB.App.State.Ticket.Utils.generateDelivery(
          ticket,
          ticketPayload
        );

        // Invoice generation
        ticket = OB.App.State.Ticket.Utils.generateInvoice(
          ticket,
          ticketPayload
        );
        if (ticket.calculatedInvoice) {
          ({
            ticket: ticket.calculatedInvoice,
            documentSequence: newDocumentSequence
          } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
            ticket.calculatedInvoice,
            newDocumentSequence,
            ticketPayload
          ));
        }

        // Cashup update
        ({
          ticket,
          cashup: newCashup
        } = OB.App.State.Cashup.Utils.updateCashupFromTicket(
          ticket,
          newCashup,
          ticketPayload
        ));

        // Ticket synchronization message
        newMessages = [
          ...newMessages,
          OB.App.State.Messages.Utils.createNewMessage(
            'Order',
            'org.openbravo.retail.posterminal.OrderLoader',
            ticket
          )
        ];

        // Ticket print message
        newMessages = [
          ...newMessages,
          OB.App.State.Messages.Utils.createPrintTicketMessage(ticket)
        ];
        if (ticket.calculatedInvoice) {
          newMessages = [
            ...newMessages,
            OB.App.State.Messages.Utils.createPrintTicketMessage(
              ticket.calculatedInvoice
            )
          ];
        }
      });

      newGlobalState.Ticket = newTicket;
      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;
      newGlobalState.TicketList = newTicketList;

      return newGlobalState;
    }
  );

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      payload.ticketIdToClose.forEach(async ticketCloseId => {
        let ticket;
        if (ticketCloseId === globalState.Ticket.id) {
          ticket = globalState.Ticket;
        } else {
          ticket = globalState.TicketList.filter(
            ticketOfFilter => ticketOfFilter.id === ticketCloseId
          );
        }

        let ticketPayload = newPayload;

        ticketPayload = await OB.App.State.Ticket.Utils.checkAnonymousReturn(
          ticket,
          ticketPayload
        );
        ticketPayload = await OB.App.State.Ticket.Utils.checkAnonymousLayaway(
          ticket,
          ticketPayload
        );
        ticketPayload = await OB.App.State.Ticket.Utils.checkNegativePayments(
          ticket,
          ticketPayload
        );
        ticketPayload = await OB.App.State.Ticket.Utils.checkExtraPayments(
          ticket,
          ticketPayload
        );
        ticketPayload = await OB.App.State.Ticket.Utils.checkPrePayments(
          ticket,
          ticketPayload
        );
        ticketPayload = await OB.App.State.Ticket.Utils.checkOverPayments(
          ticket,
          ticketPayload
        );
        ticketPayload = await OB.App.State.Ticket.Utils.checkTicketUpdated(
          ticket,
          ticketPayload
        );

        newPayload[ticket.id] = ticketPayload;
      });

      return newPayload;
    }
  );
})();
