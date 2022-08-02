/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
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
      let newTicketList = [...newGlobalState.TicketList];
      let newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];
      let currentTicket = {};

      // Set complete ticket properties
      newTicket.completeTicket = true;
      newTicket = OB.App.State.Ticket.Utils.processTicket(newTicket, payload);

      // FIXME: Move to calculateTotals?
      newTicket = OB.App.State.Ticket.Utils.updateTicketType(
        newTicket,
        payload
      );

      // generate changePayments property for payments
      newTicket = OB.App.State.Ticket.Utils.calculateChange(newTicket, payload);

      // Complete ticket payment
      newTicket = OB.App.State.Ticket.Utils.completePayment(newTicket, payload);

      // Document number generation
      ({
        ticket: newTicket,
        documentSequence: newDocumentSequence
      } = OB.App.State.DocumentSequence.Utils.generateDocumentNumber(
        newTicket,
        newDocumentSequence,
        payload
      ));

      // Delivery generation
      newTicket = OB.App.State.Ticket.Utils.generateDelivery(
        newTicket,
        payload
      );

      // Invoice generation
      newTicket = OB.App.State.Ticket.Utils.generateInvoice(newTicket, payload);
      if (newTicket.calculatedInvoice) {
        ({
          ticket: newTicket.calculatedInvoice,
          documentSequence: newDocumentSequence
        } = OB.App.State.DocumentSequence.Utils.generateDocumentNumber(
          newTicket.calculatedInvoice,
          newDocumentSequence,
          payload
        ));
      }

      // Cashup update
      ({
        ticket: newTicket,
        cashup: newCashup
      } = OB.App.State.Cashup.Utils.updateCashupFromTicket(
        newTicket,
        newCashup,
        payload
      ));

      // Ticket synchronization message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createNewMessage(
          'OBPOS_Order',
          'org.openbravo.retail.posterminal.OrderLoader',
          [OB.App.State.Ticket.Utils.cleanTicket(newTicket)],
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
          newTicket,
          {
            forcedtemplate: payload.forcedtemplate
          },
          payload.deliverAction,
          payload.deliverService
        );
      }
      if (newTicket.calculatedInvoice) {
        newMessages = OB.App.State.Messages.Utils.generateDeliverTicketMessages(
          newMessages,
          newTicket.calculatedInvoice,
          {
            forcedtemplate: payload.forcedtemplate
          },
          payload.deliverAction,
          payload.deliverService
        );
      }

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

      newGlobalState.TicketList = newTicketList;
      newGlobalState.Ticket = newTicket;
      newGlobalState.LastTicket = currentTicket;
      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;

      // Complete cashup fields
      return completeCashupFields(newGlobalState, currentTicket);
    }
  );

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.checkAnonymousReturn(
        globalState.Ticket,
        newPayload
      );
    },
    async (globalState, payload) => payload,
    100
  );

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.checkNegativePayments(
        globalState.Ticket,
        newPayload
      );
    },
    async (globalState, payload) => payload,
    110
  );

  // We skip priority 120 here because we implement an action
  // preparation that is only executed in old POS at CompleteTicketCheck file

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.checkPrePayments(
        globalState.Ticket,
        newPayload
      );
    },
    async (globalState, payload) => payload,
    130
  );

  // We skip priority 140 here because we implement an action
  // preparation that is only executed in old POS at CompleteTicketCheck file

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.checkTicketUpdated(
        globalState.Ticket,
        newPayload
      );
    },
    async (globalState, payload) => payload,
    150
  );

  function completeCashupFields(newGlobalState, currentTicket) {
    const globalState = { ...newGlobalState };

    // Total complete tickets
    globalState.Cashup.totalCompleteTickets += 1;

    // Total amount
    globalState.Cashup.totalAmount += currentTicket.grossAmount;

    // Total quantity products
    globalState.Cashup.totalQuantityProducts += currentTicket.qty;

    // Total discount amount
    globalState.Cashup.totalDiscountAmount += currentTicket.lines.reduce(
      (previousValue, currentValue) =>
        previousValue +
        currentValue.baseGrossUnitAmount -
        currentValue.grossUnitAmountWithoutTicketDiscounts,
      0
    );

    // Information per user
    const userExist = globalState.Cashup.users.find(
      x => x.name === currentTicket.businessPartner.name
    );

    if (userExist === undefined) {
      const user = {
        name: currentTicket.businessPartner.name,
        totalCompleteTickets: 1,
        totalAmount: currentTicket.grossAmount
      };

      globalState.Cashup.users = [...globalState.Cashup.users, user];
    } else {
      userExist.totalCompleteTickets += 1;
      userExist.totalAmount += currentTicket.grossAmount;
    }

    // Information per product category
    currentTicket.lines.forEach(line => {
      const productCategoryExist = globalState.Cashup.productCategories.find(
        x => x.name === line.product.productCategory
      );

      if (productCategoryExist === undefined) {
        const productCategory = {
          name: line.product.productCategory,
          numberOfItems: line.qty,
          totalAmount: line.grossUnitAmount
        };

        globalState.Cashup.productCategories = [
          ...globalState.Cashup.productCategories,
          productCategory
        ];
      } else {
        productCategoryExist.numberOfItems += line.qty;
        productCategoryExist.totalAmount += line.grossUnitAmount;
      }
    });

    return globalState;
  }
})();
