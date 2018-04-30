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
  name: 'OBPOS_MockProvider',
  checkOverpayment: true,
  providerComponent: {
    content: 'Processing payment or refund'
  },
  statics: {
    RESULT_SUCCESS: 0,
    RESULT_AUTHORIZATION_FAIL: 1,
    RESULT_ERROR: 2,
    TYPE_SALE: 0,
    TYPE_REFUND: 1,
    TYPE_VOID: 2
  },
  processPayment: function (paymentinfo) {
    return new Promise(function (resolve, reject) {
      setTimeout(function () {
        if (paymentinfo.amount === 150.50) {
          this.resolveTransactionVISA(resolve, reject);
        } else if (paymentinfo.amount === 301.00) {
          this.resolveTransactionMASTER(resolve, reject);
        } else {
          this.rejectTransaction(resolve, reject);
        }
      }.bind(this), 2000);
    }.bind(this));
  },
  resolveTransactionVISA: function (resolve, reject) {
    resolve({
      transaction: '0000001',
      authorization: '001',
      properties: {
        cardlogo: '00',
        // VISA
        voidproperties: {
          info: 'VISA OK'
        }
      }
    });
  },
  resolveTransactionMASTER: function (resolve, reject) {
    resolve({
      transaction: '0000002',
      authorization: '002',
      properties: {
        cardlogo: '01',
        // MASTER
        voidproperties: {
          info: 'MASTER OK'
        }
      }
    });
  },
  rejectTransaction: function (resolve, reject) {
    reject({
      message: 'Transaction has been rejected'
    });
  },
  getErrorMessage: function (exceptioninfo) {
    // exceptioninfo.response
    // exceptioninfo.message
    // return OB.I18N.getLabel(...
    return exceptioninfo.message;
  }
});