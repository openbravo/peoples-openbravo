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
      return OB.App.State.getState()
        .Cashup.cashPaymentMethodInfo.map(payment => payment.cashManagements) // get all cashmng
        .reduce((a, b) => a.concat(b)); // concat all arrays into one
    },

    /**
     * Retrieves cash managements not yet processed (isDraft)
     * @return {Object[]} An array with cash managements
     */
    getCashManagementsInDraft() {
      return OB.App.State.Cashup.Utils.getCashManagements().filter(
        cashMgmt => cashMgmt.isDraft
      );
    },

    /**
     * Retrieves cash managements of a given paymentMethod
     * @param {string} paymentMethodId - The id of the paymentMethod
     * @return {Object[]} An array with cash managements of the paymentMethod passed by parameters
     */
    getCashManagementsByPaymentMethodId(paymentMethodId) {
      return OB.App.State.Cashup.Utils.getCashManagements().filter(
        cashMgmt => cashMgmt.paymentMethod === paymentMethodId
      );
    }
  });
})();
