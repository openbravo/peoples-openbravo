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
  name: 'OBPOS_StandardProviderVoid',
  voidConfirmation: true,
  providerComponent: null,
  // processVoid: function (data) {
  //   // This function removes always the transaction
  //   return Promise.resolve();
  // },
  // processVoid: function (data) {
  //   // This function rejects always the transaction
  //   return Promise.reject({
  //     inResponse: null,
  //     message: 'Error from somewhere'
  //   });
  // },
  processVoid: function (data) {
    // data.receipt
    // data.payment
    var request = {
      'type': OBPOS_StandardProvider.TYPE_VOID,
      'currency': data.payment.get('isocode'),
      'amount': data.payment.get('amount'),
      'properties': data.payment.get('gatewayData').voidproperties
    };

    request = this.populateVoidRequest(request, data);

    return OBPOS_StandardProvider.remoteRequest(request);
  },
  populateVoidRequest: function (request, data) {
    return request;
  },
  getErrorMessage: function (ex) {
    // ex.response
    // ex.message
    // return OB.I18N.getLabel(...
  }
});