/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalDeliveryChangeButtonOK',
  i18nContent: 'OBMOBC_LblOk',
  events: {},
  tap: function () {
    var popup = this.owner.owner;
    popup.args.receipt.set('prepaymentChangeMode', popup.$.bodyContent.$.paymentOptions.active.setPrepaymentChange);
    popup.args.receipt.set('prepaymentChangeAmt', popup.args.deliveryChange);
    if (popup.args.receipt.get('prepaymentChangeMode')) {
      popup.args.receipt.trigger('updatePending');
    }
    if (popup.args.callback) {
      popup.args.callback();
    }
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalDeliveryChange',
  i18nHeader: 'OBPOS_LblActionRequired',
  autoDismiss: false,
  bodyContent: {
    components: [{
      name: 'labelPaymentInformation'
    }, {
      name: 'paymentOptions',
      kind: "Group",
      style: 'background-color: white; margin: 20px 30px 0 30px; text-align: left;',
      components: [{
        kind: 'OB.UI.RadioButton',
        name: 'keepPayment',
        components: [{
          name: 'keepPaymentLbl'
        }]
      }, {
        style: 'clear: both;'
      }, {
        kind: 'OB.UI.RadioButton',
        setPrepaymentChange: true,
        name: 'returnChange',
        components: [{
          name: 'returnChangeLbl'
        }]
      }]
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalDeliveryChangeButtonOK'
    }]
  },
  events: {},
  handlers: {},
  initComponents: function () {
    this.inherited(arguments);
  },
  executeOnShow: function () {
    var symbol = OB.MobileApp.model.get('terminal').symbol,
        symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
    this.$.bodyContent.$.labelPaymentInformation.setContent(OB.I18N.getLabel('OBPOS_DeliveryChangeMsg', [OB.I18N.formatCurrencyWithSymbol(this.args.deliveryChange, symbol, symbolAtRight)]));
    this.$.bodyContent.$.keepPaymentLbl.setContent(OB.I18N.getLabel('OBPOS_KeepAsPaymentMsg'));
    this.$.bodyContent.$.returnChangeLbl.setContent(OB.I18N.getLabel('OBPOS_ReturnAsChangeMsg'));
    this.$.bodyContent.$.keepPayment.activeRadio();
  }
});