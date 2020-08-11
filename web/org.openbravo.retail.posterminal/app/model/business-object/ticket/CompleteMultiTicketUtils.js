/* eslint-disable no-param-reassign */
/* eslint-disable no-loop-func */
/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Complete Ticket action
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * Set needed properties when completing a ticket.
   *
   * @param {Ticket[]} ticketList - The ticket being completed
   * @param {object} object - The calculation payload, which include:
   *             * payments - Terminal id
   *             * changePayments - Approvals to add to the ticket
   *             * considerPrepaymentAmount - Approvals to add to the ticket
   *             * terminalPayments - Approvals to add to the ticket
   *             * index -
   *
   * @returns {Ticket[]} The new TicketList but with payments included.
   */
  addPaymentLine(newTicket, paymentLine, terminalPayments) {
    let newTicketWithPayment = { ...newTicket };
    const prevChange = newTicket.change;
    newTicketWithPayment = OB.App.State.Ticket.Utils.generatePayment(
      newTicket,
      {
        payment: paymentLine,
        terminal: { paymentTypes: Object.values(terminalPayments) }
      }
    );

    // Recalculate payment and paymentWithSign properties
    const paidAmt = newTicket.payments.reduce((total, p) => {
      if (p.isPrePayment || p.isReversePayment || !newTicket.isNegative) {
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
        paymentLine.get('origAmount')
      );
    }
    return newTicketWithPayment;
  },

  /**
   * Set needed properties when completing a ticket.
   *
   * @param {Ticket[]} ticketList - The ticket being completed
   * @param {object} object - The calculation payload, which include:
   *             * payments - Terminal id
   *             * changePayments - Approvals to add to the ticket
   *             * considerPrepaymentAmount - Approvals to add to the ticket
   *             * terminalPayments - Approvals to add to the ticket
   *             * index -
   *
   * @returns {Ticket[]} The new TicketList but with payments included.
   */

  setPaymentsToReceipts(ticketList, ticketListPayload) {
    const newTicketList = [...ticketList];
    const {
      payments,
      changePayments,
      considerPrepaymentAmount,
      terminalPayments
    } = ticketListPayload;
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
                  : newTicket.gross,
                newTicket.payment
              );
            } else {
              amountToPay = OB.DEC.sub(newTicket.gross, newTicket.payment);
            }
            if (OB.DEC.compare(amountToPay) > 0) {
              const paymentMethod = terminalPayments[payment.kind];
              paymentLine = { ...payment };

              if (payment.origAmount <= amountToPay) {
                // Use all the remaining payment amount for this receipt
                payment.origAmount = OB.DEC.Zero;
                payment.amount = OB.DEC.Zero;
                newTicket = me.addPaymentLine(
                  newTicket,
                  paymentLine,
                  terminalPayments
                );
              } else {
                // Get part of the payment and go with the next order
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
                  terminalPayments
                );
                break;
              }
            } else {
              // This order is already paid, go to the next order
              break;
            }
          }
        }
      }
      return newTicket;
    });

    if (index < payments.length && considerPrepaymentAmount) {
      ticketListPayload.paymentsIndex = index;
      ticketListPayload.considerPrepaymentAmount = false;
      this.setPaymentsToReceipts(ticketList, ticketListPayload);
    }

    return ticketListWithPayments;
  }
});
