/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Payment methos
 */
(function PaymentMethodUtilsDefinition() {
  OB.App.StateAPI.Cashup.registerUtilityFunctions({
    initializePaymentMethodCashup(payload) {
      const {
        terminalPayments,
        lastCashUpPayments,
        terminalIsSlave,
        newCashup
      } = payload;
      const paymentMethods = [];
      terminalPayments.forEach(terminalPayment => {
        let startingCash = terminalPayment.currentBalance;
        let deposits = terminalPayment.payment.totalDeposits;
        let drops = terminalPayment.payment.totalDrops;

        if (lastCashUpPayments && lastCashUpPayments.length > 1) {
          const lastCashUpPayment = lastCashUpPayments.filter(function filter(
            lastCahupPaymentFilter
          ) {
            return (
              lastCahupPaymentFilter.paymentTypeId ===
              terminalPayment.payment.id
            );
          })[0];
          if (lastCashUpPayment != null) {
            // if the last cashup payments are read locally then their structure
            // is different from when reading from the server
            if ('paymentMethod' in lastCashUpPayment) {
              startingCash = lastCashUpPayment.paymentMethod.amountToKeep;
            } else {
              startingCash = lastCashUpPayment.amountToKeep;
            }

            newCashup.totalStartings = OB.DEC.add(
              newCashup.totalStartings,
              startingCash
            );
          }
        }

        if (!deposits) {
          deposits = OB.DEC.Zero;
        }
        if (!drops) {
          drops = OB.DEC.Zero;
        }

        // If payment is active
        if (
          terminalPayment.payment.active === true ||
          (terminalPayment.payment.active === false &&
            deposits !== 0 &&
            drops !== 0)
        ) {
          // Set startingCash to zero on slave terminal and payment method is share or rounding
          if (
            (terminalIsSlave && terminalPayment.paymentMethod.isshared) ||
            terminalPayment.paymentMethod.isRounding
          ) {
            startingCash = OB.DEC.Zero;
          }
          const paymentMethod = {
            id: OB.App.UUID.generate(),
            paymentMethodId: terminalPayment.payment.id,
            searchKey: terminalPayment.payment.searchKey,
            // eslint-disable-next-line no-underscore-dangle
            name: terminalPayment.payment._identifier,
            startingCash,
            totalSales: OB.DEC.Zero,
            totalReturns: OB.DEC.Zero,
            totalDeposits: deposits,
            totalDrops: drops,
            rate: terminalPayment.rate,
            isocode: terminalPayment.isocode,
            lineNo: terminalPayment.lineNo,
            newPaymentMethod: true,
            cashManagements: []
          };
          if (
            terminalPayment.paymentMethod &&
            terminalPayment.paymentMethod.countPerAmount
          ) {
            paymentMethod.countPerAmount = {};
          }
          paymentMethods.push(paymentMethod);
        }
      });
      return paymentMethods;
    },

    getPaymentMethodFromBackendObject(paymentMethodCashUpModel) {
      const paymentMethodInfo = {
        id: paymentMethodCashUpModel.id,
        paymentMethodId: paymentMethodCashUpModel.paymentmethod_id,
        searchKey: paymentMethodCashUpModel.searchKey,
        // eslint-disable-next-line no-underscore-dangle
        name: paymentMethodCashUpModel._identifier,
        startingCash: paymentMethodCashUpModel.startingCash,
        totalSales: paymentMethodCashUpModel.totalSales,
        totalReturns: paymentMethodCashUpModel.totalReturns,
        totalDeposits: paymentMethodCashUpModel.totalDeposits,
        totalDrops: paymentMethodCashUpModel.totalDrops,
        rate: paymentMethodCashUpModel.rate,
        isocode: paymentMethodCashUpModel.isocode,
        lineNo: paymentMethodCashUpModel.lineNo,
        newPaymentMethod: false,
        cashManagements: []
      };
      if (paymentMethodCashUpModel.countPerAmount) {
        paymentMethodInfo.countPerAmount =
          paymentMethodCashUpModel.countPerAmount;
      }
      return paymentMethodInfo;
    },

    addPaymentsFromBackendCashup(payload) {
      const { currentCashupFromBackend, terminalPayments } = payload;
      const paymentMethods = [];

      currentCashupFromBackend.cashPaymentMethodInfo.forEach(
        paymentMethodCashUp => {
          const terminalPayment = terminalPayments.filter(
            terminalPaymentsFilter => {
              return (
                paymentMethodCashUp.paymentmethod_id ===
                terminalPaymentsFilter.payment.id
              );
            }
          )[0];
          if (!terminalPayment) {
            // Payment method not found. This is likely due to the fact that the payment method was disabled.
            return;
          }
          if (
            terminalPayment.payment.active === true ||
            (terminalPayment.payment.active === false &&
              paymentMethodCashUp.totalSales !== 0 &&
              paymentMethodCashUp.totalReturns !== 0 &&
              paymentMethodCashUp.totalDepostis !== 0 &&
              paymentMethodCashUp.totalDrops !== 0)
          ) {
            const paymentFromBackend = OB.App.State.Cashup.Utils.getPaymentMethodFromBackendObject(
              paymentMethodCashUp
            );
            paymentMethods.push(paymentFromBackend);
          }
        }
      );

      return paymentMethods;
    },

    addNewPaymentMethodsToCurrentCashup(payload) {
      const { cashup, terminalPayments } = payload;
      const newCashup = { ...cashup };
      newCashup.cashPaymentMethodInfo = [...newCashup.cashPaymentMethodInfo];

      terminalPayments.forEach(terminalPayment => {
        const startingCash = terminalPayment.currentBalance;
        // get the cashup payment that matches with the payment method of the terminal
        const cashupPayment = cashup.cashPaymentMethodInfo.filter(
          cashupPaymentForFilter => {
            return (
              cashupPaymentForFilter.paymentMethodId ===
              terminalPayment.payment.id
            );
          }
        )[0];
        if (cashupPayment == null && terminalPayment.payment.active === true) {
          // a new payment methods was added in the backend, add it to the pos cashup
          newCashup.cashPaymentMethodInfo.push({
            id: OB.App.UUID.generate(),
            paymentMethodId: terminalPayment.payment.id,
            searchKey: terminalPayment.payment.searchKey,
            // eslint-disable-next-line no-underscore-dangle
            name: terminalPayment.payment._identifier,
            startingCash,
            totalSales: OB.DEC.Zero,
            totalReturns: OB.DEC.Zero,
            totalDeposits: OB.DEC.Zero,
            totalDrops: OB.DEC.Zero,
            rate: terminalPayment.rate,
            isocode: terminalPayment.isocode,
            lineNo: terminalPayment.lineNo,
            newPaymentMethod: true,
            cashManagements: []
          });
          // eslint-disable-next-line no-underscore-dangle
        } else if (cashupPayment.name !== terminalPayment.payment._identifier) {
          // name of a payment method has change in the backend, updating it in the pos cashup
          // eslint-disable-next-line no-underscore-dangle
          cashupPayment.name = terminalPayment.payment._identifier;
        }
      });
      return newCashup;
    },

    /**
     * Retrieves current cash (also foreign currency) of a given paymentMethod
     * @param {Object} paymentMethods - The payment methods from the cashup
     * @param {string} paymentMethodId - The id of the paymentMethod
     * @param {Object[]}   payments - payments available in the terminal
     * @return {Object} A JS Object with the currentCash and foreignCurrentCash
     */
    getPaymentMethodCurrentCash(paymentMethods, paymentMethodId, payments) {
      const paymentMethod = paymentMethods.find(
        payment => payment.paymentMethodId === paymentMethodId
      );
      // (totalDeposits - totalDrops) + startingCash + (totalSales - totalReturns) - Deposits or Drops in Draft
      const cash = OB.DEC.add(
        OB.DEC.sub(paymentMethod.totalDeposits, paymentMethod.totalDrops),
        OB.DEC.add(
          paymentMethod.startingCash,
          OB.DEC.sub(paymentMethod.totalSales, paymentMethod.totalReturns)
        )
      );
      const cashInDraft = paymentMethod.cashManagements.reduce(
        function getCashInDraft(total, cashManagement) {
          if (cashManagement.isDraft) {
            return cashManagement.type === 'deposit'
              ? total + cashManagement.amount
              : total - cashManagement.amount;
          }
          return 0;
        },
        0
      );
      const currentCash = OB.UTIL.currency.toDefaultCurrency(
        payments[paymentMethod.searchKey].paymentMethod.currency,
        OB.DEC.add(cash, cashInDraft)
      );
      const foreignCurrentCash = OB.UTIL.currency.toForeignCurrency(
        payments[paymentMethod.searchKey].paymentMethod.currency,
        currentCash
      );
      return { currentCash, foreignCurrentCash };
    },

    /**
     *   Only send to backend the payments that we have in the terminal.
     *
     *   Could happen that in the cashup we have payments that are not longer
     *   in the backend and if we send them it will fail in the backend
     */
    getCashupPaymentsThatAreAlsoInTerminalPayments(
      cashupPayments,
      terminalPayments
    ) {
      return cashupPayments.filter(cashupPayment =>
        terminalPayments.find(
          terminalPayment =>
            cashupPayment.paymentMethodId === terminalPayment.payment.id
        )
      );
    }
  });
})();
