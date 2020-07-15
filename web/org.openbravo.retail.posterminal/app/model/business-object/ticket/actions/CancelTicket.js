/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket global action that cancels a ticket and moves it to a message in the state
 */

/* eslint-disable no-use-before-define */

(() => {
  OB.App.StateAPI.Global.registerAction(
    'cancelTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      let newTicket = { ...newGlobalState.Ticket };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];

      // Set complete ticket properties
      newTicket = OB.App.State.Ticket.Utils.completeTicket(newTicket, payload);

      // Complete ticket payment
      newTicket = OB.App.State.Ticket.Utils.completePayment(newTicket, payload);

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
          'Order',
          'org.openbravo.retail.posterminal.CancelLayawayLoader',
          [newTicket]
        )
      ];

      // Ticket print message
      newMessages = [
        ...newMessages,
        OB.App.State.Messages.Utils.createPrintTicketMessage(newTicket)
      ];

      newGlobalState.Ticket = newTicket;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;

      return newGlobalState;
    }
  );

  OB.App.StateAPI.Global.cancelTicket.addActionPreparation(
    async (globalState, payload) => {
      let newPayload = { ...payload };

      newPayload = await OB.App.State.Ticket.Utils.checkAnonymousReturn(
        globalState.Ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkNegativePayments(
        globalState.Ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkExtraPayments(
        globalState.Ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkPrePayments(
        globalState.Ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkOverPayments(
        globalState.Ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkTicketCanceled(
        globalState.Ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkTicketUpdated(
        globalState.Ticket.canceledorder,
        newPayload
      );
      // newPayload = checkNewTicket(globalState.Ticket, newPayload);

      return newPayload;
    }
  );

  // FIXME: Use payload.cancelLayawayAndNew once TicketList is implemented
  // const checkNewTicket = async (ticket, payload) => {
  // if (OB.App.Security.hasPermission('OBPOS_cancelLayawayAndNew')) {
  //   const confirmation = await OB.App.View.DialogUIHandler.askConfirmation({
  //     title: 'OBPOS_cancelLayawayAndNewHeader',
  //     message: 'OBPOS_cancelLayawayAndNewBody'
  //   });
  //   const newPayload = { ...payload };
  //   newPayload.cancelLayawayAndNew = confirmation;
  //   return newPayload;
  // }
  // return payload;
  // };
})();
