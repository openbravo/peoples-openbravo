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
  updateAmountToLayaway(order, amount) {
    const { amountToLayaway } = order;
    if (amountToLayaway != null) {
      // eslint-disable-next-line no-param-reassign
      order.amountToLayaway = OB.DEC.sub(amountToLayaway, amount);
    }
  },
  /**
   * Set needed properties when completing a ticket.
   *
   * @param {object} ticket - The ticket being completed
   * @param {object} payload - The calculation payload, which include:
   *             * terminal.id - Terminal id
   *             * approvals - Approvals to add to the ticket
   *
   * @returns {object} The new state of Ticket after being completed.
   */

  setPaymentsToReceipts(
    orderListParams,
    paymentListParams,
    changePayments,
    orderListIndex,
    paymentListIndex,
    considerPrepaymentAmount,
    paymentnames
  ) {
    const orderList = [...orderListParams];
    const paymentList = [...paymentListParams];
    if (
      orderListIndex >= orderList.length ||
      paymentListIndex >= paymentList.length
    ) {
      if (paymentListIndex < paymentList.length && considerPrepaymentAmount) {
        return this.setPaymentsToReceipts(
          orderList,
          paymentList,
          changePayments,
          0,
          paymentListIndex,
          false,
          paymentnames
        );
      }
      // Finished

      return orderList;
    }

    let order = orderList[orderListIndex];
    const payment = paymentList[paymentListIndex];
    let paymentLine = {};
    const me = this;
    if (payment.origAmount) {
      const addPaymentLine = function addPaymentLine(
        pymntLine,
        pymnt,
        addPaymentCallback
      ) {
        const prevChange = order.change;
        // FIXME
        order = OB.App.State.Ticket.Utils.generatePayment(order, {
          payment: paymentLine,
          terminal: { paymentTypes: Object.values(paymentnames) }
        });

        // Recalculate payment and paymentWithSign properties
        const paidAmt = order.payments.reduce((total, p) => {
          if (p.isPrePayment || p.isReversePayment || !order.isNegative) {
            return OB.DEC.add(total, p.origAmount);
          }
          return OB.DEC.sub(total, p.origAmount);
        }, OB.DEC.Zero);
        order.payment = OB.DEC.abs(paidAmt);
        order.paymentWithSign = paidAmt;
        order.change = prevChange;
        orderList[orderListIndex] = order;
        // order.addPayment(paymentLine, function addPayment() {
        me.updateAmountToLayaway(order, paymentLine.origAmount);
        if (addPaymentCallback instanceof Function) {
          addPaymentCallback();
        }
        // });
      };

      if (
        orderListIndex === orderList.length - 1 &&
        !considerPrepaymentAmount
      ) {
        // Transfer everything
        order.changePayments = changePayments;
        if (paymentListIndex < paymentList.length) {
          // Pending payments to add
          paymentLine = { ...payment };
          paymentLine.forceAddPayment = true;

          payment.origAmount = OB.DEC.Zero;
          payment.amount = OB.DEC.Zero;
          addPaymentLine(
            paymentLine,
            payment,
            function addPaymentLineCallback() {
              return me.setPaymentsToReceipts(
                orderList,
                paymentList,
                changePayments,
                orderListIndex,
                paymentListIndex + 1,
                considerPrepaymentAmount,
                paymentnames
              );
            }
          );
        } else {
          // Finished
          return orderList;
        }
      } else {
        let amountToPay;
        if (order.amountToLayaway != null) {
          amountToPay = order.amountToLayaway;
        } else if (considerPrepaymentAmount) {
          amountToPay = OB.DEC.sub(
            order.obposPrepaymentamt ? order.obposPrepaymentamt : order.gross,
            order.payment
          );
        } else {
          amountToPay = OB.DEC.sub(order.gross, order.payment);
        }
        if (OB.DEC.compare(amountToPay) > 0) {
          const paymentMethod = paymentnames[payment.kind];
          paymentLine = { ...payment };

          if (payment.origAmount <= amountToPay) {
            // Use all the remaining payment amount for this receipt
            payment.origAmount = OB.DEC.Zero;
            payment.amount = OB.DEC.Zero;
            addPaymentLine(paymentLine, payment, function recursiveCallback() {
              return me.setPaymentsToReceipts(
                orderList,
                paymentList,
                changePayments,
                orderListIndex,
                paymentListIndex + 1,
                considerPrepaymentAmount,
                paymentnames
              );
            });
          } else {
            // Get part of the payment and go with the next order
            const amountToPayForeign = OB.DEC.mul(
              amountToPay,
              paymentMethod.mulrate,
              paymentMethod.obposPosprecision
            );
            payment.origAmount = OB.DEC.sub(payment.origAmount, amountToPay);
            payment.amount = OB.DEC.sub(payment.amount, amountToPayForeign);

            paymentLine.origAmount = amountToPay;
            paymentLine.amount = amountToPayForeign;

            addPaymentLine(paymentLine, payment, function recursiveCallback() {
              return me.setPaymentsToReceipts(
                orderList,
                paymentList,
                changePayments,
                orderListIndex + 1,
                paymentListIndex,
                considerPrepaymentAmount,
                paymentnames
              );
            });
          }
        } else {
          // This order is already paid, go to the next order
          return me.setPaymentsToReceipts(
            orderList,
            paymentList,
            changePayments,
            orderListIndex + 1,
            paymentListIndex,
            considerPrepaymentAmount,
            paymentnames
          );
        }
      }
    } else {
      return me.setPaymentsToReceipts(
        orderList,
        paymentList,
        changePayments,
        orderListIndex,
        paymentListIndex + 1,
        considerPrepaymentAmount,
        paymentnames
      );
    }
    return orderList;
  }
});
