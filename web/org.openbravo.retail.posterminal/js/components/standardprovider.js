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
  processPayment: function (data) {
    var type = data.refund ? OBPOS_StandardProvider.TYPE_REFUND : OBPOS_StandardProvider.TYPE_SALE;

    var request = {
      'type': type,
      'currency': data.currency,
      'amount': data.amount,
      'properties': {
        'provider': data.providerGroup.provider.provider
      }
    };

    request = (data.refund) ? this.populateRefundRequest(request, data) : this.populatePaymentRequest(request, data);

    return OBPOS_StandardProvider.remoteRequest(request);
  },
  populatePaymentRequest: function (request, data) {
    return request;
  },
  populateRefundRequest: function (request, data) {
    return request;
  },
  getErrorMessage: function (ex) {
    // ex.response
    // ex.message
    // return OB.I18N.getLabel(...
  }
});