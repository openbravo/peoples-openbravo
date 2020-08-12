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
   * Add a payment to a ticket
   *
   * @param {Ticket[]} ticketList - The ticket to add the payment
   * @param {object} paymentLine - The payment to be added
   * @param {object} terminal - Terminal object to use it's attributes
   * @param {object[]} terminalPayments - Payments of the terminal for amount conversions
   *
   * @returns {Ticket} The ticket with the payment added
   */
  addPaymentLine(newTicket, paymentLine, terminal, terminalPayments) {
    let newTicketWithPayment = { ...newTicket };
    const prevChange = newTicketWithPayment.change;
    newTicketWithPayment = OB.App.State.Ticket.Utils.generatePayment(
      newTicketWithPayment,
      {
        payment: paymentLine,
        terminal,
        payments: terminalPayments
      }
    );
    // Recalculate payment and paymentWithSign properties
    const paidAmt = newTicketWithPayment.payments.reduce((total, p) => {
      if (
        p.isPrePayment ||
        p.isReversePayment ||
        !newTicketWithPayment.isNegative
      ) {
        return OB.DEC.add(total, p.origAmount);
      }
      return OB.DEC.sub(total, p.origAmount);
    }, OB.DEC.Zero);
    newTicketWithPayment.payment = OB.DEC.abs(paidAmt);
    newTicketWithPayment.paymentWithSign = paidAmt;
    newTicketWithPayment.change = prevChange;
    if (newTicketWithPayment.amountToLayaway != null) {
      newTicketWithPayment.amountToLayaway = OB.DEC.sub(
        newTicketWithPayment.amountToLayaway,
        paymentLine.origAmount
      );
    }
    return newTicketWithPayment;
  },

  /**
   * Add payments between checked tickets
   *
   * @param {Ticket[]} checkedTicketList - Checked TicketList from Pay Open Tickets
   * @param {object} ticketListPayload - The payload, which include:
   *             * payments - Payments added to be shared
   *             * changePayments - Approvals to add to the ticket
   *             * considerPrepaymentAmount - True by default to consider prePayments
   *             * terminal - Terminal object to use it's attributes
   *             * terminalPayments - Payments of the terminal for amount conversions
   *             * index - Index of payments to continue sharing between tickets (in case not all payments shared)
   *
   * @returns {Ticket[]} The new TicketList but with payments included.
   */

  setPaymentsToReceipts(checkedTicketList, ticketListPayload) {
    const newTicketList = [...checkedTicketList];
    const { payments, changePayments } = ticketListPayload.multiTickets;
    const considerPrepaymentAmount =
      ticketListPayload.considerPrepaymentAmount || true;
    const { terminal } = ticketListPayload;
    const terminalPayments = ticketListPayload.payments;
    const me = this;
    let index;
    const ticketListWithPayments = newTicketList.map(function iterateTickets(
      ticket
    ) {
      let newTicket = { ...ticket };
      for (
        index = ticketListPayload.paymentsIndex || 0;
        index < payments.length;
        index += 1
      ) {
        const payment = payments[index];
        let paymentLine = {};

        if (payment.origAmount) {
          if (
            newTicketList.indexOf(newTicket) === newTicketList.length - 1 &&
            !considerPrepaymentAmount
          ) {
            // Transfer everything
            newTicket.changePayments = changePayments;
            if (index < payments.length) {
              // Pending payments to add
              paymentLine = { ...payment };
              paymentLine.forceAddPayment = true;

              payment.origAmount = OB.DEC.Zero;
              payment.amount = OB.DEC.Zero;
              newTicket = me.addPaymentLine(
                newTicket,
                paymentLine,
                terminal,
                terminalPayments
              );
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
              const paymentMethod = terminalPayments.find(
                p => p.payment.searchKey === payment.kind
              );
              paymentLine = { ...payment };

              if (payment.origAmount <= amountToPay) {
                // Use all the remaining payment amount for this receipt
                payment.origAmount = OB.DEC.Zero;
                payment.amount = OB.DEC.Zero;
                newTicket = me.addPaymentLine(
                  newTicket,
                  paymentLine,
                  terminal,
                  terminalPayments
                );
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
                newTicket = me.addPaymentLine(
                  newTicket,
                  paymentLine,
                  terminal,
                  terminalPayments
                );
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
    if (index < payments.length && considerPrepaymentAmount) {
      const ticketListPayloadRecursive = { ...ticketListPayload };
      ticketListPayloadRecursive.paymentsIndex = index;
      ticketListPayloadRecursive.considerPrepaymentAmount = false;
      this.setPaymentsToReceipts(checkedTicketList, ticketListPayloadRecursive);
    }

    return ticketListWithPayments;
  }
});
