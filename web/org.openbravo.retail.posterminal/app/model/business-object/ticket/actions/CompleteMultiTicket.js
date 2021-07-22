/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket global action that completes multiple tickets and moves them to messages in the state
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
      let currentTicket = {};

      const multiTicketList = OB.App.State.Ticket.Utils.sharePaymentsBetweenTickets(
        payload.multiTicketList,
        payload
      );

      multiTicketList.forEach(multiTicket => {
        let newMultiTicket = { ...multiTicket };

        // Set complete ticket properties
        newMultiTicket.completeTicket = newMultiTicket.amountToLayaway == null;
        newMultiTicket = OB.App.State.Ticket.Utils.processTicket(
          newMultiTicket,
          payload
        );

        // FIXME: Move to calculateTotals?
        newMultiTicket = OB.App.State.Ticket.Utils.updateTicketType(
          newMultiTicket,
          payload
        );

        // Complete ticket payment
        newMultiTicket = OB.App.State.Ticket.Utils.completePayment(
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

        // Delivery generation
        newMultiTicket = OB.App.State.Ticket.Utils.generateDelivery(
          newMultiTicket,
          payload
        );

        // Invoice generation
        if (newMultiTicket.completeTicket) {
          newMultiTicket = OB.App.State.Ticket.Utils.generateInvoice(
            newMultiTicket,
            payload
          );
          if (newMultiTicket.calculatedInvoice) {
            ({
              ticket: newMultiTicket.calculatedInvoice,
              documentSequence: newDocumentSequence
            } = OB.App.State.DocumentSequence.Utils.generateDocumentNumber(
              newMultiTicket.calculatedInvoice,
              newDocumentSequence,
              payload
            ));
          }
        }

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

        // Ticket print message
        if (
          !newTicket.calculatedInvoice ||
          (newTicket.calculatedInvoice &&
            newTicket.calculatedInvoice.fullInvoice &&
            payload.preferences &&
            payload.preferences.autoPrintReceipts)
        ) {
          newMessages = [
            ...newMessages,
            OB.App.State.Messages.Utils.createPrintTicketMessage(
              newMultiTicket,
              {
                skipSelectPrinters: multiTicketList.indexOf(multiTicket) !== 0
              }
            )
          ];
        }
        if (newMultiTicket.calculatedInvoice) {
          newMessages = [
            ...newMessages,
            OB.App.State.Messages.Utils.createPrintTicketMessage(
              newMultiTicket.calculatedInvoice,
              {
                skipSelectPrinters: multiTicketList.indexOf(multiTicket) !== 0
              }
            )
          ];
        }
      });

      // Welcome message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createPrintWelcomeMessage()
      ];

      // Add Current Ticket to Last Ticket
      currentTicket = { ...newTicket };

      // TicketList update
      ({
        ticketList: newTicketList,
        ticket: newTicket
      } = OB.App.State.TicketList.Utils.removeTicket(
        newTicketList,
        newTicket,
        payload
      ));

      newGlobalState.Ticket = newTicket;
      newGlobalState.LastTicket = currentTicket;
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

      newPayload.multiTicketList = newPayload.multiTickets.multiOrdersList.map(
        multiTicket => {
          const newMultiTicket =
            globalState.Ticket.id === multiTicket.id
              ? globalState.Ticket
              : globalState.TicketList.find(
                  ticket => ticket.id === multiTicket.id
                );
          return {
            ...newMultiTicket,
            orderType: multiTicket.orderType,
            amountToLayaway: multiTicket.amountToLayaway
          };
        }
      );

      // eslint-disable-next-line no-restricted-syntax
      for (const multiTicket of newPayload.multiTicketList) {
        // eslint-disable-next-line no-await-in-loop
        newPayload = await OB.App.State.Ticket.Utils.checkAnonymousReturn(
          multiTicket,
          newPayload
        );
        // eslint-disable-next-line no-await-in-loop
        newPayload = await OB.App.State.Ticket.Utils.checkAnonymousLayaway(
          multiTicket,
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
      for (const multiTicket of newPayload.multiTicketList) {
        // eslint-disable-next-line no-await-in-loop
        newPayload = await OB.App.State.Ticket.Utils.checkTicketUpdated(
          multiTicket,
          newPayload
        );
      }

      return newPayload;
    },
    async (globalState, payload) => payload,
    100
  );
})();
