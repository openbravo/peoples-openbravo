/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, _, Promise, OBPOS_StandardProvider */

enyo.kind({
  name: 'OBPOS_MockProviderVoid',
  voidConfirmation: true,
  providerComponent: {
    content: 'Voiding payment'
  },
  processVoid: function (voidpaymentinfo) {
    // This function is invoked to void a payment transaction
    //
    // The parameter voidpaymentinfo is a plain js object with the following fields
    // * receipt. The receipt model
    // * payment. The payment model to void
    //
    // It returns a Promise
    // * When resolved, the parameter must be an empty js object
    // * When rejected, the parameter must be the exception object
    //
    // As an example this is the processVoid() function if payment cannot be voided.
    //
    // processVoid: function (data) {
    //   // This function rejects always the transaction
    //   return Promise.reject({
    //     inResponse: null,
    //     message: 'Error from somewhere'
    //   });
    // },
    return new Promise(function (resolve, reject) {
      setTimeout(function () {
        if (voidpaymentinfo.payment.get('paymentData').properties.voidproperties.info === 'VISA OK') {
          this.resolveTransaction(resolve, reject);
        } else {
          this.rejectTransaction(resolve, reject);
        }
      }.bind(this), 2000);
    }.bind(this));
  },
  resolveTransaction: function (resolve, reject) {
    resolve({});
  },
  rejectTransaction: function (resolve, reject) {
    reject({
      message: 'Void transaction has been rejected'
    });
  },
  getErrorMessage: function (exceptioninfo) {
    // This function is invoked when processInfo function is rejected.
    // It is invoked with the parameter exceptioninfo that contains the exception
    // object of the reject, and must return the error message to display to the cashier
    return exceptioninfo.message;
  }
});