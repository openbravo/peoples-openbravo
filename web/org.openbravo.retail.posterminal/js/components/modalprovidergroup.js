/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, _, Promise, OBPOS_StandardProvider */

enyo.kind({
  name: 'OB.UI.ModalProviderGroup',
  kind: 'OB.UI.ModalAction',
  header: '',
  autoDismiss: false,
  maxheight: '600px',
  events: {
    onHideThisPopup: ''
  },
  bodyContent: {
    components: [{
      components: [{
        classes: 'row-fluid',
        components: [{
          style: 'float:left; padding-left:30px',
          name: 'lblType'
        }, {
          name: 'paymenttype',
          style: 'float:right; font-weight: bold; padding-right:30px'
        }]
      }, {
        style: 'clear: both'
      }, {
        classes: 'row-fluid',
        components: [{
          style: 'float:center; padding-left:30px; padding-right:30px',
          name: 'description'
        }]
      }, {
        style: 'clear: both'
      }]
    }, {
      name: 'providergroupcomponent'
    }]
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.headerCloseButton.hide();
  },
  executeOnShow: function () {

    var receipt = this.args.receipt;
    var amount = this.args.amount;
    var providerGroup = this.args.providerGroup;

    var isPayment = !receipt.getPaymentStatus().isNegative;

    this.$.header.setContent(isPayment ? OB.I18N.getLabel('OBPOS_LblModalPayment', [OB.I18N.formatCurrency(amount)]) : OB.I18N.getLabel('OBPOS_LblModalReturn', [OB.I18N.formatCurrency(amount)]));
    this.$.bodyContent.$.lblType.setContent(OB.I18N.getLabel('OBPOS_LblModalType'));
    this.$.bodyContent.$.paymenttype.setContent(providerGroup._identifier);
    this.$.bodyContent.$.description.setContent(providerGroup.description);

    // Set timeout needed because on ExecuteOnShow
    setTimeout(this.startPaymentRefund.bind(this), 0);

  },
  showMessageAndClose: function (message) {
    window.setTimeout(this.doHideThisPopup.bind(this), 0);
    OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblPaymentMethod'), message, [{
      label: OB.I18N.getLabel('OBMOBC_LblOk'),
      isConfirmButton: true
    }], {
      autoDismiss: false
    });
  },
  startPaymentRefund: function () {

    var receipt = this.args.receipt;
    var amount = this.args.amount;
    var currency = this.args.currency;
    var providerGroup = this.args.providerGroup;
    var isPayment = !receipt.getPaymentStatus().isNegative;

    var i;

    if (isPayment) {
      // check over payments in all payments of the group
      for (i = 0; i < providerGroup._payments.length; i++) {
        var payment = providerGroup._payments[i];

        if (!payment.paymentMethod.allowoverpayment) {
          this.showMessageAndClose(OB.I18N.getLabel('OBPOS_OverpaymentNotAvailable'));
          return;
        }

        if (_.isNumber(payment.paymentMethod.overpaymentLimit) && amount > receipt.get('gross') + payment.paymentMethod.overpaymentLimit - receipt.get('payment')) {
          this.showMessageAndClose(OB.I18N.getLabel('OBPOS_OverpaymentExcededLimit'));
          return;
        }
      }
    }

    var providerinstance = enyo.createFromKind(providerGroup.provider);

    providerinstance.processPayment({
      'receipt': receipt,
      'currency': currency,
      'amount': amount,
      'providerGroup': providerGroup
    }).then(function (response) {
      var cardlogo = response.properties.cardlogo;
      for (i = 0; i < providerGroup._payments.length; i++) {
        var payment = providerGroup._payments[i];
        if (cardlogo === payment.paymentType.searchKey) {
          // We found the payment method that applies.
          receipt.addPayment(new OB.Model.PaymentLine({
            'kind': payment.payment.searchKey,
            'name': payment.payment._identifier,
            'amount': amount,
            'rate': payment.rate,
            'mulrate': payment.mulrate,
            'isocode': payment.isocode,
            'allowOpenDrawer': payment.paymentMethod.allowopendrawer,
            'isCash': payment.paymentMethod.iscash,
            'openDrawer': payment.paymentMethod.openDrawer,
            'printtwice': payment.paymentMethod.printtwice,
            'gatewayData': {
              'transaction': response.transaction,
              'authorization': response.authorization,
              'cardmasked': response.properties.cardmasked,
              'cardlogo': response.properties.cardlogo,
              'voidproperties': response.properties.voidproperties
            },
            'paymentData': {
              'voidConfirmation': true
            }
          }));
          window.setTimeout(this.doHideThisPopup.bind(this), 0);
          return; // Success
        }
      }
      this.showMessageAndClose(OB.I18N.getLabel('OBPOS_CannotFindPaymentMethod'));
    }.bind(this))['catch'](function (exception) {
      this.showMessageAndClose(providerinstance.getErrorMessage(exception) || exception.message);
    }.bind(this));
  }
});


enyo.kind({
  name: 'OBPOS_StandardProvider',
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
    var type = data.receipt.getPaymentStatus().isNegative ? OBPOS_StandardProvider.TYPE_REFUND : OBPOS_StandardProvider.TYPE_SALE;

    var request = {
      'type': type,
      'currency': data.currency,
      'amount': data.amount,
      'properties': {
        'provider': data.providerGroup.provider
      }
    };
    if (data.receipt.getPaymentStatus().isNegative) {
      request = this.populateRefundRequest(request);
    } else {
      request = this.populatePaymentRequest(request);
    }

    return OBPOS_StandardProvider.remoteRequest(request);
  },
  populatePaymentRequest: function (request, data) {
    request.properties.cardlogo = '00';
    return request;
  },
  populateRefundRequest: function (request, data) {
    request.properties.cardlogo = '01';
    return request;
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