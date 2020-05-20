/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Cash Managements
 */
(function CashManagementsUtilsDefinition() {
  OB.App.StateAPI.Cashup.registerUtilityFunctions({
    /**
     * Retrieves all cash managements in the Cashup
     * @return {Object[]} An array with cash managements
     */
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

    /**
     * Retrieves cash managements not yet processed (isDraft)
     * @return {Object[]} An array with cash managements
     */
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

    /**
     * Retrieves cash managements of a given paymentMethod
     * @param {string} paymentMethodId - The id of the paymentMethod
     * @return {Object[]} An array with cash managements of the paymentMethod passed by parameters
     */
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
