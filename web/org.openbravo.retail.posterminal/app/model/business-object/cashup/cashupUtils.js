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
    }
  });
})();
