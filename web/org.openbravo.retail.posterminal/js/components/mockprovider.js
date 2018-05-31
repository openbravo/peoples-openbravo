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
    // This function is invoked to process a payment transaction
    //
    // The parameter paymentinfo is a plain js object with the following fields
    // * receipt. The receipt model to pay
    // * currency. The currency ISO code of the amount to process
    // * amount. The amount to process
    // * refund. Boolean value that indicates whether this payment is a refund or not.
    // * providerGroup. The provider group model uses to process this payment.
    //
    // It returns a Promise
    // * When resolved, the parameter must be a plain js object with the following fields:
    //   * transaction. The transaction ID returned by the process
    //   * authorization:. The authorization ID returned by the process
    //   * properties.cardlogo. The value used by the provider group model to select the payment method
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
        if (paymentinfo.amount === 350.50) { // This value stands for 1 x Expedition tent 4 season 2 person
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
    // This function is invoked when processInfo function is rejected.
    // It is invoked with the parameter exceptioninfo that contains the exception
    // object of the reject, and must return the error message to display to the cashier
    return exceptioninfo.message;
  }
});