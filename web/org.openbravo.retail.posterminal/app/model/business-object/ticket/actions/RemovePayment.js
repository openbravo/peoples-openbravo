/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global */

(function RemovePaymentDefinition() {
  OB.App.StateAPI.Ticket.registerAction('removePayment', (ticket, payload) => {
    const newTicket = { ...ticket };
    const { paymentIds } = payload;

    if (ticket.prepaymentChangeMode) {
      newTicket.prepaymentChangeMode = false;
      // In old implementation we would adjust the payments here also, but it is not
      // required because the previous hook execution should already have done it
    }

    // First we remove the payments specified in the payload
    newTicket.payments = ticket.payments.filter(
      payment =>
        !paymentIds.find(pId => payment.id === pId || payment.paymentId === pId)
    );

    const removedPayments = ticket.payments.filter(
      payment => !newTicket.payments.includes(payment)
    );

    // Then we remove the payments which are rounding lines for some removed payment
    newTicket.payments = newTicket.payments.filter(
      payment =>
        !removedPayments.find(
          removedPayment =>
            (removedPayment.paymentRoundingLine === payment.id && payment.id) ||
            (removedPayment.paymentRoundingLine === payment.paymentId &&
              payment.paymentId)
          // sometimes payment.id is null, payment should be ignored in this case
        )
    );

    // We add the deleted payments to the deletedPayments array if it exists
    if (newTicket.deletedPayments) {
      newTicket.deletedPayments = [
        ...newTicket.deletedPayments,
        ...removedPayments
      ];
    }

    // And finally if a removed payment reversed another payment, we need to remove the isReversed attribute from it
    newTicket.payments = newTicket.payments.map(payment => {
      const reversingPayment = removedPayments.find(
        removedPayment =>
          (removedPayment.reversedPaymentId === payment.id && payment.id) || // sometimes payment.id is null, payment should be ignored in this case
          (removedPayment.reversedPaymentId === payment.paymentId &&
            payment.paymentId)
      );
      if (reversingPayment) {
        return { ...payment, isReversed: false };
      }
      return payment;
    });

    return newTicket;
  });
})();
