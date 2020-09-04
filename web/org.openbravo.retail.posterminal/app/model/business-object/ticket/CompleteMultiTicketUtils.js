/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Complete MultiTicket action
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * Add payments between checked tickets
   *
   * @param {Ticket[]} multiTicketList - Checked TicketList from Pay Open Tickets
   * @param {object} payload - The payload, which include:
   *             * payments - Payments added to be shared
   *             * changePayments - Approvals to add to the ticket
   *             * considerPrepaymentAmount - True by default to consider prePayments
   *             * terminal - Terminal object to use it's attributes
   *             * terminalPayments - Payments of the terminal for amount conversions
   *             * index - Index of payments to continue sharing between tickets (in case not all payments shared)
   *
   * @returns {Ticket[]} The new TicketList but with payments included.
   */

  sharePaymentsBetweenTickets(multiTicketList, payload) {
    const newTicketList = [...multiTicketList];
    const { payments, changePayments } = payload.multiTickets;
    const considerPrepaymentAmount =
      payload.considerPrepaymentAmount != null
        ? payload.considerPrepaymentAmount
        : true;
    let index;
    let ticketListWithPayments = newTicketList.map(function iterateTickets(
      ticket
    ) {
      let newTicket = { ...ticket };
      for (
        index = payload.paymentsIndex || 0;
        index < payments.length;
        index += 1
      ) {
        const payment = payments[index];
        let paymentLine = {};

        if (payment.origAmount) {
          if (newTicketList.indexOf(ticket) === newTicketList.length - 1) {
            // Set isMultiTicketPayment for changePayments
            for (let i = 0; i < changePayments.length; i += 1) {
              const change = changePayments[i];
              change.isMultiTicketPayment = true;
            }
            // Transfer everything
            newTicket.changePayments = changePayments;
            if (index < payments.length) {
              // Pending payments to add
              paymentLine = { ...payment };
              paymentLine.forceAddPayment = true;

              payment.origAmount = OB.DEC.Zero;
              payment.amount = OB.DEC.Zero;
              newTicket = OB.App.State.Ticket.Utils.addPayment(newTicket, {
                ...payload,
                payment: paymentLine
              });
            } else {
              // Finished
              break;
            }
          } else {
            let amountToPay;
            if (newTicket.amountToLayaway != null) {
              amountToPay = newTicket.amountToLayaway;
            } else if (considerPrepaymentAmount) {
              amountToPay = OB.DEC.sub(
                newTicket.obposPrepaymentamt
                  ? newTicket.obposPrepaymentamt
                  : newTicket.grossAmount,
                newTicket.payment
              );
            } else {
              amountToPay = OB.DEC.sub(
                newTicket.grossAmount,
                newTicket.payment
              );
            }
            if (OB.DEC.compare(amountToPay) > 0) {
              const paymentMethod = payload.payments.find(
                p => p.payment.searchKey === payment.kind
              );
              paymentLine = { ...payment };

              if (payment.origAmount <= amountToPay) {
                // Use all the remaining payment amount for this receipt
                payment.origAmount = OB.DEC.Zero;
                payment.amount = OB.DEC.Zero;
                newTicket = OB.App.State.Ticket.Utils.addPayment(newTicket, {
                  ...payload,
                  payment: paymentLine
                });
              } else {
                // Get part of the payment and go with the next ticket
                const amountToPayForeign = OB.DEC.mul(
                  amountToPay,
                  paymentMethod.mulrate,
                  paymentMethod.obposPosprecision
                );
                payment.origAmount = OB.DEC.sub(
                  payment.origAmount,
                  amountToPay
                );
                payment.amount = OB.DEC.sub(payment.amount, amountToPayForeign);

                paymentLine.origAmount = amountToPay;
                paymentLine.amount = amountToPayForeign;
                newTicket = OB.App.State.Ticket.Utils.addPayment(newTicket, {
                  ...payload,
                  payment: paymentLine
                });
                break;
              }
            } else {
              // This ticket is already paid, go to the next ticket
              break;
            }
          }
        }
      }
      return newTicket;
    });

    // If payments are not totally shared between tickets we need to iterate again tickets to add them as overpayment, change...
    if (index < payments.length - 1 && considerPrepaymentAmount) {
      const ticketListPayloadRecursive = { ...payload };
      ticketListPayloadRecursive.paymentsIndex = index;
      ticketListPayloadRecursive.considerPrepaymentAmount = false;
      ticketListWithPayments = this.sharePaymentsBetweenTickets(
        multiTicketList,
        ticketListPayloadRecursive
      );
    }

    return ticketListWithPayments;
  }
});
