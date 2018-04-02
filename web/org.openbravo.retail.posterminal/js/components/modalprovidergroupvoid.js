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
  name: 'OB.UI.ModalProviderGroupVoid',
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

    var payment = this.args.payment;
    var amount = payment.get('amount');
    var provider = payment.get('paymentData').provider;

    this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblModalVoidTransaction', [OB.I18N.formatCurrency(amount)]));
    this.$.bodyContent.$.lblType.setContent(OB.I18N.getLabel('OBPOS_LblModalType'));
    this.$.bodyContent.$.paymenttype.setContent(provider._identifier);
    this.$.bodyContent.$.description.setContent(provider.description);

    // Set timeout needed because on ExecuteOnShow
    setTimeout(this.startVoid.bind(this), 0);
  },
  executeOnHide: function () {
    this.args.onhide();
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
  startVoid: function () {

    var payment = this.args.payment;
    var amount = payment.get('amount');
    var provider = payment.get('paymentData').provider;
    var providerinstance = this.args.providerinstance;

    var receipt = this.args.receipt;
    var removeTransaction = this.args.removeTransaction;

    this.$.bodyContent.$.providergroupcomponent.destroyComponents();
    if (providerinstance.providerComponent) {
      this.$.bodyContent.$.providergroupcomponent.createComponent(providerinstance.providerComponent).render();
    }

    providerinstance.processVoid({
      'receipt': receipt,
      'payment': payment
    }).then(function (response) {
      removeTransaction();
      window.setTimeout(this.doHideThisPopup.bind(this), 0);
    }.bind(this))['catch'](function (exception) {
      this.showMessageAndClose(providerinstance.getErrorMessage(exception) || exception.message);
    }.bind(this));
  }
});