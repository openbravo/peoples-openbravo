/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Cashup Model
 */
(function CashupUtilsDefinition() {
  OB.App.StateAPI.Cashup.registerUtilityFunctions({
    isValidTheLocalCashup(cashup) {
      return cashup.id != null;
    },

    async requestNoProcessedCashupFromBackend() {
      const response = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.master.Cashup',
        { isprocessed: 'N', isprocessedbo: 'N' }
      );
      return response.response.data;
    },

    isValidTheBackendCashup(data) {
      if (data && data.exception) {
        throw new Error(data.exception);
      } else if (data && _.isArray(data) && data.length > 0 && data[0]) {
        return true;
      } else {
        return false;
      }
    },

    async requestProcessedCashupFromBackend() {
      const response = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.master.Cashup',
        { isprocessed: 'Y' }
      );
      return response.response.data;
    },

    showPopupNotEnoughDataInCache() {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Error'),
        OB.I18N.getLabel('OBMOBC_NotEnoughDataInCache') +
          OB.I18N.getLabel('OBMOBC_LoadingErrorBody'),
        [
          {
            label: OB.I18N.getLabel('OBMOBC_Reload'),
            action() {
              window.location.reload();
            }
          }
        ],
        {
          onShowFunction(popup) {
            popup.$.headerCloseButton.hide();
          },
          autoDismiss: false
        }
      );
    },

    resetStatistics() {
      // TODO: modify state instead run methods
      // TODO !!!!!!!!!!!
      // OB.UTIL.localStorage.setItem('transitionsToOnline', 0);
      // OB.UTIL.resetNetworkInformation();
      // OB.UTIL.resetNumberOfLogClientErrors();
      // TODO !!!!!!!!!!!
    },

    initializePaymentMethodCashup(payload) {
      const { terminalPayments, lastCashUpPayments, terminalIsSlave } = payload;
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
            if (_.isObject(lastCashUpPayment.paymentMethod)) {
              startingCash = lastCashUpPayment.paymentMethod.amountToKeep;
            } else {
              startingCash = lastCashUpPayment.amountToKeep;
            }
          }
        }

        if (!deposits !== 'local') {
          deposits = OB.DEC.Zero;
        }
        if (!drops !== 'local') {
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
          paymentMethods.push({
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
          });
        }
      });
      return paymentMethods;
    },

    getTaxesFromBackendObject(backendTaxes) {
      const taxes = [];
      backendTaxes.forEach(backendTax => {
        taxes.push({
          id: backendTax.id,
          orderType: backendTax.orderType,
          name: backendTax.name,
          amount: backendTax.amount
        });
      });
      return taxes;
    },

    getPaymentMethodFromBackendObject(paymentMethodCashUpModel) {
      return {
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
    },

    createNewCashupFromScratch(payload) {
      const { cashup } = payload;
      const newCashup = { ...cashup };

      newCashup.id = OB.App.UUID.generate();
      newCashup.netSales = OB.DEC.Zero;
      newCashup.grossSales = OB.DEC.Zero;
      newCashup.netReturns = OB.DEC.Zero;
      newCashup.grossReturns = OB.DEC.Zero;
      newCashup.totalRetailTransactions = OB.DEC.Zero;
      newCashup.creationDate = payload.payload.currentDate.toISOString();
      newCashup.userId = payload.payload.userId;
      newCashup.posterminal = payload.payload.posterminal;
      newCashup.isprocessed = false;
      newCashup.cashTaxInfo = [];
      newCashup.cashCloseInfo = [];

      return newCashup;
    },

    createNewCashupFromBackend(payload) {
      const { cashup, currentCashupFromBackend } = payload;
      const newCashup = { ...cashup };

      newCashup.id = currentCashupFromBackend.id;
      newCashup.netSales = currentCashupFromBackend.netSales;
      newCashup.grossSales = currentCashupFromBackend.grossSales;
      newCashup.netReturns = currentCashupFromBackend.netReturns;
      newCashup.grossReturns = currentCashupFromBackend.grossReturns;
      newCashup.totalRetailTransactions =
        currentCashupFromBackend.totalRetailTransactions;
      newCashup.creationDate = currentCashupFromBackend.creationDate;
      newCashup.userId = currentCashupFromBackend.userId;
      newCashup.posterminal = currentCashupFromBackend.posterminal;
      newCashup.isprocessed = currentCashupFromBackend.isprocessed;
      newCashup.cashTaxInfo = OB.App.State.Cashup.Utils.getTaxesFromBackendObject(
        currentCashupFromBackend.cashTaxInfo
      );
      newCashup.cashCloseInfo = currentCashupFromBackend.cashCloseInfo;
      // newCashup.cashManagements = currentCashupFromBackend.cashMgmInfo;

      return newCashup;
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
            paymentMethods.push(
              OB.App.State.Cashup.Utils.getPaymentMethodFromBackendObject(
                paymentMethodCashUp
              )
            );
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

    getPaymentMethods() {
      return OB.App.State.getState().Cashup.cashPaymentMethodInfo;
    },

    getCashup() {
      return OB.App.State.getState().Cashup;
    },

    // TODO : this function is for compatibility with old code,
    // when all models that used it are moved to indexeddb,
    // this function should be deleted
    getCashupId() {
      return OB.App.State.persistence.getState().Cashup.id;
    },

    filterOnlyNeededDataForCompleteCashup(cashup) {
      const unFilteredObjToSend = JSON.parse(cashup.get('objToSend'));
      const filteredObjToSend = {};

      filteredObjToSend.cashCloseInfo = unFilteredObjToSend.cashCloseInfo;
      filteredObjToSend.cashMgmtIds = unFilteredObjToSend.cashMgmtIds;
      filteredObjToSend.cashUpDate = unFilteredObjToSend.cashUpDate;
      filteredObjToSend.timezoneOffset = unFilteredObjToSend.timezoneOffset;
      filteredObjToSend.lastcashupeportdate =
        unFilteredObjToSend.lastcashupeportdate;
      filteredObjToSend.approvals = unFilteredObjToSend.approvals;

      const newCashup = new Backbone.Model();
      newCashup.set('objToSend', JSON.stringify(filteredObjToSend));

      return newCashup;
    },

    getCashManagementsInDraft() {
      let cashManagementsInDraft = [];
      OB.App.State.getState().Cashup.cashPaymentMethodInfo.forEach(
        function getCashManagementsInDraft(paymentMethod) {
          const cashManagementInDraftByPayment = paymentMethod.cashManagements.filter(
            cashManagement => cashManagement.isDraft
          );
          cashManagementsInDraft = [
            ...cashManagementsInDraft,
            ...cashManagementInDraftByPayment
          ];
        }
      );
      return cashManagementsInDraft;
    },
    getCashManagements() {
      let cashManagements = [];
      OB.App.State.getState().Cashup.cashPaymentMethodInfo.forEach(
        function getCashManagementsInDraft(paymentMethod) {
          cashManagements = [
            ...cashManagements,
            ...paymentMethod.cashManagements
          ];
        }
      );
      return cashManagements;
    },

    getCashManagementsByPaymentMethodId(paymentMethodId) {
      let cashManagements = [];
      OB.App.State.getState().Cashup.cashPaymentMethodInfo.forEach(
        function getCashManagementsInDraft(paymentMethod) {
          if (paymentMethodId === paymentMethod.paymentMethodId) {
            cashManagements = [
              ...cashManagements,
              ...paymentMethod.cashManagements
            ];
          }
        }
      );
      return cashManagements;
    }
  });
})();
