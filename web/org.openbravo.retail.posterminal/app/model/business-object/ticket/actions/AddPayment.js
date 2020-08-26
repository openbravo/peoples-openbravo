/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to add a payment to the ticket
 * @author Miguel de Juana <miguel.dejuana@openbravo.com>
 */

(function AddPaymentDefinition() {
  OB.App.StateAPI.Ticket.registerAction('addPayment', (ticket, payload) => {
    let newTicket = { ...ticket };

    // newTicket = OB.App.State.Ticket.Utils.addPayment(newTicket, payload);
    // if modalDeliveryChange has been shown
    if (
      Object.prototype.hasOwnProperty.call(payload, 'prepaymentChangeMode') &&
      Object.prototype.hasOwnProperty.call(payload, 'prepaymentChangeAmt')
    ) {
      newTicket.prepaymentChangeMode = payload.prepaymentChangeMode;
      newTicket.prepaymentChangeAmt = payload.prepaymentChangeAmt;
    }

    newTicket = OB.App.State.Ticket.Utils.addPayment(newTicket, payload);
    return newTicket;
  });

  OB.App.StateAPI.Ticket.addPayment.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };

      newPayload = await OB.App.State.Ticket.Utils.checkAlreadyPaid(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkNotNumberAmount(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkStopAddingPayments(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkExactPaid(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.checkVoidLayaway(
        ticket,
        newPayload
      );
      newPayload = await OB.App.State.Ticket.Utils.managePrePaymentChange(
        ticket,
        newPayload
      );

      return newPayload;
    }
  );
})();
