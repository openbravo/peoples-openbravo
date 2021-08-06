/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Add Payment action
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * Checks if we already paid the whole ticket
   */
  async checkAlreadyPaid(ticket, payload) {
    const prePaymentAmount = ticket.payments
      .filter(function prePaymentFilter(payment) {
        return payment.isPrePayment;
      })
      .reduce((memo, pymnt) => {
        return OB.DEC.add(
          memo,
          OB.DEC.sub(pymnt.origAmount, pymnt.overpayment || 0)
        );
      }, OB.DEC.Zero);
    const isNewReversed =
      ticket.payments.find(function newReversedFind(payment) {
        return !payment.isPrePayment && payment.isReversePayment;
      }) !== undefined;
    if (
      ticket.isPaid &&
      !payload.payment.isReversePayment &&
      OB.DEC.abs(prePaymentAmount) >= OB.DEC.abs(ticket.grossAmount) &&
      !isNewReversed
    ) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_CannotIntroducePayment'
      });
    }

    return payload;
  },
  /**
   * Checks if amount of the payment is a number
   */
  async checkNotNumberAmount(ticket, payload) {
    if (!OB.DEC.isNumber(payload.payment.amount)) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_MsgPaymentAmountError'
      });
    }

    return payload;
  },
  /**
   * Checks if some of the payments have a condition that do not allow us to continue
   */
  async checkStopAddingPayments(ticket, payload) {
    if (ticket.stopAddingPayments) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_CannotAddPayments'
      });
    }

    return payload;
  },
  /**
   * Checks if ticket is paid with exact amount
   */
  async checkExactPaid(ticket, payload) {
    let pending;
    if (ticket.prepaymentChangeMode) {
      const paymentsAmt = ticket
        .get('payments')
        .reduce(function paymentsReducer(memo, payment) {
          return OB.DEC.add(memo, payment.origAmount);
        }, OB.DEC.Zero);
      pending = OB.DEC.abs(OB.DEC.sub(ticket.grossAmount, paymentsAmt));
    } else {
      pending = OB.DEC.abs(
        OB.DEC.sub(ticket.grossAmount, ticket.paymentWithSign)
      );
    }

    if (
      !payload.payment.isReversePayment &&
      pending <= 0 &&
      payload.payment.amount > 0 &&
      !payload.payment.forceAddPayment
    ) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_PaymentsExact'
      });
    }

    return payload;
  },
  /**
   * Checks if ticket is a void Layaway
   */
  async checkVoidLayaway(ticket, payload) {
    if (ticket.orderType === 3 && ticket.grossAmount === 0) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_MsgVoidLayawayPaymentError'
      });
    }

    return payload;
  },
  /**
   * Checks if ticket is a void Layaway
   */
  async managePrePaymentChange(ticket, payload) {
    let newPayload = { ...payload };
    const { payment, terminal } = newPayload;

    const generatePrepaymentChange = OB.App.Security.hasPermission(
      'OBPOS_GenerateChangeWithPrepayments'
    );
    const calculatePrepayments = terminal.terminalType.calculateprepayments;
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      newPayload
    );
    // Check configuration: Show popup just when preference and terminal are configured
    if (calculatePrepayments && generatePrepaymentChange) {
      // Check if change is generated taking into account prePayment Amount (by default, deliverable lines)
      if (
        !paymentStatus.isNegative &&
        payment.isCash &&
        ticket.paymentWithSign < ticket.obposPrepaymentamt
      ) {
        const newPaidAmount = OB.DEC.add(
          ticket.paymentWithSign,
          OB.DEC.div(payment.amount, payment.mulrate || 1)
        );

        if (
          newPaidAmount > ticket.obposPrepaymentamt &&
          newPaidAmount < ticket.grossAmount
        ) {
          const userResponse = await OB.App.View.DialogUIHandler.inputData(
            'modalDeliveryChange',
            {
              payload: newPayload,
              deliveryChange: OB.DEC.sub(
                newPaidAmount,
                ticket.obposPrepaymentamt
              )
            }
          );
          newPayload = userResponse.payload;
        }
      }
    }

    return newPayload;
  },
  getPrecision(payment, paymentTypes) {
    const terminalPayment = paymentTypes.find(
      p => p.payment.searchKey === payment.kind
    );
    return terminalPayment
      ? terminalPayment.obposPosprecision
      : OB.DEC.getScale();
  },
  /**
   * Generates the corresponding payment for the given ticket.
   *
   * @param {object} ticket - The ticket where payment will be added
   * @param {object} payload - The calculation payload, which include:
   *             * payment - The payment that will be added to the ticket
   *             * terminal.id - Terminal id
   *             * payments - Terminal payment types
   *
   * @returns {Ticket} The new state of Ticket after payment generation.
   */
  addPaymentRounding(ticket, payload) {
    const newTicket = { ...ticket };
    const { payments, payment } = payload;
    const terminalPayment = payments.find(
      p => p.paymentRounding && p.payment.searchKey === payment.kind
    );

    if (terminalPayment !== undefined && !payment.isReversePayment) {
      const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
        newTicket,
        payload
      );
      paymentStatus.pendingAmt = OB.DEC.mul(
        paymentStatus.pendingAmt,
        payment.mulrate
      );
      let roundingAmount = OB.DEC.sub(paymentStatus.pendingAmt, payment.amount);
      const terminalPaymentRounding = payments.find(
        p =>
          p.payment.searchKey ===
          terminalPayment.paymentRounding.paymentRoundingType
      );
      let amountDifference = null;
      const precision = this.getPrecision(payment, payments);
      const multiplyBy = paymentStatus.isReturn
        ? terminalPayment.paymentRounding.returnRoundingMultiple
        : terminalPayment.paymentRounding.saleRoundingMultiple;
      const rounding = paymentStatus.isReturn
        ? terminalPayment.paymentRounding.returnRoundingMode
        : terminalPayment.paymentRounding.saleRoundingMode;
      const roundingEnabled = paymentStatus.isReturn
        ? terminalPayment.paymentRounding.returnRounding
        : terminalPayment.paymentRounding.saleRounding;
      const pow = 10 ** precision;
      let paymentDifference =
        OB.DEC.mul(paymentStatus.pendingAmt, pow) % OB.DEC.mul(multiplyBy, pow);

      // If receipt is totally paid the last payment paid amount is used to compute the
      // rounding amount
      if (paymentStatus.pendingAmt === 0) {
        paymentDifference =
          OB.DEC.mul(payment.paid, pow) % OB.DEC.mul(multiplyBy, pow);
      }

      // If receipt total amount is less than equal rounding multiple in Sales/Return
      // no rounding payment line is created
      // (Rounding enabled for Sales/Returns) &&
      // ((the remaining to paid is less than rounding multiple in Sales/Return) ||
      // (the paid amount is less than rounding multiple in Sales/Return with a rounding difference) ||
      // (the payment totally paid the receipt or create an overpayment with a rounding difference))
      if (
        roundingEnabled &&
        ((roundingAmount !== 0 && OB.DEC.abs(roundingAmount) < multiplyBy) ||
          (payment.paid !== 0 &&
            paymentDifference !== 0 &&
            paymentStatus.pendingAmt < multiplyBy) ||
          (payment.paid === 0 &&
            paymentDifference !== 0 &&
            payment.amount >= paymentStatus.pendingAmt &&
            payment.amount >= multiplyBy))
      ) {
        if (rounding === 'UR') {
          if (paymentDifference !== 0) {
            amountDifference = OB.DEC.sub(
              multiplyBy,
              OB.DEC.div(paymentDifference, pow)
            );
            roundingAmount = OB.DEC.mul(amountDifference, -1);
            if (payment.amount < paymentStatus.pendingAmt) {
              amountDifference = OB.DEC.add(
                amountDifference,
                OB.DEC.div(paymentDifference, pow)
              );
            }
          }
          if (payment.amount <= paymentStatus.pendingAmt) {
            payment.amount = OB.DEC.add(payment.amount, amountDifference);
          }
        } else if (
          paymentDifference !== 0 &&
          payment.amount >= paymentStatus.pendingAmt
        ) {
          roundingAmount = OB.DEC.div(paymentDifference, pow);
          // Substract the rounding amount when the payment totally paid the receipt
          if (payment.amount === paymentStatus.pendingAmt) {
            payment.amount = OB.DEC.sub(payment.amount, roundingAmount);
          }
        }

        payment.index = payments.length;
        // Create the rounding payment line

        const newPayment = {};

        newPayment.kind = terminalPayment.paymentRounding.paymentRoundingType;
        newPayment.name = terminalPaymentRounding.payment.commercialName;
        newPayment.amount = roundingAmount;
        newPayment.origAmount = OB.DEC.div(
          newPayment.amount,
          terminalPaymentRounding.mulrate
        );
        newPayment.rate = terminalPaymentRounding.rate;
        newPayment.mulrate = terminalPaymentRounding.mulrate;
        newPayment.mulrate = terminalPaymentRounding.mulrate;
        newPayment.isocode = terminalPaymentRounding.isocode;
        newPayment.isCash = terminalPaymentRounding.paymentMethod.iscash;
        newPayment.allowOpenDrawer =
          terminalPaymentRounding.paymentMethod.allowOpenDrawer;
        newPayment.openDrawer =
          terminalPaymentRounding.paymentMethod.openDrawer;
        newPayment.printtwice =
          terminalPaymentRounding.paymentMethod.printtwice;
        newPayment.date = new Date();
        newPayment.id = OB.App.UUID.generate();
        newPayment.oBPOSPOSTerminal = payload.terminal.id;
        newPayment.orderGross = newTicket.grossAmount;
        newPayment.isPaid = newTicket.isPaid;
        newPayment.isReturnOrder = newTicket.isNegative;
        newPayment.paymentRounding = true;
        newPayment.roundedPaymentId = payment.id;
        newPayment.paid = newPayment.origAmount;
        newPayment.precision = precision;

        newTicket.payments = [...newTicket.payments, newPayment];
      }
    }
    return newTicket;
  },
  /**
   * Generates the corresponding payment for the given ticket.
   *
   * @param {object} ticket - The ticket where payment will be added
   * @param {object} payload - The calculation payload, which include:
   *             * payment - The payment that will be added to the ticket
   *             * terminal.id - Terminal id
   *             * payments - Terminal payment types
   *
   * @returns {Ticket} The new state of Ticket after payment generation.
   */
  addPayment(ticket, payload) {
    const newTicket = { ...ticket };

    const terminalPayment = payload.payments.find(
      paymentType => paymentType.payment.searchKey === payload.payment.kind
    );
    const precision = terminalPayment
      ? terminalPayment.obposPosprecision
      : OB.DEC.getScale();

    const countPerAmount =
      terminalPayment &&
      terminalPayment.paymentMethod &&
      terminalPayment.paymentMethod.countPerAmount;

    const isMergeable = ({ payment }) => {
      // The payment is not included in the nonmergeable ids array
      if (
        payload.payment.paymentData &&
        payload.payment.paymentData.nonmergeable
      ) {
        return payload.payment.paymentData.nonmergeable.every(
          id => id !== payment.id
        );
      }
      return true;
    };

    const paymentIndex = newTicket.payments.findIndex(
      payment =>
        isMergeable({ payment }) &&
        payment.kind === payload.payment.kind &&
        !payment.isPrePayment &&
        !payment.reversedPaymentId &&
        !payload.payment.reversedPaymentId &&
        (!payload.payment.paymentData ||
          payload.payment.paymentData.mergeable ||
          (payment.paymentData &&
            payload.payment.paymentData &&
            payment.paymentData.groupingCriteria &&
            payload.payment.paymentData.groupingCriteria &&
            payment.paymentData.groupingCriteria ===
              payload.payment.paymentData.groupingCriteria))
    );

    if (paymentIndex >= 0) {
      newTicket.payments = newTicket.payments.map((payment, index) => {
        // In rounding payments, we need to save the id of the payment we are rounding
        const roundingPayment = newTicket.payments.find(
          p => p.paymentRounding && !p.roundedPaymentId
        );
        if (newTicket.payments.indexOf(roundingPayment) === index) {
          const newPayment = { ...payment };
          newPayment.roundedPaymentId = newTicket.payments[paymentIndex].id;
          return newPayment;
        }
        if (index !== paymentIndex) {
          return payment;
        }

        let newPayment = { ...payment };
        // Set values defined in actionPreparations
        if (payload.payment.extraInfo) {
          newPayment = { ...newPayment, ...payload.payment.extraInfo };
        }
        newPayment.oBPOSPOSTerminal = payload.terminal.id;
        const newAmount = OB.DEC.add(
          OB.DEC.mul(
            payload.payment.amount,
            newPayment.signChanged && newPayment.amount < 0 ? -1 : 1,
            precision
          ),
          newPayment.amount,
          precision
        );
        newPayment.amount = newAmount;
        newPayment.origAmount =
          newPayment.rate && newPayment.rate !== '1'
            ? OB.DEC.div(newAmount, newPayment.mulrate)
            : newAmount;
        newPayment.paid = newPayment.origAmount;
        newPayment.precision = precision;
        // Save the payment which is rounding current payment
        if (roundingPayment) {
          newPayment.paymentRoundingLine = roundingPayment;
        }

        if (countPerAmount) {
          const key = OB.App.Locale.formatAmount(payload.payment.amount);
          newPayment.countPerAmount = { ...payment.countPerAmount };
          const currentCount = newPayment.countPerAmount[key] || 0;
          newPayment.countPerAmount[key] = currentCount + 1;
        }

        return newPayment;
      });
    } else {
      let newPayment = { ...payload.payment };
      // Set values defined in actionPreparations
      if (payload.payment.extraInfo) {
        newPayment = { ...newPayment, ...payload.payment.extraInfo };
        delete newPayment.extraInfo;
      }
      newPayment.date = new Date();
      newPayment.id = OB.App.UUID.generate();
      newPayment.oBPOSPOSTerminal = payload.terminal.id;
      newPayment.orderGross = newTicket.grossAmount;
      newPayment.isPaid = newTicket.isPaid;
      newPayment.isReturnOrder = newTicket.isNegative;
      newPayment.paid = newPayment.origAmount;
      newPayment.precision = precision;
      newPayment.cancelAndReplace =
        (newTicket.doCancelAndReplace && newTicket.replacedordernewTicket) ||
        newTicket.cancelAndReplaceChangePending;
      if (newPayment.reversedPayment) {
        newPayment.reversedPayment = {
          ...newPayment.reversedPayment,
          isReversed: true
        };
        newTicket.payments = newTicket.payments.map(payment => {
          if (payment.paymentId === newPayment.reversedPayment.paymentId) {
            return { ...payment, isReversed: true };
          }
          return payment;
        });
      }
      if (newPayment.paymentRoundingLine) {
        newPayment.paymentRoundingLine.roundedPaymentId = newPayment.id;
      }
      if (
        newPayment.openDrawer &&
        (newPayment.allowOpenDrawer || newPayment.isCash)
      ) {
        newTicket.openDrawer = newPayment.openDrawer;
      }

      // if new payment is a reverse payment, add it after reversed payment. If not, add it at the end of the array
      let index = newPayment.isReversePayment
        ? newTicket.payments.indexOf(
            newTicket.payments.find(
              p => p.paymentId === newPayment.reversedPaymentId
            )
          ) + 1
        : newTicket.payments.length;
      newTicket.payments = [...newTicket.payments];
      const roundingPayment = newTicket.payments.find(
        p => p.paymentRounding && !p.roundedPaymentId
      );
      if (roundingPayment) {
        // if new payment has a rounding payment, add new payment before rounding payment. If not, add it at the end of the array
        index = roundingPayment
          ? newTicket.payments.indexOf(roundingPayment)
          : index;
        // In rounding payments, we need to save the id of the payment we are rounding
        roundingPayment.roundedPaymentId = newPayment.id;
        // Save the payment which is rounding current payment
        newPayment.paymentRoundingLine = roundingPayment;
      }

      if (countPerAmount) {
        const key = OB.App.Locale.formatAmount(payload.payment.amount);
        newPayment.countPerAmount = { [key]: 1 };
      }

      newTicket.payments.splice(index, 0, newPayment);
    }

    const paidAmt = newTicket.payments.reduce((total, p) => {
      if (p.isPrePayment || p.isReversePayment || !newTicket.isNegative) {
        return OB.DEC.add(total, p.origAmount);
      }
      return OB.DEC.sub(total, p.origAmount);
    }, OB.DEC.Zero);
    newTicket.payment = OB.DEC.abs(paidAmt);
    newTicket.paymentWithSign = paidAmt;
    if (newTicket.amountToLayaway != null) {
      newTicket.amountToLayaway = OB.DEC.sub(
        newTicket.amountToLayaway,
        payload.payment.origAmount
      );
    }

    return newTicket;
  },

  /**
   * Fill all necessary payment properties
   */
  async fillPayment(ticket, payload) {
    const newPayload = { ...payload };
    const paymentInfo = newPayload.payments.find(
      payments => payments.payment.searchKey === newPayload.payment.kind
    );
    const newPayment = { ...payload.payment };
    newPayment.allowOpenDrawer = newPayment.allowOpenDrawer
      ? newPayment.allowOpenDrawer
      : paymentInfo.paymentMethod.allowopendrawer;
    newPayment.date = newPayment.date ? newPayment.date : null;
    newPayment.isCash = newPayment.isCash
      ? newPayment.isCash
      : paymentInfo.paymentMethod.iscash;
    newPayment.isocode = newPayment.isocode
      ? newPayment.isocode
      : paymentInfo.isocode;
    newPayment.mulrate = newPayment.mulrate
      ? newPayment.mulrate
      : paymentInfo.mulrate;
    newPayment.name = newPayment.name
      ? newPayment.name
      : // eslint-disable-next-line no-underscore-dangle
        paymentInfo.paymentMethod._identifier;
    newPayment.openDrawer = newPayment.openDrawer
      ? newPayment.openDrawer
      : paymentInfo.paymentMethod.openDrawer;
    newPayment.origAmount = newPayment.origAmount ? newPayment.origAmount : 0;
    newPayment.paid = newPayment.paid ? newPayment.paid : 0;
    newPayment.printtwice = newPayment.printtwice
      ? newPayment.printtwice
      : paymentInfo.paymentMethod.printtwice;
    newPayment.rate = newPayment.rate ? newPayment.rate : paymentInfo.rate;

    newPayload.payment = newPayment;
    return newPayload;
  }
});
