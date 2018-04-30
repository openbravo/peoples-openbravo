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
  // processVoid: function (data) {
  //   // This function rejects always the transaction
  //   return Promise.reject({
  //     inResponse: null,
  //     message: 'Error from somewhere'
  //   });
  // },
  processVoid: function (voidpaymentinfo) {
    // data.receipt
    // data.payment
    return new Promise(function (resolve, reject) {
      setTimeout(function () {
        if (voidpaymentinfo.payment.get('gatewayData').voidproperties.info === 'VISA OK') {
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
    // exceptioninfo.response
    // exceptioninfo.message
    // return OB.I18N.getLabel(...
    return exceptioninfo.message;
  }
});