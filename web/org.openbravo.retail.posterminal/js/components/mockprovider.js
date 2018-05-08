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
        if (paymentinfo.amount === 350.50) { // 1 x Expedition tent 4 season 2 person
          this.resolveTransactionVISA(paymentinfo, resolve, reject);
        } else if (paymentinfo.amount === 701.00) {
          this.resolveTransactionMASTER(paymentinfo, resolve, reject);
        } else {
          this.rejectTransaction(paymentinfo, resolve, reject);
        }
      }.bind(this), 2000);
    }.bind(this));
  },
  resolveTransactionVISA: function (paymentinfo, resolve, reject) {
    resolve({
      transaction: paymentinfo.refund ? '0000002' : '0000001',
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
  resolveTransactionMASTER: function (paymentinfo, resolve, reject) {
    resolve({
      transaction: paymentinfo.refund ? '0000012' : '0000011',
      authorization: '002',
      properties: {
        cardlogo: '01',
        refund: paymentinfo.refund,
        // MASTER
        voidproperties: {
          info: 'MASTER OK'
        }
      }
    });
  },
  rejectTransaction: function (paymentinfo, resolve, reject) {
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