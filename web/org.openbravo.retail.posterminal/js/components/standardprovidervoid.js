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
    var request = {
      'type': OBPOS_StandardProvider.TYPE_VOID,
      'currency': voidpaymentinfo.payment.get('isocode'),
      'amount': voidpaymentinfo.payment.get('amount'),
      'properties': voidpaymentinfo.payment.get('paymentData').properties.voidproperties
    };

    request = this.populateVoidRequest(request, voidpaymentinfo);

    return OBPOS_StandardProvider.remoteRequest(request);
  },
  populateVoidRequest: function (request, voidpaymentinfo) {
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