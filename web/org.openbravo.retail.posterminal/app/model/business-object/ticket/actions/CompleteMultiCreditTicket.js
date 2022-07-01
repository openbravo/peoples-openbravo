/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket global action that completes multiple credit tickets and moves them to messages in the state
 */

(() => {
  OB.App.StateAPI.Global.registerAction(
    'completeMultiCreditTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      let newTicketList = [...newGlobalState.TicketList];
      let newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];
      let currentTicket = {};

      payload.multiTicketList.forEach(multiTicket => {
        let newMultiTicket = { ...multiTicket };

        // Set complete ticket properties
        newMultiTicket.payOnCredit = true;
        newMultiTicket.businessPartner.creditUsed += OB.DEC.mul(
          OB.DEC.abs(
            OB.DEC.sub(
              newMultiTicket.grossAmount,
              newMultiTicket.paymentWithSign
            )
          ),
          Math.sign(newMultiTicket.grossAmount)
        );
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
          newMessages = OB.App.State.Messages.Utils.generateDeliverTicketMessages(
            newMessages,
            newMultiTicket,
            {},
            payload.deliverAction,
            payload.deliverService
          );
        }

        if (newMultiTicket.calculatedInvoice) {
          newMessages = OB.App.State.Messages.Utils.generateDeliverTicketMessages(
            newMessages,
            newMultiTicket.calculatedInvoice,
            {},
            payload.deliverAction,
            payload.deliverService
          );
        }

        // Add Current Ticket to Last Ticket
        currentTicket = { ...newMultiTicket };
      });

      // Welcome message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createPrintWelcomeMessage()
      ];

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

  OB.App.StateAPI.Global.completeMultiCreditTicket.addActionPreparation(
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
            amountToLayaway: multiTicket.amountToLayaway,
            fullInvoice: multiTicket.fullInvoice,
            generateInvoice: multiTicket.generateInvoice
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
      }

      // eslint-disable-next-line no-await-in-loop
      newPayload = await OB.App.State.Ticket.Utils.checkBusinessPartnerCredit(
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
    }
  );
})();
