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

    resetStatistics() {
      // TODO: modify state instead run methods
      // TODO !!!!!!!!!!!
      // OB.UTIL.localStorage.setItem('transitionsToOnline', 0);
      // OB.UTIL.resetNetworkInformation();
      // OB.UTIL.resetNumberOfLogClientErrors();
      // TODO !!!!!!!!!!!
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

    createNewCashupFromScratch(payload) {
      const { cashup } = payload;
      const newCashup = { ...cashup };

      newCashup.id = OB.App.UUID.generate();
      newCashup.netSales = OB.DEC.Zero;
      newCashup.grossSales = OB.DEC.Zero;
      newCashup.netReturns = OB.DEC.Zero;
      newCashup.grossReturns = OB.DEC.Zero;
      newCashup.totalRetailTransactions = OB.DEC.Zero;
      newCashup.totalStartings = OB.DEC.Zero;
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
      newCashup.totalStartings = OB.DEC.Zero;
      newCashup.creationDate = currentCashupFromBackend.creationDate;
      newCashup.userId = currentCashupFromBackend.userId;
      newCashup.posterminal = currentCashupFromBackend.posterminal;
      newCashup.isprocessed = currentCashupFromBackend.isprocessed;
      newCashup.cashTaxInfo = OB.App.State.Cashup.Utils.getTaxesFromBackendObject(
        currentCashupFromBackend.cashTaxInfo
      );
      newCashup.cashCloseInfo = currentCashupFromBackend.cashCloseInfo;

      return newCashup;
    },

    getCashupFilteredForSendToBackendInEachTicket(payload) {
      const { terminalPayments, cashup } = payload;

      const cashupToSend = { ...cashup };
      const cashupPayments = cashupToSend.cashPaymentMethodInfo;

      cashupToSend.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.getCashupPaymentsThatAreAlsoInTerminalPayments(
        cashupPayments,
        terminalPayments
      );

      return cashupToSend;
    },

    /**
     * converts an amount to the WebPOS amount currency
     * @param  {currencyId} fromCurrencyId    the currencyId of the amount to be converted
     * @param  {float}      amount            the amount to be converted
     * @param  {currencyId} defaultCurrencyId the currencyId of the default payment method
     * @param  {Object[]}   conversions       array of converters availables
     * @return {float}                        the converted amount
     */
    toDefaultCurrency(fromCurrencyId, amount, defaultCurrencyId, conversions) {
      if (
        fromCurrencyId === defaultCurrencyId &&
        fromCurrencyId != null &&
        defaultCurrencyId != null
      ) {
        return amount;
      }

      const converter = conversions.find(function getConverter(c) {
        return (
          c.fromCurrencyId === fromCurrencyId &&
          c.toCurrencyId === defaultCurrencyId
        );
      });

      if (!converter.length === 0) {
        OB.error(
          `Currency converter not added: ${fromCurrencyId} -> ${defaultCurrencyId}`
        );
      }

      return OB.DEC.mul(amount, converter.rate);
    },

    /**
     * converts an amount from the WebPOS currency to the toCurrencyId currency
     * @param  {currencyId} toCurrencyId      the currencyId of the final amount
     * @param  {float}      amount            the amount to be converted
     * @param  {currencyId} defaultCurrencyId the currencyId of the default payment method
     * @param  {Object[]}   conversions       array of converters availables
     * @return {float}                        the converted amount
     */
    toForeignCurrency(toCurrencyId, amount, defaultCurrencyId, conversions) {
      if (
        toCurrencyId === defaultCurrencyId &&
        toCurrencyId != null &&
        defaultCurrencyId != null
      ) {
        return amount;
      }

      const converter = conversions.find(function getConverter(c) {
        return (
          c.fromCurrencyId === defaultCurrencyId &&
          c.toCurrencyId === toCurrencyId
        );
      });

      if (!converter.length === 0) {
        OB.error(
          `Currency converter not added: ${toCurrencyId} -> ${defaultCurrencyId}`
        );
      }

      return OB.DEC.mul(amount, converter.rate);
    }
  });
})();
