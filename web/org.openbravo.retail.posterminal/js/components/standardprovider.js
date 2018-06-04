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
  name: 'OBPOS_StandardProvider',
  checkOverpayment: true,
  providerComponent: null,
  statics: {
    RESULT_SUCCESS: 0,
    RESULT_AUTHORIZATION_FAIL: 1,
    RESULT_ERROR: 2,
    TYPE_SALE: 0,
    TYPE_REFUND: 1,
    TYPE_VOID: 2,
    remoteRequest: function (request) {
      return new Promise(function (resolve, reject) {
        var url = OB.POS.hwserver.url;
        if (url) {
          var ajax = new enyo.Ajax({
            url: url.replace('/printer', '/payment'),
            cacheBust: false,
            method: 'POST',
            handleAs: 'json',
            contentType: 'application/json;charset=utf-8'
          }).go(JSON.stringify(request));
          ajax.response(function (inSender, inResponse) {
            if (inResponse.result === OBPOS_StandardProvider.RESULT_SUCCESS) {
              resolve(inResponse); // Success, remove transaction.
            } else if (inResponse.result === OBPOS_StandardProvider.RESULT_AUTHORIZATION_FAIL) {
              reject({
                response: inResponse,
                message: OB.I18N.getLabel('OBPOS_TransactionAuthFail')
              }); // Fail, do not remove transaction.
            } else { // RESULT_ERROR
              reject({
                response: inResponse,
                message: OB.I18N.getLabel('OBPOS_TransactionError')
              }); // Fail, do not remove transaction.
            }
          });
          ajax.error(function (inSender, inResponse) {
            OB.error('Error procesing request: ' + inResponse);
            reject({
              response: inResponse,
              message: OB.I18N.getLabel('OBPOS_ErrorConnect')
            });
          });
        } else {
          reject({
            response: {},
            message: OB.I18N.getLabel('OBPOS_NotConfigured')
          });
        }
      });
    }
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
    var type = paymentinfo.refund ? OBPOS_StandardProvider.TYPE_REFUND : OBPOS_StandardProvider.TYPE_SALE;

    var request = {
      'type': type,
      'currency': paymentinfo.currency,
      'amount': paymentinfo.amount,
      'properties': {
        'provider': paymentinfo.providerGroup.provider.provider
      }
    };

    request = (paymentinfo.refund) ? this.populateRefundRequest(request, paymentinfo) : this.populatePaymentRequest(request, paymentinfo);

    return OBPOS_StandardProvider.remoteRequest(request);
  },
  populatePaymentRequest: function (request, exceptioninfo) {
    return request;
  },
  populateRefundRequest: function (request, exceptioninfo) {
    return request;
  },
  getErrorMessage: function (exceptioninfo) {
    // This function is invoked when processInfo function is rejected.
    // It is invoked with the parameter exceptioninfo that contains the exception
    // object of the reject, and must return the error message to display to the cashier
    //
    // In the case of OBPOS_StandardProviderVoid exceptioninfo is a plain js object with the following fields
    // * response. The response from the hardware manager
    // * message. The message returned by the hardware manager
    return exceptioninfo.message;
  }
});