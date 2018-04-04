/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $, _, Promise */

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
    var refund = this.args.refund;
    var providerGroup = this.args.providerGroup;

    this.$.header.setContent(refund ? OB.I18N.getLabel('OBPOS_LblModalReturn', [OB.I18N.formatCurrency(amount)]) : OB.I18N.getLabel('OBPOS_LblModalPayment', [OB.I18N.formatCurrency(amount)]));
    this.$.bodyContent.$.lblType.setContent(OB.I18N.getLabel('OBPOS_LblModalType'));
    this.$.bodyContent.$.paymenttype.setContent(providerGroup.provider._identifier);
    this.$.bodyContent.$.description.setContent(providerGroup.provider.description);

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
    var refund = this.args.refund;
    var currency = this.args.currency;
    var providerGroup = this.args.providerGroup;
    var providerinstance = this.args.providerinstance;
    var attributes = this.args.attributes;
    var i;

    this.$.bodyContent.$.providergroupcomponent.destroyComponents();
    if (providerinstance.providerComponent) {
      this.$.bodyContent.$.providergroupcomponent.createComponent(providerinstance.providerComponent).render();
    }

    if (providerinstance.checkOverpayment && !refund) {
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

    providerinstance.processPayment({
      'receipt': receipt,
      'currency': currency,
      'amount': amount,
      'refund': refund,
      'providerGroup': providerGroup
    }).then(function (response) {
      var cardlogo = response.properties.cardlogo;
      var paymentline;
      for (i = 0; i < providerGroup._payments.length; i++) {
        var payment = providerGroup._payments[i];
        if (cardlogo === payment.paymentType.searchKey) {
          // We found the payment method that applies.
          paymentline = {
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
              'provider': providerGroup.provider,
              'voidConfirmation': false // Is the void provider in charge of defining confirmation.
            }
          };
          receipt.addPayment(new OB.Model.PaymentLine(Object.assign(paymentline, attributes)));
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